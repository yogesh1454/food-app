package com.teadelivery.user.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for user profile update requests.
 * Follows coding standards with comprehensive validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile update request")
public class ProfileUpdateRequest {

    @Schema(description = "User first name", example = "John")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;
    
    @Schema(description = "User last name", example = "Doe")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;
    
    @Schema(description = "User email", example = "john@example.com")
    @Email(message = "Invalid email format")
    private String email;
    
    @Schema(description = "User phone number", example = "+1234567890")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Schema(description = "Date of birth", example = "1990-01-01")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Gender", example = "MALE")
    private String gender;
    
    @Schema(description = "User bio", example = "Tea enthusiast and food lover")
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @Schema(description = "List of user addresses")
    private List<AddressUpdateDto> addresses;
    
    @Schema(description = "Company details for B2B users")
    private CompanyDetailsUpdateDto companyDetails;
    
    @Schema(description = "Business details for vendors")
    private BusinessDetailsUpdateDto businessDetails;
    
    @Schema(description = "Vehicle details for delivery partners")
    private VehicleDetailsUpdateDto vehicleDetails;
    
    @Schema(description = "Preferred language", example = "en")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    private String preferredLanguage;
    
    @Schema(description = "Timezone", example = "UTC")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
    
    @Schema(description = "Notification preferences")
    private String notificationPreferences;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Address update information")
    public static class AddressUpdateDto {
        @Schema(description = "Address type", example = "HOME")
        private String type;
        
        @Schema(description = "Street address", example = "123 Main St")
        @Size(min = 1, max = 255, message = "Street address must be between 1 and 255 characters")
        private String street;
        
        @Schema(description = "City", example = "New York")
        @Size(min = 1, max = 100, message = "City must be between 1 and 100 characters")
        private String city;
        
        @Schema(description = "State", example = "NY")
        @Size(min = 1, max = 100, message = "State must be between 1 and 100 characters")
        private String state;
        
        @Schema(description = "Postal code", example = "10001")
        @Size(min = 1, max = 20, message = "Postal code must be between 1 and 20 characters")
        private String postalCode;
        
        @Schema(description = "Country", example = "USA")
        @Size(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
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
    @Schema(description = "Company details update for B2B users")
    public static class CompanyDetailsUpdateDto {
        @Schema(description = "Company name", example = "Acme Corp")
        @Size(min = 1, max = 100, message = "Company name must be between 1 and 100 characters")
        private String companyName;
        
        @Schema(description = "Internal delivery point", example = "Reception")
        @Size(max = 100, message = "Internal delivery point must not exceed 100 characters")
        private String internalDeliveryPoint;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Business details update for vendors")
    public static class BusinessDetailsUpdateDto {
        @Schema(description = "Business name", example = "Tea & Snacks Corner")
        @Size(min = 1, max = 100, message = "Business name must be between 1 and 100 characters")
        private String businessName;
        
        @Schema(description = "Business type", example = "RESTAURANT")
        @Size(min = 1, max = 100, message = "Business type must be between 1 and 100 characters")
        private String businessType;
        
        @Schema(description = "Business registration number", example = "REG123456")
        @Size(max = 100, message = "Business registration number must not exceed 100 characters")
        private String businessRegistrationNumber;
        
        @Schema(description = "GST number", example = "GST123456789")
        @Size(max = 20, message = "GST number must not exceed 20 characters")
        private String gstNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Vehicle details update for delivery partners")
    public static class VehicleDetailsUpdateDto {
        @Schema(description = "Vehicle type", example = "MOTORCYCLE")
        @Size(min = 1, max = 50, message = "Vehicle type must be between 1 and 50 characters")
        private String vehicleType;
        
        @Schema(description = "Vehicle number", example = "DL01AB1234")
        @Size(min = 1, max = 20, message = "Vehicle number must be between 1 and 20 characters")
        private String vehicleNumber;
    }
} 