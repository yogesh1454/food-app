package com.teadelivery.ordercatalog.search.service;

import com.teadelivery.ordercatalog.search.dto.SearchRequest;
import com.teadelivery.ordercatalog.search.dto.SearchResponse;

/**
 * Unified Search Service - Full-text + fuzzy + geospatial search
 * with blended ranking
 */
public interface UnifiedSearchService {
    
    /**
     * Execute unified search with hybrid FTS + Fuzzy matching
     * and blended ranking
     * 
     * @param request Search request with query, filters, location
     * @return Search response with vendors and items
     */
    SearchResponse search(SearchRequest request);
}

