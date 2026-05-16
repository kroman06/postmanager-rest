package net.kozachok.postmanager.domain;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(UUID id, Set<RoleName> roles) {

    public boolean isAdmin() {
        return roles.contains(RoleName.ROLE_ADMIN);
    }
}
