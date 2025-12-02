package com.recn.platform.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@Slf4j
public class UserSecurity {

    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            boolean isOwner = userPrincipal.getUserId().equals(userId);
            log.debug("User {} is owner of resource {}: {}", userPrincipal.getUserId(), userId, isOwner);
            return isOwner;
        }

        return false;
    }
}

