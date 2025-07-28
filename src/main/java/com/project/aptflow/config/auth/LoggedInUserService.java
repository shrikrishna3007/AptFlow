package com.project.aptflow.config.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoggedInUserService {
    // Method to get the current logged-in user's email.
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication !=null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new IllegalStateException("Current user is not authenticated.");
    }
}
