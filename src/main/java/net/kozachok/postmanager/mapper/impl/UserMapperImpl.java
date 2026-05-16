package net.kozachok.postmanager.mapper.impl;

import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.dto.response.UserResponse;
import net.kozachok.postmanager.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse toResponse(User user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}