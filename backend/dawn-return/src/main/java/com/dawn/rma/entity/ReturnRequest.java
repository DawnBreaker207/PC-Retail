package com.dawn.rma.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "return_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ReturnRequest extends AbstractMappedEntity {

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
    @Column(columnDefinition = "jsonb")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "dispute_resolution", length = 20)
    private DisputeResolution disputeResolution;

    @Column(name = "dispute_note", columnDefinition = "TEXT")
    private String disputeNote;
}
