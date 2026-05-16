package net.kozachok.postmanager.security.impl;

import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.security.JwtService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    @Override public String generateAccessToken(User user)  { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public String generateRefreshToken(User user) { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public boolean validateToken(String token)    { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public UUID extractUserId(String token)       { throw new UnsupportedOperationException("Not implemented yet"); }
    @Override public String hashToken(String rawToken)      { throw new UnsupportedOperationException("Not implemented yet"); }
}