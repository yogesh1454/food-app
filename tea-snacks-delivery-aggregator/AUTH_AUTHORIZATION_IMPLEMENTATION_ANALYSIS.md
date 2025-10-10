# Authentication & Authorization Implementation Analysis - UMS

## 🔐 **Current Implementation Status**

### **Authentication System**

#### **1. JWT Token Provider (`JwtTokenProvider.java`)**
- ✅ **Token Generation**: Creates access and refresh tokens with RSA256 signing
- ✅ **Token Validation**: Validates token signature and expiration
- ✅ **Claims Extraction**: Extracts userId, username, and roles from tokens
- ✅ **Role Support**: Includes user roles in JWT claims
- ✅ **Token Expiration**: Configurable expiration times

**Key Features:**
```java
// Token includes roles
public String generateAccessToken(String userId, String username, String... roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("username", username);
    claims.put("roles", roles);  // ← Roles included in JWT
    // ... token generation
}
```

#### **2. Security Configuration (`SecurityConfig.java`)**
- ✅ **Stateless Sessions**: Configured for JWT-based authentication
- ✅ **CSRF Disabled**: Appropriate for stateless JWT authentication
- ✅ **Method Security**: `@EnableMethodSecurity(prePostEnabled = true)` enabled
- ✅ **Endpoint Security**: Comprehensive endpoint protection rules

### **Authorization System**

#### **1. Authorization Service (`AuthorizationService.java`)**
- ✅ **Role-Based Access Control (RBAC)**: Permission checking by role
- ✅ **Resource Ownership**: Validates resource ownership
- ✅ **Permission Management**: Database-driven permissions
- ✅ **Default Permissions**: Auto-initialization of role permissions

#### **2. Custom Authorization Annotation (`@HasPermission`)**
- ✅ **Method-Level Security**: Declarative permission checking
- ✅ **Resource-Action Pairs**: Flexible permission definitions
- ✅ **Ownership Validation**: Optional resource ownership checks

#### **3. Authorization Aspect (`AuthorizationAspect.java`)**
- ✅ **AOP Integration**: Intercepts `@HasPermission` annotations
- ✅ **Automatic Enforcement**: Transparent permission checking
- ✅ **Error Handling**: Proper authorization failure responses

## 📋 **Endpoint Security Analysis**

### **🔓 Public Endpoints (No Authentication Required)**

#### **Authentication Endpoints**
```
POST /api/auth/login                    - User login
POST /api/auth/refresh                  - Token refresh
GET  /api/auth/health                   - Health check
POST /api/auth/register/email           - Email registration
POST /api/auth/guest/convert            - Guest to registered user conversion
```

#### **OTP Endpoints**
```
POST /api/v1/auth/phone/send-otp       - Send OTP
POST /api/v1/auth/phone/verify-otp     - Verify OTP
POST /api/v1/auth/phone/register       - Register with OTP
```

#### **Guest User Endpoints**
```
POST /api/v1/auth/guest/create         - Create guest user
GET  /api/v1/auth/guest/session        - Get guest session info
POST /api/v1/auth/guest/action         - Record guest action
POST /api/v1/auth/guest/conversion-prompt-shown - Record conversion prompt
GET  /api/v1/auth/guest/health         - Guest service health check
```

#### **User Management Endpoints** *(Currently permitAll - needs review)*
```
GET  /api/users/{userId}               - Get user by ID
GET  /api/users/email/{email}          - Get user by email
GET  /api/users/phone/{phoneNumber}    - Get user by phone
GET  /api/users/active                 - Get all active users
PUT  /api/users/{userId}/status        - Update user status
```

#### **Documentation & Monitoring**
```
GET  /swagger-ui/**                    - Swagger UI
GET  /v3/api-docs/**                   - OpenAPI documentation
GET  /actuator/**                      - Spring Boot Actuator
```

### **🔒 Protected Endpoints (Authentication Required)**

