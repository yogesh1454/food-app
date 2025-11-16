# Phase 3: Delivery FSM - Stories Summary

**Created:** November 9, 2025  
**Status:** ✅ **READY FOR IMPLEMENTATION**  
**Total Stories:** 4  
**Total Story Points:** 34

---

## 📊 Stories Overview

### BE-003-22: Delivery FSM Implementation
**Story Points:** 13  
**Priority:** Critical (P0)  
**Sprint:** 17

**Scope:**
- Implement 9 Delivery states (PENDING → DELIVERED/FAILED)
- Implement 9 Delivery triggers
- Create Delivery entity with PostGIS location
- Implement DeliveryFSM with Stateless4j
- Database migration for deliveries table
- State persistence (DB + Redis cache)
- Audit trail recording
- Kafka event publishing
- Integration with Order FSM

**Key Deliverables:**
- `DeliveryState` enum (9 states)
- `DeliveryTrigger` enum (9 triggers)
- `Delivery` entity with location data
- `DeliveryFSM` service
- `DeliveryRepository`
- Database migration `V1.8__create_deliveries_table.sql`

---

### BE-003-23: Smart Rider Assignment Algorithm
**Story Points:** 8  
**Priority:** High (P1)  
**Sprint:** 17

**Scope:**
- Smart timing algorithm (assign during prep)
- Rider ranking algorithm (weighted scoring)
- Rider availability filtering
- Assignment scheduling via Redis TTL
- Retry logic (3 attempts with surge)
- Edge case handling

**Key Deliverables:**
- `SmartRiderAssignmentService` - Calculate optimal assignment delay
- `RiderRankingService` - Rank riders by score
- `RiderAssignmentService` - Find and assign riders
- `RiderAssignmentKeyExpirationListener` - Redis listener
- Retry logic with surge pricing

**Algorithm Highlights:**
```
Assignment Delay = 
  max(estimatedPrepTime, avgPrepTime) + loadFactor
  - avgRiderTravelTime - peakHourBuffer - 2

Constraints: 2 min ≤ delay ≤ 15 min

Rider Score = 
  (1/(distance+1)) * 0.35 +
  (rating/5) * 0.25 +
  acceptanceRate * 0.20 +
  (1/(currentLoad+1)) * 0.10 +
  (completedToday/20) * 0.10
```

---

### BE-003-24: Rider Search & Notification Service
**Story Points:** 5  
**Priority:** High (P1)  
**Sprint:** 17

**Scope:**
- Rider entity with PostGIS location
- Geospatial queries (ST_DWithin)
- Rider location tracking & caching
- Push notification service
- Rider response handling (accept/reject)
- Analytics & metrics

**Key Deliverables:**
- `Rider` entity with PostGIS POINT
- `RiderRepository` with geospatial queries
- `RiderLocationService` - Location tracking & batch updates
- `NotificationService` - Push notifications
- `RiderResponseHandler` - Accept/reject handling
- Database migration `V1.9__create_riders_table.sql`

**Geospatial Features:**
- PostGIS extension enabled
- Spatial index on `current_location`
- Find riders within radius (2km, 5km, 10km)
- Calculate distance between rider and restaurant
- Batch location updates every 5 seconds

---

### BE-003-25: Delivery Management APIs
**Story Points:** 8  
**Priority:** High (P1)  
**Sprint:** 18

**Scope:**
- Rider delivery APIs (9 endpoints)
- Rider status APIs (6 endpoints)
- Customer tracking APIs (3 endpoints)
- Request/Response DTOs with validation
- Swagger documentation
- Authentication & authorization

**Key Deliverables:**
- `RiderDeliveryController` - Delivery management
- `RiderStatusController` - Status & location
- `DeliveryTrackingController` - Customer tracking
- 10+ DTOs with Jakarta Validation
- Swagger/OpenAPI configuration

**API Endpoints:**

**Rider Delivery APIs:**
- `GET /api/v1/rider/deliveries/pending`
- `POST /api/v1/rider/deliveries/{id}/accept`
- `POST /api/v1/rider/deliveries/{id}/reject`
- `GET /api/v1/rider/deliveries/active`
- `POST /api/v1/rider/deliveries/{id}/reached-restaurant`
- `POST /api/v1/rider/deliveries/{id}/pickup`
- `POST /api/v1/rider/deliveries/{id}/start-delivery`
- `POST /api/v1/rider/deliveries/{id}/deliver`
- `GET /api/v1/rider/deliveries/{id}`

