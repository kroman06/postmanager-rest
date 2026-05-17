package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    // Find

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

    @Test
    void findPublished_shouldReturnFilteredByCategory_whenCategoryIdProvided() throws Exception {
        String admin = adminToken();
        String author = authorToken();

        String categoryBody = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Tech", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int categoryId = objectMapper.readTree(categoryBody).get("id").intValue();

        // with category
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Tech Article", "Content", categoryId))))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(body).get("id").asString();
        mockMvc.perform(patch("/articles/" + id + "/publish")
                .header("Authorization", author));

        // no category
        createAndPublishArticle("author@test.com");

        mockMvc.perform(get("/articles?categoryId=" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].categoryId").value(categoryId));
    }

    @Test
    void findPublished_shouldReturnAll_whenNoCategoryFilter() throws Exception {
        createAndPublishArticle("author@test.com");
        createAndPublishArticle("author@test.com");

        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void findPublished_shouldReturnEmpty_whenCategoryHasNoArticles() throws Exception {
        String categoryBody = mockMvc.perform(post("/categories")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Empty", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int categoryId = objectMapper.readTree(categoryBody).get("id").intValue();

        mockMvc.perform(get("/articles?categoryId=" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findAllArticles_shouldReturnAllStatuses_whenAdmin() throws Exception {
        String author = authorToken();

        // DRAFT
        mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Draft", "Content", null))))
                .andExpect(status().isCreated());

        // PUBLISHED
        createAndPublishArticle("author@test.com");

        // ARCHIVED
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(patch("/articles/" + id + "/archive")
                .header("Authorization", author));

        mockMvc.perform(get("/articles/admin/all")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void findAllArticles_shouldFilterByStatus_whenStatusProvided() throws Exception {
        String author = authorToken();
        mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Draft 1", "Content", null))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Draft 2", "Content", null))))
                .andExpect(status().isCreated());

        createAndPublishArticle("author@test.com");
        mockMvc.perform(get("/articles/admin/all?status=DRAFT")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void findAllArticles_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/articles/admin/all")
                        .header("Authorization", authorToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void findAllArticles_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/articles/admin/all")).andExpect(status().isUnauthorized());
    }

    // Create article

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

    // Publish article

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

    // Update article

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
    void updateArticle_shouldReturn200_whenArticleIsPublished() throws Exception {
        String author = authorToken();
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(put("/articles/" + id)
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Updated", "Content", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
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

    @Test
    void updateArticle_shouldRemoveCategory_whenCategoryIdIsNull() throws Exception {
        String admin = adminToken();
        String author = authorToken();

        String catBody = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Tech", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int catId = objectMapper.readTree(catBody).get("id").intValue();

        String artBody = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArticleRequest("Title", "Content", catId))))
                .andReturn().getResponse().getContentAsString();
        String artId = objectMapper.readTree(artBody).get("id").asString();

        mockMvc.perform(put("/articles/" + artId)
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArticleRequest("Title", "Content", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").isEmpty());
    }

    // Archive article (Author)

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

    // Admin delete article

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

    // Admin archives any article

    @Test
    void archiveArticle_shouldReturn200_whenAdmin() throws Exception {
        String id = createAndPublishArticle("author@test.com");

        mockMvc.perform(patch("/articles/" + id + "/archive")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    // Admin restores any article

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

    // Author restores own archived article

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

    // Get published articles

    @Test
    void getPublishedArticles_shouldReturn200_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Get specific article

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

    @Test
    void getArticleById_shouldReturn404_whenArticleIsDraft() throws Exception {
        String author = authorToken();
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArticleRequest("Draft", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(get("/articles/" + id))
                .andExpect(status().isNotFound());
    }

    // Get own articles

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
                        .content(objectMapper.writeValueAsString(new ArticleRequest("Draft Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(get("/articles/my/" + id)
                        .header("Authorization", author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Draft Title"));
    }

    @Test
    void findByAuthor_shouldNotReturnDraftArticles() throws Exception {
        String author = authorToken();
        mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArticleRequest("Draft", "Content", null))))
                .andExpect(status().isCreated());

        String authorId = objectMapper.readTree(
                        mockMvc.perform(get("/auth/me")
                                        .header("Authorization", author))
                                .andReturn().getResponse().getContentAsString())
                .get("id").asString();

        mockMvc.perform(get("/articles/author/" + authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findByAuthor_shouldNotReturnArchivedArticles() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(patch("/articles/" + id + "/archive")
                .header("Authorization", authorToken()));

        String authorId = objectMapper.readTree(mockMvc.perform(get("/auth/me")
                                 .header("Authorization", authorToken()))
                                .andReturn().getResponse().getContentAsString())
                .get("id").asString();

        mockMvc.perform(get("/articles/author/" + authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findByAuthor_shouldReturnEmpty_whenAuthorNotExists() throws Exception {
        mockMvc.perform(get("/articles/author/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findPublished_shouldReturnEmpty_whenCategoryIdNotExists() throws Exception {
        createAndPublishArticle("author@test.com");

        mockMvc.perform(get("/articles?categoryId=99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findAllArticles_shouldFilterByArchivedStatus_whenStatusProvided() throws Exception {
        String id = createAndPublishArticle("author@test.com");
        mockMvc.perform(patch("/articles/" + id + "/archive")
                .header("Authorization", authorToken()));

        createAndPublishArticle("author@test.com"); // published

        mockMvc.perform(get("/articles/admin/all?status=ARCHIVED")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findPublished_shouldNotReturnArticlesFromOtherCategories() throws Exception {
        String admin = adminToken();
        String author = authorToken();

        String cat1Body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Cat1", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int cat1Id = objectMapper.readTree(cat1Body).get("id").intValue();

        String cat2Body = mockMvc.perform(post("/categories")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryRequest("Cat2", "desc"))))
                .andReturn().getResponse().getContentAsString();
        int cat2Id = objectMapper.readTree(cat2Body).get("id").intValue();

        String b1 = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("A1", "C", cat1Id))))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(patch("/articles/" + objectMapper.readTree(b1).get("id").asString() + "/publish")
                .header("Authorization", author));

        String b2 = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("A2", "C", cat2Id))))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(patch("/articles/" + objectMapper.readTree(b2).get("id").asString() + "/publish")
                .header("Authorization", author));

        mockMvc.perform(get("/articles?categoryId=" + cat1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].categoryId").value(cat1Id));
    }
}