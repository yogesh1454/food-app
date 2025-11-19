package com.teadelivery.ordercatalog.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validation error response structure for validation failures.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Validation error response with field-level errors")
public class ValidationErrorResponse {
    
    @Schema(description = "Timestamp when the error occurred", example = "2024-11-08T12:30:45")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "400")
    private Integer status;
    
    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private String error;
    
    @Schema(description = "General error message", example = "Validation failed")
    private String message;
    
    @Schema(description = "API endpoint path where error occurred", example = "/api/v1/vendors")
    private String path;
    
    @Schema(description = "Map of field names to validation error messages", 
            example = "{\"companyEmail\": \"must be a well-formed email address\", \"companyName\": \"must not be blank\"}")
    private Map<String, String> validationErrors;
}
