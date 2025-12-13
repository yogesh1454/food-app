package com.teadelivery.ordercatalog.common.dto;

import lombok.Data;
import java.util.Map;

/**
 * DTO for image processing results received from Lambda via SQS
 */
@Data
public class ImageProcessingResult {
    private boolean success;
    private String entityType;
    private String entityId;
    private String imageType;
    private String originalKey;
    private Map<String, ImageVariant> variants;
    private String processedAt;

    @Data
    public static class ImageVariant {
        private String webp;
        private String jpeg;
    }
}
