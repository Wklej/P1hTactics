package com.p1h.p1htactics.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserUtils {

    public static String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) (principal)).getUsername();
        } else {
            return principal.toString();
        }
    }
}
