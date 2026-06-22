package com.dawn.user.infra.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitMQConfig {

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(UserRabbitMQConstants.EMAIL_EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(UserRabbitMQConstants.EMAIL_QUEUE, true);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(UserRabbitMQConstants.EMAIL_ROUTING_KEY);
    }
}
