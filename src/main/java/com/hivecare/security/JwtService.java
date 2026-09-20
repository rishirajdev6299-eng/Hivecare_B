package com.hivecare.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hivecare.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;

    private final long expiration;


    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {

        if (secret == null || secret.length() < 32) {

            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 characters."
            );
        }

        this.signingKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expiration = expiration;
    }


    // =====================================================
    // GENERATE TOKEN
    // =====================================================

    public String generateToken(User user) {

        return Jwts.builder()

                .subject(
                        user.getEmail()
                )

                .claim(
                        "userId",
                        user.getId()
                )

                /*
                 * This claim is NOT trusted for authorization.
                 *
                 * The current role is loaded from MySQL
                 * by CustomUserDetailsService.
                 */
                .claim(
                        "role",
                        user.getRole()
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiration
                        )
                )

                .signWith(
                        signingKey
                )

                .compact();
    }


    // =====================================================
    // EXTRACT EMAIL
    // =====================================================

    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }


    // =====================================================
    // EXTRACT USER ID
    // =====================================================

    public Long extractUserId(String token) {

        Object userId =
                getClaims(token)
                        .get("userId");

        if (userId instanceof Number number) {

            return number.longValue();
        }

        return null;
    }


    // =====================================================
    // EXTRACT ROLE
    // =====================================================

    public String extractRole(String token) {

        return getClaims(token)
                .get(
                        "role",
                        String.class
                );
    }


    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(String token) {

        try {

            Claims claims =
                    getClaims(token);

            Date expirationDate =
                    claims.getExpiration();

            return expirationDate != null
                    && expirationDate.after(
                            new Date()
                    );

        } catch (Exception exception) {

            System.out.println(
                    "JWT VALIDATION ERROR: "
                            + exception.getClass()
                                    .getSimpleName()
                            + " - "
                            + exception.getMessage()
            );

            return false;
        }
    }


    // =====================================================
    // GET CLAIMS
    // =====================================================

    private Claims getClaims(String token) {

        return Jwts.parser()

                .verifyWith(
                        signingKey
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }
}