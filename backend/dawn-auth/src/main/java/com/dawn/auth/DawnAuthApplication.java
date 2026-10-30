package com.dawn.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.dawn.auth", "com.dawn.common"})
public class DawnAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(DawnAuthApplication.class, args);
    }
}
