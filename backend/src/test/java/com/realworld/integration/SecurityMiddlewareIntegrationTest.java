package com.realworld.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realworld.entity.Article;
import com.realworld.entity.User;
import com.realworld.repository.ArticleRepository;
import com.realworld.repository.UserRepository;
import com.realworld.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityMiddlewareIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setImage("https://api.realworld.io/images/smiley-cyrus.jpeg");
        testUser.setFollowedBy(new HashSet<>());
        testUser.setFollowing(new HashSet<>());
        testUser = userRepository.save(testUser);
        validToken = jwtTokenProvider.generateToken(testUser.getId());
    }

    @Nested
    @DisplayName("JWT Authentication Tests")
    class JwtAuthenticationTests {

        @Test
        @DisplayName("Protected route without token returns 401")
        void protectedRouteWithoutToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/user")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("missing authorization credentials"));
        }

        @Test
        @DisplayName("Protected route with invalid token returns 401")
        void protectedRouteWithInvalidToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/user")
                            .header("Authorization", "Token invalid-token-here")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("missing authorization credentials"));
        }

        @Test
        @DisplayName("Protected route with malformed token returns 401")
        void protectedRouteWithMalformedToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/user")
                            .header("Authorization", "Token not.a.valid.jwt")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("missing authorization credentials"));
        }

        @Test
        @DisplayName("Protected route with valid Token prefix returns 200")
        void protectedRouteWithValidTokenPrefix_Returns200() throws Exception {
            mockMvc.perform(get("/api/user")
                            .header("Authorization", "Token " + validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value("test@example.com"))
                    .andExpect(jsonPath("$.user.username").value("testuser"));
        }

        @Test
        @DisplayName("Protected route with valid Bearer prefix returns 200")
        void protectedRouteWithValidBearerPrefix_Returns200() throws Exception {
            mockMvc.perform(get("/api/user")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value("test@example.com"))
                    .andExpect(jsonPath("$.user.username").value("testuser"));
        }
    }

    @Nested
    @DisplayName("Public vs Protected Endpoint Tests")
    class PublicVsProtectedEndpointTests {

        @Test
        @DisplayName("POST /api/users (register) is public")
        void registerEndpoint_IsPublic() throws Exception {
            Map<String, Object> userData = Map.of(
                    "email", "newuser@example.com",
                    "username", "newuser",
                    "password", "password123"
            );
            Map<String, Object> request = Map.of("user", userData);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.email").value("newuser@example.com"));
        }

        @Test
        @DisplayName("POST /api/users/login is public")
        void loginEndpoint_IsPublic() throws Exception {
            Map<String, Object> userData = Map.of(
                    "email", "test@example.com",
                    "password", "password123"
            );
            Map<String, Object> request = Map.of("user", userData);

            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("GET /api/articles is public")
        void getArticlesEndpoint_IsPublic() throws Exception {
            mockMvc.perform(get("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.articles").isArray());
        }

        @Test
        @DisplayName("GET /api/tags is public")
        void getTagsEndpoint_IsPublic() throws Exception {
            mockMvc.perform(get("/api/tags")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tags").isArray());
        }

        @Test
        @DisplayName("GET /api/profiles/:username is public")
        void getProfileEndpoint_IsPublic() throws Exception {
            mockMvc.perform(get("/api/profiles/testuser")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.profile.username").value("testuser"));
        }

        @Test
        @DisplayName("POST /api/articles without token returns 401")
        void createArticleWithoutToken_Returns401() throws Exception {
            Map<String, Object> articleData = Map.of(
                    "title", "Test Article",
                    "description", "Test description",
                    "body", "Test body"
            );
            Map<String, Object> request = Map.of("article", articleData);

            mockMvc.perform(post("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/user without token returns 401")
        void getCurrentUserWithoutToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/user")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Validation error returns 422 with correct format")
        void validationError_Returns422WithCorrectFormat() throws Exception {
            Map<String, Object> userData = Map.of(
                    "email", "test@example.com",
                    "username", "testuser",
                    "password", "password123"
            );
            Map<String, Object> request = Map.of("user", userData);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors").exists())
                    .andExpect(jsonPath("$.errors.email").isArray());
        }

        @Test
        @DisplayName("Invalid login returns 403 with correct format")
        void invalidLogin_Returns403WithCorrectFormat() throws Exception {
            Map<String, Object> userData = Map.of(
                    "email", "test@example.com",
                    "password", "wrongpassword"
            );
            Map<String, Object> request = Map.of("user", userData);

            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors").exists())
                    .andExpect(jsonPath("$.errors['email or password']").isArray());
        }

        @Test
        @DisplayName("Not found article returns 404")
        void notFoundArticle_Returns404() throws Exception {
            mockMvc.perform(get("/api/articles/non-existent-slug")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Not found profile returns 404")
        void notFoundProfile_Returns404() throws Exception {
            mockMvc.perform(get("/api/profiles/nonexistentuser")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Forbidden delete article by non-author returns 403")
        void forbiddenDeleteArticle_Returns403() throws Exception {
            User otherUser = new User();
            otherUser.setEmail("other@example.com");
            otherUser.setUsername("otheruser");
            otherUser.setPassword(passwordEncoder.encode("password123"));
            otherUser.setImage("https://api.realworld.io/images/smiley-cyrus.jpeg");
            otherUser.setFollowedBy(new HashSet<>());
            otherUser.setFollowing(new HashSet<>());
            otherUser = userRepository.save(otherUser);

            Article article = new Article();
            article.setSlug("test-article-" + otherUser.getId());
            article.setTitle("Test Article");
            article.setDescription("Test description");
            article.setBody("Test body");
            article.setAuthor(otherUser);
            article.setTagList(new HashSet<>());
            article.setFavoritedBy(new HashSet<>());
            article.setComments(new HashSet<>());
            article.setCreatedAt(Instant.now());
            article.setUpdatedAt(Instant.now());
            article = articleRepository.save(article);

            mockMvc.perform(delete("/api/articles/" + article.getSlug())
                            .header("Authorization", "Token " + validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errors").exists());
        }
    }

    @Nested
    @DisplayName("CORS Tests")
    class CorsTests {

        @Test
        @DisplayName("OPTIONS preflight request returns CORS headers")
        void optionsPreflight_ReturnsCorsHeaders() throws Exception {
            mockMvc.perform(options("/api/articles")
                            .header("Origin", "http://example.com")
                            .header("Access-Control-Request-Method", "GET")
                            .header("Access-Control-Request-Headers", "Authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"))
                    .andExpect(header().exists("Access-Control-Allow-Methods"));
        }

        @Test
        @DisplayName("GET request with Origin header returns CORS headers")
        void getWithOrigin_ReturnsCorsHeaders() throws Exception {
            mockMvc.perform(get("/api/articles")
                            .header("Origin", "http://example.com")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }
}
