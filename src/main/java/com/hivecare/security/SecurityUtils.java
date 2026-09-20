package com.hivecare.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;

@Component
public class SecurityUtils {


    private final UserRepository userRepository;


    public SecurityUtils(
            UserRepository userRepository) {

        this.userRepository =
                userRepository;
    }


    // =====================================================
    // CURRENT AUTHENTICATED USER
    // =====================================================

    public User getCurrentUser() {

        Authentication authentication =

                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }


        // AnonymousAuthenticationToken should
        // never be treated as a real user.

        if (authentication
                .getPrincipal()
                .equals("anonymousUser")) {

            return null;
        }


        String email =
                authentication.getName();


        if (email == null ||
                email.isBlank()) {

            return null;
        }


        return userRepository
                .findByEmail(
                        email
                );
    }


    // =====================================================
    // CURRENT USER IS ADMIN
    // =====================================================

    public boolean isAdmin() {

        User user =
                getCurrentUser();


        return user != null
                && "ADMIN".equalsIgnoreCase(
                        user.getRole()
                );
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    public boolean isCurrentUser(
            Long userId) {

        User currentUser =
                getCurrentUser();


        return currentUser != null
                && currentUser.getId()
                        .equals(userId);
    }


    // =====================================================
    // CURRENT USER OR ADMIN
    // =====================================================

    public boolean isCurrentUserOrAdmin(
            Long userId) {

        return isAdmin()
                || isCurrentUser(userId);
    }
}