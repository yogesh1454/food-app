# Story: User Profile Management Implementation

**Story ID:** BE-002-04  
**Story Points:** 5  
**Priority:** High  
**Sprint:** 2  
**Status:** 🔄 **IN PROGRESS**  

### User Story
**As a** registered user  
**I want** to manage my profile information  
**So that** I can keep my account details up to date  

### Acceptance Criteria
- [ ] Users can view their profile details
- [ ] Users can update their profile information
- [ ] Profile updates are properly validated
- [ ] Address information can be managed
- [ ] B2B users can update company details
- [ ] Vendors can update business information
- [ ] Delivery partners can update vehicle details
- [ ] Profile picture upload/update is supported
- [ ] Email/phone updates require verification
- [ ] Profile update history is maintained

### Technical Tasks
1. [x] Create ProfileController endpoints
2. [x] Implement ProfileService
3. [x] Create profile update DTOs
4. [x] Add input validation for profile updates
5. [x] Implement file upload for profile pictures
6. [ ] Create email/phone verification flow
7. [x] Add address management functionality
8. [ ] Implement profile history tracking
9. [x] Configure audit logging
10. [ ] Write unit and integration tests

### API Specification
```yaml
GET /users/{userId}/profile
Headers:
  Authorization: Bearer {token}
Response:
  200 OK:
    {
      "user_id": "uuid",
      "email": "string",
      "phone_number": "string",
      "first_name": "string",
      "last_name": "string",
      "profile_picture_url": "string",
      "addresses": [{
        "id": "uuid",
        "type": "HOME|WORK|OTHER",
        "street": "string",
        "city": "string",
        "state": "string",
        "postal_code": "string"
      }],
      "company_details": {  # For B2B users
        "company_name": "string",
        "internal_delivery_point": "string"
      },
      "business_details": {  # For vendors
        "business_name": "string",
        "business_type": "string"
      },
      "vehicle_details": {  # For delivery partners
        "vehicle_type": "string",
        "vehicle_number": "string"
      }
    }

PUT /users/{userId}/profile
Headers:
  Authorization: Bearer {token}
Request:
  {
    "first_name": "string",
    "last_name": "string",
    "addresses": [{
      "type": "HOME|WORK|OTHER",
      "street": "string",
      "city": "string",
      "state": "string",
      "postal_code": "string"
    }],
    # Other fields based on user type
  }
Response:
  200 OK:
    {
      "message": "Profile updated successfully"
    }
  400 Bad Request:
    {
      "error": "Validation failed",
      "details": ["Invalid address format"]
    }

POST /users/{userId}/profile/picture
Headers:
  Authorization: Bearer {token}
  Content-Type: multipart/form-data
Request:
  - file: (binary)
Response:
  200 OK:
    {
      "profile_picture_url": "string"
    }
```

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] Code follows project coding standards
- [ ] Unit tests coverage > 80%
- [ ] Integration tests verify all profile operations
- [ ] API documentation is complete
- [ ] Code review is completed
- [ ] Profile updates complete within 500ms
- [ ] File upload size limits are enforced 