**Rider Status APIs:**
- `POST /api/v1/rider/status/online`
- `POST /api/v1/rider/status/offline`
- `POST /api/v1/rider/status/break`
- `POST /api/v1/rider/status/resume`
- `POST /api/v1/rider/location`
- `GET /api/v1/rider/profile`

**Customer Tracking APIs:**
- `GET /api/v1/orders/{id}/delivery`
- `GET /api/v1/orders/{id}/delivery/tracking`
- `GET /api/v1/orders/{id}/delivery/rider`

---

## 🎯 Phase 3 Goals

### Primary Objectives
1. ✅ **Fully functional Delivery FSM** - 9 states, 9 triggers
2. ✅ **Smart rider assignment** - Assign during prep for hot food
3. ✅ **Geospatial rider search** - PostGIS queries within radius
4. ✅ **Real-time tracking** - Location updates & live tracking
5. ✅ **Complete REST APIs** - Rider & customer endpoints

### Technical Achievements
- **Delivery FSM** with Stateless4j
- **Smart timing algorithm** (2-15 min assignment delay)
- **Rider ranking** (5-factor weighted scoring)
- **PostGIS integration** (geospatial queries)
- **Redis caching** (rider locations, 5s TTL)
- **Batch processing** (location updates every 5s)
- **Push notifications** (rider assignment requests)
- **Kafka events** (delivery state changes)

---

## 📁 Files to be Created

### Java Classes (15+ files)
1. `fsm/DeliveryState.java` - Enum
2. `fsm/DeliveryTrigger.java` - Enum
3. `delivery/model/Delivery.java` - Entity
4. `delivery/repository/DeliveryRepository.java`
5. `fsm/delivery/DeliveryFSM.java` - FSM implementation
6. `delivery/service/SmartRiderAssignmentService.java`
7. `delivery/service/RiderRankingService.java`
8. `delivery/service/RiderAssignmentService.java`
9. `delivery/listener/RiderAssignmentKeyExpirationListener.java`
10. `rider/model/Rider.java` - Entity with PostGIS
11. `rider/repository/RiderRepository.java` - Geospatial queries
12. `rider/service/RiderLocationService.java`
13. `notification/service/NotificationService.java`
14. `delivery/service/RiderResponseHandler.java`
15. `delivery/controller/RiderDeliveryController.java`
16. `rider/controller/RiderStatusController.java`
17. `delivery/controller/DeliveryTrackingController.java`
18. 10+ DTOs

### Database Migrations (2 files)
1. `V1.8__create_deliveries_table.sql`
2. `V1.9__create_riders_table.sql` (with PostGIS)

### Configuration (1 file)
1. `config/OpenAPIConfig.java` - Swagger

---

## 🔗 Integration Points

### With Order FSM
```
Order FSM                    Event                    Delivery FSM
─────────                    ─────                    ────────────

PREPARING ─────────► order.ready_for_pickup ─────► CREATE delivery
                                                    FIND_RIDERS

READY_FOR_PICKUP ◄── delivery.rider_accepted ◄──── RIDER_ACCEPT
(ASSIGN_RIDER)

ASSIGNED_TO_RIDER ◄─ delivery.picked_up ◄────────── PICKUP_ORDER
(RIDER_PICKUP)

PICKED_UP ◄───────── delivery.delivered ◄─────────── DELIVER_ORDER
(DELIVER_ORDER)

CANCELLED ◄───────── delivery.failed ◄──────────────  FAIL_DELIVERY
(CANCEL_ORDER)
```

### Kafka Events
- **Consume:** `order.ready_for_pickup` (from Order FSM)
- **Publish:** `delivery.rider_accepted`, `delivery.picked_up`, `delivery.delivered`, `delivery.failed`

### Redis Usage
- **Scheduled assignment:** `rider_assignment:{orderId}` (TTL-based)
- **State cache:** `delivery:state:{deliveryId}` (24h TTL)
- **Location cache:** `rider-locations:{riderId}` (5s TTL)

---

## 📊 Story Dependencies

```
BE-003-22 (Delivery FSM)
    ↓
BE-003-23 (Smart Assignment) ← depends on BE-003-22
    ↓
BE-003-24 (Rider Search) ← depends on BE-003-23
    ↓
BE-003-25 (Delivery APIs) ← depends on BE-003-22, BE-003-24
```

