package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        return roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
    }
}