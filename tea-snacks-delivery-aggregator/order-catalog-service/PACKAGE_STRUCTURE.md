# Package Structure - Domain-Driven Design

## Overview
The codebase follows **Package by Feature/Domain** structure, organizing code by business domains rather than technical layers. This enables better encapsulation, cohesion, and future microservice extraction.

---

## 📦 Package Organization

### **1. Order Domain** (`order/`)
All order-related features and logic.

```
order/
├── fsm/                          # Order Finite State Machine
│   ├── OrderFSM.java            # Order state machine implementation
│   ├── OrderState.java          # 13 order states (CREATED → CLOSED)
│   ├── OrderTrigger.java        # 12 state triggers
│   ├── OrderType.java           # SINGLE, MULTI_RESTAURANT
│   ├── PaymentStatus.java       # Payment state enum
│   ├── SubOrderState.java       # Sub-order states for multi-restaurant
│   └── events/
│       └── OrderStateChangedEvent.java
│
├── model/                        # Order entities
│   ├── Order.java               # Parent order entity
│   ├── SubOrder.java            # Sub-order for multi-restaurant
│   └── OrderItem.java           # Order line items
│
├── repository/                   # Data access
│   ├── OrderRepository.java
│   └── SubOrderRepository.java
│
├── service/                      # Business logic
│   ├── OrderService.java        # Core order operations
│   ├── MultiRestaurantOrderService.java
│   └── StateAggregationService.java
│
├── controller/                   # REST endpoints
│   ├── OrderController.java     # Customer endpoints
│   └── RestaurantOrderController.java
│
├── dto/                          # Data transfer objects
│   ├── CreateOrderRequest.java
│   ├── OrderResponse.java
│   └── ...
│
├── status/                       # Customer-facing status
│   ├── model/
│   │   └── CustomerStatus.java  # 8 simplified customer states
│   ├── service/
│   │   ├── CustomerStatusService.java
│   │   └── StatusMapperService.java
│   ├── controller/
│   │   └── CustomerOrderController.java
│   └── dto/
│       └── CustomerStatusResponseDTO.java
│
└── timeout/                      # Order timeout handling
    ├── OrderTimeoutService.java
    └── RedisKeyExpirationListener.java
```

**Responsibilities:**
- Order lifecycle management (13 states)
- Multi-restaurant order handling
- Parent-child order relationships
- State aggregation logic
- Customer status abstraction
- Order timeout handling (restaurant: 2m, payment: 5m)

---

### **2. Delivery Domain** (`delivery/`)
All delivery-related features and logic.

```
delivery/
├── fsm/                          # Delivery Finite State Machine
│   ├── DeliveryFSM.java         # Delivery state machine
│   ├── DeliveryState.java       # 9 delivery states
│   ├── DeliveryTrigger.java     # 9 state triggers
│   └── events/
│       ├── DeliveryStateChangedEvent.java
│       ├── RiderAssignmentRequestEvent.java
│       └── RiderAssignmentResponseEvent.java
│
├── model/                        # Delivery entities
│   └── Delivery.java            # Delivery entity with PostGIS
│
├── repository/                   # Data access
│   └── DeliveryRepository.java  # With geospatial queries
│
├── service/                      # Business logic
│   ├── DeliveryService.java     # Core delivery operations
│   ├── RiderAssignmentService.java
│   ├── RiderRankingService.java
│   └── DeliveryBatchingService.java
│
├── controller/                   # REST endpoints
│   ├── RiderDeliveryController.java
│   └── DeliveryTrackingController.java
│
└── dto/                          # Data transfer objects
    ├── DeliveryResponseDTO.java
    └── ...
```

**Responsibilities:**
- Delivery lifecycle management (9 states)
- Smart rider assignment (5-factor ranking)
- Geospatial rider search (PostGIS)
- Delivery batching for multi-restaurant
- Route optimization
- Rider tracking

---

### **3. Rider Domain** (`rider/`)
All rider-related features and logic.

```
rider/
├── model/
│   └── Rider.java               # Rider entity with location
│
├── repository/
│   └── RiderRepository.java     # With PostGIS queries
│
├── service/
│   ├── RiderService.java
│   └── RiderLocationService.java
│
├── controller/
│   ├── RiderController.java
│   └── RiderStatusController.java
│
└── dto/
    └── ...
```

**Responsibilities:**
- Rider profile management
- Real-time location tracking
- Rider availability status
- Rider performance metrics

---

### **4. Common/Shared** (`common/`)
Shared infrastructure and base classes.

```
common/
├── fsm/                          # FSM base framework
│   ├── BaseStateMachine.java    # Abstract FSM base
│   ├── EventPublisher.java      # Kafka event publishing
│   ├── StateCacheService.java   # Redis state caching
│   ├── StateAuditService.java   # State change auditing
│   ├── StateMachineException.java
│   └── InvalidStateTransitionException.java
│
├── exception/                    # Global exceptions
├── dto/                          # Shared DTOs
└── util/                         # Utility classes
```

**Responsibilities:**
- FSM framework and base classes
- Event publishing infrastructure
- State caching (Redis)
- State auditing
- Common exceptions and utilities

