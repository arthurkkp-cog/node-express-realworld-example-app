package com.realworld.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest {
    private ArticleData article;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleData {
        private String title;
        private String description;
        private String body;
        private List<String> tagList;
    }
}
