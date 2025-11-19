# Story: Basic Email Registration Implementation

**Story ID:** BE-002-01  
**Story Points:** 3  
**Priority:** Critical  
**Sprint:** 1  
**Status:** ✅ COMPLETED

### User Story
**As a** new user of the Tea & Snacks Delivery platform  
**I want** to register an account using my email and password  
**So that** I can create a secure account and access the platform  

### Acceptance Criteria
- [x] Users can register with email and password
- [x] Email uniqueness is validated
- [x] Password strength requirements are enforced (minimum 6 characters)
- [x] User profile is created automatically after registration
- [x] JWT access and refresh tokens are generated
- [x] User account is stored in PostgreSQL database
- [x] Input validation prevents SQL injection and XSS attacks
- [x] Registration response includes user details and tokens

### Technical Tasks
1. [x] Create User and UserProfile database schema
2. [x] Implement UserRepository with Spring Data JPA
3. [x] Create RegistrationRequest DTOs for email registration
4. [x] Implement input validation using Hibernate Validator
5. [x] Create RegistrationService with email registration logic
6. [x] Add email registration endpoint in RegistrationController
7. [x] Configure password encoding with BCrypt
8. [x] Implement JWT token generation service
9. [x] Database migration scripts (Flyway)
10. [x] Spring Security configuration for stateless authentication

### Moved to Other Stories
- **Phone OTP Registration** → BE-002-01A
- **Social Login** → BE-002-10
- **Guest User Management** → BE-002-01B
- **Rate Limiting** → BE-002-02 (Authentication Service)
- **Kafka Event Publishing** → BE-002-08
- **Comprehensive Testing** → BE-002-01C

### API Specification

#### 1. Email Registration
```http
POST /api/v1/auth/register/email
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe",
  "phoneNumber": "+91-9876543210"
}
```

#### 2. Phone Registration (OTP Flow)
```http
# Step 1: Send OTP
POST /api/v1/auth/phone/send-otp
Content-Type: application/json

{
  "phoneNumber": "+91-9876543210"
}

# Step 2: Verify OTP and Register
POST /api/v1/auth/phone/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+91-9876543210",
  "otp": "123456",
  "name": "John Doe",
  "email": "user@example.com"
}
```

#### 3. Social Registration
```http
POST /api/v1/auth/register/social
Content-Type: application/json

{
  "provider": "GOOGLE",
  "socialToken": "google_oauth_token",
  "name": "John Doe",
  "email": "user@gmail.com"
}
```

#### 4. Guest User Creation
```http
POST /api/v1/auth/guest/create
Content-Type: application/json

{
  "deviceId": "unique_device_identifier",
  "userAgent": "Mozilla/5.0..."
}
```

#### 5. Guest to Registered User Conversion
```http
POST /api/v1/auth/guest/convert
Content-Type: application/json

{
  "guestUserId": "guest_user_id",
  "registrationData": {
    "email": "user@example.com",
    "password": "securePassword123",
    "name": "John Doe",
    "phoneNumber": "+91-9876543210"
  }
}
```

#### Response Format
```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "token": "jwt_token",
    "refreshToken": "refresh_token",
    "userType": "REGISTERED|GUEST",
    "profileCompletion": 25
  },
  "message": "Registration successful"
}
```

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] Code follows project coding standards
- [ ] Unit tests coverage > 80%
- [ ] Integration tests verify all registration flows
- [ ] API documentation is complete
- [ ] Code review is completed
- [ ] Registration performance meets requirements (< 500ms) 