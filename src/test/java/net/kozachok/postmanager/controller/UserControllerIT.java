package net.kozachok.postmanager.controller;

import net.kozachok.postmanager.BaseIntegrationTest;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.request.RoleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends BaseIntegrationTest {
    // find

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

    // add

    @Test
    void addRole_shouldReturn200_whenAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(patch("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_AUTHOR")));
    }

    @Test
    void addRole_shouldReturn403_whenNotAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(patch("/admin/users/" + id + "/role")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void addRole_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(patch("/admin/users/" + UUID.randomUUID() + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isNotFound());
    }

    // remove

    @Test
    void removeRole_shouldReturn200_whenAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(patch("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_READER")))
                .andExpect(jsonPath("$.roles.length()").value(1));
    }

    @Test
    void removeRole_shouldReturn400_whenUserDoesNotHaveRole() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(delete("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_AUTHOR)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeRole_shouldReturn400_whenRemovingLastRole() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(delete("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_READER)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeRole_shouldReturn403_whenNotAdmin() throws Exception {
        String readerBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", readerToken()))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(readerBody).get("id").asString();

        mockMvc.perform(delete("/admin/users/" + id + "/role")
                        .header("Authorization", readerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_READER)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeRole_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(delete("/admin/users/" + UUID.randomUUID() + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest(Set.of(RoleName.ROLE_READER)))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeRole_shouldReturn400_whenRemovingLastAdmin() throws Exception {
        String adminBody = mockMvc.perform(get("/auth/me")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(adminBody).get("id").asString();

        mockMvc.perform(delete("/admin/users/" + id + "/role")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RoleRequest(Set.of(RoleName.ROLE_ADMIN)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot remove the last admin in the system"));
    }
}