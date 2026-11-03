package com.dawn.user;

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

/** dawn-user: authentication, user profile, address. */
@SpringBootApplication(scanBasePackages = "com.dawn.user")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.user", "com.dawn.wishlist"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EnableFeignClients(basePackages = "com.dawn")
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.user", "com.dawn.wishlist"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.user", "com.dawn.wishlist"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DawnUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnUserApplication.class, args);
    }

}
