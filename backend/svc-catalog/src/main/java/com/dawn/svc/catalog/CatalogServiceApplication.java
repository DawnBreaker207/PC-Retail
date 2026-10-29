package com.dawn.svc.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** catalog-service: dawn-catalog + dawn-builder + dawn-user. */
@SpringBootApplication(scanBasePackages = "com.dawn.svc.catalog")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder", "com.dawn.user"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder", "com.dawn.user"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder", "com.dawn.user"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
