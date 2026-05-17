package net.kozachok.postmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.dto.request.RoleRequest;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addRole(@PathVariable UUID id,
                                @Valid @RequestBody RoleRequest request) {
        return userService.addRole(id, request.roles());
    }

    @DeleteMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse removeRole(@PathVariable UUID id,
                                   @Valid @RequestBody RoleRequest request) {
        return userService.removeRole(id, request.roles());
    }
}