package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.request.CommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerIT extends BaseIntegrationTest {

    private String createPublishedArticle() throws Exception {
        String author = getToken("author@test.com");
        String body = mockMvc.perform(post("/articles")
                        .header("Authorization", author)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ArticleRequest("Title", "Content", null))))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asString();

        mockMvc.perform(patch("/articles/" + id + "/publish")
                .header("Authorization", author));

        return id;
    }

    // US-18 Add comment

    @Test
    void addComment_shouldReturn201_whenArticlePublished() throws Exception {
        String articleId = createPublishedArticle();
        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Great article!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great article!"));
    }

    @Test
    void addComment_shouldReturn404_whenArticleNotFound() throws Exception {
        mockMvc.perform(post("/articles/" + UUID.randomUUID() + "/comments")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Comment"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_shouldReturn401_whenUnauthenticated() throws Exception {
        String articleId = createPublishedArticle();
        mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Comment"))))
                .andExpect(status().isUnauthorized());
    }

    // US-19 Edit own comment

    @Test
    void editComment_shouldReturn200_whenOwner() throws Exception {
        String articleId = createPublishedArticle();
        String reader = readerToken();
        String commentBody = mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .header("Authorization", reader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Original"))))
                .andReturn().getResponse().getContentAsString();

        String commentId = objectMapper.readTree(commentBody).get("id").asString();
        mockMvc.perform(patch("/comments/" + commentId)
                        .header("Authorization", reader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void editComment_shouldReturn403_whenNotOwner() throws Exception {
        String articleId = createPublishedArticle();
        String commentBody = mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Original"))))
                .andReturn().getResponse().getContentAsString();

        String commentId = objectMapper.readTree(commentBody).get("id").asString();
        createUser("other@test.com", RoleName.ROLE_READER);
        mockMvc.perform(patch("/comments/" + commentId)
                        .header("Authorization", getToken("other@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Hacked"))))
                .andExpect(status().isForbidden());
    }

    // US-20 Admin delete comment

    @Test
    void deleteComment_shouldReturn204_whenAdmin() throws Exception {
        String articleId = createPublishedArticle();
        String commentBody = mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("To be deleted"))))
                .andReturn().getResponse().getContentAsString();

        String commentId = objectMapper.readTree(commentBody).get("id").asString();
        mockMvc.perform(delete("/comments/" + commentId)
                        .header("Authorization", adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_shouldReturn403_whenNotAdmin() throws Exception {
        String articleId = createPublishedArticle();
        String commentBody = mockMvc.perform(post("/articles/" + articleId + "/comments")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentRequest("Comment"))))
                .andReturn().getResponse().getContentAsString();

        String commentId = objectMapper.readTree(commentBody).get("id").asString();
        createUser("other@test.com", RoleName.ROLE_READER);
        mockMvc.perform(delete("/comments/" + commentId)
                        .header("Authorization", getToken("other@test.com")))
                .andExpect(status().isForbidden());
    }
}