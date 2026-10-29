package com.dawn.svc.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** auth-service: identity provider (dawn-user), issues RS256 access tokens. */
@SpringBootApplication(scanBasePackages = "com.dawn.svc.auth")
@ComponentScan(
        basePackages = {"com.dawn.svc.auth", "com.dawn.common", "com.dawn.user"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.user"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.user"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
