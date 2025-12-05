package com.realworld.controller;

import com.realworld.dto.*;
import com.realworld.entity.User;
import com.realworld.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserDTO user = authService.createUser(request.getUser());
        return new ResponseEntity<>(UserResponse.builder().user(user).build(), HttpStatus.CREATED);
    }

    @PostMapping("/users/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        UserDTO user = authService.login(request.getUser());
        return ResponseEntity.ok(UserResponse.builder().user(user).build());
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserDTO user = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(UserResponse.builder().user(user).build());
    }

    @PutMapping("/user")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        UserDTO user = authService.updateUser(request.getUser(), currentUser.getId());
        return ResponseEntity.ok(UserResponse.builder().user(user).build());
    }
}
