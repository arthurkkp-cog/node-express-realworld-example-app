package com.realworld.controller;

import com.realworld.dto.TagsResponse;
import com.realworld.entity.User;
import com.realworld.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public ResponseEntity<TagsResponse> getTags(@AuthenticationPrincipal User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        List<String> tags = tagService.getTags(userId);
        return ResponseEntity.ok(TagsResponse.builder().tags(tags).build());
    }
}
