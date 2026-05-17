package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// service/impl/UserServiceImpl.java
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse changeRole(UUID id, RoleName roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleName));

        user.setRoles(Set.of(role));
        return userMapper.toResponse(userRepository.save(user));
    }
}