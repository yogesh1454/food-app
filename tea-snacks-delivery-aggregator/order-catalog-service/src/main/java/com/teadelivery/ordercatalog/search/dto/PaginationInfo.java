package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination information")
public class PaginationInfo {
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private Integer currentPage;
    
    @Schema(description = "Total number of results", example = "87")
    private Long totalResults;
    
    @Schema(description = "Has more results", example = "true")
    private Boolean hasMore;
    
    @Schema(description = "Page size", example = "20")
    private Integer pageSize;
}

