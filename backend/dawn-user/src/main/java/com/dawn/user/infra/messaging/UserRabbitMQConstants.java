package com.dawn.user.infra.messaging;

public final class UserRabbitMQConstants {

    public static final String EMAIL_EXCHANGE = "dawn.email.exchange";
    public static final String EMAIL_QUEUE = "dawn.email.queue";
    public static final String EMAIL_ROUTING_KEY = "dawn.email";

    private UserRabbitMQConstants() {
    }
}
