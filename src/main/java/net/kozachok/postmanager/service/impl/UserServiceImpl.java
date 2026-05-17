package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.exception.ArticleApiException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public UserResponse addRole(UUID id, Set<RoleName> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Set<Role> rolesToAdd = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", name)))
                .collect(Collectors.toSet());

        user.getRoles().addAll(rolesToAdd);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse removeRole(UUID id, Set<RoleName> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Set<Role> rolesToRemove = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", name)))
                .collect(Collectors.toSet());

        Set<RoleName> userRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<RoleName> missing = roleNames.stream()
                .filter(name -> !userRoleNames.contains(name))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new ArticleApiException(
                    "User does not have roles: " + missing, HttpStatus.BAD_REQUEST);
        }

        if (user.getRoles().size() - rolesToRemove.size() < 1) {
            throw new ArticleApiException(
                    "Cannot remove all roles from user", HttpStatus.BAD_REQUEST);
        }

        user.getRoles().removeAll(rolesToRemove);
        return userMapper.toResponse(userRepository.save(user));
    }
}