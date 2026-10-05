package com.dawn.builder.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompatibilityEngineTest {

    private final CompatibilityEngine engine = new CompatibilityEngine();

    @Test
    void validBuild_passes() {
        Map<String, Map<String, Object>> specs = Map.of(
                "CPU", Map.of("socket", "LGA1700", "tdp", 65),
                "MAINBOARD", Map.of("socket", "LGA1700", "ramType", "DDR5", "slotCount", 4, "formFactor", "ATX"),
                "RAM", Map.of("ramType", "DDR5", "sticks", 2),
                "GPU", Map.of("tdp", 200, "length", 300),
                "PSU", Map.of("wattage", 750, "formFactor", "ATX"),
                "CASE", Map.of("maxGpuLength", 350, "supportedFormFactors", List.of("ATX"), "supportedPsuFormFactors", List.of("ATX")),
                "STORAGE", Map.of());
        var result = engine.validate(specs);
        assertTrue(result.valid(), () -> "expected valid but got: " + result.errors());
    }

    @Test
    void socketMismatch_fails() {
        Map<String, Map<String, Object>> specs = Map.of(
                "CPU", Map.of("socket", "LGA1700"),
                "MAINBOARD", Map.of("socket", "AM5", "ramType", "DDR5", "slotCount", 4, "formFactor", "ATX"),
                "RAM", Map.of("ramType", "DDR5", "sticks", 2),
                "GPU", Map.of(),
                "PSU", Map.of("wattage", 750, "formFactor", "ATX"),
                "CASE", Map.of("supportedFormFactors", List.of("ATX"), "supportedPsuFormFactors", List.of("ATX")),
                "STORAGE", Map.of());
        var result = engine.validate(specs);
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(i -> i.code().equals("SOCKET_MISMATCH")));
    }

    @Test
    void missingRequiredSlot_fails() {
        var result = engine.validate(Map.of("CPU", Map.of("socket", "LGA1700")));
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(i -> i.code().equals("MISSING_SLOT")));
    }
}
