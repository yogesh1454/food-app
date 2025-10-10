# Story: Progressive User Data Collection and Event Publishing

**Story ID:** BE-002-11  
**Story Points:** 8  
**Priority:** High  
**Sprint:** 2  

### User Story
**As a** system architect  
**I want** to collect and publish user preference data progressively  
**So that** other services (like SDS) can use this data for personalization  

### Acceptance Criteria
- [ ] Comprehensive preference data models covering:
  - Food Preferences (Cuisines, Dietary Restrictions, Spice Level)
  - Delivery Preferences (Time, Budget, Distance)
  - Notification Preferences (Channels, Types)
  - Payment Preferences (Methods, Auto-apply)
  - Location Preferences (Multiple Addresses)
  - Accessibility Preferences (Language, Font, Voice)
  - Marketing Preferences (Personalization, Data Sharing)
- [ ] Preference data validation and business rules
- [ ] Progress calculation and completion percentage computation
- [ ] Preference events published to Kafka for other services
- [ ] Preference conflict resolution and data integrity
- [ ] Preference update history and audit trail
- [ ] Integration with profile management
- [ ] Data analytics events for preference changes

### Technical Tasks
1. [ ] Create UserPreferencesService
2. [ ] Implement preference CRUD APIs
3. [ ] Set up Kafka event publishing for preference changes
4. [ ] Create preference completion calculation service
5. [ ] Implement preference validation and business rules
6. [ ] Add preference analytics event publishing
7. [ ] Set up preference data monitoring
8. [ ] Configure preference caching strategy
9. [ ] Implement preference conflict resolution
10. [ ] Write integration tests for preference flows

### Data Models
```yaml
# User Preferences
user_preferences:
  food_preferences:
    preferred_cuisines: array
    dietary_restrictions: array
    spice_level: enum # MILD, MEDIUM, SPICY, EXTRA_SPICY
    allergies: array
    
  delivery_preferences:
    preferred_delivery_time: enum # ASAP, 30MIN, 1HR, SCHEDULE
    budget_range: enum # 0_200, 200_500, 500_PLUS
    max_delivery_distance: number # in km
    delivery_instructions: string
    
  notification_preferences:
    order_updates: boolean
    offers_discounts: boolean
    new_restaurant_alerts: boolean
    channels: array # PUSH, SMS, EMAIL, WHATSAPP
    
  payment_preferences:
    default_payment_method: string
    save_payment_info: boolean
    auto_apply_offers: boolean
    payment_methods: array
    
  location_preferences:
    addresses: array
    default_address_id: uuid
    work_address: object
    home_address: object
    
  accessibility_preferences:
    language: string
    font_size: string
    voice_navigation: boolean
    color_contrast: string
    
  marketing_preferences:
    personalized_recommendations: boolean
    location_based_offers: boolean
    birthday_anniversary_deals: boolean
    share_data_for_experience: boolean

# Preference Completion Tracking
preference_completion:
  user_id: uuid
  categories_completed: array
  completion_percentage: number
  last_updated: timestamp
  total_preferences: number
  completed_preferences: number
```

### Kafka Events
```yaml
# User Registration Event
user.registered:
  version: 1.0
  payload:
    user_id: uuid
    registration_type: string
    account_type: string
    timestamp: datetime
    basic_profile: object

# Profile Updated Event
user.profile.updated:
  version: 1.0
  payload:
    user_id: uuid
    updated_fields: array
    update_type: string
    timestamp: datetime
    completion_percentage: number

# Preferences Added Event
user.preferences.added:
  version: 1.0
  payload:
    user_id: uuid
    preference_type: string
    preferences: object
    timestamp: datetime

# Address Added Event
user.address.added:
  version: 1.0
  payload:
    user_id: uuid
    address_type: string
    address: object
    timestamp: datetime
```

### API Endpoints
```yaml
# Unified Preference Management
POST /api/v1/preferences:
  request:
    category: string # "food", "delivery", "notifications", "payment", "location", "accessibility", "marketing"
    data: object # Category-specific preference data
  response:
    success: boolean
    completion_percentage: number
    next_category: string
    message: string

# Example requests:
# Food Preferences
POST /api/v1/preferences
{
  "category": "food",
  "data": {
    "preferred_cuisines": ["Indian", "Chinese", "Italian"],
    "dietary_restrictions": ["Vegetarian", "Jain"],
    "spice_level": "MEDIUM",
    "allergies": ["Peanuts", "Dairy"]
  }
}

# Delivery Preferences
POST /api/v1/preferences
{
  "category": "delivery",
  "data": {
    "preferred_delivery_time": "ASAP",
    "budget_range": "200_500",
    "max_delivery_distance": 5,
    "delivery_instructions": "Call before delivery"
  }
}

# Update Specific Preference Category
PUT /api/v1/preferences/{category}:
  request:
    data: object # Category-specific preference data
  response:
    success: boolean
    completion_percentage: number
    message: string

# Get All Preferences
GET /api/v1/preferences:
  response:
    preferences: object # All user preferences by category
    completion_percentage: number
    categories_completed: array
    remaining_categories: array

# Get Specific Category Preferences
GET /api/v1/preferences/{category}:
  response:
    category: string
    data: object
    last_updated: datetime

# Get Preference Completion Status
GET /api/v1/preferences/completion-status:
  response:
    completion_percentage: number
    categories_completed: array
    remaining_categories: array
    total_preferences: number
    completed_preferences: number
    next_recommended_category: string
```

### Analytics Events
```yaml
events:
  profile_completion:
    stage_started:
      user_id: uuid
      stage: string
      timestamp: datetime
      
    stage_completed:
      user_id: uuid
      stage: string
      time_taken: number
      skipped_fields: array
      
    data_added:
      user_id: uuid
      category: string
      field_count: number
      timestamp: datetime
```

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] Preference APIs are tested end-to-end
- [ ] Kafka events are properly published for all preference changes
- [ ] Event consumers can successfully process preference events
- [ ] Preference validation and business rules are working
- [ ] Analytics events are captured for preference changes
- [ ] Performance metrics are collected and monitored
- [ ] Data integrity and conflict resolution are verified
- [ ] Documentation is complete 