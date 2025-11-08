package com.teadelivery.user.profile.service;

import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.model.UserProfile;
import com.teadelivery.user.profile.repository.ProfileHistoryRepository;
import com.teadelivery.user.profile.repository.UserProfileRepository;
import com.teadelivery.user.profile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simple unit tests for ProfileService without Spring context.
 * Follows coding standards with comprehensive test coverage.
 */
class ProfileServiceSimpleTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ProfileHistoryRepository profileHistoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    private ProfileService profileService;

    private User testUser;
    private UserProfile testUserProfile;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(userRepository, userProfileRepository, profileHistoryRepository, fileStorageService);
        
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .name("Test User")
                .role(User.Role.CUSTOMER)
                .userType(User.UserType.REGISTERED)
                .build();

        testUserProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void testGetUserProfile_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserProfile));

        // Act
        ProfileResponse result = profileService.getUserProfile(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.getUserProfile(testUserId));
        assertEquals("User not found: " + testUserId, exception.getMessage());

        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(userProfileRepository);
    }

    @Test
    void testGetUserProfile_ProfileNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.getUserProfile(testUserId));
        assertEquals("Profile not found for user: " + testUserId, exception.getMessage());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // Act
        ProfileResponse result = profileService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(profileHistoryRepository).save(any());
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.updateUserProfile(testUserId, request));
        assertEquals("User not found: " + testUserId, exception.getMessage());

        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(userProfileRepository);
    }

    @Test
    void testUpdateUserProfile_ProfileNotFound_CreatesNew() {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("New")
                .lastName("Profile")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // Act
        ProfileResponse result = profileService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("Profile", result.getLastName());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void testProfileCompletionIsCalculated() {
        // Arrange
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // Act
        ProfileResponse result = profileService.updateUserProfile(testUserId, request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProfileCompletionPercentage());
        assertTrue(result.getProfileCompletionPercentage() > 0);
    }
} 