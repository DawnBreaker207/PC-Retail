package com.dawn.rating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dawn.rating", "com.dawn.common"})
public class DawnRatingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnRatingApplication.class, args);
    }
}
