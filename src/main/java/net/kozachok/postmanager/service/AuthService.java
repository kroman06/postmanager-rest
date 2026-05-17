package net.kozachok.postmanager.service;

import net.kozachok.postmanager.dto.request.LoginRequest;
import net.kozachok.postmanager.dto.request.RegisterRequest;
import net.kozachok.postmanager.dto.response.TokenResponse;
import net.kozachok.postmanager.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {
    UserResponse getCurrentUser(UUID id);
    UserResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(String refreshToken);
    void logout(String s);
}
