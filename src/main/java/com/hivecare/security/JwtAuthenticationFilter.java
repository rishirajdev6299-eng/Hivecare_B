package com.hivecare.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {

        this.jwtService =
                jwtService;

        this.userDetailsService =
                userDetailsService;
    }


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain)

            throws ServletException, IOException {


        // =================================================
        // GET AUTHORIZATION HEADER
        // =================================================

        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );


        // =================================================
        // NO TOKEN
        // =================================================

        if (authorizationHeader == null
                || !authorizationHeader
                        .startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // GET TOKEN
        // =================================================

        String token =
                authorizationHeader
                        .substring(7)
                        .trim();


        if (token.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }


        try {

            // =================================================
            // VALIDATE TOKEN SIGNATURE + EXPIRATION
            // =================================================

            if (!jwtService.isTokenValid(token)) {

                SecurityContextHolder
                        .clearContext();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "application/json"
                );

                response.getWriter().write(
                        "{\"message\":\"Invalid or expired token.\"}"
                );

                return;
            }


            // =================================================
            // GET EMAIL FROM JWT
            // =================================================

            String email =
                    jwtService.extractEmail(
                            token
                    );


            if (email == null ||
                    email.isBlank()) {

                SecurityContextHolder
                        .clearContext();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "application/json"
                );

                response.getWriter().write(
                        "{\"message\":\"Invalid token.\"}"
                );

                return;
            }


            // =================================================
            // LOAD USER FROM MYSQL
            //
            // IMPORTANT:
            //
            // CustomUserDetailsService loads:
            //
            // email → MySQL → CURRENT ROLE
            //
            // We do NOT use role from localStorage.
            // We do NOT use role claim for authorization.
            // =================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    == null) {


                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        email
                                );


                UsernamePasswordAuthenticationToken
                        authentication =

                        new UsernamePasswordAuthenticationToken(

                                userDetails,

                                null,

                                userDetails
                                        .getAuthorities()
                        );


                authentication.setDetails(

                        new WebAuthenticationDetailsSource()
                                .buildDetails(
                                        request
                                )
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "JWT AUTHENTICATED: "
                                + email
                                + " | AUTHORITIES: "
                                + userDetails
                                        .getAuthorities()
                );
            }


        } catch (Exception exception) {

            System.out.println(
                    "JWT AUTHENTICATION ERROR: "
                            + exception.getClass()
                                    .getSimpleName()
                            + " - "
                            + exception.getMessage()
            );

            SecurityContextHolder
                    .clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    "{\"message\":\"JWT authentication failed.\"}"
            );

            return;
        }


        // =================================================
        // CONTINUE REQUEST
        // =================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}