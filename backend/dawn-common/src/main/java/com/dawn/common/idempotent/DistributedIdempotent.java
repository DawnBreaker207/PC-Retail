package com.dawn.common.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as idempotent: a Redis key derived from {@code keyExpression} (SpEL)
 * is SET NX EX {@code ttlSeconds}. Duplicate calls within the TTL window receive the
 * cached result immediately without re-executing the method body.
 *
 * <pre>{@code
 * @DistributedIdempotent(keyExpression = "'payment:' + #request.orderId", ttlSeconds = 86400)
 * public PaymentResult pay(PaymentRequest request) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedIdempotent {
    /** SpEL expression evaluated against method arguments to produce the dedup key. */
    String keyExpression();

    /** How long (seconds) to remember the result. Default: 24 hours. */
    int ttlSeconds() default 86400;
}
