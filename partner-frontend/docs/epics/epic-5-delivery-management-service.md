# Epic 5: Delivery Management Service

**Epic ID:** BE-005  
**Priority:** High (P1)  
**Business Value:** Enables efficient delivery partner management and order fulfillment  
**Estimated Effort:** 3-4 sprints  
**Dependencies:** Epic 1 (Local Development Foundation), Epic 2 (User Management Service), Epic 3 (Order & Catalog Management)  

## Description
Develop the Delivery Management Service that handles delivery partner onboarding, order assignment, real-time tracking, and route optimization for the Tea & Snacks Delivery Aggregator platform.

## Business Justification
Delivery Management is essential for order fulfillment:
- Enables efficient delivery partner onboarding and management
- Provides real-time order tracking for customers
- Optimizes delivery routes to reduce costs and time
- Supports specialized delivery scenarios (trains, buses, factories)
- Tracks delivery performance and partner ratings
- Ensures reliable last-mile delivery experience

## Key Components
- **Delivery Partner Management**: Registration, profile management, vehicle information
- **Real-Time Location Tracking**: GPS location updates and storage
- **Order Assignment**: Intelligent assignment algorithms based on location and availability
- **Basic Route Optimization**: Simple routing logic (MapmyIndia integration deferred to cloud phase)
- **Delivery Status Tracking**: Status updates throughout delivery lifecycle
- **Availability Management**: Online/offline status and capacity management
- **Performance Analytics**: Delivery metrics and partner performance tracking

## Acceptance Criteria
- [ ] Delivery partners can register and manage profiles with vehicle information
- [ ] Real-time location updates are processed and stored efficiently
- [ ] Orders are assigned to available partners based on proximity and capacity
- [ ] Basic route optimization provides delivery paths using simple algorithms
- [ ] Delivery status updates are tracked and communicated to all stakeholders
- [ ] Partner availability is managed with online/offline status
- [ ] Performance metrics are calculated for delivery time, success rate, ratings
- [ ] Events are published to local Kafka for delivery status changes
- [ ] Support for specialized delivery scenarios (train stations, bus stops, factories)
- [ ] Real-time location data is cached in local Redis for quick access
- [ ] Delivery partner mobile app integration works seamlessly

## Technical Requirements
- **Framework**: Spring Boot 3.2.x with Spring Data JPA
- **Database**: Local PostgreSQL with delivery_partners table and location tracking
- **Messaging**: Local Kafka for delivery event publishing
- **Caching**: Local Redis for real-time location data
- **Geospatial**: PostGIS for location-based queries
- **Testing**: JUnit 5, Mockito, TestContainers for integration tests

## API Endpoints
- `POST /delivery-partners` - Register delivery partner
- `GET /delivery-partners/{partnerId}` - Get partner profile
- `PUT /delivery-partners/{partnerId}/location` - Update real-time location
- `PUT /delivery-partners/{partnerId}/availability` - Update availability status
- `POST /deliveries/assign` - Assign delivery to partner
- `GET /deliveries/{deliveryId}` - Get delivery details
- `PUT /deliveries/{deliveryId}/status` - Update delivery status
- `GET /deliveries/partner/{partnerId}` - Get partner's deliveries
- `GET /routes/basic` - Get basic delivery route

## Infrastructure Requirements

### Infrastructure Scaling for Epic 5
- **ADD**: Delivery Management Service (new microservice)
- **SCALE**: PostgreSQL (delivery tables, partner tables, location tracking)
- **SCALE**: Kafka (delivery status updates, location updates, assignment events)
- **SCALE**: Redis (real-time location caching, partner availability status)
- **REUSE**: User Management Service (delivery partner authentication)
- **REUSE**: Notification Service (delivery status notifications)

### Infrastructure Commands
```bash
# Start Epic 5 infrastructure (includes all previous services)
docker-compose up -d postgres redis kafka user-management-service notification-service order-catalog-service delivery-management-service

# Include search if needed for location-based delivery
docker-compose up -d postgres redis kafka elasticsearch user-management-service notification-service order-catalog-service search-discovery-service delivery-management-service
```

### Database Schema Extensions
- **delivery_partners** table: Partner profiles, vehicle info, ratings
- **deliveries** table: Delivery assignments, status tracking, timestamps
- **partner_locations** table: Real-time location tracking
- **delivery_routes** table: Basic route optimization data

### Real-Time Requirements
- **Location Updates**: High-frequency Redis updates for partner locations
- **Status Notifications**: Real-time Kafka events for delivery status changes
- **Assignment Logic**: Fast partner matching based on location and availability

### Dependencies on Other Epic Infrastructure
- **Epic 2**: Delivery partner authentication and profile management
- **Epic 3**: Order data for delivery assignments
- **Epic 4**: Location-based search for partner-order matching
- **Epic 6**: Payment integration for delivery fees
- **Epic 7**: Real-time notifications for delivery updates

## Success Metrics
- Average delivery assignment time < 2 minutes
- Location update processing < 100ms
- Delivery success rate > 95%
- Partner satisfaction score > 4.0/5.0
- Basic route optimization reduces delivery time by 10%
