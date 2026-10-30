package com.dawn.payment;

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

/** dawn-payment: payment + VNPay gateway. */
@SpringBootApplication(scanBasePackages = "com.dawn.payment")
@ComponentScan(
        basePackages = {"com.dawn.common", "com.dawn.payment"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = SpringBootApplication.class))
@EnableFeignClients(basePackages = "com.dawn")
@EntityScan(basePackages = {"com.dawn.common", "com.dawn.payment"})
@EnableJpaRepositories(basePackages = {"com.dawn.common", "com.dawn.payment"})
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DawnPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnPaymentApplication.class, args);
    }

}
