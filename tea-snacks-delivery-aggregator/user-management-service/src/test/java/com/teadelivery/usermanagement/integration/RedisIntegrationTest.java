package com.teadelivery.usermanagement.integration;

import com.teadelivery.usermanagement.repository.TokenBlacklistRepository;
import com.teadelivery.usermanagement.service.SessionStorageService;
import com.teadelivery.usermanagement.service.UserProfileCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Redis functionality
 */
@SpringBootTest
@ActiveProfiles("test")
class RedisIntegrationTest {

    @Autowired
    private SessionStorageService sessionStorageService;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private UserProfileCacheService userProfileCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_SESSION_ID = "test-session-456";
    private static final String TEST_TOKEN = "test-token-789";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        redisTemplate.delete("session:" + TEST_USER_ID);
        redisTemplate.delete("blacklist:tokens:" + TEST_TOKEN);
        redisTemplate.delete("user:" + TEST_USER_ID + ":profile");
        userProfileCacheService.evictUserProfileCache(TEST_USER_ID);
    }

    @Test
    void testSessionStorageOperations() {
        // Test storing session
        sessionStorageService.storeSession(TEST_USER_ID, TEST_SESSION_ID, "Test Device");

        // Test retrieving session
        Map<Object, Object> sessionData = sessionStorageService.getSession(TEST_USER_ID);
        assertFalse(sessionData.isEmpty());
        assertEquals(TEST_SESSION_ID, sessionData.get("sessionId"));
        assertEquals("Test Device", sessionData.get("deviceInfo"));

        // Test session validation
        assertTrue(sessionStorageService.isSessionValid(TEST_USER_ID, TEST_SESSION_ID));
        assertFalse(sessionStorageService.isSessionValid(TEST_USER_ID, "wrong-session"));

        // Test session removal
        sessionStorageService.removeSession(TEST_USER_ID);
        Map<Object, Object> removedSession = sessionStorageService.getSession(TEST_USER_ID);
        assertTrue(removedSession.isEmpty());
    }

    @Test
    void testTokenBlacklistOperations() {
        // Test token is not blacklisted initially
        assertFalse(tokenBlacklistRepository.isTokenBlacklisted(TEST_TOKEN));

        // Test blacklisting token
        tokenBlacklistRepository.blacklistToken(TEST_TOKEN);
        assertTrue(tokenBlacklistRepository.isTokenBlacklisted(TEST_TOKEN));

        // Test removing from blacklist
        tokenBlacklistRepository.removeFromBlacklist(TEST_TOKEN);
        assertFalse(tokenBlacklistRepository.isTokenBlacklisted(TEST_TOKEN));
    }

    @Test
    void testUserProfileCaching() {
        // Test profile is not cached initially
        assertFalse(userProfileCacheService.isProfileCached(TEST_USER_ID));

        // Test caching profile
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("firstName", "John");
        profileData.put("lastName", "Doe");
        profileData.put("email", "john.doe@example.com");

        userProfileCacheService.cacheUserProfile(TEST_USER_ID, profileData);
        assertTrue(userProfileCacheService.isProfileCached(TEST_USER_ID));

        // Test retrieving cached profile
        Map<String, Object> cachedProfile = userProfileCacheService.getCachedUserProfile(TEST_USER_ID);
        assertEquals("John", cachedProfile.get("firstName"));
        assertEquals("Doe", cachedProfile.get("lastName"));
        assertEquals("john.doe@example.com", cachedProfile.get("email"));

        // Test updating profile field
        userProfileCacheService.updateProfileField(TEST_USER_ID, "firstName", "Jane");

        // Test cache eviction
        userProfileCacheService.evictUserProfileCache(TEST_USER_ID);
        assertFalse(userProfileCacheService.isProfileCached(TEST_USER_ID));
    }

    @Test
    void testUserPreferencesCaching() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "en");
        preferences.put("theme", "dark");
        preferences.put("notifications", true);

        // Test caching preferences
        userProfileCacheService.cacheUserPreferences(TEST_USER_ID, preferences);

        // Test retrieving preferences
        Map<String, Object> cachedPreferences = userProfileCacheService.getCachedUserPreferences(TEST_USER_ID);
        assertEquals("en", cachedPreferences.get("language"));
        assertEquals("dark", cachedPreferences.get("theme"));
        assertEquals(true, cachedPreferences.get("notifications"));
    }

    @Test
    void testCacheStatistics() {
        // Cache some data
        sessionStorageService.storeSession(TEST_USER_ID, TEST_SESSION_ID, "Test Device");
        tokenBlacklistRepository.blacklistToken(TEST_TOKEN);

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("firstName", "John");
        userProfileCacheService.cacheUserProfile(TEST_USER_ID, profileData);

        // Test statistics
        Map<String, Object> stats = userProfileCacheService.getCacheStatistics();
        assertTrue((Long) stats.get("cachedProfiles") >= 1);
        assertTrue((Long) stats.get("totalCacheEntries") >= 1);

        // Test blacklist count
        assertTrue(tokenBlacklistRepository.getBlacklistedTokensCount() >= 1);

        // Test session count
        assertTrue(sessionStorageService.getActiveSessionsCount() >= 1);
    }

    @Test
    void testRedisConnectionAndBasicOperations() {
        // Test basic Redis operations
        String testKey = "test:connection";
        String testValue = "test-value";

        redisTemplate.opsForValue().set(testKey, testValue);
        Object retrievedValue = redisTemplate.opsForValue().get(testKey);
        assertEquals(testValue, retrievedValue);

        redisTemplate.delete(testKey);
        assertNull(redisTemplate.opsForValue().get(testKey));
    }
}
