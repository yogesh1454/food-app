package com.teadelivery.ordercatalog.vendor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for image upload operations.
 * Based on IMAGE_STORAGE_AND_RENDERING_SPECIFICATION - returns 202 Accepted
 * for async processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageUploadResponse {

    /**
     * Status of the upload operation.
     * ACCEPTED - Image uploaded, processing initiated
     * READY - Image processed and available
     * FAILED - Processing failed
     */
    private String status;

    /**
     * Human-readable message about the upload status.
     */
    private String message;

    /**
     * Entity ID (vendor, branch, or menu item)
     */
    private Long entityId;

    /**
     * Type of entity (vendor, branch, menu-item)
     */
    private String entityType;

    /**
     * Type of image uploaded (logo, cover, storefront, primary, etc.)
     */
    private String imageType;

    /**
     * S3 key where the original file is stored.
     */
    private String fileKey;

    /**
     * CDN URLs for different image sizes (available after processing).
     * Keys: thumbnail, small, medium, large
     */
    private Map<String, String> urls;

    /**
     * Processing status details.
     */
    private ProcessingStatus processing;

    /**
     * Timestamp when the image was uploaded.
     */
    private Instant uploadedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingStatus {
        private String status; // PENDING, PROCESSING, COMPLETED, FAILED
        private String message;
        private Instant startedAt;
        private Instant completedAt;
    }

    // ==================== Factory Methods ====================

    /**
     * Create response for successful upload (async processing initiated).
     */
    public static ImageUploadResponse accepted(Long entityId, String entityType,
            String imageType, String fileKey) {
        return ImageUploadResponse.builder()
                .status("ACCEPTED")
                .message("Image uploaded successfully. Processing initiated asynchronously.")
                .entityId(entityId)
                .entityType(entityType)
                .imageType(imageType)
                .fileKey(fileKey)
                .uploadedAt(Instant.now())
                .processing(ProcessingStatus.builder()
                        .status("PENDING")
                        .message("Image is queued for processing")
                        .startedAt(Instant.now())
                        .build())
                .build();
    }

    /**
     * Create response when image processing is complete.
     */
    public static ImageUploadResponse ready(Long entityId, String entityType,
            String imageType, Map<String, String> urls) {
        return ImageUploadResponse.builder()
                .status("READY")
                .message("Image processed and available.")
                .entityId(entityId)
                .entityType(entityType)
                .imageType(imageType)
                .urls(urls)
                .processing(ProcessingStatus.builder()
                        .status("COMPLETED")
                        .message("All image variants generated")
                        .completedAt(Instant.now())
                        .build())
                .build();
    }

    /**
     * Create response for failed upload/processing.
     */
    public static ImageUploadResponse failed(Long entityId, String entityType,
            String imageType, String errorMessage) {
        return ImageUploadResponse.builder()
                .status("FAILED")
                .message(errorMessage)
                .entityId(entityId)
                .entityType(entityType)
                .imageType(imageType)
                .processing(ProcessingStatus.builder()
                        .status("FAILED")
                        .message(errorMessage)
                        .build())
                .build();
    }
}
