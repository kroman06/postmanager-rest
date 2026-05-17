package net.kozachok.postmanager;

import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseInitializer.class)
public abstract class BaseIntegrationTest {
    @Autowired protected MockMvc        mockMvc;
    @Autowired protected ObjectMapper   objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected RoleRepository roleRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JdbcTemplate   jdbcTemplate;

    protected static final String TEST_PASSWORD = "Password1!";

    @BeforeEach
    void setUpBaseData() {
        jdbcTemplate.execute("TRUNCATE TABLE users, refresh_tokens, comments, articles, categories CASCADE");

        createUser("admin@test.com", RoleName.ROLE_ADMIN);
        createUser("author@test.com", RoleName.ROLE_AUTHOR);
        createUser("reader@test.com", RoleName.ROLE_READER);
    }

    @SuppressWarnings("UnusedReturnValue")
    protected User createUser(String email, RoleName role) {
        Role r = roleRepository.findByName(role).orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(Set.of(r));
        return userRepository.save(user);
    }

    protected String getToken(String email) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(email, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("accessToken").asString();
    }

    protected String adminToken() throws Exception {
        return getToken("admin@test.com");
    }

    protected String authorToken() throws Exception {
        return getToken("author@test.com");
    }

    protected String readerToken() throws Exception {
        return getToken("reader@test.com");
    }
}