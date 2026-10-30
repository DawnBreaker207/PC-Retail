package com.dawn.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** dawn-catalog: catalog + builder. */
@SpringBootApplication(scanBasePackages = "com.dawn.catalog")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EnableFeignClients(basePackages = "com.dawn")
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.catalog", "com.dawn.builder"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DawnCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnCatalogApplication.class, args);
    }

}
