package com.teadelivery.ordercatalog.common.controller;

import com.teadelivery.ordercatalog.common.dto.ImageProcessingCallback;
import com.teadelivery.ordercatalog.common.service.ImageProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for internal callbacks from AWS Lambda.
 * These endpoints are called by the image processing Lambda function
 * to notify the backend when image processing is complete.
 */
@RestController
@RequestMapping("/api/internal/callbacks")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Internal Callbacks", description = "Internal endpoints for Lambda callbacks")
public class ImageCallbackController {

    private final ImageProcessingService imageProcessingService;

    @Operation(summary = "Image processing complete callback", description = """
            Called by Lambda after image variants are generated.
            Updates the database with final CDN URLs and triggers search sync.

            **Security Note:** This endpoint should be secured in production
            (e.g., via API Gateway authentication, Lambda function URL auth,
            or internal VPC access only).
            """)
    @ApiResponse(responseCode = "200", description = "Callback processed successfully")
    @ApiResponse(responseCode = "500", description = "Error processing callback")
    @PostMapping("/image-processing")
    public ResponseEntity<Map<String, Object>> handleImageProcessingCallback(
            @RequestBody ImageProcessingCallback callback,
            @RequestHeader(value = "X-Lambda-Source", required = false) String lambdaSource) {

        log.info("Received image processing callback from: {}, entityType={}, entityId={}",
                lambdaSource, callback.getEntityType(), callback.getEntityId());

        try {
            imageProcessingService.handleProcessingComplete(callback);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Callback processed successfully",
                    "entityType", callback.getEntityType(),
                    "entityId", callback.getEntityId(),
                    "imageType", callback.getImageType()));

        } catch (Exception e) {
            log.error("Error processing image callback: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "entityType", callback.getEntityType() != null ? callback.getEntityType() : "unknown",
                    "entityId", callback.getEntityId() != null ? callback.getEntityId() : "unknown"));
        }
    }

    @Operation(summary = "Health check for callback endpoint")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "image-callback"));
    }
}
