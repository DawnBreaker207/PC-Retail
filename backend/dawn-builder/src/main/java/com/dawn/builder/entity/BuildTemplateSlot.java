package com.dawn.builder.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "build_template_slot", uniqueConstraints = {
        @UniqueConstraint(name = "uq_template_slot", columnNames = {"template_id", "slot_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BuildTemplateSlot extends AbstractMappedEntity {

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 20)
    private SlotType slotType;

    @Column(name = "default_sku_id")
    private Long defaultSkuId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_sku_ids", columnDefinition = "jsonb")
    private List<Long> allowedSkuIds;

    @Column(name = "is_removable", nullable = false)
    @Builder.Default
    private boolean removable = false;
}
