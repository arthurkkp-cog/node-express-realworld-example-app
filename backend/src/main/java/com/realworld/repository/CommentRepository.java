package com.realworld.repository;

import com.realworld.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.article.slug = :slug " +
           "AND (c.author.demo = true OR c.author.id = :userId) " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByArticleSlugAndVisibleToUser(@Param("slug") String slug, @Param("userId") Long userId);
    
    @Query("SELECT c FROM Comment c WHERE c.article.slug = :slug " +
           "AND c.author.demo = true " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByArticleSlugForAnonymous(@Param("slug") String slug);
    
    List<Comment> findByArticleSlugOrderByCreatedAtDesc(String slug);
}
