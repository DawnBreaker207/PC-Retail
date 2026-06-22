package com.dawn.user.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import com.dawn.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PasswordResetToken extends AbstractMappedEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "used_at")
    private Instant usedAt;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