**Recommended Implementation Order:**
1. **Week 1:** BE-003-22 (Delivery FSM) - Foundation
2. **Week 2:** BE-003-23 (Smart Assignment) + BE-003-24 (Rider Search) - Core logic
3. **Week 3:** BE-003-25 (Delivery APIs) - REST endpoints

---

## 🧪 Testing Strategy

### Unit Tests
- [ ] Delivery FSM state transitions
- [ ] Smart assignment delay calculation
- [ ] Rider ranking algorithm
- [ ] Geospatial queries
- [ ] Location batch updates
- [ ] Notification sending

### Integration Tests
- [ ] Complete delivery lifecycle
- [ ] Rider assignment flow
- [ ] Location tracking
- [ ] Redis scheduled assignment
- [ ] Kafka event publishing
- [ ] REST API endpoints

### Performance Tests
- [ ] 100+ concurrent assignments
- [ ] Geospatial query performance
- [ ] Location update throughput
- [ ] Redis cache hit rate

---

## 📚 Reference Documents

1. **Design Documents:**
   - `docs/business-flows/03_DELIVERY_FSM_DESIGN.md`
   - `docs/business-flows/06_SMART_ASSIGNMENT_ALGORITHM.md`

2. **Related Stories:**
   - `BE-003-17-base-fsm-framework-v2.md`
   - `BE-003-18-order-fsm-implementation-v2.md`
   - `BE-003-21-order-management-apis-v2.md`

3. **Standards:**
   - `docs/REST_API_STANDARDS.md`

---

## 🎯 Success Criteria

### Functional
- ✅ Delivery FSM manages complete delivery lifecycle
- ✅ Riders assigned during food preparation (hot food delivery)
- ✅ Riders found within 2km radius in < 1 second
- ✅ Location updates processed in batches every 5 seconds
- ✅ Push notifications sent to riders within 1 second
- ✅ Customers can track delivery in real-time

### Technical
- ✅ All 9 delivery states implemented
- ✅ Smart assignment delay: 2-15 minutes
- ✅ Rider ranking with 5-factor scoring
- ✅ PostGIS geospatial queries working
- ✅ Redis caching with 5s TTL
- ✅ Kafka events published correctly
- ✅ REST APIs with Swagger docs

### Quality
- ✅ Unit test coverage > 80%
- ✅ Integration tests passing
- ✅ Performance tests passing
- ✅ Code reviewed and approved
- ✅ Documentation complete

---

## 🚀 Next Steps

### After Phase 3 Completion
1. **Phase 4:** FSM Integration & Customer Status
   - Event-driven integration between Order & Delivery FSMs
   - Customer status abstraction layer
   - StatusMapper service
   - Customer status API
   - Push notification service

2. **Phase 5:** Multi-Restaurant Support
   - Parent-child order model
   - Sub-order state aggregation
   - Delivery batching algorithm
   - Payment distribution

3. **Phase 6:** Testing & Optimization
   - End-to-end integration tests
   - Load testing (10K concurrent orders)
   - Monitoring dashboards
   - Edge case handling

---

## 📈 Story Points Breakdown

| Story | Points | Complexity | Reason |
|-------|--------|------------|--------|
| BE-003-22 | 13 | High | FSM implementation, DB schema, integration |
| BE-003-23 | 8 | Medium | Complex algorithm, Redis scheduling |
| BE-003-24 | 5 | Medium | PostGIS, geospatial queries, notifications |
| BE-003-25 | 8 | Medium | Multiple controllers, DTOs, Swagger |
| **Total** | **34** | | |

---

## ✅ Definition of Done (Phase 3)

- [ ] All 4 stories implemented
- [ ] Delivery FSM working end-to-end
- [ ] Smart rider assignment working
- [ ] Geospatial search working
- [ ] Location tracking working
- [ ] Push notifications working
- [ ] REST APIs documented
- [ ] Unit tests passing (> 80% coverage)
- [ ] Integration tests passing
- [ ] Performance tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
- [ ] Ready for Phase 4

---

**Created by:** Cascade AI  
**Date:** November 9, 2025  
**Status:** ✅ Ready for implementation  
**Next:** Begin BE-003-22 (Delivery FSM Implementation)
