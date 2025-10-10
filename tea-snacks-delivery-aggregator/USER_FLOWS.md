# User Flows Documentation

This document outlines all the user flows available in the Tea Snacks Delivery Aggregator User Management Service.

## Table of Contents

1. [Phone OTP Registration Flow](#phone-otp-registration-flow)
2. [Guest User Management Flow](#guest-user-management-flow)
3. [Email Registration Flow](#email-registration-flow)
4. [JWT Authentication Flow](#jwt-authentication-flow)
5. [User Profile Management Flow](#user-profile-management-flow)
6. [Health Check Flows](#health-check-flows)
7. [API Documentation Flow](#api-documentation-flow)

---

## 📱 Phone OTP Registration Flow

**Story**: BE-002-01A - Phone OTP Registration  
**Status**: ✅ COMPLETED

### Flow Overview
Complete user registration process using phone number verification with OTP.

### Step-by-Step Flow

#### 1. Send OTP
**Endpoint**: `POST /api/v1/auth/phone/send-otp`  
**Purpose**: Initiate OTP verification process

**Request**:
```json
{
  "phoneNumber": "+1234567890"
}
```

**Response**:
```json
{
  "message": "OTP sent successfully",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 2. Verify OTP
**Endpoint**: `POST /api/v1/auth/phone/verify-otp`  
**Purpose**: Verify the OTP code

**Request**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "otp": "123456"
}
```

**Response**:
```json
{
  "verified": true,
  "message": "OTP verified successfully"
}
```

#### 3. Complete Registration
**Endpoint**: `POST /api/v1/auth/phone/register`  
**Purpose**: Create user account after OTP verification

**Request**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "password": "password123"
}
```

**Response**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "message": "User registered successfully"
}
```

### User Journey
```
User enters phone → OTP sent → User enters OTP → User provides details → Account created
```

---

## 👤 Guest User Management Flow

**Story**: BE-002-01B - Guest User Management  
**Status**: ✅ COMPLETED

### Flow Overview
Allow users to browse as guests and convert to registered users later.

### Step-by-Step Flow

#### 1. Create Guest User
**Endpoint**: `POST /api/v1/auth/guest/create`  
**Purpose**: Create a guest user session

**Request**:
```json
{
  "deviceId": "device_123456789",
  "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)"
}
```

**Response**:
```json
{
  "guestUserId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionToken": "guest_session_token_123",
  "message": "Guest user created successfully"
}
```

#### 2. Record Guest Actions
**Endpoint**: `POST /api/v1/auth/guest/action`  
**Purpose**: Track guest user behavior

**Request**:
```json
{
  "sessionToken": "guest_session_token_123",
  "action": "BROWSE_MENU"
}
```

**Response**:
```json
{
  "actionRecorded": true,
  "message": "Action recorded successfully"
}
```

#### 3. Convert to Registered User
**Endpoint**: `POST /api/auth/guest/convert`  
**Purpose**: Convert guest user to registered user

**Request**:
```json
{
  "guestUserId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "guest@example.com",
  "password": "password123",
  "name": "Guest User",
  "phoneNumber": "+1234567890"
}
```

**Response**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "guest@example.com",
  "role": "CUSTOMER",
  "userType": "REGISTERED",
  "profileCompletion": 25,
  "message": "Guest user converted successfully"
}
```

### User Journey
```
Guest browses → Records actions → Gets conversion prompt → Converts to registered user
```

---

## 📧 Email Registration Flow

**Story**: BE-002-02 - JWT Authentication Service  
**Status**: ✅ COMPLETED

### Flow Overview
Direct user registration using email and password with immediate JWT token generation.

### Step-by-Step Flow

#### 1. Direct Registration
**Endpoint**: `POST /api/auth/register/email`  
**Purpose**: Register user with email and receive tokens immediately

**Request**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe",
  "phoneNumber": "+1234567890"
}
```

**Response**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "user@example.com",
  "role": "CUSTOMER",
  "userType": "REGISTERED",
  "profileCompletion": 25,
  "message": "User registered successfully"
}
```

### User Journey
```
User provides email/password → Account created → Immediate login with tokens
```

---

## 🔐 JWT Authentication Flow

**Story**: BE-002-02 - JWT Authentication Service  
**Status**: ✅ COMPLETED

### Flow Overview
Complete authentication system with JWT tokens, refresh, and logout functionality.

### Step-by-Step Flow

#### 1. Login
**Endpoint**: `POST /api/auth/login`  
**Purpose**: Authenticate user and receive JWT tokens

**Request**:
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 2. Refresh Token
**Endpoint**: `POST /api/auth/refresh`  
**Purpose**: Get new access token using refresh token

**Request**:
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 3. Logout
**Endpoint**: `POST /api/auth/logout`  
**Purpose**: Logout user and invalidate tokens

**Request**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "message": "Logged out successfully"
}
```

### User Journey
```
User enters credentials → JWT tokens issued → User uses tokens → Refresh when needed → Logout
```

---

## 👤 User Profile Management Flow

**Story**: BE-002-01C - User Profile Management  
**Status**: ✅ COMPLETED

### Flow Overview
Complete user profile management with CRUD operations.

### Step-by-Step Flow

#### 1. Get User by ID
**Endpoint**: `GET /api/users/{userId}`  
**Purpose**: Retrieve user information by ID

**Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "John Doe",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "role": "CUSTOMER",
  "userType": "REGISTERED",
  "status": "ACTIVE",
  "profileCompletionPercentage": 25,
  "createdAt": "2025-08-03T14:30:00Z",
  "updatedAt": "2025-08-03T14:30:00Z"
}
```

#### 2. Get User by Email
**Endpoint**: `GET /api/users/email/{email}`  
**Purpose**: Retrieve user information by email

**Response**: Same as Get User by ID

#### 3. Get User by Phone
**Endpoint**: `GET /api/users/phone/{phoneNumber}`  
**Purpose**: Retrieve user information by phone number

**Response**: Same as Get User by ID

#### 4. Get All Active Users
**Endpoint**: `GET /api/users/active`  
**Purpose**: Retrieve all active users

**Response**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "John Doe",
    "email": "user@example.com",
    "status": "ACTIVE"
  }
]
```

#### 5. Update User Status
**Endpoint**: `PUT /api/users/{userId}/status`  
**Purpose**: Update user status

**Request**:
```json
{
  "status": "ACTIVE"
}
```

**Response**:
```json
{
  "message": "User status updated successfully"
}
```

### User Journey
```
Admin/System queries user data → Retrieves user information → Updates user status
```

---

## 🏥 Health Check Flows

**Status**: ✅ COMPLETED

### Available Health Checks

#### 1. Authentication Health
**Endpoint**: `GET /api/auth/health`  
**Response**: `"Authentication service is healthy"`

#### 2. User Management Health
**Endpoint**: `GET /api/users/health`  
**Response**: `"User management service is healthy"`

#### 3. OTP Service Health
**Endpoint**: `GET /api/v1/auth/phone/health`  
**Response**: `"OTP service is healthy"`

#### 4. Guest Service Health
**Endpoint**: `GET /api/v1/auth/guest/health`  
**Response**: `"Guest service is healthy"`

---

## 📊 API Documentation Flow

**Status**: ✅ COMPLETED

### Access Points

#### 1. Swagger UI
**URL**: `http://localhost:8080/swagger-ui/index.html`  
**Purpose**: Interactive API documentation and testing

#### 2. OpenAPI Documentation
**URL**: `http://localhost:8080/v3/api-docs`  
**Purpose**: Raw OpenAPI specification

### User Journey
```
Developer opens Swagger → Browses available endpoints → Tests API calls → Views responses
```

---

## 🎯 Complete User Journey Map

### New User Journey
```
1. Guest User (Optional)
   ↓
2. Phone OTP Registration OR Email Registration
   ↓
3. Account Created with JWT Tokens
   ↓
4. Login/Refresh/Logout Cycle
   ↓
5. Profile Management
```

### Returning User Journey
```
1. Login with Credentials
   ↓
2. Receive JWT Tokens
   ↓
3. Use Protected Endpoints
   ↓
4. Refresh Tokens as Needed
   ↓
5. Logout
```

---

## 📈 Flow Completion Status

| Flow | Status | Implementation |
|------|--------|----------------|
| **Phone OTP Registration** | ✅ **COMPLETE** | Full implementation with rate limiting |
| **Guest User Management** | ✅ **COMPLETE** | Create, track actions, convert |
| **Email Registration** | ✅ **COMPLETE** | Direct registration with tokens |
| **JWT Authentication** | ✅ **COMPLETE** | Login, refresh, logout |
| **User Profile Management** | ✅ **COMPLETE** | CRUD operations |
| **Health Checks** | ✅ **COMPLETE** | All services covered |
| **API Documentation** | ✅ **COMPLETE** | Swagger UI available |

**All core user flows are now implemented and functional!** 🚀

---

## 🔧 Testing the Flows

To test all flows, ensure the application is running:

```bash
./gradlew :user-management-service:bootRun
```

Then access the Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

Or use curl commands as shown in each flow section above. 