---

### **5. Event-Driven Integration** (`event/`)
Kafka event consumers for FSM coordination.

```
event/
├── consumer/
│   ├── OrderEventConsumer.java      # Listens to order-events
│   └── DeliveryEventConsumer.java   # Listens to delivery-events
│
└── producer/
    └── (EventPublisher in common/fsm/)
```

**Responsibilities:**
- Order FSM ↔ Delivery FSM coordination
- Event-driven state transitions
- Idempotency handling
- Manual commit for exactly-once processing

---

### **6. Configuration** (`config/`)
Spring Boot configuration classes.

```
config/
├── KafkaProducerConfig.java
├── KafkaConsumerConfig.java
├── RedisConfig.java
├── OpenAPIConfig.java
└── ...
```

---

### **7. Other Domains**
```
menu/          # Menu and catalog management
vendor/        # Vendor/restaurant management
notification/  # Push notifications (future)
audit/         # Audit logging
```

---

## 🎯 Design Principles

### **1. Package by Feature/Domain**
- ✅ Code organized by business capability
- ✅ High cohesion within packages
- ✅ Low coupling between packages
- ✅ Easy to understand and navigate

### **2. Domain-Driven Design (DDD)**
- ✅ Clear domain boundaries (Order, Delivery, Rider)
- ✅ Domain logic encapsulated within domains
- ✅ Shared kernel in `common/`
- ✅ Ubiquitous language reflected in code

### **3. Microservice-Ready**
- ✅ Each domain can be extracted as a microservice
- ✅ Clear API boundaries (controllers)
- ✅ Event-driven communication (Kafka)
- ✅ Independent data models

### **4. Separation of Concerns**
- ✅ FSM logic within domain folders
- ✅ Status abstraction within order domain
- ✅ Timeout handling within order domain
- ✅ Common infrastructure in `common/`

---

## 📊 Benefits

### **Before (Technical Layers)**
```
❌ fsm/                  # Shared FSM (mixed concerns)
❌ status/               # Standalone (unclear ownership)
❌ timeout/              # Standalone (unclear ownership)
❌ order/
❌ delivery/
```

**Problems:**
- FSM logic scattered across technical layers
- Unclear which domain owns status/timeout
- Hard to extract as microservices
- Low cohesion, high coupling

### **After (Domain Features)**
```
✅ order/
   ├── fsm/             # Order FSM
   ├── status/          # Customer status (order concern)
   └── timeout/         # Order timeout (order concern)
✅ delivery/
   └── fsm/             # Delivery FSM
✅ common/
   └── fsm/             # Shared FSM base classes
```

**Benefits:**
- Clear domain ownership
- High cohesion within domains
- Easy microservice extraction
- Better encapsulation
- Follows DDD principles

---

## 🚀 Future Microservice Extraction

When ready to split into microservices:

### **Order Service**
```
order-service/
├── order/              # Move entire order/ package
├── common/fsm/         # Copy shared FSM base
└── event/consumer/     # OrderEventConsumer
```

### **Delivery Service**
```
delivery-service/
├── delivery/           # Move entire delivery/ package
├── rider/              # Move entire rider/ package
├── common/fsm/         # Copy shared FSM base
└── event/consumer/     # DeliveryEventConsumer
```

### **Communication**
- ✅ Kafka events (order-events, delivery-events)
- ✅ REST APIs for synchronous calls
- ✅ Shared event schemas

---

## 📝 Migration Notes

### **What Changed**
1. **Order FSM** moved from `fsm/` to `order/fsm/`
2. **Delivery FSM** moved from `fsm/delivery/` to `delivery/fsm/`
3. **Status** moved from `status/` to `order/status/`
4. **Timeout** moved from `timeout/` to `order/timeout/`
5. **Base FSM** moved from `fsm/base/` to `common/fsm/`
6. **All imports** updated across 100+ files
7. **Test files** moved to match new structure

### **What Stayed Same**
- ✅ All functionality preserved
- ✅ Git history preserved (used `git mv`)
- ✅ No breaking changes to APIs
- ✅ All tests still valid

---

## 🔍 Finding Code

### **Order-related code?**
→ Look in `order/` package

### **Delivery-related code?**
→ Look in `delivery/` package

### **FSM base classes?**
→ Look in `common/fsm/` package

### **Customer status?**
→ Look in `order/status/` package

### **Timeout handling?**
→ Look in `order/timeout/` package

---

## ✅ Checklist for New Features

When adding a new feature:

1. **Identify the domain** (Order, Delivery, Rider, etc.)
2. **Place code in domain package**
   - FSM logic → `domain/fsm/`
   - Entities → `domain/model/`
   - Services → `domain/service/`
   - Controllers → `domain/controller/`
   - DTOs → `domain/dto/`
3. **Use common infrastructure** from `common/`
4. **Publish events** for cross-domain communication
5. **Keep domain boundaries clear**

---

**Last Updated:** November 10, 2025  
**Refactoring Commit:** `3a35360`  
**Status:** ✅ Complete
