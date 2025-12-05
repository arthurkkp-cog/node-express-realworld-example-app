package com.realworld.service;

import com.realworld.entity.Tag;
import com.realworld.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<String> getTags(Long currentUserId) {
        List<Tag> tags;
        if (currentUserId != null) {
            tags = tagRepository.findPopularTags(currentUserId);
        } else {
            tags = tagRepository.findPopularTagsForAnonymous();
        }

        return tags.stream()
                .map(Tag::getName)
                .limit(10)
                .collect(Collectors.toList());
    }
}
