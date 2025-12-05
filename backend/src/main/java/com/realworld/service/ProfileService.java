package com.realworld.service;

import com.realworld.dto.ProfileDTO;
import com.realworld.entity.User;
import com.realworld.exception.AppException;
import com.realworld.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileDTO getProfile(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "profile", "not found"));

        boolean following = false;
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                following = user.getFollowedBy().stream()
                        .anyMatch(follower -> follower.getId().equals(currentUserId));
            }
        }

        return ProfileDTO.builder()
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .following(following)
                .build();
    }

    @Transactional
    public ProfileDTO followUser(String username, Long currentUserId) {
        User userToFollow = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "profile", "not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        currentUser.getFollowing().add(userToFollow);
        userRepository.save(currentUser);

        return ProfileDTO.builder()
                .username(userToFollow.getUsername())
                .bio(userToFollow.getBio())
                .image(userToFollow.getImage())
                .following(true)
                .build();
    }

    @Transactional
    public ProfileDTO unfollowUser(String username, Long currentUserId) {
        User userToUnfollow = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "profile", "not found"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        currentUser.getFollowing().remove(userToUnfollow);
        userRepository.save(currentUser);

        return ProfileDTO.builder()
                .username(userToUnfollow.getUsername())
                .bio(userToUnfollow.getBio())
                .image(userToUnfollow.getImage())
                .following(false)
                .build();
    }

    public ProfileDTO mapToProfileDTO(User user, Long currentUserId) {
        boolean following = false;
        if (currentUserId != null && user.getFollowedBy() != null) {
            following = user.getFollowedBy().stream()
                    .anyMatch(follower -> follower.getId().equals(currentUserId));
        }

        return ProfileDTO.builder()
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .following(following)
                .build();
    }
}
