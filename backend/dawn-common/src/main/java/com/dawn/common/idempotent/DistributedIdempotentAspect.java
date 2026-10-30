package com.dawn.common.idempotent;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedIdempotentAspect {

    private static final String KEY_PREFIX = "idempotent:";
    private static final String SENTINEL = "__processing__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, DistributedIdempotent idempotent) throws Throwable {
        String key = KEY_PREFIX + resolveKey(pjp, idempotent.keyExpression());
        Duration ttl = Duration.ofSeconds(idempotent.ttlSeconds());

        // Try to claim the key: SET NX EX
        Boolean claimed = stringRedisTemplate.opsForValue().setIfAbsent(key, SENTINEL, ttl);
        if (Boolean.FALSE.equals(claimed)) {
            // Another thread/instance already processed or is processing this request
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && !SENTINEL.equals(cached)) {
                // Return cached result
                Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();
                log.debug("Idempotent cache hit for key '{}'", key);
                return objectMapper.readValue(cached, returnType);
            }
            // Still processing elsewhere — let the call through (avoids deadlock on retry)
            log.warn("Idempotent key '{}' still held by another instance, proceeding", key);
        }

        try {
            Object result = pjp.proceed();
            if (result != null) {
                String serialized = objectMapper.writeValueAsString(result);
                stringRedisTemplate.opsForValue().set(key, serialized, ttl);
            }
            return result;
        } catch (Throwable t) {
            // On failure, release the key so the caller can retry
            stringRedisTemplate.delete(key);
            throw t;
        }
    }

    private String resolveKey(ProceedingJoinPoint pjp, String expression) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Parameter[] params = method.getParameters();
        Object[] args = pjp.getArgs();

        EvaluationContext ctx = new StandardEvaluationContext();
        for (int i = 0; i < params.length; i++) {
            ctx.setVariable(params[i].getName(), args[i]);
        }
        return String.valueOf(parser.parseExpression(expression).getValue(ctx));
    }
}
