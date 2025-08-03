package com.teadelivery.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * JWT configuration properties and utilities.
 * Follows coding standards with proper documentation.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String issuer = "tea-delivery";
    private String audience = "tea-delivery-users";
    private long accessTokenExpiration = 3600; // 1 hour in seconds
    private long refreshTokenExpiration = 86400; // 24 hours in seconds
    private String algorithm = "RS256";
    
    private KeyPair keyPair;
    
    public JwtConfig() {
        this.keyPair = generateKeyPair();
    }
    
    /**
     * Generates RSA key pair for JWT signing.
     * 
     * @return RSA key pair
     */
    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
} 