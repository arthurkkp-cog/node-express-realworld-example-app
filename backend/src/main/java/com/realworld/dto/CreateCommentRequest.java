package com.realworld.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    private CommentData comment;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private String body;
    }
}
