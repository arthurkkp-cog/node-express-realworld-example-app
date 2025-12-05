package com.realworld.controller;

import com.realworld.dto.ProfileDTO;
import com.realworld.dto.ProfileResponse;
import com.realworld.entity.User;
import com.realworld.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        ProfileDTO profile = profileService.getProfile(username, userId);
        return ResponseEntity.ok(ProfileResponse.builder().profile(profile).build());
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<ProfileResponse> followUser(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        ProfileDTO profile = profileService.followUser(username, currentUser.getId());
        return ResponseEntity.ok(ProfileResponse.builder().profile(profile).build());
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ProfileResponse> unfollowUser(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        ProfileDTO profile = profileService.unfollowUser(username, currentUser.getId());
        return ResponseEntity.ok(ProfileResponse.builder().profile(profile).build());
    }
}
