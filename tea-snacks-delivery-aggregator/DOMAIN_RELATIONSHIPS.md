# Domain Relationships Documentation

## 🏗️ **Consolidated DDD Structure**

### **Overview**
The User Management Service has been restructured to follow proper Domain-Driven Design (DDD) principles, consolidating related functionality and eliminating duplication.

### **Domain Structure**
```
user/
├── config/                    # Global configurations
│   ├── JwtConfig.java        # JWT configuration
│   ├── SecurityConfig.java   # Spring Security configuration
│   ├── SwaggerConfig.java    # API documentation
│   └── GlobalExceptionHandler.java
├── auth/                      # Authentication & Authorization
│   ├── controller/
│   │   └── AuthenticationController.java
│   ├── service/
│   │   ├── AuthenticationService.java
│   │   └── JwtTokenProvider.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── RefreshTokenRequest.java
│       └── RefreshTokenResponse.java
├── profile/                   # User Management (including registration)
│   ├── controller/
│   │   ├── UserController.java
│   │   └── OtpController.java
│   ├── service/
│   │   ├── OtpService.java
│   │   ├── PhoneNumberValidator.java
│   │   └── SmsService.java
│   ├── model/
│   │   ├── User.java
│   │   ├── UserProfile.java
│   │   └── OtpSession.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── OtpSessionRepository.java
│   └── dto/
│       ├── OtpRequest.java
│       ├── OtpResponse.java
│       ├── OtpVerificationRequest.java
│       └── OtpVerificationResponse.java
├── guest/                     # Guest User Management
│   ├── controller/
│   │   └── GuestUserController.java
│   ├── service/
│   │   ├── GuestUserService.java
│   │   └── DeviceFingerprintService.java
│   ├── model/
│   │   └── GuestUser.java
│   ├── repository/
│   │   └── GuestUserRepository.java
│   └── dto/
│       ├── GuestUserRequest.java
│       ├── GuestUserResponse.java
│       └── GuestSessionResponse.java
└── password/                  # Password Management
    ├── controller/
    ├── service/
    │   └── PasswordService.java
    └── dto/
```

## 🔗 **Domain Relationships**

### **1. Profile Domain (Core User Management)**
**Responsibilities:**
- ✅ User registration (OTP verification)
- ✅ User profile management
- ✅ User CRUD operations
- ✅ Shared User entity and repository

**Key Components:**
- `User` entity (shared across domains)
- `UserRepository` (shared across domains)
- `OtpService` (registration workflow)
- `UserController` (profile management)

**Relationships:**
- **Provides** User entity to Auth domain
- **Provides** User entity to Guest domain (for conversion)
- **Consumes** Auth domain for user creation after OTP verification

### **2. Auth Domain (Authentication & Authorization)**
**Responsibilities:**
- ✅ JWT token generation and validation
- ✅ User login/logout
- ✅ Token refresh
- ✅ Password validation

**Key Components:**
- `AuthenticationService` (login/logout logic)
- `JwtTokenProvider` (token management)
- `AuthenticationController` (auth endpoints)

**Relationships:**
- **Consumes** User entity from Profile domain
- **Consumes** UserRepository from Profile domain
- **Provides** authentication context to other domains

### **3. Guest Domain (Guest User Management)**
**Responsibilities:**
- ✅ Guest user creation and session management
- ✅ Device fingerprinting
- ✅ Guest-to-user conversion tracking
- ✅ Session limitations and expiry

**Key Components:**
- `GuestUser` entity (separate from User)
- `GuestUserService` (guest lifecycle management)
- `DeviceFingerprintService` (device identification)

**Relationships:**
- **Consumes** User entity from Profile domain (for conversion)
- **Provides** guest session data to Profile domain
- **Independent** domain with its own lifecycle

### **4. Password Domain (Password Management)**
**Responsibilities:**
- ✅ Password reset workflows
- ✅ Password change operations
- ✅ Password validation rules

**Key Components:**
- `PasswordService` (password operations)
- Password-specific DTOs and controllers

**Relationships:**
- **Consumes** Auth domain for authentication
- **Consumes** User entity from Profile domain
- **Provides** password management to Profile domain

