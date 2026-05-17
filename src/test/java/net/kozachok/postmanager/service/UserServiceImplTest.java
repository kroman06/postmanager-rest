package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.exception.ArticleApiException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.UserMapper;
import net.kozachok.postmanager.repository.RoleRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    private final UUID USER_ID = UUID.randomUUID();

    private Role role(RoleName name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }

    private User userWithRoles(RoleName... names) {
        User user = new User();
        user.setId(USER_ID);
        Set<Role> roles = new HashSet<>();
        for (RoleName name : names) roles.add(role(name));
        user.setRoles(roles);
        return user;
    }

    // addRole

    @Test
    void addRole_shouldAddRole_whenUserExists() {
        User user = userWithRoles(RoleName.ROLE_READER);
        Role authorRole = role(RoleName.ROLE_AUTHOR);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_AUTHOR)).thenReturn(Optional.of(authorRole));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenReturn(mock(UserResponse.class));

        assertThatNoException().isThrownBy(() ->
                userService.addRole(USER_ID, Set.of(RoleName.ROLE_AUTHOR)));

        verify(userRepository).save(argThat(u ->
                u.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_AUTHOR)));
    }

    @Test
    void addRole_shouldThrowNotFound_whenUserNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addRole(USER_ID, Set.of(RoleName.ROLE_AUTHOR)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addRole_shouldThrowNotFound_whenRoleNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userWithRoles(RoleName.ROLE_READER)));
        when(roleRepository.findByName(RoleName.ROLE_AUTHOR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addRole(USER_ID, Set.of(RoleName.ROLE_AUTHOR)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // removeRole

    @Test
    void removeRole_shouldRemoveRole_whenValid() {
        User user = userWithRoles(RoleName.ROLE_READER, RoleName.ROLE_AUTHOR);
        Role authorRole = role(RoleName.ROLE_AUTHOR);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_AUTHOR)).thenReturn(Optional.of(authorRole));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenReturn(mock(UserResponse.class));

        assertThatNoException().isThrownBy(() ->
                userService.removeRole(USER_ID, Set.of(RoleName.ROLE_AUTHOR)));

        verify(userRepository).save(argThat(u ->
                u.getRoles().stream().noneMatch(r -> r.getName() == RoleName.ROLE_AUTHOR)));
    }

    @Test
    void removeRole_shouldThrowBadRequest_whenUserDoesNotHaveRole() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userWithRoles(RoleName.ROLE_READER)));
        when(roleRepository.findByName(RoleName.ROLE_AUTHOR)).thenReturn(Optional.of(role(RoleName.ROLE_AUTHOR)));

        assertThatThrownBy(() -> userService.removeRole(USER_ID, Set.of(RoleName.ROLE_AUTHOR)))
                .isInstanceOf(ArticleApiException.class);
    }

    @Test
    void removeRole_shouldThrowBadRequest_whenRemovingLastRole() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userWithRoles(RoleName.ROLE_READER)));
        when(roleRepository.findByName(RoleName.ROLE_READER)).thenReturn(Optional.of(role(RoleName.ROLE_READER)));

        assertThatThrownBy(() -> userService.removeRole(USER_ID, Set.of(RoleName.ROLE_READER)))
                .isInstanceOf(ArticleApiException.class);
    }

    @Test
    void removeRole_shouldThrowBadRequest_whenRemovingLastAdmin() {
        when(userRepository.findById(USER_ID)).thenReturn(
                Optional.of(userWithRoles(RoleName.ROLE_ADMIN, RoleName.ROLE_AUTHOR)));
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(role(RoleName.ROLE_ADMIN)));
        when(userRepository.countByRoleName(RoleName.ROLE_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.removeRole(USER_ID, Set.of(RoleName.ROLE_ADMIN)))
                .isInstanceOf(ArticleApiException.class)
                .hasMessageContaining("last admin");
    }

    @Test
    void removeRole_shouldSucceed_whenOtherAdminsExist() {
        User user = userWithRoles(RoleName.ROLE_ADMIN, RoleName.ROLE_AUTHOR);
        Role adminRole = role(RoleName.ROLE_ADMIN);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.countByRoleName(RoleName.ROLE_ADMIN)).thenReturn(2L);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenReturn(mock(UserResponse.class));

        assertThatNoException().isThrownBy(() ->
                userService.removeRole(USER_ID, Set.of(RoleName.ROLE_ADMIN)));
    }

    @Test
    void removeRole_shouldThrowNotFound_whenUserNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRole(USER_ID, Set.of(RoleName.ROLE_READER)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // findAll

    @Test
    void findAll_shouldReturnMappedUsers() {
        User user = userWithRoles(RoleName.ROLE_READER);
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(mock(UserResponse.class));

        var result = userService.findAll();

        assertThat(result).hasSize(1);
        verify(userMapper).toResponse(user);
    }
}