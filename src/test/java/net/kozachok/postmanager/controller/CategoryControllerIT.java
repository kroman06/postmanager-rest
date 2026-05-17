package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerIT extends BaseIntegrationTest {
    // Create category
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

    // Get categories

    @Test
    void getCategories_shouldReturn200_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // Update category

    @Test
    void updateCategory_shouldReturn200_whenAdmin() throws Exception {
        String admin = adminToken();
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Original", "desc"))))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).get("id").intValue();
        mockMvc.perform(put("/categories/" + id)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Updated", "new desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateCategory_shouldReturn403_whenNotAdmin() throws Exception {
        String admin = adminToken();
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Tech", "desc"))))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).get("id").intValue();
        mockMvc.perform(put("/categories/" + id)
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Hacked", "desc"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCategory_shouldReturn409_whenNameAlreadyExists() throws Exception {
        String admin = adminToken();
        mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("First", "desc"))))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Second", "desc"))))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).get("id").intValue();
        mockMvc.perform(put("/categories/" + id)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("First", "desc"))))
                .andExpect(status().isConflict());
    }


    @Test
    void updateCategory_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Name", "desc"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCategory_shouldReturn404_whenCategoryNotFound() throws Exception {
        mockMvc.perform(put("/categories/99999")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Name", "desc"))))
                .andExpect(status().isNotFound());
    }

    // Delete category

    @Test
    void deleteCategory_shouldSetArticleCategoryToNull_whenCategoryDeleted() throws Exception {
        String admin = adminToken();
        String author = authorToken();

        String catBody = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("ToDelete", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int catId = objectMapper.readTree(catBody).get("id").intValue();

        String artBody = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", catId))))
                .andReturn().getResponse().getContentAsString();
        String artId = objectMapper.readTree(artBody).get("id").asString();
        mockMvc.perform(patch("/articles/" + artId + "/publish")
                .header("Authorization", author));

        mockMvc.perform(delete("/categories/" + catId)
                        .header("Authorization", admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/articles/" + artId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").isEmpty())
                .andExpect(jsonPath("$.categoryName").isEmpty());
    }

    @Test
    void deleteCategory_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCategory_shouldReturn204_whenAdmin() throws Exception {
        String admin = adminToken();
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("ToDelete", "desc"))))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).get("id").intValue();
        mockMvc.perform(delete("/categories/" + id)
                        .header("Authorization", admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_shouldReturn403_whenNotAdmin() throws Exception {
        String admin = adminToken();
        String body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("ToDelete", "desc"))))
                .andReturn().getResponse().getContentAsString();

        int id = objectMapper.readTree(body).get("id").intValue();
        mockMvc.perform(delete("/categories/" + id)
                        .header("Authorization", readerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/categories/99999")
                        .header("Authorization", adminToken()))
                .andExpect(status().isNotFound());
    }
}