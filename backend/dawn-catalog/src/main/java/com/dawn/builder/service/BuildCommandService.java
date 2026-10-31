package com.dawn.builder.service;

import com.dawn.builder.entity.BuildTemplate;
import com.dawn.builder.repository.JpaBuildTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-side service for build templates.
 * Handles create, update, and delete operations.
 */
@Service
@RequiredArgsConstructor
public class BuildCommandService {

    private final JpaBuildTemplateRepository templateRepository;

    @Transactional
    public BuildTemplate saveTemplate(BuildTemplate template) {
        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }
}
