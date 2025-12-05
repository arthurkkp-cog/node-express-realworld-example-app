package com.realworld.service;

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
import org.springframework.http.HttpStatus;

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

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setBio("Test bio");
        testUser.setImage("https://api.realworld.io/images/smiley-cyrus.jpeg");
        testUser.setDemo(false);
        testUser.setFollowedBy(new HashSet<>());
        testUser.setFavorites(new HashSet<>());
        testUser.setFollowing(new HashSet<>());

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("test-tag");

        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setSlug("test-article-1");
        testArticle.setTitle("Test Article");
        testArticle.setDescription("Test description");
        testArticle.setBody("Test body content");
        testArticle.setCreatedAt(Instant.now());
        testArticle.setUpdatedAt(Instant.now());
        testArticle.setAuthor(testUser);
        testArticle.setTagList(new HashSet<>(Collections.singletonList(testTag)));
        testArticle.setFavoritedBy(new HashSet<>());
        testArticle.setComments(new HashSet<>());
    }

    @Test
    void getArticle_NotFound() {
        when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                articleService.getArticle("nonexistent", null));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
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

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void deleteArticle_NotFound() {
        when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                articleService.deleteArticle("nonexistent", 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
