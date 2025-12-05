package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Image DTO with multiple sizes for optimized loading
 * Images are stored in S3 and served via CloudFront CDN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Image with multiple sizes for different display contexts")
public class ImageDto {
    
    @Schema(description = "Image type", example = "cover", allowableValues = {"cover", "logo", "gallery", "thumbnail"})
    private String type;
    
    @Schema(description = "Map of image URLs by size (original, large, medium, small, thumbnail)")
    private Map<String, String> urls;
    
    @Schema(description = "Image dimensions", example = "{\"width\": 1920, \"height\": 1080}")
    private Map<String, Integer> dimensions;
    
    @Schema(description = "Display order for multiple images", example = "1")
    private Integer displayOrder;
}