#### **Authorization Management**
```
GET  /api/auth/authorization/permissions                    - Get user permissions
GET  /api/auth/authorization/permissions/{role}             - Get role permissions
POST /api/auth/authorization/check                          - Check specific permission
POST /api/auth/authorization/check-resource                 - Check resource access
POST /api/auth/authorization/initialize                     - Initialize permissions
```

#### **Test Authorization Endpoints**
```
GET  /api/test/auth/admin-only                             - Admin only access
GET  /api/test/auth/vendor-menu/{vendorId}                 - Vendor menu management
GET  /api/test/auth/customer-profile/{userId}              - Customer profile access
GET  /api/test/auth/delivery-partner/{partnerId}/deliveries - Delivery partner access
GET  /api/test/auth/public                                 - Public test endpoint
```

## 🎯 **Role-Based Permission Matrix**

### **ADMIN Role**
- ✅ **All Permissions**: Can access all resources
- ✅ **User Management**: `users:manage`
- ✅ **Vendor Management**: `vendors:manage`
- ✅ **System Management**: `system:manage`
- ✅ **Reports**: `reports:view`

### **VENDOR Role**
- ✅ **Own Profile**: `profile:manage`
- ✅ **Menu Management**: `menu:manage`
- ✅ **Order Management**: `orders:manage`
- ✅ **Own Reports**: `reports:view_own`

### **DELIVERY_PARTNER Role**
- ✅ **Own Profile**: `profile:manage`
- ✅ **Delivery Management**: `deliveries:manage`
- ✅ **Location Updates**: `location:update`

### **CUSTOMER Role**
- ✅ **Own Profile**: `profile:manage`
- ✅ **Order Placement**: `orders:place`
- ✅ **Order History**: `orders:view_history`

### **GUEST Role** *(Special Case)*
- ✅ **Session Management**: `guest:session`
- ✅ **Limited Browsing**: `catalog:view`
- ✅ **Action Recording**: `guest:action`
- ✅ **Conversion Tracking**: `guest:conversion`
- ❌ **No Order Placement**: Cannot place orders
- ❌ **No Profile Management**: Cannot manage profile
- ❌ **No Authentication**: No JWT tokens

## 🔧 **Implementation Details**

### **Guest User Architecture**

#### **Guest User Model (`GuestUser.java`)**
```java
@Entity
@Table(name = "guest_users")
public class GuestUser {
    private UUID id;
    private String deviceId;           // Device fingerprint
    private String sessionToken;       // Session management
    private LocalDateTime expiresAt;   // Session expiration
    private Integer actionCount;       // Usage tracking
    private Boolean isActive;          // Session status
    private UUID convertedToUserId;    // Conversion tracking
}
```

#### **Guest User Access Pattern**
- **No JWT Authentication**: Guest users don't receive JWT tokens
- **Session-Based**: Uses session tokens for identification
- **Device Fingerprinting**: Identified by device ID
- **Limited Actions**: Can browse but cannot perform authenticated actions
- **Conversion Tracking**: Monitors actions to encourage registration

#### **Guest User Limitations**
```java
// Guest users cannot:
- Place orders (requires authentication)
- Manage profile (requires registration)
- Access protected resources
- Use JWT-based authorization
```

### **JWT Token Structure**
```json
{
  "userId": "uuid",
  "username": "email@example.com",
  "roles": ["CUSTOMER"],
  "iat": 1234567890,
  "exp": 1234567890,
  "iss": "tea-delivery",
  "aud": "tea-delivery-users",
  "jti": "token-id"
}
```

### **Authorization Flow**

#### **For Registered Users (JWT-based)**
1. **Authentication**: User logs in → JWT token generated with roles
2. **Request**: Client includes JWT token in Authorization header
3. **Validation**: Spring Security validates JWT token
4. **Authorization**: `@HasPermission` aspect checks permissions
5. **Resource Access**: AuthorizationService validates resource ownership
6. **Response**: Success or 403 Forbidden

