package com.realworld.controller;

import com.realworld.dto.*;
import com.realworld.entity.User;
import com.realworld.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/articles")
    public ResponseEntity<ArticlesResponse> getArticles(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String favorited,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        ArticlesResponse response = articleService.getArticles(tag, author, favorited, offset, limit, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/articles/feed")
    public ResponseEntity<ArticlesResponse> getFeed(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        ArticlesResponse response = articleService.getFeed(offset, limit, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/articles")
    public ResponseEntity<ArticleResponse> createArticle(
            @RequestBody CreateArticleRequest request,
            @AuthenticationPrincipal User currentUser) {
        ArticleDTO article = articleService.createArticle(request.getArticle(), currentUser.getId());
        return new ResponseEntity<>(ArticleResponse.builder().article(article).build(), HttpStatus.CREATED);
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<ArticleResponse> getArticle(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        ArticleDTO article = articleService.getArticle(slug, userId);
        return ResponseEntity.ok(ArticleResponse.builder().article(article).build());
    }

    @PutMapping("/articles/{slug}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable String slug,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal User currentUser) {
        ArticleDTO article = articleService.updateArticle(request.getArticle(), slug, currentUser.getId());
        return ResponseEntity.ok(ArticleResponse.builder().article(article).build());
    }

    @DeleteMapping("/articles/{slug}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        articleService.deleteArticle(slug, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articles/{slug}/comments")
    public ResponseEntity<CommentsResponse> getComments(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        List<CommentDTO> comments = articleService.getCommentsByArticle(slug, userId);
        return ResponseEntity.ok(CommentsResponse.builder().comments(comments).build());
    }

    @PostMapping("/articles/{slug}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String slug,
            @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        CommentDTO comment = articleService.addComment(request.getComment().getBody(), slug, currentUser.getId());
        return ResponseEntity.ok(CommentResponse.builder().comment(comment).build());
    }

    @DeleteMapping("/articles/{slug}/comments/{id}")
    public ResponseEntity<Object> deleteComment(
            @PathVariable String slug,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        articleService.deleteComment(id, currentUser.getId());
        return ResponseEntity.ok().body(java.util.Collections.emptyMap());
    }

    @PostMapping("/articles/{slug}/favorite")
    public ResponseEntity<ArticleResponse> favoriteArticle(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        ArticleDTO article = articleService.favoriteArticle(slug, currentUser.getId());
        return ResponseEntity.ok(ArticleResponse.builder().article(article).build());
    }

    @DeleteMapping("/articles/{slug}/favorite")
    public ResponseEntity<ArticleResponse> unfavoriteArticle(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        ArticleDTO article = articleService.unfavoriteArticle(slug, currentUser.getId());
        return ResponseEntity.ok(ArticleResponse.builder().article(article).build());
    }
}
