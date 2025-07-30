# Story: Multi-Type User Registration Implementation

**Story ID:** BE-002-01  
**Story Points:** 8  
**Priority:** Critical  
**Sprint:** 1  

### User Story
**As a** new user of the Tea & Snacks Delivery platform  
**I want** to register an account using multiple authentication methods (email, phone, social)  
**So that** I can quickly access the platform and start using personalized features  

### Acceptance Criteria
- [ ] Users can register with email and password
- [ ] Users can register with phone number and OTP verification
- [ ] Users can register using social login (Google, Facebook, Apple)
- [ ] Guest user accounts can be created
- [ ] Guest users can convert to registered users
- [ ] Email and phone number uniqueness is validated
- [ ] Password strength requirements are enforced
- [ ] OTP verification is required for phone registration
- [ ] Social login tokens are validated
- [ ] Basic profile creation is initiated after registration
- [ ] Input validation prevents SQL injection and XSS attacks
- [ ] Registration events are published to Kafka
- [ ] Rate limiting is applied to prevent abuse
- [ ] JWT tokens are generated for authenticated sessions

### Technical Tasks
1. [ ] Create User and UserProfile database schema
2. [ ] Implement UserRepository with Spring Data JPA
3. [ ] Create RegistrationRequest DTOs for different auth methods
4. [ ] Implement input validation using Hibernate Validator
5. [ ] Create UserService with multi-type registration logic
6. [ ] Implement OTP service for phone verification
7. [ ] Implement social login validation service
8. [ ] Create guest user management service
9. [ ] Add registration endpoints in AuthController
10. [ ] Configure password encoding with BCrypt
11. [ ] Implement rate limiting for registration endpoints
12. [ ] Add Kafka event publishing for registration events
13. [ ] Implement JWT token generation service
14. [ ] Write unit and integration tests

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