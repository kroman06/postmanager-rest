package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleName name);
}