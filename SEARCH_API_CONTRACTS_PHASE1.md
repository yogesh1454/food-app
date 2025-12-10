# Search & Discovery API Contracts - Phase 1

## 🎉 Status: READY FOR FRONTEND INTEGRATION

All API contracts have been implemented with placeholder responses. Frontend team can start integration immediately.

---

## 📋 API Overview

We have implemented **4 mobile-optimized REST APIs** for Search & Discovery:

1. **Discovery Feed API** - Homepage feed with 4 sections
2. **Unified Search API** - Search vendors and items
3. **Vendor Menu API** - Complete menu with recommendations
4. **Recommendations API** - Personalized recommendations

All APIs include:
- ✅ Complete request/response DTOs
- ✅ OpenAPI/Swagger documentation
- ✅ Input validation
- ✅ Cache-Control headers
- ✅ Placeholder implementations (mock responses)

---

## 🔗 API Endpoints

### Base URL
```
http://localhost:8081/api/v1/search
```

### 1. Discovery Feed API

**Endpoint:** `GET /api/v1/search/feed`

**Description:** Homepage discovery feed with 4 sections:
- Nearby Vendors (location-based, blended ranking)
- Popular Items (last 30 days in area)
- Recommended Items (personalized)
- Top Ordered Items (trending, last 7 days)

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| latitude | Double | Yes | - | User latitude (-90 to 90) |
| longitude | Double | Yes | - | User longitude (-180 to 180) |
| radius | Integer | No | 5 | Search radius in km (1-20) |
| userId | UUID | No | - | User ID for personalization |
| page | Integer | No | 0 | Page number (0-based) |
| size | Integer | No | 20 | Page size (1-100) |

**Example Request:**
```bash
curl -X GET "http://localhost:8081/api/v1/search/feed?latitude=12.9716&longitude=77.5946&radius=5&userId=123e4567-e89b-12d3-a456-426614174000&page=0&size=20"
```

**Response:** `DiscoveryFeedResponse`
```json
{
  "nearbyVendors": [...],
  "popularItems": [...],
  "recommendedItems": [...],
  "topOrderedItems": [...],
  "searchSuggestions": ["Masala Chai", "Samosa", "Filter Coffee"],
  "metadata": {
    "totalVendors": 45,
    "cacheHit": true,
    "rankingVersion": "v2-blended"
  }
}
```

**Cache-Control:** `max-age=600, stale-while-revalidate=1800` (10 minutes)

---

### 2. Unified Search API

**Endpoint:** `GET /api/v1/search`

**Description:** Hybrid search with full-text search, fuzzy matching, and blended ranking

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| q | String | Yes | - | Search query |
| type | String | No | "all" | Search type: all, vendors, items |
| latitude | Double | Yes | - | User latitude |
| longitude | Double | Yes | - | User longitude |
| page | Integer | No | 0 | Page number (0-based) |
| size | Integer | No | 20 | Page size (1-100) |
| city | String | No | - | City filter |
| radiusKm | Integer | No | 5 | Search radius in km (1-20) |

**Example Request:**
```bash
curl -X GET "http://localhost:8081/api/v1/search?q=chai&type=all&latitude=12.9716&longitude=77.5946&page=0&size=20"
```

**Response:** `SearchResponse`
```json
{
  "query": "chai",
  "type": "all",
  "results": {
    "vendors": [...],
    "items": [...]
  },
  "suggestions": ["chai latte", "masala chai"],
  "pagination": {
    "currentPage": 0,
    "totalResults": 87,
    "hasMore": true,
    "pageSize": 20
  },
  "metadata": {
    "searchTime": 45,
    "cacheHit": false,
    "rankingStrategy": "blended-v2",
    "queryType": "hybrid-fts-fuzzy"
  }
}
```

---

### 3. Vendor Menu API

**Endpoint:** `GET /api/v1/search/vendors/{branchId}/menu`

**Description:** Complete vendor menu with categories, items, and recommendations

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| branchId | Long | Yes | Branch ID |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | No | User ID for personalization |
| latitude | Double | No | User latitude |
| longitude | Double | No | User longitude |

**Example Request:**
```bash
curl -X GET "http://localhost:8081/api/v1/search/vendors/101/menu?userId=123e4567-e89b-12d3-a456-426614174000&latitude=12.9716&longitude=77.5946"
```

**Response:** `VendorMenuResponse`
```json
{
  "vendor": {
    "branchId": 101,
    "branchName": "Chai Express - MG Road",
    ...
  },
  "categories": [
    {
      "categoryName": "Beverages",
      "displayOrder": 1,
      "items": [...]
    }
  ],
  "recommendations": [...],
  "popularItems": [...]
}
```

**Cache-Control:** `max-age=300` (5 minutes)

---

### 4. Recommendations API

**Endpoint:** `GET /api/v1/search/recommendations`

**Description:** Personalized vendor and item recommendations

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| userId | UUID | Yes | - | User ID |
| latitude | Double | Yes | - | User latitude |
| longitude | Double | Yes | - | User longitude |
| radiusKm | Integer | No | 5 | Search radius in km (1-20) |

**Example Request:**
```bash
curl -X GET "http://localhost:8081/api/v1/search/recommendations?userId=123e4567-e89b-12d3-a456-426614174000&latitude=12.9716&longitude=77.5946&radiusKm=5"
```

