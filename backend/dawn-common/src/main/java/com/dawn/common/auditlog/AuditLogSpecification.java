package com.dawn.common.auditlog;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AuditLogSpecification {

    public static Specification<AuditLog> fromDate(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    startDate.toInstant(ZoneOffset.UTC));
        };
    }

    public static Specification<AuditLog> toDate(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    endDate.toInstant(ZoneOffset.UTC));
        };
    }

    public static Specification<AuditLog> build(LocalDateTime startDate, LocalDateTime endDate) {
        return Specification
                .where(fromDate(startDate))
                .and(toDate(endDate));
    }
}
