package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.request.ChangeRoleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends BaseIntegrationTest {

    @Test
    void findAll_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findAll_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", readerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRole_shouldReturn200_whenAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(patch("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangeRoleRequest(RoleName.ROLE_AUTHOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_AUTHOR"));
    }

    @Test
    void changeRole_shouldReturn403_whenNotAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(patch("/admin/users/" + id + "/role")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangeRoleRequest(RoleName.ROLE_AUTHOR))))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRole_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(patch("/admin/users/" + UUID.randomUUID() + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangeRoleRequest(RoleName.ROLE_AUTHOR))))
                .andExpect(status().isNotFound());
    }
}