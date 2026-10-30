package com.dawn.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dawn.shipping", "com.dawn.common"})
public class DawnShippingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnShippingApplication.class, args);
    }
}
