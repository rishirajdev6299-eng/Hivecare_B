package com.hivecare.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;
import com.hivecare.security.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final String frontendUrl;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${app.frontend.url:https://hivecare.vercel.app}")
            String frontendUrl) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;

        this.frontendUrl =
                frontendUrl
                        .trim()
                        .replaceAll("/+$", "");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email =
                oauthUser.getAttribute("email");

        // =====================================================
        // EMAIL CHECK
        // =====================================================

        if (email == null || email.trim().isEmpty()) {

            response.sendRedirect(
                    frontendUrl
                            + "/login?error=email_missing"
            );

            return;
        }

        email =
                email
                        .trim()
                        .toLowerCase();

        // =====================================================
        // FIND USER
        // =====================================================

        User user =
                userRepository.findByEmail(email);

        // =====================================================
        // FALLBACK USER CREATION
        // =====================================================

        if (user == null) {

            user = new User();

            String name =
                    oauthUser.getAttribute("name");

            if (name == null
                    || name.trim().isEmpty()) {

                name = "HiveCare User";
            }

            user.setName(
                    name.trim()
            );

            user.setEmail(email);

            user.setPassword(null);

            /*
             * New OAuth accounts are normal USER accounts.
             *
             * Admin / Worker roles should be controlled
             * from MySQL.
             */
            user.setRole("USER");

            user.setProvider("GOOGLE");

            String providerId =
                    oauthUser.getAttribute("sub");

            user.setProviderId(providerId);

            user.setBlocked(false);

            user.setRejectedBookings(0);

            user.setAvailable(false);

            user.setAvailabilitySet(false);

            user.setLoginAttempts(0);

            user.setLoginLockedUntil(null);

            user =
                    userRepository.save(user);
        }

        // =====================================================
        // BLOCKED ACCOUNT
        // =====================================================

        if (user.isBlocked()) {

            response.sendRedirect(
                    frontendUrl
                            + "/login?error=account_blocked"
            );

            return;
        }

        // =====================================================
        // GENERATE JWT
        // =====================================================

        /*
         * The role comes from the CURRENT MySQL user row.
         *
         * The frontend must not be trusted for authorization.
         */
        String token =
                jwtService.generateToken(user);

        // =====================================================
        // FRONTEND REDIRECT
        // =====================================================

        String redirectUrl =
                frontendUrl
                        + "/oauth-success?token="
                        + URLEncoder.encode(
                                token,
                                StandardCharsets.UTF_8
                        );

        response.sendRedirect(
                redirectUrl
        );
    }
}