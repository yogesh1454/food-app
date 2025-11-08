package com.teadelivery.user.profile.dto;

import com.teadelivery.user.profile.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user profile response.
 * Follows coding standards with comprehensive profile data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile response")
public class ProfileResponse {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "User email", example = "john@example.com")
    private String email;
    
    @Schema(description = "User phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "User first name", example = "John")
    private String firstName;
    
    @Schema(description = "User last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "User full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Profile picture URL", example = "https://example.com/avatar.jpg")
    private String profilePictureUrl;
    
    @Schema(description = "Date of birth", example = "1990-01-01")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Gender", example = "MALE")
    private String gender;
    
    @Schema(description = "User bio", example = "Tea enthusiast and food lover")
    private String bio;
    
    @Schema(description = "Profile completion percentage", example = "75")
    private Integer profileCompletionPercentage;
    
    @Schema(description = "User role", example = "CUSTOMER")
    private String role;
    
    @Schema(description = "User type", example = "REGISTERED")
    private String userType;
    
    @Schema(description = "Email verification status", example = "true")
    private Boolean emailVerified;
    
    @Schema(description = "Phone verification status", example = "true")
    private Boolean phoneVerified;
    
    @Schema(description = "List of user addresses")
    private List<AddressDto> addresses;
    
    @Schema(description = "Company details for B2B users")
    private CompanyDetailsDto companyDetails;
    
    @Schema(description = "Business details for vendors")
    private BusinessDetailsDto businessDetails;
    
    @Schema(description = "Vehicle details for delivery partners")
    private VehicleDetailsDto vehicleDetails;
    
    @Schema(description = "Preferred language", example = "en")
    private String preferredLanguage;
    
    @Schema(description = "Timezone", example = "UTC")
    private String timezone;
    
    @Schema(description = "Notification preferences")
    private String notificationPreferences;
    
    @Schema(description = "Profile creation date")
    private String createdAt;
    
    @Schema(description = "Profile last update date")
    private String updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Address information")
    public static class AddressDto {
        @Schema(description = "Address ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID id;
        
        @Schema(description = "Address type", example = "HOME")
        private String type;
        
        @Schema(description = "Street address", example = "123 Main St")
        private String street;
        
        @Schema(description = "City", example = "New York")
        private String city;
        
        @Schema(description = "State", example = "NY")
        private String state;
        
        @Schema(description = "Postal code", example = "10001")
        private String postalCode;
        
        @Schema(description = "Country", example = "USA")
        private String country;
        
        @Schema(description = "Latitude", example = "40.7128")
        private Double latitude;
        
        @Schema(description = "Longitude", example = "-74.0060")
        private Double longitude;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Company details for B2B users")
    public static class CompanyDetailsDto {
        @Schema(description = "Company name", example = "Acme Corp")
        private String companyName;
        
        @Schema(description = "Internal delivery point", example = "Reception")
        private String internalDeliveryPoint;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Business details for vendors")
    public static class BusinessDetailsDto {
        @Schema(description = "Business name", example = "Tea & Snacks Corner")
        private String businessName;
        
        @Schema(description = "Business type", example = "RESTAURANT")
        private String businessType;
        
        @Schema(description = "Business registration number", example = "REG123456")
        private String businessRegistrationNumber;
        
        @Schema(description = "GST number", example = "GST123456789")
        private String gstNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Vehicle details for delivery partners")
    public static class VehicleDetailsDto {
        @Schema(description = "Vehicle type", example = "MOTORCYCLE")
        private String vehicleType;
        
        @Schema(description = "Vehicle number", example = "DL01AB1234")
        private String vehicleNumber;
    }
} 