package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Metadata for discovery feed response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Discovery feed metadata")
public class FeedMetadata {
    
    @Schema(description = "Total number of vendors in feed", example = "45")
    private Integer totalVendors;
    
    @Schema(description = "Cache expiration time", example = "2024-11-26T10:15:00Z")
    private Instant cacheUntil;
    
    @Schema(description = "Was this response served from cache", example = "true")
    private Boolean cacheHit;
    
    @Schema(description = "Ranking version used", example = "v2-blended")
    private String rankingVersion;
}

