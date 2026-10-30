package com.dawn.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.dawn.media", "com.dawn.common"})
public class DawnMediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnMediaApplication.class, args);
    }
}
