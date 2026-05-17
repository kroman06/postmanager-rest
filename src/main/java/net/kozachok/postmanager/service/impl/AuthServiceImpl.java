package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.config.JwtProperties;
import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.dto.request.RegisterRequest;
import net.kozachok.postmanager.dto.response.TokenResponse;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.exception.*;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RefreshTokenRepository;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.security.JwtService;
import net.kozachok.postmanager.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository         roleRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final JwtProperties          jwtProperties;
    private final UserMapper             userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Role readerRole = roleRepository.findByName(RoleName.ROLE_READER)
                .orElseThrow(() -> new RuntimeException("Role READER not found in DB"));

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(Set.of(readerRole));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ArticleApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ArticleApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        return issueTokenPair(user);
    }

    @Override
    public void logout(String rawRefreshToken) {
        String hash = jwtService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(stored -> {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
        });
    }

    @Override
    public TokenResponse refresh(String rawRefreshToken) {
        if (!jwtService.validateToken(rawRefreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String hash = jwtService.hashToken(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!stored.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokenPair(stored.getUser());
    }

    private TokenResponse issueTokenPair(User user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String rawRefresh   = jwtService.generateRefreshToken(user);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(jwtService.hashToken(rawRefresh));
        token.setExpiresAt(LocalDateTime.now().plus(jwtProperties.getRefreshExpiration()));
        token.setRevoked(false);
        refreshTokenRepository.save(token);

        return new TokenResponse(accessToken, rawRefresh);
    }
}