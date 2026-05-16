package net.kozachok.postmanager.security;

import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.exception.ArticleApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CurrentUser user) {
            return user;
        }
        throw new ArticleApiException("User not authenticated", HttpStatus.UNAUTHORIZED);
    }
}