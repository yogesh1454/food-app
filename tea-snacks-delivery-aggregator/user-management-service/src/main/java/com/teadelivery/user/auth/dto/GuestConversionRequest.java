package com.teadelivery.user.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for guest to user conversion requests.
 * Follows coding standards with comprehensive conversion data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestConversionRequest {

    private String guestUserId;
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
} 