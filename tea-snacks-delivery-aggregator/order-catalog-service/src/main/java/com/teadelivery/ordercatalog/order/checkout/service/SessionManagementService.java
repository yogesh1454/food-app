package com.teadelivery.ordercatalog.order.checkout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing checkout sessions in Redis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagementService {
    
    private static final String SESSION_KEY_PREFIX = "checkout:session:";
    private static final Duration SESSION_TTL = Duration.ofMinutes(15);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Create or retrieve existing checkout session
     */
    public String createSession(CheckoutSession session) {
        String sessionId = generateSessionId(session);
        session.setCheckoutSessionId(sessionId);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plus(SESSION_TTL));
        session.setStatus(CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT);
        
        String key = SESSION_KEY_PREFIX + sessionId;
        
        // Check if session already exists (idempotency)
        CheckoutSession existing = getSession(sessionId).orElse(null);
        if (existing != null && existing.getStatus() == CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT) {
            log.info("Returning existing session: {}", sessionId);
            return sessionId;
        }
        
        // Store new session
        redisTemplate.opsForValue().set(key, session, SESSION_TTL);
        log.info("Created checkout session: {} with TTL: {} minutes", sessionId, SESSION_TTL.toMinutes());
        
        return sessionId;
    }
    
    /**
     * Get checkout session by ID
     */
    public Optional<CheckoutSession> getSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            log.debug("Session not found: {}", sessionId);
            return Optional.empty();
        }
        
        try {
            CheckoutSession session = objectMapper.convertValue(value, CheckoutSession.class);
            
            // Check if expired
            if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.info("Session expired: {}", sessionId);
                deleteSession(sessionId);
                return Optional.empty();
            }
            
            return Optional.of(session);
        } catch (Exception e) {
            log.error("Error deserializing session: {}", sessionId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Lock session for order creation (atomic operation)
     * Updates status from READY_FOR_COMMIT to IN_PROGRESS
     * 
     * @param sessionId Session ID to lock
     * @return Locked session
     * @throws IllegalStateException if session cannot be locked
     */
    public CheckoutSession lockSession(String sessionId) {
        log.info("Attempting to lock session: {}", sessionId);
        
        Optional<CheckoutSession> sessionOpt = getSession(sessionId);
        
        if (sessionOpt.isEmpty()) {
            throw new IllegalStateException("Session not found: " + sessionId);
        }
        
        CheckoutSession session = sessionOpt.get();
        
        // Check current status
        if (session.getStatus() == CheckoutResponse.CheckoutStatus.COMMITTED) {
            throw new IllegalStateException("Session already committed: " + sessionId);
        }
        
        if (session.getStatus() == CheckoutResponse.CheckoutStatus.IN_PROGRESS) {
            throw new IllegalStateException("Session already in progress: " + sessionId);
        }
        
        if (session.getStatus() != CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT) {
            throw new IllegalStateException("Session not ready for commit. Status: " + session.getStatus());
        }
        
        // Check if session is stale (> 5 minutes old)
        if (session.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalStateException("Session is stale (> 5 minutes old): " + sessionId);
        }
        
        // Update status to IN_PROGRESS (atomic lock)
        session.setStatus(CheckoutResponse.CheckoutStatus.IN_PROGRESS);
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, session, SESSION_TTL);
        
        log.info("Session locked successfully: {}", sessionId);
        return session;
    }
    
    /**
     * Update session status
     */
    public void updateSessionStatus(String sessionId, CheckoutResponse.CheckoutStatus status) {
        updateSessionStatus(sessionId, status, null);
    }
    
    /**
     * Update session status with order ID
     */
    public void updateSessionStatus(String sessionId, CheckoutResponse.CheckoutStatus status, String orderId) {
        Optional<CheckoutSession> sessionOpt = getSession(sessionId);
        
        if (sessionOpt.isPresent()) {
            CheckoutSession session = sessionOpt.get();
            session.setStatus(status);
            
            // Store order ID if provided
            if (orderId != null) {
                session.setOrderId(orderId);
            }
            
            String key = SESSION_KEY_PREFIX + sessionId;
            
            if (status == CheckoutResponse.CheckoutStatus.COMMITTED) {
                // Reduce TTL to 5 minutes for committed sessions
                redisTemplate.opsForValue().set(key, session, Duration.ofMinutes(5));
                log.info("Session committed: {}, orderId: {}, TTL reduced to 5 minutes", sessionId, orderId);
            } else {
                redisTemplate.opsForValue().set(key, session, SESSION_TTL);
                log.info("Session status updated: {} -> {}", sessionId, status);
            }
        } else {
            log.warn("Cannot update status - session not found: {}", sessionId);
        }
    }
    
    /**
     * Delete session
     */
    public void deleteSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.info("Session deleted: {}", sessionId);
    }
    
    /**
     * Generate unique session ID based on request content
     * Format: chk_<timestamp>_<hash>
     */
    private String generateSessionId(CheckoutSession session) {
        try {
            // Create input string for hashing
            String input = session.getUserId().toString() +
                          session.getVendorBranchId() +
                          session.getItems().toString() +
                          session.getDeliveryAddress().toString();
            
            // Generate SHA-256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string and take first 20 characters
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            String hashPart = hexString.substring(0, 20);
            long timestamp = System.currentTimeMillis();
            
            return String.format("chk_%d_%s", timestamp, hashPart);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating session ID", e);
            // Fallback to timestamp-based ID
            return "chk_" + System.currentTimeMillis() + "_" + session.getUserId().toString().substring(0, 8);
        }
    }
    
    /**
     * Check if session exists and is valid
     */
    public boolean isSessionValid(String sessionId) {
        return getSession(sessionId)
            .map(session -> session.getStatus() == CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT)
            .orElse(false);
    }
}
