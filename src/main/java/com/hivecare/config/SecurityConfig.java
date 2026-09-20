package com.hivecare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfigurationSource;

import com.hivecare.security.JwtAuthenticationFilter;
import com.hivecare.services.OAuthUserService;

@Configuration
public class SecurityConfig {

    private final OAuthUserService oauthUserService;

    private final OAuth2SuccessHandler oauth2SuccessHandler;

    private final CorsConfigurationSource corsConfigurationSource;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String frontendUrl;

    public SecurityConfig(
            OAuthUserService oauthUserService,
            OAuth2SuccessHandler oauth2SuccessHandler,
            CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.frontend.url:https://hivecare.vercel.app}")
            String frontendUrl) {

        this.oauthUserService =
                oauthUserService;

        this.oauth2SuccessHandler =
                oauth2SuccessHandler;

        this.corsConfigurationSource =
                corsConfigurationSource;

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.frontendUrl =
                frontendUrl
                        .trim()
                        .replaceAll("/+$", "");
    }

    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )

                // =================================================
                // JWT STATELESS
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // PUBLIC AUTH
                        // =========================================

                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/register",
                                "/api/users/forgot-password",
                                "/api/users/reset-password"
                        )
                        .permitAll()

                        // =========================================
                        // OAUTH
                        // =========================================

                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/error"
                        )
                        .permitAll()

                        // =========================================
                        // PUBLIC SERVICES
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/services",
                                "/api/subjects/**"
                        )
                        .permitAll()

                        // =========================================
                        // PUBLIC CUSTOMER REVIEWS
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reviews"
                        )
                        .permitAll()

                        // =========================================
                        // ADMIN ONLY
                        // =========================================

                        .requestMatchers(
                                "/api/admin/**",
                                "/api/workers/admin/**",
                                "/api/users/admin/**",
                                "/api/bookings/admin/**",
                                "/api/reviews/admin"
                        )
                        .hasRole("ADMIN")

                        // =========================================
                        // ADMIN SERVICE MANAGEMENT
                        // =========================================

                        .requestMatchers(
                                "/api/services/**"
                        )
                        .hasRole("ADMIN")

                        // =========================================
                        // WORKER ONLY
                        // =========================================

                        .requestMatchers(
                                "/api/workers/**"
                        )
                        .hasRole("WORKER")

                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // =================================================
                // GOOGLE OAUTH LOGIN
                // =================================================

                .oauth2Login(oauth -> oauth

                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(
                                        oauthUserService
                                )
                        )

                        .successHandler(
                                oauth2SuccessHandler
                        )

                        .failureHandler(
                                (request,
                                 response,
                                 exception) -> {

                                    response.sendRedirect(
                                            frontendUrl
                                                    + "/login?error=oauth_failed"
                                    );
                                }
                        )
                );

        return http.build();
    }
}