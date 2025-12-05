package com.realworld.repository;

import com.realworld.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    @Query("SELECT t FROM Tag t JOIN t.articles a WHERE " +
           "(a.author.demo = true OR a.author.id = :userId) " +
           "GROUP BY t ORDER BY COUNT(a) DESC")
    List<Tag> findPopularTags(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Tag t JOIN t.articles a WHERE a.author.demo = true " +
           "GROUP BY t ORDER BY COUNT(a) DESC")
    List<Tag> findPopularTagsForAnonymous();
}
