# Epic 2: User Management Service - Architecture Overview

## System Architecture

### High-Level Design
```
┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │   Web App       │
│                 │    │                 │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          └──────────────────────┼──────────────────────┐
                                 │                      │
                    ┌─────────────┴─────────────┐       │
                    │      API Gateway          │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   Authentication    │  │       │
                    │  │   & Authorization   │  │       │
                    │  └─────────────────────┘  │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   Rate Limiting     │  │       │
                    │  │   & Security        │  │       │
                    │  └─────────────────────┘  │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   Routing & Load    │  │       │
                    │  │   Balancing         │  │       │
                    │  └─────────────────────┘  │       │
                    └─────────────┬─────────────┘       │
                                  │                      │
                    ┌─────────────┴─────────────┐       │
                    │   User Management Service │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   Authentication    │  │       │
                    │  │   & Authorization   │  │       │
                    │  └─────────────────────┘  │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   User Registration │  │       │
                    │  │   & Profile Mgmt    │  │       │
                    │  └─────────────────────┘  │       │
                    │                           │       │
                    │  ┌─────────────────────┐  │       │
                    │  │   Preference Mgmt   │  │       │
                    │  │   & Data Collection │  │       │
                    │  └─────────────────────┘  │       │
                    └─────────────┬─────────────┘       │
                                  │                      │
          ┌───────────────────────┼──────────────────────┘
          │                       │
┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐
│   PostgreSQL      │  │   Redis Cache     │  │   Apache Kafka    │
│   (Primary DB)    │  │   (Sessions)      │  │   (Events)        │
└───────────────────┘  └───────────────────┘  └───────────────────┘
```

### Service Components

#### 1. API Gateway
- **Authentication & Authorization**: JWT token validation and role-based access control
- **Rate Limiting**: Per user and IP-based request limiting
- **Security Headers**: CORS, CSP, and security header management
- **Routing & Load Balancing**: Request routing to appropriate services
- **Request/Response Transformation**: Data format conversion and validation
- **API Documentation**: Swagger/OpenAPI integration

#### 2. User Management Service
- **JWT Token Management**: Token generation, validation, and refresh
- **Session Management**: Redis-based session storage
- **User Registration**: Multi-type registration flows
- **Profile Management**: User profiles and addresses
- **Preference Management**: User preferences and data collection

#### 3. User Registration & Management
- **Multi-Type Registration**: Email, phone, social login
- **Profile Management**: User profiles and addresses
- **Password Management**: Change, reset, and security
- **Guest User Support**: Limited access and conversion

#### 4. Preference Management
- **Data Collection**: Progressive preference gathering
- **Validation**: Business rules and conflict resolution
- **Event Publishing**: Kafka events for other services
- **Analytics**: User behavior tracking

## Data Models

### Core Entities
```java
@Entity
public class User {
    private Long id;
    private String email;
    private String phoneNumber;
    private String name;
    private UserType userType; // REGISTERED, GUEST, SOCIAL
    private UserStatus status; // ACTIVE, INACTIVE, SUSPENDED
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int profileCompletionPercentage;
    private boolean preferencesComplete;
}

@Entity
public class UserPreferences {
    private Long id;
    private Long userId;
    
    // Food preferences
    private List<String> preferredCuisines;
    private List<String> dietaryRestrictions;
    private SpiceLevel spiceLevel;
    
    // Delivery preferences
    private DeliveryTimePreference deliveryTime;
    private BudgetRange budgetRange;
    private int maxDeliveryDistance;
    
    // Notification preferences
    private boolean orderUpdates;
    private boolean offersAndDiscounts;
    private List<NotificationChannel> channels;
    
    // Payment preferences
    private String defaultPaymentMethod;
    private boolean savePaymentInfo;
    private boolean autoApplyOffers;
    
    // Location preferences
    private List<UserAddress> addresses;
    private UUID defaultAddressId;
    
    // Accessibility preferences
    private String language;
    private String fontSize;
    private boolean voiceNavigation;
    
    // Marketing preferences
    private boolean personalizedRecommendations;
    private boolean locationBasedOffers;
    private boolean shareDataForExperience;
}

@Entity
public class UserAddress {
    private Long id;
    private Long userId;
    private String addressType; // HOME, WORK, OTHER
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String landmark;
    private boolean isDefault;
    private Double latitude;
    private Double longitude;
}
```

