package com.Jobtrackr.jta.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration; // 1 hour default

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration; // 7 days default

    public String generateAccessToken(String email, String role, UUID userId) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("userId", userId.toString())
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(String email, UUID userId) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", userId.toString())
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    @Deprecated
    public String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token)
                    .getClaim("role")
                    .asString();
        } catch (JWTVerificationException e) {
            log.warn("Role extraction failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractUserId(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token)
                    .getClaim("userId")
                    .asString();
        } catch (JWTVerificationException e) {
            log.warn("UserId extraction failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractTokenType(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token)
                    .getClaim("type")
                    .asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    public boolean isAccessToken(String token) {
        String type = extractTokenType(token);
        return type == null || "access".equals(type);
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token decode failed: {}", e.getMessage());
            return null;
        }
    }
}