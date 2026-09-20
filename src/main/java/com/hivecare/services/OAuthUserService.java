
package com.hivecare.services;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hivecare.model.User;
import com.hivecare.repository.UserRepository;

@Service
public class OAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public OAuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauthUser = super.loadUser(userRequest);

        String provider =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId()
                        .toUpperCase();

        String email =
                oauthUser.getAttribute("email");

        String name =
                oauthUser.getAttribute("name");

        String providerId =
                oauthUser.getName();

        // =====================================================
        // GOOGLE
        // =====================================================

        if ("GOOGLE".equals(provider)) {

            providerId =
                    oauthUser.getAttribute("sub");

            if (name == null || name.trim().isEmpty()) {

                name =
                        oauthUser.getAttribute("given_name");
            }
        }

        // =====================================================
        // FACEBOOK
        // =====================================================

        if ("FACEBOOK".equals(provider)) {

            providerId =
                    oauthUser.getAttribute("id");
        }

        // =====================================================
        // APPLE
        // =====================================================

        if ("APPLE".equals(provider)) {

            String appleSub =
                    oauthUser.getAttribute("sub");

            if (appleSub != null) {
                providerId = appleSub;
            }

            if (email == null) {
                email = oauthUser.getAttribute("email");
            }

            if (name == null || name.trim().isEmpty()) {
                name = "HiveCare User";
            }
        }

        // =====================================================
        // VALIDATE EMAIL
        // =====================================================

        if (email == null || email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email was not provided by " + provider
            );
        }

        email = email.trim().toLowerCase();

        // =====================================================
        // VALIDATE PROVIDER ID
        // =====================================================

        if (providerId == null ||
            providerId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Provider ID was not provided by " + provider
            );
        }

        // =====================================================
        // 1. FIND BY PROVIDER + PROVIDER ID
        // =====================================================

        Optional<User> providerUser =
                userRepository.findByProviderAndProviderId(
                        provider,
                        providerId
                );

        if (providerUser.isPresent()) {

            User existingUser =
                    providerUser.get();

            // Check blocked account
            if (existingUser.isBlocked()) {

                throw new RuntimeException(
                        "Your account has been blocked by administrator."
                );
            }

            // Existing social account
            return oauthUser;
        }

        // =====================================================
        // 2. FIND BY EMAIL
        // =====================================================

        User existingEmailUser =
                userRepository.findByEmail(email);

        // =====================================================
        // EXISTING HIVECARE ACCOUNT
        // =====================================================

        if (existingEmailUser != null) {

            // Check blocked account
            if (existingEmailUser.isBlocked()) {

                throw new RuntimeException(
                        "Your account has been blocked by administrator."
                );
            }

            /*
             * IMPORTANT:
             *
             * We DO NOT check the normal password here.
             *
             * Google/Facebook/Apple has already authenticated
             * the user.
             *
             * If the email already exists in MySQL,
             * connect this social provider to that account.
             */

            existingEmailUser.setProvider(provider);
            existingEmailUser.setProviderId(providerId);

            // Do not change:
            // - password
            // - role
            // - phone
            // - address
            // - workerService

            userRepository.save(existingEmailUser);

            return oauthUser;
        }

        // =====================================================
        // 3. CREATE NEW SOCIAL USER
        // =====================================================

        User newUser = new User();

        newUser.setName(
                name != null && !name.trim().isEmpty()
                        ? name.trim()
                        : "HiveCare User"
        );

        newUser.setEmail(email);

        /*
         * Social users do not need a local password.
         */
        newUser.setPassword(null);

        /*
         * New social users are normal USER accounts.
         */
        newUser.setRole("USER");

        newUser.setProvider(provider);
        newUser.setProviderId(providerId);

        newUser.setBlocked(false);
        newUser.setRejectedBookings(0);

        userRepository.save(newUser);

        return oauthUser;
    }
}

