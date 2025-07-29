//package com.example.api_gateway.Integration;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource(properties = {
//    "spring.main.web-application-type=servlet"
//})
//class ApiGatewayIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private JwtService jwtService;
//
//    private String validToken;
//    private String expiredToken;
//
//    @BeforeEach
//    void setUp() {
//        // Create test tokens
//        String secretKey = "Z2V0LWFjdHVhbC1rZXktZnJvbS1wcm9wcy1maWxlLW9yLWVudi1zZWN1cmVseQ==";
//        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
//
//        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
//        validToken = Jwts.builder()
//                .setSubject("test@example.com")
//                .setExpiration(expiration)
//                .claim("roles", List.of("USER", "ADMIN"))
//                .signWith(key)
//                .compact();
//
//        Date pastExpiration = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
//        expiredToken = Jwts.builder()
//                .setSubject("test@example.com")
//                .setExpiration(pastExpiration)
//                .claim("roles", List.of("USER"))
//                .signWith(key)
//                .compact();
//    }
//
//    @Test
//    void testLoginEndpoint_WithValidCredentials_ShouldReturnOk() throws Exception {
//        String url = "/api/auth/login";
//        String requestBody = """
//                {
//                    \"email\": \"test@example.com\",
//                    \"password\": \"password123\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    void testLoginEndpoint_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/login";
//        String requestBody = """
//                {
//                    \"email\": \"test@example.com\",
//                    \"password\": \"wrongpassword\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testLoginEndpoint_WithShortPassword_ShouldReturnBadRequest() throws Exception {
//        String url = "/api/auth/login";
//        String requestBody = """
//                {
//                    \"email\": \"test@example.com\",
//                    \"password\": \"123\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    void testRegisterEndpoint_WithValidCredentials_ShouldReturnOk() throws Exception {
//        String url = "/api/auth/register";
//        String requestBody = """
//                {
//                    \"email\": \"newuser@example.com\",
//                    \"password\": \"password123\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    void testRegisterEndpoint_WithShortPassword_ShouldReturnBadRequest() throws Exception {
//        String url = "/api/auth/register";
//        String requestBody = """
//                {
//                    \"email\": \"newuser@example.com\",
//                    \"password\": \"123\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    void testRegisterEndpoint_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
//        String url = "/api/auth/register";
//        String requestBody = """
//                {
//                    \"email\": \"invalid-email\",
//                    \"password\": \"password123\"
//                }
//                """;
//        mockMvc.perform(MockMvcRequestBuilders.post(url)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithoutToken_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/protected";
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithValidToken_ShouldReturnOk() throws Exception {
//        String url = "/api/auth/protected";
//        when(jwtService.extractClaim(eq(validToken), any()))
//            .thenReturn("test@example.com")
//            .thenReturn(List.of("USER", "ADMIN"));
//        when(jwtService.validateToken(validToken)).thenReturn(true);
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithExpiredToken_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/protected";
//        when(jwtService.extractClaim(eq(expiredToken), any())).thenReturn("test@example.com");
//        when(jwtService.validateToken(expiredToken)).thenReturn(false);
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/protected";
//        String invalidToken = "invalid.token.format";
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithMalformedAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/protected";
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testProtectedEndpoint_WithEmptyToken_ShouldReturnUnauthorized() throws Exception {
//        String url = "/api/auth/protected";
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
//    }
//
//    @Test
//    void testNonExistentEndpoint_ShouldReturnNotFound() throws Exception {
//        String url = "/api/nonexistent";
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound());
//    }
//}