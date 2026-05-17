package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.dto.response.UserResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse addRole(UUID id, Set<RoleName> roles);
    UserResponse removeRole(UUID id, Set<RoleName> roles);
}