## API Design

### API Gateway Routes
```yaml
# Authentication Routes
POST /api/v1/auth/register/email
POST /api/v1/auth/register/phone
POST /api/v1/auth/phone/send-otp
POST /api/v1/auth/phone/verify-otp
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/refresh
POST /api/v1/auth/oauth/{provider}

# Gateway Configuration
- Route to: User Management Service
- Authentication: JWT validation
- Rate Limiting: Per user/IP
- CORS: Configured for frontend apps
- Security Headers: Applied
```

### User Management APIs
```yaml
# User Profile Routes
GET /api/v1/user/profile
PUT /api/v1/user/profile
POST /api/v1/user/password/change
POST /api/v1/user/password/reset
GET /api/v1/user/addresses
POST /api/v1/user/addresses
PUT /api/v1/user/addresses/{id}
DELETE /api/v1/user/addresses/{id}

# Gateway Configuration
- Route to: User Management Service
- Authentication: Required (JWT)
- Authorization: Role-based access
- Rate Limiting: Per user
```

### Preference APIs
```yaml
# Unified Preference Management Routes
POST /api/v1/preferences                    # Create/Update preference category
PUT /api/v1/preferences/{category}          # Update specific category
GET /api/v1/preferences                     # Get all preferences
GET /api/v1/preferences/{category}          # Get specific category
GET /api/v1/preferences/completion-status   # Get completion status

# Supported Categories:
# - food: Cuisines, dietary restrictions, spice level, allergies
# - delivery: Time, budget, distance, instructions
# - notifications: Channels, types, frequency
# - payment: Methods, auto-apply, save info
# - location: Addresses, default location
# - accessibility: Language, font, voice, contrast
# - marketing: Recommendations, offers, data sharing

# Gateway Configuration
- Route to: User Management Service
- Authentication: Required (JWT)
- Authorization: User can only access own preferences
- Rate Limiting: Per user
```

## Event Schema

### User Lifecycle Events
```yaml
user.registered:
  version: 1.0
  payload:
    userId: uuid
    registrationMethod: string
    userType: string
    timestamp: datetime
    profileData: object

user.login:
  version: 1.0
  payload:
    userId: uuid
    loginMethod: string
    timestamp: datetime
    sessionId: string

user.logout:
  version: 1.0
  payload:
    userId: uuid
    sessionId: string
    sessionDuration: number
    timestamp: datetime
```

### Profile & Preference Events
```yaml
user.profile.updated:
  version: 1.0
  payload:
    userId: uuid
    updatedFields: array
    completionPercentage: number
    timestamp: datetime

user.preferences.updated:
  version: 1.0
  payload:
    userId: uuid
    preferenceCategory: string
    updatedValues: object
    completionPercentage: number
    timestamp: datetime

user.address.added:
  version: 1.0
  payload:
    userId: uuid
    addressType: string
    address: object
    timestamp: datetime
```

## Security Architecture

### Authentication Flow
```
1. User submits credentials to API Gateway
2. API Gateway routes request to User Management Service
3. User Management Service validates credentials
4. JWT token generated and stored in Redis
5. Token returned to client via API Gateway
6. Client includes token in subsequent requests
7. API Gateway validates token and applies authorization
8. API Gateway routes request to appropriate service
9. Service processes request and returns response
10. API Gateway applies security headers and returns response
```

