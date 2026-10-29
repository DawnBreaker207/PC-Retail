package com.dawn.svc.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** payment-service: dawn-payment. */
@SpringBootApplication(scanBasePackages = "com.dawn.svc.payment")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.payment"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.payment"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.payment"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
