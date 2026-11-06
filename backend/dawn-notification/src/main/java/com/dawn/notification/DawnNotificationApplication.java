package com.dawn.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dawn.notification")
@EnableFeignClients(basePackages = "com.dawn")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.notification"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.notification"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.notification"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DawnNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnNotificationApplication.class, args);
    }
}
