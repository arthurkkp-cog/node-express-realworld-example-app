package com.realworld.service;

import com.realworld.dto.ArticleDTO;
import com.realworld.dto.CreateArticleRequest;
import com.realworld.entity.Article;
import com.realworld.entity.Tag;
import com.realworld.entity.User;
import com.realworld.exception.AppException;
import com.realworld.repository.ArticleRepository;
import com.realworld.repository.CommentRepository;
import com.realworld.repository.TagRepository;
import com.realworld.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProfileService profileService;

    private ArticleService articleService;

    private User testUser;
    private Article testArticle;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(articleRepository, userRepository, 
                tagRepository, commentRepository, profileService);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("hashedPassword")
                .bio("Test bio")
                .image("https://api.realworld.io/images/smiley-cyrus.jpeg")
                .demo(false)
                .followedBy(new HashSet<>())
                .favorites(new HashSet<>())
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("test-tag")
                .build();

        testArticle = Article.builder()
                .id(1L)
                .slug("test-article-1")
                .title("Test Article")
                .description("Test description")
                .body("Test body content")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .author(testUser)
                .tagList(new HashSet<>(Collections.singletonList(testTag)))
                .favoritedBy(new HashSet<>())
                .comments(new HashSet<>())
                .build();
    }

    @Test
    void getArticle_NotFound() {
        when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                articleService.getArticle("nonexistent", null));

        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void deleteArticle_Success() {
        when(articleRepository.findBySlug("test-article-1")).thenReturn(Optional.of(testArticle));
        doNothing().when(articleRepository).delete(testArticle);

        assertDoesNotThrow(() -> articleService.deleteArticle("test-article-1", 1L));
        verify(articleRepository).delete(testArticle);
    }

    @Test
    void deleteArticle_NotAuthor() {
        when(articleRepository.findBySlug("test-article-1")).thenReturn(Optional.of(testArticle));

        AppException exception = assertThrows(AppException.class, () ->
                articleService.deleteArticle("test-article-1", 999L));

        assertEquals(403, exception.getStatus().value());
    }

    @Test
    void deleteArticle_NotFound() {
        when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                articleService.deleteArticle("nonexistent", 1L));

        assertEquals(404, exception.getStatus().value());
    }
}
