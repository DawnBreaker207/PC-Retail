package com.dawn.engagement.entity;

import com.dawn.common.entity.AbstractMappedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "article")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Article extends AbstractMappedEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(length = 30)
    @Builder.Default
    private String category = "NEWS";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
