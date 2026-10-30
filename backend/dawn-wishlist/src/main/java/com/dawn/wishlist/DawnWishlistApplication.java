package com.dawn.wishlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.dawn.wishlist", "com.dawn.common"})
public class DawnWishlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(DawnWishlistApplication.class, args);
    }
}
