package com.dawn.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** Vector sở thích dạng thưa: trọng số theo tên danh mục + giá trung bình đã xem. */
@Entity
@Table(name = "user_preference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preference_vector", columnDefinition = "jsonb", nullable = false)
    private Map<String, Double> preferenceVector;

    @Column(name = "average_price", precision = 14, scale = 2)
    private BigDecimal averagePrice;

    @Column(name = "event_count", nullable = false)
    private long eventCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
