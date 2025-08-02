# Story: Guest User Management and Conversion

**Story ID:** BE-002-01B  
**Story Points:** 3  
**Priority:** Medium  
**Sprint:** 2  

### User Story
**As a** potential user of the Tea & Snacks Delivery platform  
**I want** to browse and explore the platform without creating an account  
**So that** I can evaluate the service before committing to registration  

### Acceptance Criteria
- [ ] Users can create guest accounts with minimal information
- [ ] Guest users can browse restaurants and menus
- [ ] Guest users are prompted to register when attempting restricted actions
- [ ] Guest users can convert to registered users seamlessly
- [ ] Guest sessions have configurable expiry (default: 24 hours)
- [ ] Guest user data is preserved during conversion
- [ ] Guest users have limited API access (read-only operations)
- [ ] Guest user analytics are tracked separately
- [ ] Guest sessions are cleaned up automatically after expiry

### Technical Tasks
1. [ ] Create GuestUserService for guest account management
2. [ ] Implement guest user creation with device fingerprinting
3. [ ] Create guest session management with Redis
4. [ ] Implement guest to registered user conversion
5. [ ] Add guest user access control and limitations
6. [ ] Create guest user cleanup job
7. [ ] Add guest user analytics events
8. [ ] Implement guest data preservation during conversion
9. [ ] Add guest user monitoring and metrics
10. [ ] Write unit and integration tests

### API Specification

#### 1. Create Guest User
```http
POST /api/v1/auth/guest/create
Content-Type: application/json

{
  "deviceId": "unique_device_identifier",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1",
  "sessionMetadata": {
    "platform": "web|ios|android",
    "version": "1.0.0"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Guest account created",
  "data": {
    "guestUserId": "guest_uuid",
    "sessionToken": "guest_session_token",
    "userType": "GUEST",
    "expiryTime": "2024-01-02T10:00:00Z",
    "limitations": [
      "cannot_place_orders",
      "cannot_save_favorites",
      "limited_search_history"
    ]
  }
}
```

#### 2. Convert Guest to Registered User
```http
POST /api/v1/auth/guest/convert
Content-Type: application/json
Authorization: Bearer guest_session_token

{
  "registrationMethod": "email|phone|social",
  "registrationData": {
    "email": "user@example.com",
    "password": "securePassword123",
    "name": "John Doe",
    "phoneNumber": "+91-9876543210"
  },
  "preserveData": {
    "searchHistory": true,
    "browsingPreferences": true,
    "cartItems": true
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Account conversion successful",
  "data": {
    "userId": "registered_user_uuid",
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token",
    "userType": "REGISTERED",
    "profileCompletion": 30,
    "preservedData": {
      "searchHistoryItems": 15,
      "cartItemsTransferred": 3
    }
  }
}
```

#### 3. Get Guest Session Info
```http
GET /api/v1/auth/guest/session
Authorization: Bearer guest_session_token
```

**Response:**
```json
{
  "success": true,
  "data": {
    "guestUserId": "guest_uuid",
    "sessionStatus": "active|expired",
    "expiryTime": "2024-01-02T10:00:00Z",
    "timeRemaining": "23h 45m",
    "limitations": [
      "cannot_place_orders",
      "cannot_save_favorites"
    ],
    "conversionPrompts": {
      "showAfterActions": 5,
      "currentActionCount": 3
    }
  }
}
```

### Business Rules
- Guest sessions expire after 24 hours of inactivity
- Guest users can only perform read-only operations
- Guest users are prompted to register after 5 significant actions
- Device fingerprinting prevents duplicate guest accounts
- Guest data is preserved for 7 days after session expiry
- Maximum 1 guest account per device per day
- Guest users cannot place orders or save preferences

### Access Limitations
- **Allowed Operations**: Browse restaurants, view menus, search, view reviews
- **Restricted Operations**: Place orders, save favorites, write reviews, access order history
- **Conversion Triggers**: Attempt to place order, save favorite, write review

### Data Preservation During Conversion
- Search history (last 30 searches)
- Cart items (if any)
- Browsing preferences
- Location preferences
- Recently viewed restaurants

### Error Handling
- **Device limit exceeded**: Return limit error with conversion prompt
- **Invalid guest session**: Return authentication error
- **Conversion data validation**: Return validation errors
- **Session expired**: Return expiry error with re-creation option

### Dependencies
- **BE-002-01**: Basic email registration for conversion
- **BE-002-01A**: Phone OTP registration for conversion
- **BE-002-07**: Redis integration for session management
- **BE-002-08**: Kafka integration for analytics events

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] Code follows project coding standards
- [ ] Unit tests coverage > 80%
- [ ] Integration tests verify guest user flows
- [ ] API documentation is complete
- [ ] Code review is completed
- [ ] Guest session cleanup job tested
- [ ] Conversion flow tested with all registration methods
- [ ] Analytics events verified in Kafka
- [ ] Performance meets requirements (guest creation < 100ms)
