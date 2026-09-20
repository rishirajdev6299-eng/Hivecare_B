package com.hivecare.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {


    private final UserRepository userRepository;


    public CustomUserDetailsService(
            UserRepository userRepository) {

        this.userRepository =
                userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {


        if (email == null ||
                email.isBlank()) {

            throw new UsernameNotFoundException(
                    "Email is required."
            );
        }


        String normalizedEmail =
                email.trim()
                        .toLowerCase();


        User user =
                userRepository
                        .findOptionalByEmail(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "User not found."
                                        )
                        );


        // =================================================
        // ROLE FROM MYSQL
        // =================================================

        String role =
                user.getRole();


        if (role == null ||
                role.isBlank()) {

            throw new UsernameNotFoundException(
                    "User role is not configured."
            );
        }


        role =
                role.trim()
                        .toUpperCase();


        // =================================================
        // VALID ROLES ONLY
        // =================================================

        if (!role.equals("USER")
                && !role.equals("WORKER")
                && !role.equals("ADMIN")) {

            throw new UsernameNotFoundException(
                    "Invalid user role: " + role
            );
        }


        // =================================================
        // SPRING USER
        // =================================================

        return new org.springframework.security.core.userdetails.User(

                user.getEmail(),

                user.getPassword() == null
                        ? ""
                        : user.getPassword(),

                // enabled
                !user.isBlocked(),

                // account non-expired
                true,

                // credentials non-expired
                true,

                // account non-locked
                !user.isBlocked(),

                // =========================================
                // ROLE FROM MYSQL
                // =========================================

                Collections.singletonList(

                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                )
        );
    }
}