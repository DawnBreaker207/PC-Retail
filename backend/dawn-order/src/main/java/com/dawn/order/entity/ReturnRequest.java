package com.dawn.order.entity;

import com.dawn.order.entity.enums.ReturnStatus;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "return_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Hidden
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_order_id", nullable = false)
    private Long subOrderId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> images = List.of();

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "disputed_at")
    private Instant disputedAt;

    @Column(name = "dispute_resolved_by")
    private Long disputeResolvedBy;

    @Column(name = "dispute_resolution", length = 20)
    private String disputeResolution;

    @Column(name = "dispute_note", columnDefinition = "TEXT")
    private String disputeNote;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_holder", length = 100)
    private String accountHolder;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
