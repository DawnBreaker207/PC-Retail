package com.dawn.common.auditlog;

import com.dawn.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(loggable)")
    public Object audit(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        Long actorId = SecurityUtils.getCurrentUserId();
        String actorRole = SecurityUtils.getCurrentUserRole();
        String entityId = loggable.entityId();
        if (entityId == null || entityId.isEmpty()) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof Long) {
                entityId = String.valueOf(args[0]);
            }
        }

        try {
            long id = entityId != null && !entityId.isEmpty() ? Long.parseLong(entityId) : 0L;
            Object result = joinPoint.proceed();
            auditLogService.log(
                    loggable.action(),
                    loggable.entity(),
                    entityId != null ? Long.parseLong(entityId) : null,
                    null,
                    result != null ? result.toString() : null,
                    actorId,
                    actorRole,
                    loggable.tier());
            return result;
        } catch (Throwable t) {
            auditLogService.log(
                    loggable.action(),
                    loggable.entity(),
                    entityId != null ? Long.parseLong(entityId) : null,
                    null,
                    null,
                    actorId,
                    actorRole,
                    loggable.tier());
            throw t;
        }
    }
}
