package com.dawn.ai.vectorizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Converts structured PC product specs into a normalized 13-dimensional float vector.
 * Dimension layout (order is fixed — changing breaks existing pgvector data):
 *  [0]  price_norm          price / 100_000_000
 *  [1]  ram_norm            ram_gb / 128
 *  [2]  storage_norm        storage_gb / 4096
 *  [3]  gpu_vram_norm       gpu_vram_gb / 24
 *  [4]  cpu_cores_norm      cpu_cores / 32
 *  [5]  cpu_score_norm      cpu_benchmark / 100_000
 *  [6]  display_hz_norm     refresh_rate_hz / 360
 *  [7]  display_size_norm   display_inches / 18
 *  [8]  battery_norm        battery_wh / 100
 *  [9]  weight_norm         weight_kg / 5
 *  [10] is_laptop           1 if laptop, 0 otherwise
 *  [11] is_gaming           1 if gaming category/tag
 *  [12] is_workstation      1 if workstation/professional category/tag
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSpecVectorizer {

    public static final int DIMENSIONS = 13;

    private final ObjectMapper objectMapper;

    public float[] vectorize(long skuId, long priceVnd, String categoryName, String specsJson) {
        float[] v = new float[DIMENSIONS];

        v[0] = clamp((float) (priceVnd / 100_000_000.0));

        JsonNode specs = parseSpecs(specsJson);

        v[1]  = clamp(readFloat(specs, 128f,  "ram_gb", "ram", "memory_gb", "dung_luong_ram"));
        v[2]  = clamp(readFloat(specs, 4096f, "storage_gb", "ssd_gb", "hdd_gb", "o_cung_gb", "storage"));
        v[3]  = clamp(readFloat(specs, 24f,   "gpu_vram_gb", "vram_gb", "vram", "gpu_vram"));
        v[4]  = clamp(readFloat(specs, 32f,   "cpu_cores", "so_nhan", "cores"));
        v[5]  = clamp(readFloat(specs, 100_000f, "cpu_score", "cpu_benchmark", "benchmark_score"));
        v[6]  = clamp(readFloat(specs, 360f,  "refresh_rate", "display_hz", "tan_so_quet", "hz", "man_hinh_hz"));
        v[7]  = clamp(readFloat(specs, 18f,   "display_inches", "screen_size", "man_hinh", "kich_co_man_hinh"));
        v[8]  = clamp(readFloat(specs, 100f,  "battery_wh", "pin_wh", "battery", "pin"));
        v[9]  = clamp(readFloat(specs, 5f,    "weight_kg", "can_nang", "trong_luong", "weight"));

        String cat = categoryName == null ? "" : categoryName.toLowerCase();
        boolean isLaptop      = cat.contains("laptop") || cat.contains("may tinh xach tay") || cat.contains("notebook");
        boolean isGaming      = cat.contains("gaming") || cat.contains("game");
        boolean isWorkstation = cat.contains("workstation") || cat.contains("do_hoa") || cat.contains("professional");

        // also check tags in specs JSON
        if (specs != null) {
            JsonNode tags = specs.path("tags");
            if (tags.isArray()) {
                for (JsonNode tag : tags) {
                    String t = tag.asText("").toLowerCase();
                    if (t.contains("laptop") || t.contains("notebook")) isLaptop = true;
                    if (t.contains("gaming") || t.contains("game"))     isGaming = true;
                    if (t.contains("workstation") || t.contains("do hoa")) isWorkstation = true;
                }
            }
        }

        v[10] = isLaptop      ? 1f : 0f;
        v[11] = isGaming      ? 1f : 0f;
        v[12] = isWorkstation ? 1f : 0f;

        return v;
    }

    private float readFloat(JsonNode specs, float divisor, String... keys) {
        if (specs == null || specs.isMissingNode()) return 0f;
        for (String key : keys) {
            JsonNode n = specs.path(key);
            if (!n.isMissingNode() && !n.isNull() && n.isNumber()) {
                return (float) (n.doubleValue() / divisor);
            }
        }
        return 0f;
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private JsonNode parseSpecs(String specsJson) {
        if (specsJson == null || specsJson.isBlank()) return null;
        try {
            return objectMapper.readTree(specsJson);
        } catch (Exception e) {
            log.debug("Could not parse specs JSON: {}", e.getMessage());
            return null;
        }
    }

    public String toPostgresVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }
}
