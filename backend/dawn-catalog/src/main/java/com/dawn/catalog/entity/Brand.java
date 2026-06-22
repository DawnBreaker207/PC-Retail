package com.dawn.catalog.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "brand")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Hidden
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Brand extends AbstractMappedEntity {
    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    private Instant deletedAt;
}
