package com.realworld.service;

import com.realworld.dto.LoginRequest;
import com.realworld.dto.RegisterRequest;
import com.realworld.dto.UserDTO;
import com.realworld.entity.User;
import com.realworld.exception.AppException;
import com.realworld.repository.UserRepository;
import com.realworld.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("hashedPassword");
        testUser.setBio("Test bio");
        testUser.setImage("https://api.realworld.io/images/smiley-cyrus.jpeg");
        testUser.setFollowedBy(new HashSet<>());
        testUser.setFollowing(new HashSet<>());
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyLong())).thenReturn("test-token");

        RegisterRequest.UserData userData = new RegisterRequest.UserData();
        userData.setEmail("test@example.com");
        userData.setUsername("testuser");
        userData.setPassword("password123");

        UserDTO result = authService.createUser(userData);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        assertEquals("test-token", result.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        RegisterRequest.UserData userData = new RegisterRequest.UserData();
        userData.setEmail("test@example.com");
        userData.setUsername("testuser");
        userData.setPassword("password123");

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertTrue(exception.getErrors().containsKey("email"));
    }

    @Test
    void createUser_UsernameAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RegisterRequest.UserData userData = new RegisterRequest.UserData();
        userData.setEmail("test@example.com");
        userData.setUsername("testuser");
        userData.setPassword("password123");

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertTrue(exception.getErrors().containsKey("username"));
    }

    @Test
    void createUser_BlankEmail() {
        RegisterRequest.UserData userData = new RegisterRequest.UserData();
        userData.setEmail("");
        userData.setUsername("testuser");
        userData.setPassword("password123");

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        assertTrue(exception.getErrors().containsKey("email"));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L)).thenReturn("test-token");

        LoginRequest.UserData userData = new LoginRequest.UserData();
        userData.setEmail("test@example.com");
        userData.setPassword("password123");

        UserDTO result = authService.login(userData);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("test-token", result.getToken());
    }

    @Test
    void login_InvalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        LoginRequest.UserData userData = new LoginRequest.UserData();
        userData.setEmail("test@example.com");
        userData.setPassword("wrongpassword");

        AppException exception = assertThrows(AppException.class, () ->
                authService.login(userData));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest.UserData userData = new LoginRequest.UserData();
        userData.setEmail("nonexistent@example.com");
        userData.setPassword("password123");

        AppException exception = assertThrows(AppException.class, () ->
                authService.login(userData));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(1L)).thenReturn("test-token");

        UserDTO result = authService.getCurrentUser(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getCurrentUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                authService.getCurrentUser(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
