package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search metadata and performance information")
public class SearchMetadata {
    
    @Schema(description = "Search execution time in milliseconds", example = "45")
    private Long searchTime;
    
    @Schema(description = "Was result served from cache", example = "false")
    private Boolean cacheHit;
    
    @Schema(description = "Ranking strategy used", example = "blended-v2")
    private String rankingStrategy;
    
    @Schema(description = "Query type (FTS, fuzzy, hybrid, etc.)", example = "hybrid-fts-fuzzy")
    private String queryType;
}

