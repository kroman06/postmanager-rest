package net.kozachok.postmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(
            summary = "Get all users",
            description = "Returns a list of all registered users. Requires administrator role."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @Operation(
            summary = "Add roles to user",
            description = "Adds one or more roles to the specified user. Requires administrator role."
    )
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addRole(@PathVariable UUID id,
                                @Valid @RequestBody RoleRequest request) {
        return userService.addRole(id, request.roles());
    }

    @Operation(
            summary = "Remove roles from user",
            description = "Removes one or more roles from the specified user. Requires administrator role."
    )
    @DeleteMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse removeRole(@PathVariable UUID id,
                                   @Valid @RequestBody RoleRequest request) {
        return userService.removeRole(id, request.roles());
    }
}