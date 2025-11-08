# Shared Components Strategy for Tea & Snacks Delivery Aggregator

## Overview
This document outlines the strategy for organizing shared components across microservices to avoid code duplication while maintaining service independence.

## Shared Module Structure

```
shared/
├── common/                          # Core shared library
│   ├── src/main/java/com/teadelivery/common/
│   │   ├── security/                # JWT validation and security components
│   │   │   ├── JwtTokenValidator.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── SecurityConstants.java
│   │   │   ├── UserPrincipal.java
│   │   │   └── SecurityUtils.java
│   │   │
│   │   ├── dto/                     # Common DTOs shared across services
│   │   │   ├── ApiResponse.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── PagedResponse.java
│   │   │   └── UserContext.java
│   │   │
│   │   ├── exception/               # Common exceptions and handlers
│   │   │   ├── BaseException.java
│   │   │   ├── ValidationException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   │
│   │   ├── config/                  # Common configurations
│   │   │   ├── RedisConfig.java
│   │   │   ├── KafkaConfig.java
│   │   │   ├── JacksonConfig.java
│   │   │   └── CommonSecurityConfig.java
│   │   │
│   │   ├── util/                    # Utility classes
│   │   │   ├── DateUtils.java
│   │   │   ├── ValidationUtils.java
│   │   │   ├── EncryptionUtils.java
│   │   │   └── LocationUtils.java
│   │   │
│   │   ├── constants/               # Application-wide constants
│   │   │   ├── ApiConstants.java
│   │   │   ├── CacheConstants.java
│   │   │   └── KafkaTopics.java
│   │   │
│   │   ├── annotation/              # Custom annotations
│   │   │   ├── RequiresRole.java
│   │   │   ├── ValidateUser.java
│   │   │   └── AuditLog.java
│   │   │
│   │   ├── model/                   # Common domain models
│   │   │   ├── BaseEntity.java
│   │   │   ├── AuditableEntity.java
│   │   │   └── Location.java
│   │   │
│   │   └── event/                   # Common event structures
│   │       ├── BaseEvent.java
│   │       ├── UserEvent.java
│   │       └── OrderEvent.java
│   │
│   └── build.gradle                 # Shared library dependencies
│
└── client/                          # Inter-service communication clients
    ├── src/main/java/com/teadelivery/client/
    │   ├── user/
    │   │   ├── UserServiceClient.java
    │   │   └── dto/
    │   │       ├── UserDto.java
    │   │       └── UserValidationDto.java
    │   │
    │   ├── order/
    │   │   ├── OrderServiceClient.java
    │   │   └── dto/
    │   │
    │   └── config/
    │       └── FeignClientConfig.java
    │
    └── build.gradle

```

## Best Practices for Shared Components

### 1. **JWT Validation Components (shared/common/security/)**

**JwtTokenValidator.java**
```java
@Component
public class JwtTokenValidator {
    
    public boolean validateToken(String token) {
        // JWT validation logic
    }
    
    public Claims extractClaims(String token) {
        // Extract claims from JWT
    }
    
    public UserPrincipal extractUserPrincipal(String token) {
        // Extract user details for security context
    }
}
```

**JwtAuthenticationFilter.java**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Common JWT filter that all services can use
}
```

### 2. **Common DTOs (shared/common/dto/)**

**ApiResponse.java**
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    // Standardized response format across all services
}
```

**UserContext.java**
```java
public class UserContext {
    private String userId;
    private String userType;
    private Set<String> roles;
    private String companyId; // For B2B users
    // User context passed between services
}
```

### 3. **What Should NOT Go in Shared Module**

❌ **Avoid These in Shared:**
- Business logic specific to one service
- Database entities specific to one domain
- Service-specific configurations
- Heavy dependencies that not all services need

✅ **Good Candidates for Shared:**
- JWT validation and security utilities
- Common DTOs for inter-service communication
- Utility functions used by multiple services
- Common exception types and handlers
- Standardized response formats
- Common configurations (Redis, Kafka)

## Implementation Strategy

### Phase 1: Security Components
1. Move JWT validation logic to `shared/common/security/`
2. Create common security configurations
3. Update all microservices to use shared security components

### Phase 2: Common DTOs and Responses
1. Create standardized API response formats
2. Define common error handling
3. Create user context DTOs for inter-service communication

### Phase 3: Utility and Configuration
1. Move common utilities to shared module
2. Standardize Redis and Kafka configurations
3. Create common constants and enums

## Dependency Management

### In Microservice build.gradle:
```gradle
dependencies {
    implementation project(':shared:common')
    implementation project(':shared:client')  // If needed
    
    // Service-specific dependencies
    implementation 'org.springframework.boot:spring-boot-starter-web'
    // ... other dependencies
}
```

### In shared/common/build.gradle:
```gradle
dependencies {
    // Only include dependencies that are truly common
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Avoid heavy dependencies unless all services need them
}
```

## Benefits of This Approach

1. **Code Reusability**: JWT validation logic written once, used everywhere
2. **Consistency**: Standardized error handling and response formats
3. **Maintainability**: Security updates in one place
4. **Type Safety**: Shared DTOs ensure consistent data contracts
5. **Reduced Duplication**: Common utilities and configurations

## Anti-Patterns to Avoid

1. **Shared Database Entities**: Each service should own its data model
2. **Business Logic in Shared**: Keep domain-specific logic in respective services
3. **Heavy Dependencies**: Don't add dependencies that only one service needs
4. **Tight Coupling**: Shared components should be loosely coupled utilities

## Migration Strategy

1. **Start Small**: Begin with JWT validation and common DTOs
2. **Gradual Migration**: Move components to shared as patterns emerge
3. **Service Independence**: Ensure services can still run independently
4. **Versioning**: Use semantic versioning for shared module changes
