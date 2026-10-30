package com.dawn.tax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.dawn.tax", "com.dawn.common"})
public class DawnTaxApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnTaxApplication.class, args);
    }
}
