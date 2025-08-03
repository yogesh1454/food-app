package com.teadelivery.user.auth.service;

import com.teadelivery.user.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token provider for authentication and authorization.
 * Follows coding standards with comprehensive token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    /**
     * Generates access token for user.
     * 
     * @param userId user ID
     * @param username username (email or phone)
     * @param roles user roles
     * @return access token
     */
    public String generateAccessToken(String userId, String username, String... roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getAccessTokenExpiration(), ChronoUnit.SECONDS)))
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Generates refresh token for user.
     * 
     * @param userId user ID
     * @return refresh token
     */
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getRefreshTokenExpiration(), ChronoUnit.SECONDS)))
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Validates JWT token.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts user ID from JWT token.
     * 
     * @param token JWT token
     * @return user ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }

    /**
     * Extracts username from JWT token.
     * 
     * @param token JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("username", String.class);
    }

    /**
     * Extracts roles from JWT token.
     * 
     * @param token JWT token
     * @return roles array
     */
    public String[] getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("roles", String[].class);
    }

    /**
     * Gets token expiration time.
     * 
     * @param token JWT token
     * @return expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }

    /**
     * Checks if token is expired.
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Gets signing key for JWT.
     * 
     * @return signing key
     */
    private Key getSigningKey() {
        return jwtConfig.getKeyPair().getPrivate();
    }

    /**
     * Gets verification key for JWT.
     * 
     * @return verification key
     */
    public Key getVerificationKey() {
        return jwtConfig.getKeyPair().getPublic();
    }
} 