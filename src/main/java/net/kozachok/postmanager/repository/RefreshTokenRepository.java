package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteAllByUserId(UUID userId);
    void deleteAllByRevokedTrueOrExpiresAtBefore(LocalDateTime now);
}
