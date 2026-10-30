package com.dawn.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.dawn.notification", "com.dawn.common"})
@EnableScheduling
public class DawnNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnNotificationApplication.class, args);
    }
}
