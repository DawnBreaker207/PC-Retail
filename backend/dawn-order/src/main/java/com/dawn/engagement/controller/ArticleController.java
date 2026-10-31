package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.Article;
import com.dawn.engagement.service.ArticleCommandService;
import com.dawn.engagement.service.ArticleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleQueryService articleQueryService;
    private final ArticleCommandService articleCommandService;

    // Public: PUBLISHED and not deleted only
    @GetMapping("/articles")
    public List<Article> published() {
        return articleQueryService.listPublished();
    }

    @GetMapping("/articles/{slug}")
    public Article bySlug(@PathVariable String slug) {
        return articleQueryService.getBySlug(slug);
    }

    @PostMapping("/admin/articles")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article create(@RequestBody Article a) {
        return articleCommandService.create(a, SecurityUtils.getCurrentUserId());
    }

    @PutMapping("/admin/articles/{id}/publish")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article publish(@PathVariable Long id) {
        return articleCommandService.publish(id);
    }

    @PutMapping("/admin/articles/{id}/unpublish")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Article unpublish(@PathVariable Long id) {
        return articleCommandService.unpublish(id);
    }
}
