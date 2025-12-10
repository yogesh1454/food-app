# Epic 2: User Management Service

**Epic ID:** BE-002  
**Priority:** Critical (P0)  
**Business Value:** Enables user registration, authentication, and authorization for all platform users  
**Estimated Effort:** 2-3 sprints  
**Dependencies:** Epic 1 (Local Development Foundation)  

## Description
Develop the User Management Service that handles authentication, authorization, user registration, and profile management for customers, vendors, delivery partners, and administrators in the Tea & Snacks Delivery Aggregator platform.

## Business Justification
User management is fundamental to the platform's operation:
- Enables secure access control for different user types
- Supports B2B and B2C customer segments
- Provides foundation for personalized experiences
- Ensures compliance with security requirements
- Enables role-based feature access

## Key Components
- **User Registration System**: Multi-type user registration (Customer, Vendor, Delivery Partner, Admin)
- **Authentication Service**: JWT-based authentication with refresh tokens
- **Authorization Framework**: Role-based access control (RBAC) implementation
- **Profile Management**: User profile CRUD operations with validation
- **Password Management**: Secure password reset and change functionality
- **B2B User Support**: Company association and internal delivery points
- **Session Management**: Local Redis-based session handling

## Acceptance Criteria
- [ ] Users can register with different user types (Customer, Vendor, Delivery Partner, Admin)
- [ ] Authentication via JWT tokens works correctly with proper expiration
- [ ] Role-based authorization is enforced across all endpoints
- [ ] User profiles can be created, read, updated with proper validation
- [ ] Password reset flow works via email with secure tokens
- [ ] B2B users can be associated with companies and internal delivery points
- [ ] All endpoints follow REST API specification from architecture document
- [ ] Integration with local Redis for session and token management
- [ ] Proper error handling with standardized error responses
- [ ] Input validation prevents injection attacks and malformed data
- [ ] Audit logging for all authentication and authorization events
- [ ] Rate limiting on authentication endpoints to prevent brute force

## Technical Requirements
- **Framework**: Spring Boot 3.2.x with Spring Security
- **Database**: Local PostgreSQL with users table schema
- **Caching**: Local Redis for session management and token blacklisting
- **Security**: JWT tokens with RS256 signing, bcrypt password hashing
- **Validation**: Hibernate Validator for input validation
- **Testing**: JUnit 5 with Mockito, TestContainers for integration tests

## API Endpoints
- `POST /auth/register` - User registration
- `POST /auth/login` - User login with JWT generation
- `POST /auth/refresh` - Token refresh
- `POST /auth/logout` - User logout with token invalidation
- `GET /users/{userId}` - Fetch user profile
- `PUT /users/{userId}` - Update user profile
- `POST /users/password-reset` - Initiate password reset
- `PUT /users/password` - Change password

## Infrastructure Requirements

### Current Infrastructure (Minimal Setup)
- **PostgreSQL**: User data, authentication records, profiles
- **Redis**: Session management, JWT token blacklisting, user profile caching
- **Kafka**: User events, authentication events, notification triggers
- **User Management Service**: Core authentication and user management
- **Notification Service**: Password reset emails, welcome messages

### Infrastructure Scaling Commands
```bash
# Start minimal Epic 2 infrastructure
./infrastructure/docker/start-minimal-epic2.sh

# Optimize resources (stop non-essential services)
./infrastructure/docker/stop-heavy-services.sh

# Scale up when needed for integration with other epics
docker-compose up -d
```

### Dependencies on Other Epic Infrastructure
- **Epic 3+**: Will reuse PostgreSQL for order data relationships
- **Epic 4**: Will reuse Redis for search result caching
- **Epic 7-9**: Will add monitoring stack (Prometheus/Grafana)
- **Epic 10**: Will migrate to cloud infrastructure

## Success Metrics
- Authentication response time < 100ms
- Zero critical security vulnerabilities
- Support for 1,000+ concurrent authenticated users
- 99.9% authentication success rate
- Password reset completion rate > 90%
