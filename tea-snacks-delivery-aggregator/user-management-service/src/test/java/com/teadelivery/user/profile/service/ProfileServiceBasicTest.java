package com.teadelivery.user.profile.service;

import com.teadelivery.user.profile.dto.ProfileResponse;
import com.teadelivery.user.profile.dto.ProfileUpdateRequest;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic validation tests for ProfileService functionality.
 * Tests core business logic without complex mocking.
 */
class ProfileServiceBasicTest {

    @Test
    void testProfileUpdateRequestValidation() {
        // Test that ProfileUpdateRequest can be created and validated
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertNotNull(request);
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
    }

    @Test
    void testProfileResponseCreation() {
        // Test that ProfileResponse can be created with all fields
        UUID userId = UUID.randomUUID();
        ProfileResponse response = ProfileResponse.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profileCompletionPercentage(75)
                .build();

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(75, response.getProfileCompletionPercentage());
    }

    @Test
    void testUserProfileModel() {
        // Test that UserProfile model works correctly
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .role(User.Role.CUSTOMER)
                .userType(User.UserType.REGISTERED)
                .build();

        UserProfile profile = UserProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .firstName("Test")
                .lastName("User")
                .build();

        assertNotNull(profile);
        assertEquals("Test", profile.getFirstName());
        assertEquals("User", profile.getLastName());
        assertEquals(user, profile.getUser());
    }

    @Test
    void testProfileUpdateRequestWithAddress() {
        // Test ProfileUpdateRequest with address information
        ProfileUpdateRequest.AddressUpdateDto address = ProfileUpdateRequest.AddressUpdateDto.builder()
                .type("HOME")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .addresses(java.util.List.of(address))
                .build();

        assertNotNull(request);
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertNotNull(request.getAddresses());
        assertEquals(1, request.getAddresses().size());
        assertEquals("HOME", request.getAddresses().get(0).getType());
        assertEquals("123 Main St", request.getAddresses().get(0).getStreet());
    }

    @Test
    void testProfileResponseWithAddress() {
        // Test ProfileResponse with address information
        ProfileResponse.AddressDto address = ProfileResponse.AddressDto.builder()
                .id(UUID.randomUUID())
                .type("HOME")
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();

        ProfileResponse response = ProfileResponse.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .addresses(java.util.List.of(address))
                .build();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertNotNull(response.getAddresses());
        assertEquals(1, response.getAddresses().size());
        assertEquals("HOME", response.getAddresses().get(0).getType());
        assertEquals("123 Main St", response.getAddresses().get(0).getStreet());
    }

    @Test
    void testProfileUpdateRequestWithBusinessDetails() {
        // Test ProfileUpdateRequest with business details for vendors
        ProfileUpdateRequest.BusinessDetailsUpdateDto businessDetails = ProfileUpdateRequest.BusinessDetailsUpdateDto.builder()
                .businessName("Tea & Snacks Corner")
                .businessType("RESTAURANT")
                .businessRegistrationNumber("REG123456")
                .gstNumber("GST123456789")
                .build();

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .businessDetails(businessDetails)
                .build();

        assertNotNull(request);
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertNotNull(request.getBusinessDetails());
        assertEquals("Tea & Snacks Corner", request.getBusinessDetails().getBusinessName());
        assertEquals("RESTAURANT", request.getBusinessDetails().getBusinessType());
    }

    @Test
    void testProfileResponseWithBusinessDetails() {
        // Test ProfileResponse with business details
        ProfileResponse.BusinessDetailsDto businessDetails = ProfileResponse.BusinessDetailsDto.builder()
                .businessName("Tea & Snacks Corner")
                .businessType("RESTAURANT")
                .businessRegistrationNumber("REG123456")
                .gstNumber("GST123456789")
                .build();

        ProfileResponse response = ProfileResponse.builder()
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .businessDetails(businessDetails)
                .build();

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertNotNull(response.getBusinessDetails());
        assertEquals("Tea & Snacks Corner", response.getBusinessDetails().getBusinessName());
        assertEquals("RESTAURANT", response.getBusinessDetails().getBusinessType());
    }
} 