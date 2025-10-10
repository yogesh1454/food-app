package com.teadelivery.user.profile.service;

import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.model.ProfileHistory;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.model.UserProfile;
import com.teadelivery.user.profile.repository.ProfileHistoryRepository;
import com.teadelivery.user.profile.repository.UserProfileRepository;
import com.teadelivery.user.profile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileService.
 * Follows coding standards with comprehensive test coverage.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ProfileHistoryRepository profileHistoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private UserProfile testUserProfile;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
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
    void getUserProfile_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserProfile));

        // Act
        ProfileResponse result = profileService.getUserProfile(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId.toString(), result.getUserId());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Test", result.getFirstName());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
    }

    @Test
    void getUserProfile_UserNotFound() {
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
    void getUserProfile_ProfileNotFound() {
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
    void updateUserProfile_Success() {
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
        assertEquals("Updated", result.getFirstName());

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(profileHistoryRepository).save(any(ProfileHistory.class));
    }

    @Test
    void updateUserProfile_UserNotFound() {
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
    void updateUserProfile_ProfileNotFound_CreatesNew() {
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
    void uploadProfilePicture_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserProfile));
        when(fileStorageService.uploadFile(any(), anyString())).thenReturn("https://example.com/profile.jpg");
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // Act
        String result = profileService.uploadProfilePicture(testUserId, file);

        // Assert
        assertNotNull(result);
        assertEquals("https://example.com/profile.jpg", result);

        verify(userRepository).findById(testUserId);
        verify(userProfileRepository).findByUserId(testUserId);
        verify(fileStorageService).uploadFile(any(), eq("profile-pictures"));
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void uploadProfilePicture_UserNotFound() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.uploadProfilePicture(testUserId, file));
        assertEquals("User not found: " + testUserId, exception.getMessage());

        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(userProfileRepository, fileStorageService);
    }

    @Test
    void uploadProfilePicture_InvalidFileType() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.txt", 
                "text/plain", 
                "test content".getBytes()
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.uploadProfilePicture(testUserId, file));
        assertEquals("Invalid file type. Only JPG, PNG, and GIF are allowed.", exception.getMessage());

        verify(userRepository).findById(testUserId);
        verifyNoMoreInteractions(userProfileRepository, fileStorageService);
    }
} 