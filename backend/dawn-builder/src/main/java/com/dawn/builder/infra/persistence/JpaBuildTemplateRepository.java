package com.dawn.builder.infra.persistence;

import com.dawn.builder.entity.BuildTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBuildTemplateRepository extends JpaRepository<BuildTemplate, Long> {
}
