package com.dawn.order;

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

/** dawn-order: order + cart + promotion + engagement + return + reporting. */
@SpringBootApplication(scanBasePackages = "com.dawn.order")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion",
                "com.dawn.engagement", "com.dawn.return", "com.dawn.reporting"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EnableFeignClients(basePackages = "com.dawn")
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion",
        "com.dawn.engagement", "com.dawn.return", "com.dawn.reporting"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion",
        "com.dawn.engagement", "com.dawn.return", "com.dawn.reporting"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DawnOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnOrderApplication.class, args);
    }

}
