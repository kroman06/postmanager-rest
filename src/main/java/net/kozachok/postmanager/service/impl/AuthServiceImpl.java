package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.dto.request.RegisterRequest;
import net.kozachok.postmanager.dto.response.TokenResponse;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RefreshTokenRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.security.JwtService;
import net.kozachok.postmanager.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public UserResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
