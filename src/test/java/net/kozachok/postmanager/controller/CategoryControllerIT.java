package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerIT extends BaseIntegrationTest {
    // US-04 Create category
    @Test
    void createCategory_shouldReturn201_whenAdmin() throws Exception {
        mockMvc.perform(post("/categories")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Technology", "Tech articles"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Technology"));
    }

    @Test
    void createCategory_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/categories")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Technology", "Tech articles"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Technology", "desc"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_shouldReturn409_whenNameAlreadyExists() throws Exception {
        String admin = adminToken();
        mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Technology", "desc"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Technology", "another desc"))))
                .andExpect(status().isConflict());
    }

    // US-05 Get categories
    @Test
    void getCategories_shouldReturn200_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}