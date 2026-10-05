package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.Article;
import com.dawn.engagement.infra.persistence.JpaArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final JpaArticleRepository articleRepo;

    // Public: chỉ PUBLISHED + chưa xóa
    @GetMapping("/articles")
    public List<Article> published() {
        return articleRepo.findByStatusAndDeletedAtIsNull("PUBLISHED");
    }

    @GetMapping("/articles/{slug}")
    public Article bySlug(@PathVariable String slug) {
        return articleRepo.findBySlugAndDeletedAtIsNull(slug)
                .filter(a -> "PUBLISHED".equals(a.getStatus()))
                .orElseThrow(() -> new com.dawn.common.exception.wrapper.ResourceNotFoundException("Article not found: " + slug));
    }

    @PostMapping("/admin/articles")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article create(@RequestBody Article a) {
        a.setId(null);
        a.setAuthorId(SecurityUtils.getCurrentUserId());
        if ("PUBLISHED".equals(a.getStatus())) a.setPublishedAt(Instant.now());
        return articleRepo.save(a);
    }

    @PutMapping("/admin/articles/{id}/publish")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article publish(@PathVariable Long id) {
        Article a = articleRepo.findById(id).orElseThrow();
        a.setStatus("PUBLISHED");
        a.setPublishedAt(Instant.now());
        return articleRepo.save(a);
    }

    @PutMapping("/admin/articles/{id}/unpublish")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article unpublish(@PathVariable Long id) {
        Article a = articleRepo.findById(id).orElseThrow();
        a.setStatus("HIDDEN");
        return articleRepo.save(a);
    }
}
