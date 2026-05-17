package net.kozachok.postmanager.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private RefreshToken token(boolean revoked, boolean expired) {
        RefreshToken token = new RefreshToken();
        token.setRevoked(revoked);
        token.setExpiresAt(expired
                ? LocalDateTime.now().minusDays(1)
                : LocalDateTime.now().plusDays(7));
        return token;
    }

    // isExpired

    @Test
    void isExpired_shouldReturnFalse_whenExpiresAtIsInFuture() {
        assertThat(token(false, false).isExpired()).isFalse();
    }

    @Test
    void isExpired_shouldReturnTrue_whenExpiresAtIsInPast() {
        assertThat(token(false, true).isExpired()).isTrue();
    }

    // isValid

    @Test
    void isValid_shouldReturnTrue_whenNotRevokedAndNotExpired() {
        assertThat(token(false, false).isValid()).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenRevoked() {
        assertThat(token(true, false).isValid()).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenExpired() {
        assertThat(token(false, true).isValid()).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenRevokedAndExpired() {
        assertThat(token(true, true).isValid()).isFalse();
    }
}