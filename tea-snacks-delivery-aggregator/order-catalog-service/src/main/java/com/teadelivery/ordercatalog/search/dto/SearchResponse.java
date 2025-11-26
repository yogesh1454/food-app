package com.teadelivery.ordercatalog.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified search response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Unified search response with vendors, items, and metadata")
public class SearchResponse {
    
    @Schema(description = "Original search query", example = "chai")
    private String query;
    
    @Schema(description = "Search type", example = "all")
    private String type;
    
    @Schema(description = "Search results")
    private SearchResults results;
    
    @Schema(description = "Search suggestions", example = "[\"chai latte\", \"masala chai\"]")
    private List<String> suggestions;
    
    @Schema(description = "Pagination information")
    private PaginationInfo pagination;
    
    @Schema(description = "Search metadata")
    private SearchMetadata metadata;
}

