# Epic 3: Order & Catalog Management Service

**Epic ID:** BE-003  
**Priority:** Critical (P0)  
**Business Value:** Core business functionality for managing vendors, menus, and order processing  
**Estimated Effort:** 4-5 sprints  
**Dependencies:** Epic 1 (Local Development Foundation), Epic 2 (User Management Service)  

## Description
Develop the consolidated Order & Catalog Management Service that handles vendor onboarding, menu management, order lifecycle, and basic reporting functionality for the Tea & Snacks Delivery Aggregator platform.

## Business Justification
This service is the core business logic that enables:
- Vendor onboarding and management for tea & snack providers
- Menu catalog management with real-time availability
- Complete order lifecycle from placement to completion
- Support for specialized delivery scenarios (trains, buses, factories)
- B2B order processing with internal delivery points
- Basic sales reporting and analytics

## Key Components
- **Vendor Management**: Registration, profile management, operating hours, status tracking
- **Menu Management**: CRUD operations for menu items, categories, pricing, availability
- **Order Processing**: Order creation, validation, status tracking, lifecycle management
- **Catalog Service**: Menu browsing, vendor discovery, availability checking
- **Reporting Service**: Sales reports, vendor performance, order analytics
- **Event Publishing**: Local Kafka integration for order/vendor events
- **Specialized Delivery**: Support for train, bus, and factory delivery scenarios

## Acceptance Criteria
- [ ] Vendors can register and manage their profiles with complete information
- [ ] Menu items can be created, updated, deleted with proper validation
- [ ] Orders can be placed with validation for availability and business rules
- [ ] Order status can be tracked through complete lifecycle (PENDING → DELIVERED)
- [ ] Vendor operating hours and availability are properly enforced
- [ ] Menu versioning supports cache invalidation strategies
- [ ] Basic sales reports are generated with key metrics
- [ ] Events are published to local Kafka for order and vendor state changes
- [ ] Support for B2B orders with company associations and internal delivery points
- [ ] Support for captive audience delivery (trains, buses) with scheduling
- [ ] Factory delivery with internal location mapping
- [ ] Proper error handling with standardized error responses
- [ ] Input validation prevents invalid orders and data corruption

## Technical Requirements
- **Framework**: Spring Boot 3.2.x with Spring Data JPA
- **Database**: Local PostgreSQL with vendors, menu_items, orders, order_items tables
- **Messaging**: Local Kafka for event publishing
- **Caching**: Local Redis for menu caching and popular items
- **Validation**: Hibernate Validator for business rule enforcement
- **Testing**: JUnit 5, Mockito, TestContainers for integration tests

## API Endpoints
- `POST /vendors` - Register new vendor
- `GET /vendors/{vendorId}` - Fetch vendor profile
- `PUT /vendors/{vendorId}` - Update vendor profile
- `PUT /vendors/{vendorId}/menu` - Update vendor menu
- `GET /menus/{vendorId}` - Get menu for a vendor
- `POST /orders` - Create new order
- `GET /orders/{orderId}` - Fetch order details
- `PUT /orders/{orderId}/status` - Update order status
- `GET /orders/customer/{customerId}` - Get customer orders
- `GET /reports/daily-sales` - Generate sales reports

## Infrastructure Requirements

### Infrastructure Scaling for Epic 3
- **ADD**: Order Catalog Service (new microservice)
- **SCALE**: PostgreSQL (add order tables, vendor tables, menu tables)
- **SCALE**: Kafka (order events, inventory updates, vendor status changes)
- **REUSE**: Redis (menu caching, order session data)
- **REUSE**: User Management Service (vendor authentication, customer orders)

### Infrastructure Commands
```bash
# Start Epic 3 infrastructure (includes Epic 2 services)
docker-compose up -d postgres redis kafka user-management-service notification-service order-catalog-service

# Full infrastructure for integration testing
docker-compose up -d
```

### Database Schema Extensions
- **vendors** table: Vendor profiles, operating hours, status
- **menu_items** table: Menu catalog with pricing and availability
- **orders** table: Order lifecycle and status tracking
- **order_items** table: Order line items with quantities

### Dependencies on Other Epic Infrastructure
- **Epic 2**: Requires User Management Service for authentication
- **Epic 4**: Will provide data to Search & Discovery Service via Kafka
- **Epic 5**: Will integrate with Delivery Management for order fulfillment
- **Epic 6**: Will integrate with Payment Management for order processing

## Success Metrics
- Order processing time < 500ms
- Support for 1000+ vendors and 10,000+ menu items
- Process 10,000+ orders per day
- 99.9% order accuracy (no data corruption)
- Menu cache hit rate > 80%
