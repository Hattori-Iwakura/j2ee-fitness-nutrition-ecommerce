package com.example.j2ee_fitness_nutrition_ecommerce.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Locale;

/**
 * Resolves the current user's email for both form login ({@link UserDetails}) and OAuth2 ({@link OAuth2User}).
 * {@code @AuthenticationPrincipal UserDetails} is null for Google OAuth — use this instead.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String getCurrentUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String u = userDetails.getUsername();
            return u != null ? u.trim() : null;
        }
        if (principal instanceof OAuth2User oauth2User) {
            Object email = oauth2User.getAttribute("email");
            return email != null ? email.toString().trim() : null;
        }
        return null;
    }

    public static String requireUserEmail(Authentication authentication) {
        String email = getCurrentUserEmail(authentication);
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authentication required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
