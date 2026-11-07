package com.delivery_tracker_app.app.integration.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the real SecurityConfig (not the permissive TestSecurityConfig) end to end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-that-is-long-enough",
        "app.user.username=testuser",
        "app.user.password=testpass",
        "app.user.roles=ADMIN"
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginBody(String password) {
        return "{\"username\":\"testuser\",\"password\":\"" + password + "\"}";
    }

    @Test
    @DisplayName("unauthenticated GET /api/v1/clients must return 401")
    void getClients_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("unauthenticated GET /api/v1/clients/{id} must return 401")
    void getClientById_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("unauthenticated GET /api/v1/drivers must return 401")
    void getDrivers_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("login with valid credentials returns a JWT that grants access to clients")
    void login_ThenAccessClients() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("testpass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
        String token = body.path("data").path("token").asText();

        mockMvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("login with a wrong password returns 401")
    void login_WithWrongPassword_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("invalid or malformed bearer token returns 401")
    void getClients_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/clients").header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("expired bearer token returns 401")
    void getClients_WithExpiredToken_Returns401() throws Exception {
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                "test-secret-key-that-is-long-enough".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String expired = io.jsonwebtoken.Jwts.builder()
                .subject("testuser")
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 120_000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OpenAPI documents used by Swagger UI are public")
    void openApiDocs_WithoutToken_Return200() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths").isNotEmpty());
    }
}
