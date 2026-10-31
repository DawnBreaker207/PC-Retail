package com.dawn.engagement.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.engagement.entity.Article;
import com.dawn.engagement.infra.persistence.JpaArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleQueryService {

    private final JpaArticleRepository articleRepo;

    @Transactional(readOnly = true)
    public List<Article> listPublished() {
        return articleRepo.findByStatusAndDeletedAtIsNull("PUBLISHED");
    }

    @Transactional(readOnly = true)
    public Article getBySlug(String slug) {
        return articleRepo.findBySlugAndDeletedAtIsNull(slug)
                .filter(a -> "PUBLISHED".equals(a.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Article not found: " + slug));
    }
}
