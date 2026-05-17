package net.kozachok.postmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.kozachok.postmanager.domain.RoleName;

import java.util.Set;

public record RoleRequest(@NotNull @Size(min = 1) Set<RoleName> roles) {}