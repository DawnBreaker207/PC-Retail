package com.dawn.engagement.infra.persistence;

import com.dawn.engagement.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByStatusAndDeletedAtIsNull(String status);

    Optional<Article> findBySlugAndDeletedAtIsNull(String slug);
}
