package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;

public interface UserMapper {
    UserResponse toResponse(User user);
}