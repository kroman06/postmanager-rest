package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.dto.request.RefreshTokenRequest;
import net.kozachok.postmanager.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends BaseIntegrationTest {
    // Checking current user
    @Test
    void getCurrentUser_shouldReturnUserDetails_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("reader@test.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_READER"));
    }

    @Test
    void getCurrentUser_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // US-01 Registration
    @Test
    void register_shouldReturn201_whenValidData() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("new@test.com", "Password1!", "John", "Doe"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_READER"));
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        createUser("existing@test.com", RoleName.ROLE_READER);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("existing@test.com", "Password1!", "Jane", "Doe"))))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shouldReturn400_whenInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("not-an-email", "Password1!", "John", "Doe"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("valid@test.com", "123", "John", "Doe"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // US-02 Login

    @Test
    void login_shouldReturnTokens_whenValidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("reader@test.com", TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_shouldReturn401_whenWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("reader@test.com", "wrongPassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401_whenUserNotFound() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ghost@test.com", TEST_PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    // US-03 Refresh Token
    @Test
    void refresh_shouldReturnNewTokens_whenValidToken() throws Exception {
        String loginBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("reader@test.com", TEST_PASSWORD))))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginBody).get("refreshToken").asString();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_shouldReturn401_whenInvalidToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest("invalid.jwt.token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldRevokeToken_whenValidToken() throws Exception {
        String loginBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("reader@test.com", TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginBody).get("refreshToken").asString();

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }
}