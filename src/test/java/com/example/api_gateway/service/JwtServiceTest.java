package com.example.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String validSecretKey;
    private String validToken;
    private String expiredToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // Base64 encoded secret key for testing
        validSecretKey = "Z2V0LWFjdHVhbC1rZXktZnJvbS1wcm9wcy1maWxlLW9yLWVudi1zZWN1cmVseQ==";
        ReflectionTestUtils.setField(jwtService, "secretKey", validSecretKey);

        // Create a valid token
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(validSecretKey));
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .setExpiration(expiration)
                .claim("roles", List.of("USER", "ADMIN"))
                .signWith(key)
                .compact();

        // Create an expired token
        Date pastExpiration = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setExpiration(pastExpiration)
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();

        // Invalid token (malformed)
        invalidToken = "invalid.token.format";
    }

    @Test
    void testExtractClaim_WithValidToken_ShouldReturnSubject() {
        // Given
        Function<Claims, String> claimResolver = Claims::getSubject;

        // When
        String result = jwtService.extractClaim(validToken, claimResolver);

        // Then
        assertEquals("test@example.com", result);
    }

    @Test
    void testExtractClaim_WithValidToken_ShouldReturnRoles() {
        // Given
        Function<Claims, List<String>> claimResolver = claims -> claims.get("roles", List.class);

        // When
        List<String> result = jwtService.extractClaim(validToken, claimResolver);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("USER"));
        assertTrue(result.contains("ADMIN"));
    }

    @Test
    void testExtractClaim_WithInvalidToken_ShouldThrowException() {
        // Given
        Function<Claims, String> claimResolver = Claims::getSubject;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractClaim(invalidToken, claimResolver);
        });
    }

    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        // When
        boolean result = jwtService.validateToken(validToken);

        // Then
        assertTrue(result);
    }

    @Test
    void testValidateToken_WithExpiredToken_ShouldReturnFalse() {
        // When
        boolean result = jwtService.validateToken(expiredToken);

        // Then
        assertFalse(result);
    }

    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // When
        boolean result = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(result);
    }

    @Test
    void testValidateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean result = jwtService.validateToken(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testValidateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean result = jwtService.validateToken("");

        // Then
        assertFalse(result);
    }

    @Test
    void testExtractClaim_WithNullToken_ShouldThrowException() {
        // Given
        Function<Claims, String> claimResolver = Claims::getSubject;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractClaim(null, claimResolver);
        });
    }

    @Test
    void testExtractClaim_WithEmptyToken_ShouldThrowException() {
        // Given
        Function<Claims, String> claimResolver = Claims::getSubject;

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.extractClaim("", claimResolver);
        });
    }
} 