**Response:** `RecommendationResponse`
```json
{
  "recommendedVendors": [...],
  "recommendedItems": [...],
  "frequentlyOrdered": [...],
  "timeBasedRecommendations": [...],
  "recommendationContext": "Based on your order history and time of day"
}
```

**Cache-Control:** `max-age=900, private` (15 minutes, personalized)

---

## 📦 Response DTOs

### Common DTOs

#### ImagesResponse
```json
{
  "primary": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
  "cover": {
    "thumbnail": "https://cdn.foodapp.com/vendors/101/cover_thumbnail.jpg",
    "small": "https://cdn.foodapp.com/vendors/101/cover_small.jpg",
    "medium": "https://cdn.foodapp.com/vendors/101/cover_medium.jpg"
  },
  "logo": {
    "small": "https://cdn.foodapp.com/vendors/101/logo_small.png"
  }
}
```

#### RankingScores (for debugging)
```json
{
  "total": 0.89,
  "fts": 0.95,
  "fuzzy": 0.85,
  "popularity": 0.78,
  "proximity": 0.92
}
```

#### NutritionInfo
```json
{
  "calories": 120,
  "protein": 3.5,
  "carbs": 25.0,
  "fat": 2.5,
  "servingSize": "200ml"
}
```

### VendorSearchResult
Complete vendor details with images, rating, delivery info, and ranking scores.

### MenuItemSearchResult
Complete menu item details with images, nutrition, dietary info, and ranking scores.

---

## 🚀 Getting Started

### 1. Start the Application
```bash
cd tea-snacks-delivery-aggregator/order-catalog-service
./gradlew bootRun
```

### 2. Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

### 3. Test Endpoints
All endpoints are documented in Swagger with example requests and responses.

### 4. Mock Responses
Currently, all endpoints return empty lists/placeholder data. Phase 2 will implement actual search logic.

---

## 📱 Mobile Integration Notes

### Image Loading Strategy
1. **List/Grid Views**: Use `images.primary` or `images.cover.thumbnail` (~10KB)
2. **Detail Screens**: Use `images.cover.medium` (~80KB) with progressive loading
3. **Full Screen**: Use `images.cover.original` (~500KB) on demand

### Caching
- Discovery Feed: Cache for 10 minutes
- Search Results: Don't cache (user-specific)
- Vendor Menu: Cache for 5 minutes
- Recommendations: Cache for 15 minutes (private)

### Pagination
Use `pagination.hasMore` to determine if more results are available. Increment `page` parameter for next page.

### Error Handling
All APIs return standard HTTP status codes:
- 200: Success
- 400: Bad Request (invalid parameters)
- 401: Unauthorized (missing user ID where required)
- 404: Not Found (vendor doesn't exist)
- 500: Internal Server Error

---

## 🔧 Development Notes

### Current Status (Phase 1)
- ✅ All DTOs created
- ✅ All REST endpoints implemented
- ✅ OpenAPI documentation complete
- ✅ Input validation added
- ✅ Cache headers configured
- ⏳ Actual search logic (Phase 2)
- ⏳ Database integration (Phase 2)
- ⏳ AWS SNS/SQS integration (Phase 2)

### What Frontend Can Do Now
1. ✅ Call all 4 APIs and get mock responses
2. ✅ Test request/response structure
3. ✅ Implement UI components based on response DTOs
4. ✅ Test pagination logic
5. ✅ Implement image loading strategy
6. ✅ Test error handling

### What's Coming in Phase 2
1. Actual database queries (PostGIS, FTS, fuzzy search)
2. Blended ranking algorithm implementation
3. AWS SNS/SQS event-driven sync
4. Redis caching with tile-based strategy
5. Real vendor/item data
6. Search analytics tracking

---

## 📝 File Locations

### DTOs
```
src/main/java/com/teadelivery/ordercatalog/search/dto/
├── ImageDto.java
├── ImagesResponse.java
├── RankingScores.java
├── NutritionInfo.java
├── VendorSearchResult.java
├── MenuItemSearchResult.java
├── DiscoveryFeedResponse.java
├── FeedMetadata.java
├── SearchRequest.java
├── SearchResponse.java
├── SearchResults.java
├── PaginationInfo.java
├── SearchMetadata.java
├── VendorMenuResponse.java
├── MenuCategoryDto.java
└── RecommendationResponse.java
```

### Controller
```
src/main/java/com/teadelivery/ordercatalog/search/controller/
└── SearchController.java
```

### Service Interfaces
```
src/main/java/com/teadelivery/ordercatalog/search/service/
├── DiscoveryFeedService.java
├── UnifiedSearchService.java
├── VendorMenuService.java
└── RecommendationService.java
```

---

## 🤝 Collaboration

### Frontend Team Next Steps
1. Review this document
2. Access Swagger UI for interactive API testing
3. Start implementing UI components
4. Share feedback on API structure
5. Request any additional fields/endpoints needed

### Questions or Issues?
- Check Swagger UI for detailed API documentation
- Review DTO classes for complete field definitions
- Raise any concerns or suggestions

---

**Phase 1 Complete! 🎉**

Frontend team can now start parallel development while we implement Phase 2 (actual search logic, database integration, and AWS services).

