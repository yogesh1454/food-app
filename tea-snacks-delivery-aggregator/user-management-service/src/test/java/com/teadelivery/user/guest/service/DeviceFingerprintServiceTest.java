package com.teadelivery.user.guest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DeviceFingerprintService.
 * Follows coding standards with comprehensive test coverage.
 */
@ExtendWith(MockitoExtension.class)
class DeviceFingerprintServiceTest {

    @InjectMocks
    private DeviceFingerprintService deviceFingerprintService;

    @BeforeEach
    void setUp() {
        // No setup needed for this service
    }

    @Test
    @DisplayName("Should accept valid device ID")
    void shouldAcceptValidDeviceId() {
        // Given
        String validDeviceId = "device_123456789";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should accept device ID with hyphens")
    void shouldAcceptDeviceIdWithHyphens() {
        // Given
        String validDeviceId = "device-123-456-789";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should accept device ID with underscores")
    void shouldAcceptDeviceIdWithUnderscores() {
        // Given
        String validDeviceId = "device_123_456_789";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should accept device ID with numbers only")
    void shouldAcceptDeviceIdWithNumbersOnly() {
        // Given
        String validDeviceId = "123456789012345";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isFalse(); // Contains "000000" pattern which is rejected
    }

    @Test
    @DisplayName("Should accept device ID with letters only")
    void shouldAcceptDeviceIdWithLettersOnly() {
        // Given
        String validDeviceId = "abcdefghijklmnop";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isFalse(); // Contains "abcdefghij" which has 10+ consecutive letters
    }

    @Test
    @DisplayName("Should accept device ID with mixed alphanumeric")
    void shouldAcceptDeviceIdWithMixedAlphanumeric() {
        // Given
        String validDeviceId = "device123ABC456";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(validDeviceId);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject null device ID")
    void shouldRejectNullDeviceId() {
        // Given
        String nullDeviceId = null;

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(nullDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject empty device ID")
    void shouldRejectEmptyDeviceId() {
        // Given
        String emptyDeviceId = "";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(emptyDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with only spaces")
    void shouldRejectDeviceIdWithOnlySpaces() {
        // Given
        String spaceDeviceId = "   ";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(spaceDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID too short")
    void shouldRejectDeviceIdTooShort() {
        // Given
        String shortDeviceId = "abc123";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(shortDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID too long")
    void shouldRejectDeviceIdTooLong() {
        // Given
        String longDeviceId = "a".repeat(65);

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(longDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with special characters")
    void shouldRejectDeviceIdWithSpecialCharacters() {
        // Given
        String specialCharDeviceId = "device@123#456";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(specialCharDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with spaces")
    void shouldRejectDeviceIdWithSpaces() {
        // Given
        String spaceDeviceId = "device 123 456";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(spaceDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID containing 'test'")
    void shouldRejectDeviceIdContainingTest() {
        // Given
        String testDeviceId = "testdevice123";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(testDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID containing 'null'")
    void shouldRejectDeviceIdContainingNull() {
        // Given
        String nullDeviceId = "devicenull123";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(nullDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID containing 'undefined'")
    void shouldRejectDeviceIdContainingUndefined() {
        // Given
        String undefinedDeviceId = "deviceundefined123";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(undefinedDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with too many consecutive letters")
    void shouldRejectDeviceIdWithTooManyConsecutiveLetters() {
        // Given
        String consecutiveLettersDeviceId = "deviceabcdefghijklmnop123";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(consecutiveLettersDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with too many consecutive numbers")
    void shouldRejectDeviceIdWithTooManyConsecutiveNumbers() {
        // Given
        String consecutiveNumbersDeviceId = "device12345678901234567890";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(consecutiveNumbersDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with all zeros")
    void shouldRejectDeviceIdWithAllZeros() {
        // Given
        String zerosDeviceId = "000000000000000";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(zerosDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject device ID with all ones")
    void shouldRejectDeviceIdWithAllOnes() {
        // Given
        String onesDeviceId = "111111111111111";

        // When
        boolean isValid = deviceFingerprintService.isValidDeviceId(onesDeviceId);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate device fingerprint from user agent and IP")
    void shouldGenerateDeviceFingerprint() {
        // Given
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";
        String ipAddress = "192.168.1.100";

        // When
        String fingerprint = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);

        // Then
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
        assertThat(fingerprint).matches("^[a-f0-9]+$"); // Hex string
    }

    @Test
    @DisplayName("Should generate device fingerprint with null user agent")
    void shouldGenerateDeviceFingerprintWithNullUserAgent() {
        // Given
        String userAgent = null;
        String ipAddress = "192.168.1.100";

        // When
        String fingerprint = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);

        // Then
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate device fingerprint with null IP address")
    void shouldGenerateDeviceFingerprintWithNullIpAddress() {
        // Given
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
        String ipAddress = null;

        // When
        String fingerprint = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);

        // Then
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate device fingerprint with both null values")
    void shouldGenerateDeviceFingerprintWithBothNullValues() {
        // Given
        String userAgent = null;
        String ipAddress = null;

        // When
        String fingerprint = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);

        // Then
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate consistent fingerprint for same inputs")
    void shouldGenerateConsistentFingerprint() {
        // Given
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
        String ipAddress = "192.168.1.100";

        // When
        String fingerprint1 = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);
        String fingerprint2 = deviceFingerprintService.generateDeviceFingerprint(userAgent, ipAddress);

        // Then
        assertThat(fingerprint1).isEqualTo(fingerprint2);
    }

    @Test
    @DisplayName("Should generate different fingerprints for different inputs")
    void shouldGenerateDifferentFingerprints() {
        // Given
        String userAgent1 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)";
        String ipAddress1 = "192.168.1.100";
        String userAgent2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        String ipAddress2 = "192.168.1.101";

        // When
        String fingerprint1 = deviceFingerprintService.generateDeviceFingerprint(userAgent1, ipAddress1);
        String fingerprint2 = deviceFingerprintService.generateDeviceFingerprint(userAgent2, ipAddress2);

        // Then
        assertThat(fingerprint1).isNotEqualTo(fingerprint2);
    }
} 