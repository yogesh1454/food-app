package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Structured image response with quick access to primary image
 * and size-specific URLs for cover, logo, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structured image response with primary image and size variants")
public class ImagesResponse {
    
    @Schema(description = "Primary image URL (typically thumbnail)", 
            example = "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg")
    private String primary;
    
    @Schema(description = "Cover image URLs by size")
    private Map<String, String> cover;
    
    @Schema(description = "Logo image URLs by size")
    private Map<String, String> logo;
    
    @Schema(description = "Gallery image URLs by size (for multiple images)")
    private Map<String, String> gallery;
}

