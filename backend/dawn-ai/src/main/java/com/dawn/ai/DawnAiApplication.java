package com.dawn.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dawn.ai")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.ai"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.ai"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.ai"})
@EnableJpaAuditing
@EnableScheduling
public class DawnAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnAiApplication.class, args);
    }

}
