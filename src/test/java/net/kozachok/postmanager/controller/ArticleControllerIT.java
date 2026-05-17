package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings("SameParameterValue")
class ArticleControllerIT extends BaseIntegrationTest {
    private String createAndPublishArticle(String authorEmail) throws Exception {
        String author = getToken(authorEmail);
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();
        mockMvc.perform(patch("/articles/" + id + "/publish")
                        .header("Authorization", author))
                .andExpect(status().isOk());

        return id;
    }

    // Pagination

    @Test
    void findPublished_shouldReturnPaginatedResult() throws Exception {
        String authorToken = authorToken();
        for (int i = 0; i < 12; i++) {
            String body = mockMvc.perform(post("/articles")
                            .header("Authorization", authorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new ArticleRequest("Title " + i, "Content " + i, null))))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String id = objectMapper.readTree(body).get("id").asString();
            mockMvc.perform(patch("/articles/" + id + "/publish")
                            .header("Authorization", authorToken))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/articles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    // findMyById — not owner

    @Test
    void findMyById_shouldReturn403_whenNotOwner() throws Exception {
        createUser("other@test.com", RoleName.ROLE_AUTHOR);
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", authorToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(get("/articles/my/" + id)
                        .header("Authorization", getToken("other@test.com")))
                .andExpect(status().isForbidden());
    }

    // US-06 Create article

    @Test
    void createArticle_shouldReturn201WithDraft_whenAuthor() throws Exception {
        mockMvc.perform(post("/articles")
                        .header("Authorization", authorToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("My Article", "Content here", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value("My Article"));
    }

    @Test
    void createArticle_shouldReturn403_whenReader() throws Exception {
        mockMvc.perform(post("/articles")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andExpect(status().isForbidden());
    }

    // US-07 Publish article

    @Test
    void publishArticle_shouldReturn200WithPublished_whenOwner() throws Exception {
        String author = authorToken();
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();
        mockMvc.perform(patch("/articles/" + id + "/publish")
                        .header("Authorization", author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());
    }

    @Test
    void publishArticle_shouldReturn400_whenAlreadyPublished() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(patch("/articles/" + id + "/publish")
                        .header("Authorization", authorToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishArticle_shouldReturn403_whenNotOwner() throws Exception {
        createUser("other@test.com", RoleName.ROLE_AUTHOR);
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", getToken("author@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();
        mockMvc.perform(patch("/articles/" + id + "/publish")
                        .header("Authorization", getToken("other@test.com")))
                .andExpect(status().isForbidden());
    }

    // US-08 Update article

    @Test
    void updateArticle_shouldReturn200_whenOwner() throws Exception {
        String author = authorToken();
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Original", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(put("/articles/" + id)
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Updated Title", "Updated Content", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateArticle_shouldReturn403_whenNotOwner() throws Exception {
        createUser("other@test.com", RoleName.ROLE_AUTHOR);
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", authorToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(put("/articles/" + id)
                        .header("Authorization", getToken("other@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Hacked", "Content", null))))
                .andExpect(status().isForbidden());
    }

    // US-09 Archive article (Author)

    @Test
    void archiveArticle_shouldReturn200_whenOwnerAndPublished() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void archiveArticle_shouldReturn400_whenDraft() throws Exception {
        String author = authorToken();
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();
        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", author))
                .andExpect(status().isBadRequest());
    }

    // US-12 Admin delete article

    @Test
    void deleteArticle_shouldReturn204_whenAdmin() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(delete("/articles/" + id)
                        .header("Authorization", adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteArticle_shouldReturn400_whenAuthorDeletesPublished() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(delete("/articles/" + id)
                        .header("Authorization", authorToken()))
                .andExpect(status().isBadRequest());
    }

    // US-13 Admin archives any article

    @Test
    void archiveArticle_shouldReturn200_whenAdmin() throws Exception {
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    // US-14 Admin restores any article

    @Test
    void restoreArticle_shouldReturn200_whenAdmin() throws Exception {
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/articles/" + id + "/restore")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    // US-15 Author restores own archived article

    @Test
    void restoreArticle_shouldReturn200_whenOwner() throws Exception {
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/articles/" + id + "/restore")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void restoreArticle_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        createUser("other@test.com", RoleName.ROLE_AUTHOR);
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/articles/" + id + "/restore")
                        .header("Authorization", getToken("other@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void restoreArticle_shouldReturn400_whenNotArchived() throws Exception {
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/restore")
                        .header("Authorization", authorToken()))
                .andExpect(status().isBadRequest());
    }

    // US-16 Get published articles

    @Test
    void getPublishedArticles_shouldReturn200_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // US-17 Get specific article

    @Test
    void getArticleById_shouldReturn200_whenPublished() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(get("/articles/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getArticleById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/articles/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // US-11 Get own articles

    @Test
    void getMyArticles_shouldReturn200_whenAuthor() throws Exception {
        mockMvc.perform(get("/articles/my")
                        .header("Authorization", authorToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMyArticles_shouldReturn403_whenReader() throws Exception {
        mockMvc.perform(get("/articles/my")
                        .header("Authorization", readerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void findMyById_shouldReturn404_whenArticleNotExists() throws Exception {
        mockMvc.perform(get("/articles/my/" + UUID.randomUUID())
                        .header("Authorization", authorToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void findMyById_shouldReturn200_whenOwner() throws Exception {
        String author = authorToken();
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Draft Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(get("/articles/my/" + id)
                        .header("Authorization", author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Draft Title"));
    }
}