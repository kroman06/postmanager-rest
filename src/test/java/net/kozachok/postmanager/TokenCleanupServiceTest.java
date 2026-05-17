package net.kozachok.postmanager;

import net.kozachok.postmanager.repository.RefreshTokenRepository;
import net.kozachok.postmanager.service.impl.TokenCleanupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    private TokenCleanupServiceImpl tokenCleanupService;

    @Test
    void cleanupExpiredTokens_shouldDeleteRevokedAndExpiredTokens() {
        tokenCleanupService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteAllByRevokedTrueOrExpiresAtBefore(
                argThat(time -> !time.isAfter(LocalDateTime.now())));
    }
}