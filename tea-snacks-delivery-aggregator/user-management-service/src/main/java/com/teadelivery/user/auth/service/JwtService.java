package com.teadelivery.user.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry-minutes}")
    private int accessTokenExpiryMinutes;

    @Value("${app.jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "ACCESS");
        
        return createToken(claims, email, accessTokenExpiryMinutes * 60 * 1000L);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(UUID userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("type", "REFRESH");
        
        return createToken(claims, email, refreshTokenExpiryDays * 24 * 60 * 60 * 1000L);
    }

    /**
     * Create JWT token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public UUID getUserIdFromToken(String token) {
        String userIdStr = getClaimFromToken(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdStr);
    }

    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract token type from token
     */
    public String getTokenTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is access token
     */
    public Boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is refresh token
     */
    public Boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}
