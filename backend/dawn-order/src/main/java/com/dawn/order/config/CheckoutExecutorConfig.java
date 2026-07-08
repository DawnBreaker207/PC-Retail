package com.dawn.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CheckoutExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor checkoutExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("checkout-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.afterPropertiesSet();
        return executor;
    }
}
