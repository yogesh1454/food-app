# Story: Authentication-Authorization Integration

**Story ID:** BE-002-13  
**Story Points:** 3  
**Priority:** High  
**Sprint:** 2  
**Status:** ✅ **COMPLETED**

### User Story
**As a** system administrator  
**I want** the authentication and authorization systems to work together seamlessly  
**So that** users can access resources based on their roles and permissions automatically  

### Acceptance Criteria
- [ ] JWT tokens include user roles from the database
- [ ] Authorization framework reads roles from JWT tokens
- [ ] Role-based permissions are enforced automatically
- [ ] Resource ownership validation works with authenticated users
- [ ] Authorization failures return proper error messages
- [ ] Integration works with all user types (ADMIN, VENDOR, DELIVERY_PARTNER, CUSTOMER)
- [ ] Performance impact is minimal (< 50ms additional latency)
- [ ] Comprehensive logging for authorization decisions

### Technical Tasks
1. [ ] Update JWT token generation to include user roles
2. [ ] Modify JWT token parsing to extract roles
3. [ ] Update AuthorizationService to read roles from JWT claims
4. [ ] Integrate role-based permission checking with authentication
5. [ ] Test authorization with authenticated users
6. [ ] Implement proper error handling for authorization failures
7. [ ] Add comprehensive logging for authorization decisions
8. [ ] Performance testing and optimization
9. [ ] Update API documentation with authorization examples
10. [ ] Integration testing with all user roles

### Integration Points

#### **JWT Token Enhancement**
```java
// Current JWT token structure
{
  "userId": "uuid",
  "username": "email@example.com",
  "roles": ["CUSTOMER"]  // ← Add this
}

// Enhanced JWT token structure
{
  "userId": "uuid",
  "username": "email@example.com",
  "roles": ["ADMIN", "VENDOR"],  // ← Multiple roles support
  "permissions": ["users:manage", "profile:manage"]  // ← Optional permissions
}
```

#### **Authorization Service Integration**
```java
// Current approach
private User.Role getUserRoleFromAuthentication(Authentication authentication) {
    // Extract from JWT claims
    String roleString = authentication.getAuthorities().stream()
        .findFirst()
        .map(Object::toString)
        .orElse("CUSTOMER");
    return User.Role.valueOf(roleString.replace("ROLE_", ""));
}

// Enhanced approach
private User.Role getUserRoleFromAuthentication(Authentication authentication) {
    // Extract from JWT claims with proper error handling
    Claims claims = extractClaimsFromAuthentication(authentication);
    String roleString = claims.get("roles", String.class);
    return User.Role.valueOf(roleString);
}
```

#### **Resource Ownership Validation**
```java
// Enhanced resource ownership check
public boolean isResourceOwner(Authentication authentication, UUID resourceOwnerId) {
    Claims claims = extractClaimsFromAuthentication(authentication);
    String userId = claims.get("userId", String.class);
    String userRole = claims.get("roles", String.class);
    
    // Admin can access all resources
    if ("ADMIN".equals(userRole)) {
        return true;
    }
    
    // Other users can only access their own resources
    return userId.equals(resourceOwnerId.toString());
}
```

### API Endpoints to Test

#### **Admin-Only Endpoints**
```bash
# Should work for ADMIN users
curl -H "Authorization: Bearer {admin-token}" \
     -X GET http://localhost:8080/api/test/auth/admin-only

# Should fail for CUSTOMER users
curl -H "Authorization: Bearer {customer-token}" \
     -X GET http://localhost:8080/api/test/auth/admin-only
```

#### **Role-Based Endpoints**
```bash
# Vendor menu management
curl -H "Authorization: Bearer {vendor-token}" \
     -X GET http://localhost:8080/api/test/auth/vendor-menu/{vendorId}

# Customer profile access
curl -H "Authorization: Bearer {customer-token}" \
     -X GET http://localhost:8080/api/test/auth/customer-profile/{userId}
```

#### **Authorization Management**
```bash
# Get user permissions
curl -H "Authorization: Bearer {token}" \
     -X GET http://localhost:8080/api/auth/authorization/permissions

# Check specific permission
curl -H "Authorization: Bearer {token}" \
     -X POST http://localhost:8080/api/auth/authorization/check \
     -H "Content-Type: application/json" \
     -d '{"resource":"users","action":"manage"}'
```

### Database Schema Updates
```sql
-- No new tables needed, using existing:
-- users.role (already exists)
-- permissions (already exists)
-- role_permissions (already exists)

-- Ensure JWT tokens include role information
-- Update authentication service to include roles in token generation
-- Update authorization service to read roles from JWT claims
```

### Definition of Done
- [ ] All acceptance criteria are met and verified
- [ ] JWT tokens include user roles correctly
- [ ] Authorization framework reads roles from JWT tokens
- [ ] Role-based access control works for all user types
- [ ] Resource ownership validation works correctly
- [ ] Performance impact is measured and acceptable
- [ ] Comprehensive logging is implemented
- [ ] Integration tests pass for all scenarios
- [ ] API documentation is updated
- [ ] Security review is completed

### Dependencies
- BE-002-02 (JWT Authentication Service) - ✅ Completed
- BE-002-03 (Authorization Framework) - ✅ Completed
- Database with user roles and permissions - ✅ Ready

### Related Backlog Items
- BE-002-14 (Security Enhancements for Production Deployment) - 📋 Backlog
  - Account lockout mechanism
  - Additional security headers (HSTS, CSP)
  - JWT token blacklisting
  - Input sanitization
  - Security monitoring and alerting

### Risk Assessment
- **Low Risk**: JWT token modification
- **Low Risk**: Authorization service integration
- **Medium Risk**: Performance impact of role checking
- **Low Risk**: Backward compatibility

### Success Metrics
- JWT tokens include user roles
- Authorization decisions are made based on JWT roles
- All user types can access appropriate resources
- Performance impact < 50ms
- 100% test coverage for integration scenarios 