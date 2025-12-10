# Preference API Design Analysis

## Question: Should we create different endpoints for each preference category?

## Answer: NO - Unified approach is better

## Analysis

### ❌ Problems with Separate Endpoints Approach

#### 1. **API Proliferation**
```yaml
# 7 different endpoints for 7 categories
POST /api/v1/preferences/food
POST /api/v1/preferences/delivery
POST /api/v1/preferences/notifications
POST /api/v1/preferences/payment
POST /api/v1/preferences/location
POST /api/v1/preferences/accessibility
POST /api/v1/preferences/marketing

# Plus update endpoints
PUT /api/v1/preferences/food
PUT /api/v1/preferences/delivery
# ... and so on

# Plus get endpoints
GET /api/v1/preferences/food
GET /api/v1/preferences/delivery
# ... and so on

# Total: 21 endpoints for preferences alone!
```

#### 2. **Code Duplication**
- Each endpoint needs similar validation logic
- Each endpoint needs similar error handling
- Each endpoint needs similar response formatting
- Each endpoint needs similar security checks

#### 3. **Maintenance Overhead**
- Adding new preference categories requires new endpoints
- Changes to common logic require updates across all endpoints
- Testing becomes more complex with more endpoints
- Documentation becomes verbose

#### 4. **Inconsistent Behavior**
- Hard to ensure all endpoints behave identically
- Different teams might implement endpoints differently
- Versioning becomes complex across multiple endpoints

### ✅ Benefits of Unified Approach

#### 1. **Single Endpoint Design**
```yaml
# One endpoint handles all preference categories
POST /api/v1/preferences
{
  "category": "food",
  "data": { ... }
}

POST /api/v1/preferences
{
  "category": "delivery", 
  "data": { ... }
}

# Only 5 endpoints total:
POST /api/v1/preferences                    # Create/Update
PUT /api/v1/preferences/{category}          # Update specific
GET /api/v1/preferences                     # Get all
GET /api/v1/preferences/{category}          # Get specific
GET /api/v1/preferences/completion-status   # Get status
```

#### 2. **Consistent Behavior**
- All preference categories use the same validation logic
- All categories use the same error handling
- All categories use the same response format
- All categories use the same security model

#### 3. **Easy to Extend**
- Adding new preference categories requires no new endpoints
- Just add the category to the enum and validation schema
- Frontend can dynamically handle new categories

#### 4. **Better Performance**
- Fewer endpoints to maintain and monitor
- Simpler routing logic
- Easier caching strategies

## Implementation Details

### Backend Implementation
```java
@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {
    
    @PostMapping
    public ResponseEntity<PreferenceResponse> savePreferences(
            @RequestBody PreferenceRequest request) {
        
        // Validate category
        PreferenceCategory category = PreferenceCategory.valueOf(
            request.getCategory().toUpperCase());
        
        // Validate category-specific data
        preferenceValidator.validate(category, request.getData());
        
        // Save preferences
        UserPreferences preferences = preferenceService.savePreferences(
            getCurrentUserId(), category, request.getData());
        
        // Calculate completion
        int completionPercentage = preferenceService.calculateCompletion(
            getCurrentUserId());
        
        // Get next recommended category
        String nextCategory = preferenceService.getNextRecommendedCategory(
            getCurrentUserId());
        
        return ResponseEntity.ok(new PreferenceResponse(
            true, completionPercentage, nextCategory, "Preferences saved"));
    }
    
    @GetMapping("/{category}")
    public ResponseEntity<CategoryPreferenceResponse> getCategoryPreferences(
            @PathVariable String category) {
        
        PreferenceCategory prefCategory = PreferenceCategory.valueOf(
            category.toUpperCase());
        
        Object data = preferenceService.getCategoryPreferences(
            getCurrentUserId(), prefCategory);
        
        return ResponseEntity.ok(new CategoryPreferenceResponse(
            category, data, LocalDateTime.now()));
    }
}
```

