# Story: Phone Number Registration with OTP Verification

**Story ID:** BE-002-01A  
**Story Points:** 5  
**Priority:** High  
**Sprint:** 1  

### User Story
**As a** new user of the Tea & Snacks Delivery platform  
**I want** to register using my phone number with OTP verification  
**So that** I can quickly create an account without remembering passwords  

### Acceptance Criteria
- [ ] Users can initiate phone registration by entering phone number
- [ ] OTP is generated and sent via SMS to the provided phone number
- [ ] Users can verify OTP within 5 minutes of generation
- [ ] Phone number uniqueness is validated before sending OTP
- [ ] OTP has configurable expiry time (default: 5 minutes)
- [ ] Users can request OTP resend with rate limiting (max 3 attempts per 10 minutes)
- [ ] Invalid OTP attempts are limited (max 5 attempts per session)
- [ ] Successful OTP verification creates user account with JWT tokens
- [ ] Phone registration events are published to Kafka
- [ ] OTP generation and validation are logged for audit

### Technical Tasks
1. [ ] Create OtpService for generation and validation
2. [ ] Implement phone number validation and formatting
3. [ ] Create OTP storage mechanism (Redis with TTL)
4. [ ] Implement rate limiting for OTP requests
5. [ ] Add OTP verification endpoints
6. [ ] Integrate with SMS service (BE-002-09 dependency)
7. [ ] Add phone registration event publishing
8. [ ] Implement OTP session management
9. [ ] Add comprehensive logging and monitoring
10. [ ] Write unit and integration tests

### API Specification

#### 1. Send OTP
```http
POST /api/v1/auth/phone/send-otp
Content-Type: application/json

{
  "phoneNumber": "+91-9876543210"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "sessionId": "otp_session_uuid",
  "expiryMinutes": 5,
  "resendAllowed": true,
  "attemptsRemaining": 3
}
```

#### 2. Verify OTP and Register
```http
POST /api/v1/auth/phone/verify-otp
Content-Type: application/json

{
  "sessionId": "otp_session_uuid",
  "phoneNumber": "+91-9876543210",
  "otp": "123456",
  "name": "John Doe",
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "userId": "uuid",
    "accessToken": "jwt_token",
    "refreshToken": "refresh_token",
    "userType": "REGISTERED",
    "profileCompletion": 25
  }
}
```

#### 3. Resend OTP
```http
POST /api/v1/auth/phone/resend-otp
Content-Type: application/json

{
  "sessionId": "otp_session_uuid",
  "phoneNumber": "+91-9876543210"
}
```

### Business Rules
- Phone numbers must be in international format (+country_code-number)
- OTP is 6-digit numeric code
- OTP expires after 5 minutes
- Maximum 3 OTP requests per phone number per 10 minutes
- Maximum 5 OTP verification attempts per session
- Phone number must be unique across all users
- Successful verification automatically creates user account

### Error Handling
- **Invalid phone number format**: Return validation error
- **Phone number already registered**: Return conflict error
- **Rate limit exceeded**: Return rate limit error with retry time
- **Invalid/expired OTP**: Return validation error with attempts remaining
- **SMS delivery failure**: Return service error with retry option

### Dependencies
- **BE-002-09**: SMS notification service for OTP delivery
- **BE-002-07**: Redis integration for OTP storage and rate limiting
- **BE-002-08**: Kafka integration for event publishing

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] Code follows project coding standards
- [ ] Unit tests coverage > 80%
- [ ] Integration tests verify OTP flow end-to-end
- [ ] API documentation is complete
- [ ] Code review is completed
- [ ] Performance meets requirements (OTP generation < 200ms)
- [ ] Security review completed (OTP generation is cryptographically secure)
- [ ] SMS integration tested with real provider
- [ ] Rate limiting tested under load
