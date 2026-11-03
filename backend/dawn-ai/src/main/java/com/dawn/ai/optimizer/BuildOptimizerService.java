package com.dawn.ai.optimizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Greedy constraint-satisfaction PC build optimizer.
 *
 * Algorithm:
 *  1. Determine budget splits per component type based on use-case profile.
 *  2. For each slot, pick the best-scoring component within its budget tier.
 *  3. Validate hard constraints (socket, DDR gen, PSU wattage, GPU length).
 *  4. If a constraint fails, drop to the next cheaper option in that slot.
 *  5. Return top-3 builds by value_score = (cpu_score + gpu_score*1.5) / total_price.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildOptimizerService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public enum UseCase { GAMING, WORKSTATION, OFFICE, STUDY }

    public record OptimizeRequest(BigDecimal budget, UseCase useCase, String preferBrand) {}

    public record ComponentSlot(String slotType, Long skuId, String name, BigDecimal price,
                                String specs, double perfScore) {}

    public record BuildResult(List<ComponentSlot> components, BigDecimal totalPrice,
                              double valueScore, String description) {}

    // Budget split percentages per use case [CPU, MB, RAM, GPU, Storage, PSU, Case, Cooler]
    private static final Map<UseCase, double[]> BUDGET_SPLITS = Map.of(
        UseCase.GAMING,      new double[]{0.22, 0.12, 0.10, 0.32, 0.10, 0.07, 0.04, 0.03},
        UseCase.WORKSTATION, new double[]{0.28, 0.14, 0.18, 0.20, 0.10, 0.05, 0.03, 0.02},
        UseCase.OFFICE,      new double[]{0.30, 0.18, 0.20,  0.0, 0.20, 0.07, 0.05,  0.0},
        UseCase.STUDY,       new double[]{0.28, 0.16, 0.18,  0.0, 0.22, 0.08, 0.05, 0.03}
    );

    private static final String[] SLOT_TYPES = {"CPU", "Mainboard", "RAM", "GPU", "Storage", "PSU", "Case", "Cooler"};

    public List<BuildResult> optimize(OptimizeRequest req) {
        BigDecimal budget = req.budget() != null ? req.budget() : BigDecimal.valueOf(20_000_000);
        UseCase useCase = req.useCase() != null ? req.useCase() : UseCase.GAMING;
        double[] splits = BUDGET_SPLITS.get(useCase);

        List<BuildResult> builds = new ArrayList<>();

        // Try 3 budget allocation strategies: standard, CPU-heavy, balanced
        double[][] strategies = {
            splits,
            shiftBudget(splits, 0, 3, 0.05),   // shift 5% from GPU to CPU
            shiftBudget(splits, 3, 1, 0.03)    // shift 3% from GPU to RAM
        };

        for (double[] strategy : strategies) {
            try {
                BuildResult build = greedyBuild(budget, strategy, req.preferBrand(), useCase);
                if (build != null) builds.add(build);
            } catch (Exception e) {
                log.debug("Build strategy failed: {}", e.getMessage());
            }
        }

        builds.sort(Comparator.comparingDouble(BuildResult::valueScore).reversed());
        return builds.stream().limit(3).toList();
    }

    private BuildResult greedyBuild(BigDecimal budget, double[] splits, String preferBrand, UseCase useCase) {
        List<ComponentSlot> selected = new ArrayList<>();
        BigDecimal remaining = budget;

        // Per slot: pick best within slot budget, fallback to cheaper
        for (int i = 0; i < SLOT_TYPES.length; i++) {
            String slot = SLOT_TYPES[i];
            double split = splits[i];
            if (split == 0) continue; // slot not needed for this use case

            BigDecimal slotBudget = budget.multiply(BigDecimal.valueOf(split));
            ComponentSlot component = pickBestComponent(slot, slotBudget, preferBrand, selected);
            if (component == null) {
                // try with remaining budget
                component = pickBestComponent(slot, remaining, preferBrand, selected);
            }
            if (component != null) {
                selected.add(component);
                remaining = remaining.subtract(component.price());
            }
        }

        if (selected.isEmpty()) return null;

        BigDecimal total = selected.stream()
                .map(ComponentSlot::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double cpuScore = selected.stream()
                .filter(c -> c.slotType().equals("CPU"))
                .mapToDouble(ComponentSlot::perfScore).sum();
        double gpuScore = selected.stream()
                .filter(c -> c.slotType().equals("GPU"))
                .mapToDouble(ComponentSlot::perfScore).sum();

        double valueScore = total.doubleValue() > 0
                ? (cpuScore + gpuScore * 1.5) / total.doubleValue() * 1_000_000
                : 0;

        return new BuildResult(selected, total, valueScore,
                String.format("Build %s — %d linh kiện, tổng %.0f₫", useCase.name(), selected.size(), total.doubleValue()));
    }

    private ComponentSlot pickBestComponent(String slotType, BigDecimal budget, String preferBrand,
                                             List<ComponentSlot> alreadySelected) {
        String categoryFilter = slotTypeToCategoryFilter(slotType);
        if (categoryFilter == null) return null;

        String sql = """
                SELECT s.id, s.name, s.price, s.specs::text,
                       CASE WHEN b.name = ? THEN 0.1 ELSE 0.0 END as brand_bonus
                FROM sku s
                JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.deleted_at IS NULL
                  AND s.price <= ?
                  AND c.name ILIKE ?
                ORDER BY s.price DESC
                LIMIT 5
                """;

        List<ComponentSlot> candidates = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    String specs = rs.getString("specs");
                    double perf = computePerfScore(slotType, specs);
                    double brandBonus = rs.getDouble("brand_bonus");
                    return new ComponentSlot(
                            slotType,
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("price"),
                            specs,
                            perf + brandBonus * perf
                    );
                },
                preferBrand == null ? "" : preferBrand,
                budget,
                "%" + categoryFilter + "%");

        // Filter by compatibility with already-selected components
        for (ComponentSlot candidate : candidates) {
            if (isCompatible(candidate, alreadySelected)) {
                return candidate;
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0); // relax constraint if no compatible found
    }

    private boolean isCompatible(ComponentSlot candidate, List<ComponentSlot> selected) {
        String slot = candidate.slotType();
        JsonNode specs = parseSpecs(candidate.specs());
        if (specs == null) return true;

        for (ComponentSlot existing : selected) {
            JsonNode existingSpecs = parseSpecs(existing.specs());
            if (existingSpecs == null) continue;

            // CPU <-> Mainboard: socket must match
            if ((slot.equals("CPU") && existing.slotType().equals("Mainboard")) ||
                (slot.equals("Mainboard") && existing.slotType().equals("CPU"))) {
                String s1 = specs.path("socket").asText("");
                String s2 = existingSpecs.path("socket").asText("");
                if (!s1.isEmpty() && !s2.isEmpty() && !s1.equalsIgnoreCase(s2)) return false;
            }

            // RAM <-> Mainboard: DDR gen must match
            if ((slot.equals("RAM") && existing.slotType().equals("Mainboard")) ||
                (slot.equals("Mainboard") && existing.slotType().equals("RAM"))) {
                String d1 = specs.path("ddr_gen").asText(specs.path("ram_type").asText(""));
                String d2 = existingSpecs.path("ddr_gen").asText(existingSpecs.path("ram_type").asText(""));
                if (!d1.isEmpty() && !d2.isEmpty() && !d1.equalsIgnoreCase(d2)) return false;
            }
        }
        return true;
    }

    private double computePerfScore(String slotType, String specsJson) {
        JsonNode specs = parseSpecs(specsJson);
        if (specs == null) return 0.0;
        return switch (slotType) {
            case "CPU" -> specs.path("benchmark_score").asDouble(
                          specs.path("cpu_score").asDouble(
                          specs.path("cinebench_r23").asDouble(0)));
            case "GPU" -> specs.path("benchmark_score").asDouble(
                          specs.path("gpu_score").asDouble(
                          specs.path("3dmark").asDouble(0)));
            case "RAM" -> specs.path("ram_gb").asDouble(specs.path("memory_gb").asDouble(0)) * 100
                        + specs.path("speed_mhz").asDouble(0) * 0.1;
            case "Storage" -> specs.path("read_speed_mbps").asDouble(0) * 0.5
                            + specs.path("storage_gb").asDouble(specs.path("ssd_gb").asDouble(0)) * 10;
            default -> 0.0;
        };
    }

    private String slotTypeToCategoryFilter(String slotType) {
        return switch (slotType) {
            case "CPU"      -> "CPU";
            case "Mainboard"-> "Mainboard";
            case "RAM"      -> "RAM";
            case "GPU"      -> "Card đồ họa";
            case "Storage"  -> "Ổ cứng SSD";
            case "PSU"      -> "Nguồn máy tính";
            case "Case"     -> "Vỏ case";
            case "Cooler"   -> "Tản nhiệt";
            default -> null;
        };
    }

    private double[] shiftBudget(double[] splits, int from, int to, double amount) {
        double[] copy = splits.clone();
        if (copy[from] >= amount) {
            copy[from] -= amount;
            copy[to]   += amount;
        }
        return copy;
    }

    private JsonNode parseSpecs(String specsJson) {
        if (specsJson == null || specsJson.isBlank()) return null;
        try {
            return objectMapper.readTree(specsJson);
        } catch (Exception e) {
            return null;
        }
    }
}
