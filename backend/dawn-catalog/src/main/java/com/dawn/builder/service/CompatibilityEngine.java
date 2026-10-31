package com.dawn.builder.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 10 rules in one engine instead of 10 classes — split when rules outgrow this
@Service
public class CompatibilityEngine {

    public record Issue(String slot, String code, String severity, String message) {
    }

    public record Result(boolean valid, List<Issue> errors, List<Issue> warnings) {
    }

    private static final List<String> REQUIRED = List.of("CPU", "MAINBOARD", "RAM", "GPU", "PSU", "CASE", "STORAGE");

    public Result validate(Map<String, Map<String, Object>> specsBySlot) {
        List<Issue> errors = new ArrayList<>();
        List<Issue> warnings = new ArrayList<>();
        if (specsBySlot == null || specsBySlot.isEmpty()) {
            return new Result(false, List.of(new Issue("*", "EMPTY_BUILD", "ERROR", "Build trống")), warnings);
        }
        // 0. Required slots
        for (String s : REQUIRED) {
            if (!specsBySlot.containsKey(s)) {
                errors.add(new Issue(s, "MISSING_SLOT", "ERROR", "Thiếu slot bắt buộc: " + s));
            }
        }
        Map<String, Object> cpu = specsBySlot.get("CPU");
        Map<String, Object> board = specsBySlot.get("MAINBOARD");
        Map<String, Object> ram = specsBySlot.get("RAM");
        Map<String, Object> psu = specsBySlot.get("PSU");
        Map<String, Object> gpu = specsBySlot.get("GPU");
        Map<String, Object> pcCase = specsBySlot.get("CASE");
        Map<String, Object> cooler = specsBySlot.get("COOLER");

        // 1. Socket CPU == Mainboard
        checkEq(cpu, board, "socket", "CPU", "MAINBOARD", "SOCKET_MISMATCH", errors);
        // 2. RAM type == Mainboard ramType
        checkEq(ram, board, "ramType", "RAM", "MAINBOARD", "RAM_TYPE_MISMATCH", errors);
        // 3. RAM speed support (WARNING)
        checkIn(ram, board, "speed", "supportedSpeeds", "RAM", "RAM_SPEED_UNSUPPORTED", warnings);
        // 4. Cooler socket support
        if (cpu != null && cooler != null) {
            Object socket = cpu.get("socket");
            Object supported = cooler.get("supportedSockets");
            if (socket != null && supported instanceof List<?> list && !list.contains(socket.toString())) {
                errors.add(new Issue("COOLER", "COOLER_SOCKET", "ERROR", "Cooler không hỗ trợ socket " + socket));
            }
        }
        // 5. PSU wattage (TDP + 20% margin)
        if (psu != null) {
            double tdp = num(cpu, "tdp") + num(gpu, "tdp") + 100;
            double wattage = num(psu, "wattage");
            if (wattage > 0 && wattage < tdp * 1.2) {
                errors.add(new Issue("PSU", "PSU_WATTAGE", "ERROR", "PSU yếu: cần >=" + Math.round(tdp * 1.2) + "W"));
            }
        }
        // 6. GPU clearance
        checkLte(gpu, pcCase, "length", "maxGpuLength", "GPU", "GPU_CLEARANCE", errors);
        // 7. Cooler height clearance
        checkLte(cooler, pcCase, "height", "maxCoolerHeight", "COOLER", "COOLER_CLEARANCE", errors);
        // 8. Mainboard formFactor in Case
        checkIn(board, pcCase, "formFactor", "supportedFormFactors", "MAINBOARD", "FORM_FACTOR", errors);
        // 9. PSU formFactor in Case
        checkIn(psu, pcCase, "formFactor", "supportedPsuFormFactors", "PSU", "PSU_FORM_FACTOR", errors);
        // 10. RAM slots capacity (WARNING when exceeded)
        if (ram != null && board != null) {
            double sticks = num(ram, "sticks");
            double slots = num(board, "slotCount");
            if (sticks > 0 && slots > 0 && sticks > slots) {
                errors.add(new Issue("RAM", "RAM_SLOTS", "ERROR", "Quá số khe RAM"));
            }
        }
        return new Result(errors.isEmpty(), errors, warnings);
    }

    private static Object val(Map<String, Object> m, String k) {
        return m == null ? null : m.get(k);
    }

    private static double num(Map<String, Object> m, String k) {
        Object v = val(m, k);
        if (v instanceof Number n) return n.doubleValue();
        try {
            return v == null ? 0 : Double.parseDouble(v.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void checkEq(Map<String, Object> a, Map<String, Object> b, String key,
                         String slotA, String slotB, String code, List<Issue> errors) {
        Object va = val(a, key);
        Object vb = val(b, key);
        if (va != null && vb != null && !va.toString().equalsIgnoreCase(vb.toString())) {
            errors.add(new Issue(slotA, code, "ERROR", slotA + "." + key + "=" + va + " != " + slotB + "." + key + "=" + vb));
        }
    }

    @SuppressWarnings("unchecked")
    private void checkIn(Map<String, Object> item, Map<String, Object> holder, String key, String allowedKey,
                         String slot, String code, List<Issue> out) {
        Object v = val(item, key);
        Object allowed = val(holder, allowedKey);
        if (v != null && allowed instanceof List<?> list && !list.contains(v.toString())) {
            out.add(new Issue(slot, code, out == null ? "ERROR" : "WARNING", slot + "." + key + "=" + v + " không được hỗ trợ"));
        }
    }

    private void checkLte(Map<String, Object> item, Map<String, Object> holder, String key, String maxKey,
                          String slot, String code, List<Issue> errors) {
        double v = num(item, key);
        double max = num(holder, maxKey);
        if (v > 0 && max > 0 && v > max) {
            errors.add(new Issue(slot, code, "ERROR", slot + " quá khổ: " + v + " > " + max));
        }
    }
}
