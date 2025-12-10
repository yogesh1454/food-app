package com.teadelivery.ordercatalog.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure for all API errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard error response")
public class ErrorResponse {
    
    @Schema(description = "Timestamp when the error occurred", example = "2024-11-08T12:30:45")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "404")
    private Integer status;
    
    @Schema(description = "HTTP status reason phrase", example = "Not Found")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Vendor not found")
    private String message;
    
    @Schema(description = "API endpoint path where error occurred", example = "/api/v1/vendors/123")
    private String path;
}
