package com.realworld.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    private String slug;
    private String title;
    private String description;
    private String body;
    private List<String> tagList;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean favorited;
    private int favoritesCount;
    private ProfileDTO author;
}