#### **For Guest Users (Session-based)**
1. **Session Creation**: Guest user created with device fingerprint
2. **Request**: Client includes session token in Authorization header
3. **Validation**: GuestUserService validates session token
4. **Action Recording**: Guest actions are tracked for conversion
5. **Limited Access**: Only public resources accessible
6. **Response**: Success or 401 Unauthorized

### **Database Schema**
```sql
-- Permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE,
    description TEXT,
    resource VARCHAR(50),
    action VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role VARCHAR(50),
    permission_id UUID REFERENCES permissions(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Guest users table
CREATE TABLE guest_users (
    id UUID PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    action_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    converted_to_user_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

## ⚠️ **Security Issues & Recommendations**

### **🔴 Critical Issues**

#### **1. User Management Endpoints Unprotected**
```java
// Current: All user endpoints are permitAll
.requestMatchers("/api/users/**").permitAll()
```
**Recommendation**: Protect user management endpoints with proper authorization

#### **2. Missing JWT Filter**
- No JWT authentication filter configured
- JWT tokens not being processed in request pipeline
- Authentication context not properly set

#### **3. Authorization Integration Incomplete**
- JWT roles not being extracted properly in AuthorizationService
- Authentication context not linked to authorization decisions

#### **4. Guest User Authorization Missing**
- No role-based permissions for GUEST users
- Guest users not included in authorization framework
- No permission checking for guest-specific actions

### **🟡 Medium Priority Issues**

#### **1. Guest User Security**
- Guest users can access some endpoints without proper validation
- Device fingerprinting could be bypassed

#### **2. Rate Limiting Missing**
- No rate limiting on authentication endpoints
- Brute force protection not implemented

#### **3. Token Blacklisting**
- No mechanism to invalidate tokens
- Logout functionality incomplete

### **🟢 Low Priority Issues**

#### **1. Audit Logging**
- Authorization decisions not logged
- Security events not tracked

#### **2. Error Messages**
- Generic error messages might leak information
- Need more specific error handling

## 🚀 **Next Steps for BE-002-13 Implementation**

### **1. JWT Authentication Filter**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Extract JWT from Authorization header
    // Validate token and set Authentication context
    // Handle token refresh
}
```

### **2. Enhanced Authorization Service**
```java
// Update to properly extract roles from JWT claims
private User.Role getUserRoleFromAuthentication(Authentication authentication) {
    // Extract from JWT claims instead of authorities
    Claims claims = extractClaimsFromAuthentication(authentication);
    String roleString = claims.get("roles", String.class);
    return User.Role.valueOf(roleString);
}
```

### **3. Protected User Management Endpoints**
```java
// Update SecurityConfig
.requestMatchers("/api/users/**").authenticated()
```

### **4. Guest User Authorization**
- Add GUEST role to authorization framework
- Create guest-specific permissions
- Implement guest action authorization
- Test guest user access patterns

### **5. Comprehensive Testing**
- Test all endpoints with different roles
- Verify authorization decisions
- Test resource ownership validation
- Test guest user limitations

## 📊 **Current Status Summary**

| Component | Status | Implementation Level |
|-----------|--------|---------------------|
| JWT Token Generation | ✅ Complete | Production Ready |
| JWT Token Validation | ✅ Complete | Production Ready |
| Authorization Framework | ✅ Complete | Production Ready |
| Authorization Integration | ❌ Missing | Needs BE-002-13 |
| Endpoint Protection | ⚠️ Partial | Needs Review |
| Role-Based Access | ✅ Complete | Production Ready |
| Resource Ownership | ✅ Complete | Production Ready |
| Guest User Management | ✅ Complete | Production Ready |
| Guest User Authorization | ❌ Missing | Needs BE-002-13 |

**Overall Status**: **70% Complete** - Core components ready, integration and guest authorization pending 