## 🔄 **Data Flow & Interactions**

### **User Registration Flow:**
```
1. Guest User Creation (Guest Domain)
   ↓
2. OTP Registration (Profile Domain)
   ↓
3. User Account Creation (Profile Domain)
   ↓
4. Authentication Setup (Auth Domain)
```

### **User Authentication Flow:**
```
1. Login Request (Auth Domain)
   ↓
2. User Validation (Profile Domain)
   ↓
3. JWT Token Generation (Auth Domain)
   ↓
4. Session Management (Auth Domain)
```

### **Guest-to-User Conversion Flow:**
```
1. Guest Session (Guest Domain)
   ↓
2. Conversion Trigger (Guest Domain)
   ↓
3. User Registration (Profile Domain)
   ↓
4. Account Linking (Profile Domain)
```

## 📊 **API Endpoints by Domain**

### **Auth Domain Endpoints:**
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Token refresh
- `GET /api/auth/health` - Auth service health

### **Profile Domain Endpoints:**
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/phone/{phoneNumber}` - Get user by phone
- `GET /api/users/active` - Get all active users
- `PUT /api/users/{userId}/status` - Update user status
- `GET /api/users/health` - User service health
- `POST /api/v1/auth/phone/send-otp` - Send OTP
- `POST /api/v1/auth/phone/verify-otp` - Verify OTP
- `POST /api/v1/auth/phone/resend-otp` - Resend OTP
- `GET /api/v1/auth/phone/health` - OTP service health

### **Guest Domain Endpoints:**
- `POST /api/v1/auth/guest/create` - Create guest user
- `GET /api/v1/auth/guest/session` - Get guest session
- `POST /api/v1/auth/guest/action` - Record guest action
- `POST /api/v1/auth/guest/conversion-prompt-shown` - Record conversion prompt
- `GET /api/v1/auth/guest/health` - Guest service health

## 🎯 **Benefits of Consolidated Structure**

### **1. Single Source of Truth**
- ✅ User entity managed in one place (Profile domain)
- ✅ No duplicate user management logic
- ✅ Consistent user data across domains

### **2. Clear Domain Boundaries**
- ✅ Each domain has specific responsibilities
- ✅ Minimal cross-domain dependencies
- ✅ Easy to understand and maintain

### **3. Improved Maintainability**
- ✅ Reduced code duplication
- ✅ Centralized user management
- ✅ Clear separation of concerns

### **4. Better Testability**
- ✅ Domain-specific tests
- ✅ Isolated unit tests
- ✅ Clear integration points

## 🔧 **Configuration Management**

### **Global Configurations (`user/config/`):**
- `JwtConfig` - JWT token configuration
- `SecurityConfig` - Spring Security settings
- `SwaggerConfig` - API documentation
- `GlobalExceptionHandler` - Error handling

### **Domain-Specific Configurations:**
- Each domain can have its own configuration if needed
- Global configs are shared across all domains
- Consistent configuration patterns

## 📈 **Future Enhancements**

### **Planned Improvements:**
1. **Event-Driven Architecture** - Add domain events for cross-domain communication
2. **CQRS Pattern** - Separate read and write models for complex queries
3. **Saga Pattern** - For complex multi-domain workflows
4. **API Gateway Integration** - Centralized routing and security

### **Monitoring & Observability:**
1. **Health Checks** - All domains have health endpoints
2. **Metrics Collection** - Domain-specific metrics
3. **Distributed Tracing** - Cross-domain request tracking
4. **Logging Standards** - Consistent logging across domains

## ✅ **Validation Results**

### **Application Status:**
- ✅ Application starts successfully
- ✅ All health endpoints responding
- ✅ Swagger documentation working
- ✅ API endpoints properly configured
- ✅ Domain relationships functioning correctly

### **Test Results:**
- ✅ All unit tests passing
- ✅ Guest user tests working
- ✅ Device fingerprinting tests working
- ✅ Build successful with no errors

---

**Last Updated:** August 3, 2025  
**Version:** 1.0.0  
**Status:** ✅ Production Ready 