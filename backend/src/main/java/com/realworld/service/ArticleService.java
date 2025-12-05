package com.realworld.service;

import com.realworld.dto.*;
import com.realworld.entity.Article;
import com.realworld.entity.Comment;
import com.realworld.entity.Tag;
import com.realworld.entity.User;
import com.realworld.exception.AppException;
import com.realworld.repository.ArticleRepository;
import com.realworld.repository.CommentRepository;
import com.realworld.repository.TagRepository;
import com.realworld.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final ProfileService profileService;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository,
                          TagRepository tagRepository, CommentRepository commentRepository,
                          ProfileService profileService) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
        this.profileService = profileService;
    }

    public ArticlesResponse getArticles(String tag, String author, String favorited, 
                                         int offset, int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 10));
        
        Page<Article> articlesPage = articleRepository.findAllWithFilters(
                currentUserId, tag, author, favorited, pageable);
        
        long totalCount = articleRepository.countWithFilters(currentUserId, tag, author, favorited);

        List<ArticleDTO> articleDTOs = articlesPage.getContent().stream()
                .map(article -> mapToArticleDTO(article, currentUserId))
                .collect(Collectors.toList());

        return ArticlesResponse.builder()
                .articles(articleDTOs)
                .articlesCount(totalCount)
                .build();
    }

    public ArticlesResponse getFeed(int offset, int limit, Long currentUserId) {
        Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 10));
        
        Page<Article> articlesPage = articleRepository.findFeedArticles(currentUserId, pageable);
        long totalCount = articleRepository.countFeedArticles(currentUserId);

        List<ArticleDTO> articleDTOs = articlesPage.getContent().stream()
                .map(article -> mapToArticleDTO(article, currentUserId))
                .collect(Collectors.toList());

        return ArticlesResponse.builder()
                .articles(articleDTOs)
                .articlesCount(totalCount)
                .build();
    }

    public ArticleDTO getArticle(String slug, Long currentUserId) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        Map.of("article", List.of("not found"))));

        return mapToArticleDTO(article, currentUserId);
    }

    @Transactional
    public ArticleDTO createArticle(CreateArticleRequest.ArticleData articleData, Long authorId) {
        String title = articleData.getTitle();
        String description = articleData.getDescription();
        String body = articleData.getBody();
        List<String> tagList = articleData.getTagList() != null ? articleData.getTagList() : new ArrayList<>();

        Map<String, List<String>> errors = new HashMap<>();

        if (title == null || title.trim().isEmpty()) {
            errors.put("title", List.of("can't be blank"));
        }

        if (description == null || description.trim().isEmpty()) {
            errors.put("description", List.of("can't be blank"));
        }

        if (body == null || body.trim().isEmpty()) {
            errors.put("body", List.of("can't be blank"));
        }

        if (!errors.isEmpty()) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, errors);
        }

        String slug = slugify(title) + "-" + authorId;

        if (articleRepository.existsBySlug(slug)) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, 
                    Map.of("title", List.of("must be unique")));
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagList) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
            tags.add(tag);
        }

        Article article = Article.builder()
                .slug(slug)
                .title(title)
                .description(description)
                .body(body)
                .tagList(tags)
                .author(author)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        article = articleRepository.save(article);

        return mapToArticleDTO(article, authorId);
    }

    @Transactional
    public ArticleDTO updateArticle(UpdateArticleRequest.ArticleData articleData, String slug, Long userId) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, Map.of()));

        if (!article.getAuthor().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, 
                    Map.of("message", List.of("You are not authorized to update this article")));
        }

        String newSlug = slug;
        if (articleData.getTitle() != null && !articleData.getTitle().trim().isEmpty()) {
            newSlug = slugify(articleData.getTitle()) + "-" + userId;
            
            if (!newSlug.equals(slug) && articleRepository.existsBySlug(newSlug)) {
                throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, 
                        Map.of("title", List.of("must be unique")));
            }
            
            article.setTitle(articleData.getTitle());
            article.setSlug(newSlug);
        }

        if (articleData.getDescription() != null) {
            article.setDescription(articleData.getDescription());
        }

        if (articleData.getBody() != null) {
            article.setBody(articleData.getBody());
        }

        if (articleData.getTagList() != null) {
            article.getTagList().clear();
            
            Set<Tag> tags = new HashSet<>();
            for (String tagName : articleData.getTagList()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                tags.add(tag);
            }
            article.setTagList(tags);
        }

        article.setUpdatedAt(Instant.now());
        article = articleRepository.save(article);

        return mapToArticleDTO(article, userId);
    }

    @Transactional
    public void deleteArticle(String slug, Long userId) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, Map.of()));

        if (!article.getAuthor().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, 
                    Map.of("message", List.of("You are not authorized to delete this article")));
        }

        articleRepository.delete(article);
    }

    public List<CommentDTO> getCommentsByArticle(String slug, Long currentUserId) {
        List<Comment> comments;
        if (currentUserId != null) {
            comments = commentRepository.findByArticleSlugAndVisibleToUser(slug, currentUserId);
        } else {
            comments = commentRepository.findByArticleSlugForAnonymous(slug);
        }

        return comments.stream()
                .map(comment -> mapToCommentDTO(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO addComment(String body, String slug, Long userId) {
        if (body == null || body.trim().isEmpty()) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, 
                    Map.of("body", List.of("can't be blank")));
        }

        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "article", "not found"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        Comment comment = Comment.builder()
                .body(body)
                .article(article)
                .author(author)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        comment = commentRepository.save(comment);

        return mapToCommentDTO(comment, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, Map.of()));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AppException(HttpStatus.FORBIDDEN, 
                    Map.of("message", List.of("You are not authorized to delete this comment")));
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public ArticleDTO favoriteArticle(String slug, Long userId) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "article", "not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        user.getFavorites().add(article);
        userRepository.save(user);

        return mapToArticleDTO(article, userId);
    }

    @Transactional
    public ArticleDTO unfavoriteArticle(String slug, Long userId) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "article", "not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        user.getFavorites().remove(article);
        userRepository.save(user);

        return mapToArticleDTO(article, userId);
    }

    private ArticleDTO mapToArticleDTO(Article article, Long currentUserId) {
        List<String> tagNames = article.getTagList().stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.toList());

        boolean favorited = false;
        if (currentUserId != null) {
            favorited = article.getFavoritedBy().stream()
                    .anyMatch(user -> user.getId().equals(currentUserId));
        }

        return ArticleDTO.builder()
                .slug(article.getSlug())
                .title(article.getTitle())
                .description(article.getDescription())
                .body(article.getBody())
                .tagList(tagNames)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .favorited(favorited)
                .favoritesCount(article.getFavoritedBy().size())
                .author(profileService.mapToProfileDTO(article.getAuthor(), currentUserId))
                .build();
    }

    private CommentDTO mapToCommentDTO(Comment comment, Long currentUserId) {
        return CommentDTO.builder()
                .id(comment.getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .body(comment.getBody())
                .author(profileService.mapToProfileDTO(comment.getAuthor(), currentUserId))
                .build();
    }

    private String slugify(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");
        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug;
    }
}