### Frontend Implementation
```javascript
class PreferenceService {
    async savePreferences(category, data) {
        const response = await fetch('/api/v1/preferences', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.token}`
            },
            body: JSON.stringify({ category, data })
        });
        
        return response.json();
    }
    
    async getPreferences(category = null) {
        const url = category 
            ? `/api/v1/preferences/${category}`
            : '/api/v1/preferences';
            
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${this.token}`
            }
        });
        
        return response.json();
    }
}

// Usage
const preferenceService = new PreferenceService();

// Save food preferences
await preferenceService.savePreferences('food', {
    preferred_cuisines: ['Indian', 'Chinese'],
    dietary_restrictions: ['Vegetarian'],
    spice_level: 'MEDIUM'
});

// Save delivery preferences
await preferenceService.savePreferences('delivery', {
    preferred_delivery_time: 'ASAP',
    budget_range: '200_500',
    max_delivery_distance: 5
});
```

## Category Definitions

### Supported Categories
```yaml
categories:
  food:
    description: "Food and dietary preferences"
    fields:
      - preferred_cuisines: array
      - dietary_restrictions: array
      - spice_level: enum
      - allergies: array
    
  delivery:
    description: "Delivery preferences"
    fields:
      - preferred_delivery_time: enum
      - budget_range: enum
      - max_delivery_distance: number
      - delivery_instructions: string
    
  notifications:
    description: "Notification preferences"
    fields:
      - order_updates: boolean
      - offers_discounts: boolean
      - new_restaurant_alerts: boolean
      - channels: array
    
  payment:
    description: "Payment preferences"
    fields:
      - default_payment_method: string
      - save_payment_info: boolean
      - auto_apply_offers: boolean
      - payment_methods: array
    
  location:
    description: "Location preferences"
    fields:
      - addresses: array
      - default_address_id: uuid
    
  accessibility:
    description: "Accessibility preferences"
    fields:
      - language: string
      - font_size: string
      - voice_navigation: boolean
      - color_contrast: string
    
  marketing:
    description: "Marketing preferences"
    fields:
      - personalized_recommendations: boolean
      - location_based_offers: boolean
      - birthday_anniversary_deals: boolean
      - share_data_for_experience: boolean
```

## Validation Strategy

### Category Validation
```java
public enum PreferenceCategory {
    FOOD, DELIVERY, NOTIFICATIONS, PAYMENT, LOCATION, ACCESSIBILITY, MARKETING;
    
    public static boolean isValid(String category) {
        try {
            valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### Data Validation
```java
@Component
public class PreferenceValidator {
    
    public void validate(PreferenceCategory category, Object data) {
        switch (category) {
            case FOOD:
                validateFoodPreferences((FoodPreferences) data);
                break;
            case DELIVERY:
                validateDeliveryPreferences((DeliveryPreferences) data);
                break;
            // ... other categories
        }
    }
    
    private void validateFoodPreferences(FoodPreferences data) {
        // Category-specific validation
        if (data.getPreferredCuisines() == null || data.getPreferredCuisines().isEmpty()) {
            throw new ValidationException("At least one cuisine preference is required");
        }
        // ... more validation
    }
}
```

## Event Publishing

### Unified Event Structure
```yaml
user.preferences.updated:
  version: 1.0
  payload:
    userId: uuid
    category: string
    updatedData: object
    completionPercentage: number
    timestamp: datetime
```

## Conclusion

The unified approach is significantly better because:

1. **Reduces API complexity** from 21 endpoints to 5
2. **Ensures consistent behavior** across all preference categories
3. **Makes maintenance easier** with centralized logic
4. **Simplifies frontend integration** with consistent patterns
5. **Enables easy extension** for new preference categories
6. **Improves performance** with fewer endpoints to maintain

This design follows REST principles while being pragmatic about reducing complexity and improving maintainability. 