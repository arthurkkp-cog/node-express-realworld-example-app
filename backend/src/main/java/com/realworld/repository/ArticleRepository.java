package com.realworld.repository;

import com.realworld.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    Optional<Article> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT a FROM Article a WHERE " +
           "(a.author.demo = true OR a.author.id = :userId) " +
           "ORDER BY a.createdAt DESC")
    Page<Article> findAllArticles(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE " +
           "(a.author.demo = true OR a.author.id = :userId) " +
           "AND (:tag IS NULL OR EXISTS (SELECT t FROM a.tagList t WHERE t.name = :tag)) " +
           "AND (:author IS NULL OR a.author.username = :author) " +
           "AND (:favorited IS NULL OR EXISTS (SELECT u FROM a.favoritedBy u WHERE u.username = :favorited)) " +
           "ORDER BY a.createdAt DESC")
    Page<Article> findAllWithFilters(
        @Param("userId") Long userId,
        @Param("tag") String tag,
        @Param("author") String author,
        @Param("favorited") String favorited,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(a) FROM Article a WHERE " +
           "(a.author.demo = true OR a.author.id = :userId) " +
           "AND (:tag IS NULL OR EXISTS (SELECT t FROM a.tagList t WHERE t.name = :tag)) " +
           "AND (:author IS NULL OR a.author.username = :author) " +
           "AND (:favorited IS NULL OR EXISTS (SELECT u FROM a.favoritedBy u WHERE u.username = :favorited))")
    long countWithFilters(
        @Param("userId") Long userId,
        @Param("tag") String tag,
        @Param("author") String author,
        @Param("favorited") String favorited
    );
    
    @Query("SELECT a FROM Article a JOIN a.author.followedBy f WHERE f.id = :userId ORDER BY a.createdAt DESC")
    Page<Article> findFeedArticles(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Article a JOIN a.author.followedBy f WHERE f.id = :userId")
    long countFeedArticles(@Param("userId") Long userId);
    
    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
