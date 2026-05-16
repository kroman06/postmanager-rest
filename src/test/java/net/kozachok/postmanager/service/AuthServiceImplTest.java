package net.kozachok.postmanager.service;

import net.kozachok.postmanager.config.JwtProperties;
import net.kozachok.postmanager.domain.RefreshToken;
import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.dto.request.RegisterRequest;
import net.kozachok.postmanager.dto.response.TokenResponse;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.exception.ArticleApiException;
import net.kozachok.postmanager.exception.EmailAlreadyExistsException;
import net.kozachok.postmanager.exception.InvalidRefreshTokenException;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RefreshTokenRepository;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.security.JwtService;
import net.kozachok.postmanager.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UserMapper userMapper;
    @Mock private RoleRepository roleRepository;
    @Mock private JwtProperties jwtProperties;
    @InjectMocks private AuthServiceImpl authService;

    // ── register ─────────────────────────────────────────────

    @Test
    void register_shouldSaveUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("user@test.com", "password123", "John", "Doe");

        Role readerRole = new Role();
        readerRole.setName(RoleName.ROLE_READER);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_READER)).thenReturn(Optional.of(readerRole));
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenReturn(mock(UserResponse.class));

        authService.register(request);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("user@test.com") &&
                        u.getPasswordHash().equals("hashed_password")
        ));
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("taken@test.com", "password", "John", "Doe");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // ── login ────────────────────────────────────────────────

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("user@test.com", "password");

        User user = new User();
        user.setEmail("user@test.com");
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh_token");
        when(jwtService.hashToken("refresh_token")).thenReturn("hashed_token");
        when(jwtProperties.getRefreshExpiration()).thenReturn(Duration.ofDays(7));

        TokenResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@test.com", "pass")))
                .isInstanceOf(ArticleApiException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordWrong() {
        User user = new User();
        user.setPasswordHash("hashed");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrong")))
                .isInstanceOf(ArticleApiException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── refresh ──────────────────────────────────────────────

    @Test
    void refresh_shouldReturnNewTokens_andRevokeOld_whenTokenValid() {
        String rawToken = "valid.refresh.token";
        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusDays(7));
        stored.setUser(user);

        when(jwtService.validateToken(rawToken)).thenReturn(true);
        when(jwtService.hashToken(rawToken)).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken(user)).thenReturn("new_access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new_refresh");
        when(jwtService.hashToken("new_refresh")).thenReturn("new_hash");
        when(jwtProperties.getRefreshExpiration()).thenReturn(Duration.ofDays(7));

        TokenResponse response = authService.refresh(rawToken);

        assertThat(stored.getRevoked()).isTrue();
        assertThat(response.accessToken()).isEqualTo("new_access");
        assertThat(response.refreshToken()).isEqualTo("new_refresh");
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    void refresh_shouldThrowInvalidToken_whenSignatureInvalid() {
        when(jwtService.validateToken(any())).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("bad.token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refresh_shouldThrowInvalidToken_whenNotFoundInDb() {
        when(jwtService.validateToken(any())).thenReturn(true);
        when(jwtService.hashToken(any())).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown.token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refresh_shouldThrowInvalidToken_whenRevoked() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(true);
        stored.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(jwtService.validateToken(any())).thenReturn(true);
        when(jwtService.hashToken(any())).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh("revoked.token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refresh_shouldThrowInvalidToken_whenExpired() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(jwtService.validateToken(any())).thenReturn(true);
        when(jwtService.hashToken(any())).thenReturn("hash");
        when(refreshTokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh("expired.token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}