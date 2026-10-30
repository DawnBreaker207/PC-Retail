package com.dawn.builder.controller;

import com.dawn.builder.entity.BuildTemplate;
import com.dawn.builder.infra.persistence.JpaBuildTemplateRepository;
import com.dawn.builder.service.CompatibilityEngine;
import com.dawn.common.security.AuthorizationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/builder")
@RequiredArgsConstructor
public class BuilderController {

    private final CompatibilityEngine engine;
    private final JpaBuildTemplateRepository templateRepo;

    @PostMapping("/validate")
    public CompatibilityEngine.Result validate(@RequestBody Map<String, Map<String, Object>> body) {
        Map<String, Map<String, Object>> specs = body.containsKey("slots")
                ? unchecked(body.get("slots"))
                : body;
        return engine.validate(specs);
    }

    @GetMapping("/templates")
    public List<BuildTemplate> templates() {
        return templateRepo.findAll();
    }

    @GetMapping("/templates/{id}")
    public BuildTemplate template(@PathVariable Long id) {
        return templateRepo.findById(id).orElseThrow(() -> new com.dawn.common.exception.wrapper.ResourceNotFoundException("Template not found: " + id));
    }

    @PostMapping("/templates")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public BuildTemplate create(@RequestBody BuildTemplate t) {
        t.setId(null);
        t.setCreatedBy(com.dawn.common.utils.SecurityUtils.getCurrentUserId());
        return templateRepo.save(t);
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public BuildTemplate update(@PathVariable Long id, @RequestBody BuildTemplate t) {
        BuildTemplate existing = template(id);
        existing.setName(t.getName());
        existing.setDescription(t.getDescription());
        existing.setThumbnailUrl(t.getThumbnailUrl());
        existing.setStatus(t.getStatus());
        return templateRepo.save(existing);
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public void delete(@PathVariable Long id) {
        templateRepo.deleteById(id);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> unchecked(Object o) {
        return (Map<String, Map<String, Object>>) o;
    }
}