### Authorization Model
```yaml
Roles:
  - GUEST: Limited access, browse only
  - USER: Full access to own data
  - ADMIN: Full access to all data

Permissions:
  - READ_OWN_PROFILE
  - UPDATE_OWN_PROFILE
  - READ_OWN_PREFERENCES
  - UPDATE_OWN_PREFERENCES
  - MANAGE_OWN_ADDRESSES
  - READ_ALL_USERS (ADMIN)
  - UPDATE_ALL_USERS (ADMIN)
```

### Security Measures
- **API Gateway Security**:
  - JWT token validation
  - Rate limiting per user and IP
  - CORS configuration
  - Security headers (CSP, HSTS, etc.)
  - Request/response validation
- **Service Security**:
  - JWT token generation and validation
  - Password hashing with BCrypt
  - Input validation and sanitization
  - HTTPS encryption
  - Audit logging

## Performance Architecture

### Caching Strategy
```yaml
Redis Cache:
  - User sessions: Until logout
  - User profiles: 15 minutes
  - User preferences: 30 minutes
  - Addresses: 1 hour
  - API responses: 5 minutes
```

### Database Optimization
```yaml
Indexes:
  - User email (unique)
  - User phone (unique)
  - User status
  - User created_at
  - Preferences user_id
  - Addresses user_id

Partitioning:
  - Users by created_date
  - Events by timestamp
```

### Performance Targets
- **API Response Time**: < 1 second (95th percentile)
- **Database Queries**: < 100ms
- **Cache Hit Rate**: > 90%
- **Event Publishing**: < 50ms
- **System Availability**: > 99.9%

## Integration Points

### External Services
```yaml
Email Service (SendGrid):
  - Registration confirmation
  - Password reset
  - Notification emails

SMS Service (Gupshup):
  - OTP delivery
  - SMS notifications

OAuth Providers:
  - Google OAuth
  - Facebook OAuth
  - Apple OAuth
```

### Internal Services
```yaml
Search & Discovery Service:
  - Receives preference events
  - Uses user preferences for recommendations

Order Management Service:
  - Receives user context
  - Uses user addresses and preferences

Notification Service:
  - Receives notification preferences
  - Manages user communication channels

Analytics Service:
  - Receives user behavior events
  - Tracks user engagement and preferences
```

## Monitoring & Observability

### Metrics
```yaml
Application Metrics:
  - API response times
  - Error rates
  - Request throughput
  - Active sessions

Business Metrics:
  - User registration rate
  - Profile completion rate
  - Preference collection rate
  - User engagement metrics

Infrastructure Metrics:
  - Database performance
  - Cache hit rates
  - Kafka lag
  - System resources
```

### Logging
```yaml
Structured Logging:
  - Request/response logs
  - Error logs with stack traces
  - Security audit logs
  - Performance logs

Log Levels:
  - DEBUG: Detailed debugging information
  - INFO: General application flow
  - WARN: Warning conditions
  - ERROR: Error conditions
```

### Alerting
```yaml
Critical Alerts:
  - Service unavailability
  - High error rates
  - Database connection issues
  - Cache failures

Warning Alerts:
  - High response times
  - Low cache hit rates
  - High memory usage
  - Disk space issues
```

## Deployment Architecture

### Container Strategy
```yaml
Docker Containers:
  - API Gateway (Kong/Spring Cloud Gateway)
  - User Management Service
  - PostgreSQL Database
  - Redis Cache
  - Apache Kafka
  - Monitoring Stack (Prometheus, Grafana)
```

### Environment Strategy
```yaml
Environments:
  - Development: Local development
  - Staging: Pre-production testing
  - Production: Live environment

Configuration:
  - Environment-specific configs
  - Feature toggles
  - External service endpoints
  - Security configurations
```

This architecture provides a scalable, secure, and maintainable foundation for the User Management Service, supporting the comprehensive user journey while maintaining clear separation of concerns and enabling efficient development and deployment. 