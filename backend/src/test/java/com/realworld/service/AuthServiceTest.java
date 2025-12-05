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
import org.springframework.security.crypto.password.PasswordEncoder;

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
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("hashedPassword")
                .bio("Test bio")
                .image("https://api.realworld.io/images/smiley-cyrus.jpeg")
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyLong())).thenReturn("test-token");

        RegisterRequest.UserData userData = RegisterRequest.UserData.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

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

        RegisterRequest.UserData userData = RegisterRequest.UserData.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(422, exception.getStatus().value());
        assertTrue(exception.getErrors().containsKey("email"));
    }

    @Test
    void createUser_UsernameAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RegisterRequest.UserData userData = RegisterRequest.UserData.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(422, exception.getStatus().value());
        assertTrue(exception.getErrors().containsKey("username"));
    }

    @Test
    void createUser_BlankEmail() {
        RegisterRequest.UserData userData = RegisterRequest.UserData.builder()
                .email("")
                .username("testuser")
                .password("password123")
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                authService.createUser(userData));

        assertEquals(422, exception.getStatus().value());
        assertTrue(exception.getErrors().containsKey("email"));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L)).thenReturn("test-token");

        LoginRequest.UserData userData = LoginRequest.UserData.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UserDTO result = authService.login(userData);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("test-token", result.getToken());
    }

    @Test
    void login_InvalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        LoginRequest.UserData userData = LoginRequest.UserData.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                authService.login(userData));

        assertEquals(403, exception.getStatus().value());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest.UserData userData = LoginRequest.UserData.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        AppException exception = assertThrows(AppException.class, () ->
                authService.login(userData));

        assertEquals(403, exception.getStatus().value());
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

        assertEquals(404, exception.getStatus().value());
    }
}
