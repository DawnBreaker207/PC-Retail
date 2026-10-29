package com.dawn.svc.order;

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

/** order-service: dawn-order + dawn-cart + dawn-promotion. */
@SpringBootApplication(scanBasePackages = "com.dawn.svc.order")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EnableFeignClients(basePackages = "com.dawn")
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.order", "com.dawn.cart", "com.dawn.promotion"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
