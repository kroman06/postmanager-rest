package net.kozachok.postmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import net.kozachok.postmanager.domain.RoleName;

public record ChangeRoleRequest(@NotNull RoleName role) {}