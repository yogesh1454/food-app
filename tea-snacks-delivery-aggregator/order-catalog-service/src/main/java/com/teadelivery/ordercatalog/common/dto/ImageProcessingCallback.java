package com.teadelivery.ordercatalog.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for Lambda image processing callback.
 * Received when Lambda completes processing an image.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageProcessingCallback {

    /**
     * Whether processing succeeded
     */
    private boolean success;

    /**
     * Entity type: vendors, branches, menu-items
     */
    private String entityType;

    /**
     * Entity ID
     */
    private String entityId;

    /**
     * Image type: logo, cover, storefront, primary, etc.
     */
    private String imageType;

    /**
     * Original S3 key
     */
    private String originalKey;

    /**
     * Generated variants with CDN URLs
     * Map structure: { "thumbnail": { "webp": "url", "jpeg": "url" }, ... }
     */
    private Map<String, VariantUrls> variants;

    /**
     * Timestamp when processing completed
     */
    private Instant processedAt;

    /**
     * Error message if processing failed
     */
    private String error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantUrls {
        private String webp;
        private String jpeg;
    }
}
