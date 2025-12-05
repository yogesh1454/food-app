package com.teadelivery.ordercatalog.search.service.impl;

import com.teadelivery.ordercatalog.search.dto.*;
import com.teadelivery.ordercatalog.search.mapper.SearchMapper;
import com.teadelivery.ordercatalog.search.model.SearchMenuItem;
import com.teadelivery.ordercatalog.search.model.SearchVendor;
import com.teadelivery.ordercatalog.search.repository.SearchMenuItemRepository;
import com.teadelivery.ordercatalog.search.repository.SearchVendorRepository;
import com.teadelivery.ordercatalog.search.service.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Unified Search Service Implementation
 * 
 * Coordinates hybrid search (FTS + Fuzzy) with blended ranking
 * across vendors and menu items
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedSearchServiceImpl implements UnifiedSearchService {

        private final SearchVendorRepository vendorRepository;
        private final SearchMenuItemRepository menuItemRepository;
        private final SearchMapper mapper;

        @Value("${search.geospatial.default-radius-km:5}")
        private Integer defaultRadiusKm;

        @Value("${features.search.bypass-geospatial:false}")
        private Boolean bypassGeospatial;

        @Override
        public SearchResponse search(SearchRequest request) {
                log.info("Executing unified search: query='{}', type='{}', location=({}, {}), bypassGeo={}",
                                request.getQuery(), request.getType(), request.getLatitude(), request.getLongitude(),
                                bypassGeospatial);

                long startTime = System.currentTimeMillis();

                // Validate request
                if (request.getQuery() == null || request.getQuery().isBlank()) {
                        return buildEmptyResponse(request);
                }

                // Calculate radius in meters - use very large radius if bypassing geospatial
                int radiusMeters = Boolean.TRUE.equals(bypassGeospatial)
                                ? 10_000_000 // ~10,000km - effectively worldwide
                                : (request.getRadiusKm() != null ? request.getRadiusKm() : defaultRadiusKm) * 1000;
                int limit = request.getSize() != null ? request.getSize() : 20;

                // Execute searches based on type
                List<SearchVendor> vendors = Collections.emptyList();
                List<SearchMenuItem> items = Collections.emptyList();

                String searchType = request.getType() != null ? request.getType() : "all";

                if ("all".equalsIgnoreCase(searchType) || "vendors".equalsIgnoreCase(searchType)) {
                        vendors = vendorRepository.hybridSearch(
                                        request.getQuery(),
                                        request.getLatitude(),
                                        request.getLongitude(),
                                        radiusMeters,
                                        request.getCity(),
                                        limit);
                }

                if ("all".equalsIgnoreCase(searchType) || "items".equalsIgnoreCase(searchType)) {
                        items = menuItemRepository.hybridSearch(
                                        request.getQuery(),
                                        request.getLatitude(),
                                        request.getLongitude(),
                                        radiusMeters,
                                        limit);
                }

                // Convert to DTOs
                List<VendorSearchResult> vendorResults = mapper.toVendorSearchResults(vendors);
                List<MenuItemSearchResult> itemResults = mapper.toMenuItemSearchResults(items);

                // Build results
                SearchResults results = SearchResults.builder()
                                .vendors(vendorResults)
                                .items(itemResults)
                                .build();

                // Calculate search time
                long searchTime = System.currentTimeMillis() - startTime;

                // Build pagination info
                PaginationInfo pagination = PaginationInfo.builder()
                                .currentPage(request.getPage() != null ? request.getPage() : 0)
                                .totalResults((long) (vendors.size() + items.size()))
                                .hasMore(false) // TODO: Implement proper pagination
                                .pageSize(limit)
                                .build();

                // Build metadata
                SearchMetadata metadata = SearchMetadata.builder()
                                .searchTime(searchTime)
                                .cacheHit(false)
                                .rankingStrategy("blended-v2")
                                .queryType("hybrid-fts-fuzzy")
                                .build();

                // TODO: Get actual suggestions from popular queries table
                List<String> suggestions = generateSuggestions(request.getQuery());

                return SearchResponse.builder()
                                .query(request.getQuery())
                                .type(searchType)
                                .results(results)
                                .suggestions(suggestions)
                                .pagination(pagination)
                                .metadata(metadata)
                                .build();
        }

        /**
         * Build empty response for invalid queries
         */
        private SearchResponse buildEmptyResponse(SearchRequest request) {
                return SearchResponse.builder()
                                .query(request.getQuery())
                                .type(request.getType() != null ? request.getType() : "all")
                                .results(SearchResults.builder()
                                                .vendors(Collections.emptyList())
                                                .items(Collections.emptyList())
                                                .build())
                                .suggestions(Collections.emptyList())
                                .pagination(PaginationInfo.builder()
                                                .currentPage(0)
                                                .totalResults(0L)
                                                .hasMore(false)
                                                .pageSize(20)
                                                .build())
                                .metadata(SearchMetadata.builder()
                                                .searchTime(0L)
                                                .cacheHit(false)
                                                .rankingStrategy("blended-v2")
                                                .queryType("none")
                                                .build())
                                .build();
        }

        /**
         * Generate search suggestions (placeholder)
         * TODO: Query popular queries table for actual suggestions
         */
        private List<String> generateSuggestions(String query) {
                // Placeholder implementation
                if (query.toLowerCase().contains("chai")) {
                        return List.of("masala chai", "chai latte", "filter coffee");
                } else if (query.toLowerCase().contains("samosa")) {
                        return List.of("samosa", "vada pav", "pakora");
                }
                return Collections.emptyList();
        }
}
