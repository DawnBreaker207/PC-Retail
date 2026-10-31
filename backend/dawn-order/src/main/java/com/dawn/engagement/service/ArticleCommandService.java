package com.dawn.engagement.service;

import com.dawn.engagement.entity.Article;
import com.dawn.engagement.infra.persistence.JpaArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ArticleCommandService {

    private final JpaArticleRepository articleRepo;

    @Transactional
    public Article create(Article a, Long authorId) {
        a.setId(null);
        a.setAuthorId(authorId);
        if ("PUBLISHED".equals(a.getStatus())) a.setPublishedAt(Instant.now());
        return articleRepo.save(a);
    }

    @Transactional
    public Article publish(Long id) {
        Article a = articleRepo.findById(id).orElseThrow();
        a.setStatus("PUBLISHED");
        a.setPublishedAt(Instant.now());
        return articleRepo.save(a);
    }

    @Transactional
    public Article unpublish(Long id) {
        Article a = articleRepo.findById(id).orElseThrow();
        a.setStatus("HIDDEN");
        return articleRepo.save(a);
    }
}
