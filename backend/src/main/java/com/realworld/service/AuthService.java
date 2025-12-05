package com.realworld.service;

import com.realworld.dto.*;
import com.realworld.entity.User;
import com.realworld.exception.AppException;
import com.realworld.repository.UserRepository;
import com.realworld.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserDTO createUser(RegisterRequest.UserData userData) {
        String email = userData.getEmail() != null ? userData.getEmail().trim() : null;
        String username = userData.getUsername() != null ? userData.getUsername().trim() : null;
        String password = userData.getPassword() != null ? userData.getPassword().trim() : null;

        Map<String, List<String>> errors = new HashMap<>();

        if (email == null || email.isEmpty()) {
            errors.put("email", List.of("can't be blank"));
        }

        if (username == null || username.isEmpty()) {
            errors.put("username", List.of("can't be blank"));
        }

        if (password == null || password.isEmpty()) {
            errors.put("password", List.of("can't be blank"));
        }

        if (!errors.isEmpty()) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, errors);
        }

        checkUserUniqueness(email, username);

        User user = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(password))
                .image(userData.getImage() != null ? userData.getImage() : "https://api.realworld.io/images/smiley-cyrus.jpeg")
                .bio(userData.getBio())
                .demo(false)
                .build();

        user = userRepository.save(user);

        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .token(tokenProvider.generateToken(user.getId()))
                .build();
    }

    public UserDTO login(LoginRequest.UserData userData) {
        String email = userData.getEmail() != null ? userData.getEmail().trim() : null;
        String password = userData.getPassword() != null ? userData.getPassword().trim() : null;

        Map<String, List<String>> errors = new HashMap<>();

        if (email == null || email.isEmpty()) {
            errors.put("email", List.of("can't be blank"));
        }

        if (password == null || password.isEmpty()) {
            errors.put("password", List.of("can't be blank"));
        }

        if (!errors.isEmpty()) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, errors);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, 
                        Map.of("email or password", List.of("is invalid"))));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException(HttpStatus.FORBIDDEN, 
                    Map.of("email or password", List.of("is invalid")));
        }

        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .token(tokenProvider.generateToken(user.getId()))
                .build();
    }

    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .token(tokenProvider.generateToken(user.getId()))
                .build();
    }

    @Transactional
    public UserDTO updateUser(UpdateUserRequest.UserData userData, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "user", "not found"));

        if (userData.getEmail() != null && !userData.getEmail().isEmpty()) {
            user.setEmail(userData.getEmail());
        }

        if (userData.getUsername() != null && !userData.getUsername().isEmpty()) {
            user.setUsername(userData.getUsername());
        }

        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userData.getPassword()));
        }

        if (userData.getImage() != null) {
            user.setImage(userData.getImage());
        }

        if (userData.getBio() != null) {
            user.setBio(userData.getBio());
        }

        user = userRepository.save(user);

        return UserDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .image(user.getImage())
                .token(tokenProvider.generateToken(user.getId()))
                .build();
    }

    private void checkUserUniqueness(String email, String username) {
        Map<String, List<String>> errors = new HashMap<>();

        if (userRepository.existsByEmail(email)) {
            errors.put("email", List.of("has already been taken"));
        }

        if (userRepository.existsByUsername(username)) {
            errors.put("username", List.of("has already been taken"));
        }

        if (!errors.isEmpty()) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, errors);
        }
    }
}
