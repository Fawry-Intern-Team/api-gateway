package com.example.api_gateway.Integration;

import com.example.api_gateway.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.main.web-application-type=reactive"
})
class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtService jwtService;

    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        // Create test tokens
        String secretKey = "Z2V0LWFjdHVhbC1rZXktZnJvbS1wcm9wcy1maWxlLW9yLWVudi1zZWN1cmVseQ==";
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .setExpiration(expiration)
                .claim("roles", List.of("USER", "ADMIN"))
                .signWith(key)
                .compact();

        Date pastExpiration = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setExpiration(pastExpiration)
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();
    }

    @Test
    void testLoginEndpoint_WithValidCredentials_ShouldReturnOk() {
        // Given
        String url = "/api/auth/login";
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk() // Should work if auth-service is running and registered with Eureka
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testLoginEndpoint_WithInvalidCredentials_ShouldReturnUnauthorized() {
        // Given
        String url = "/api/auth/login";
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "wrongpassword"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLoginEndpoint_WithShortPassword_ShouldReturnBadRequest() {
        // Given
        String url = "/api/auth/login";
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "123"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRegisterEndpoint_WithValidCredentials_ShouldReturnOk() {
        // Given
        String url = "/api/auth/register";
        String requestBody = """
                {
                    "email": "newuser@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk() // Should work if auth-service is running and registered with Eureka
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void testRegisterEndpoint_WithShortPassword_ShouldReturnBadRequest() {
        // Given
        String url = "/api/auth/register";
        String requestBody = """
                {
                    "email": "newuser@example.com",
                    "password": "123"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testRegisterEndpoint_WithInvalidEmail_ShouldReturnBadRequest() {
        // Given
        String url = "/api/auth/register";
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        // When & Then
        webTestClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testProtectedEndpoint_WithoutToken_ShouldReturnUnauthorized() {
        // Given
        String url = "/api/auth/protected";

        // When & Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedEndpoint_WithValidToken_ShouldReturnOk() {
        // Given
        String url = "/api/auth/protected";
        when(jwtService.extractClaim(eq(validToken), any()))
            .thenReturn("test@example.com")
            .thenReturn(List.of("USER", "ADMIN"));
        when(jwtService.validateToken(validToken)).thenReturn(true);

        // When & Then
        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk(); // Should work if auth-service is running and registered with Eureka
    }

    @Test
    void testProtectedEndpoint_WithExpiredToken_ShouldReturnUnauthorized() {
        // Given
        String url = "/api/auth/protected";
        when(jwtService.extractClaim(eq(expiredToken), any())).thenReturn("test@example.com");
        when(jwtService.validateToken(expiredToken)).thenReturn(false);

        // When & Then
        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedEndpoint_WithInvalidToken_ShouldReturnUnauthorized() {
        // Given
        String url = "/api/auth/protected";
        String invalidToken = "invalid.token.format";

        // When & Then
        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedEndpoint_WithMalformedAuthorizationHeader_ShouldReturnUnauthorized() {
        // Given
        String url = "/api/auth/protected";

        // When & Then
        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedEndpoint_WithEmptyToken_ShouldReturnUnauthorized() {
        // Given
        String url = "api/auth/protected";

        // When & Then
        webTestClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

//    @Test
//    void testHealthCheck_ShouldBeAccessible() {
//        // Given
//        String url = "/actuator/health";
//
//        // When & Then
//        webTestClient.get()
//                .uri(url)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("UP");
//    }

    @Test
    void testNonExistentEndpoint_ShouldReturnNotFound() {
        // Given
        String url = "/api/nonexistent";

        // When & Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
} 