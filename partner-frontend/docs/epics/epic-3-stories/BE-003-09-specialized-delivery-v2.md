# BE-003-09: Specialized Delivery Scenarios

**Story ID:** BE-003-09  
**Story Points:** 13  
**Priority:** Medium (P1)  
**Sprint:** 8  
**Epic:** BE-003  
**Dependencies:** BE-003-07 (Order Creation)

---

## 📖 User Story

**As a** customer traveling or at workplace  
**I want** to order food for delivery at trains, buses, or factories  
**So that** I can receive food at specific locations and times

---

## ✅ Acceptance Criteria

1. **Train Delivery**
   - [ ] Customer provides: train_number, coach_number, seat_number, station_code
   - [ ] Scheduled arrival time captured
   - [ ] Delivery window: 15 mins before to 5 mins after arrival
   - [ ] Requires 1+ hour advance notice
   - [ ] Validation: station must be in supported list

2. **Bus Delivery**
   - [ ] Customer provides: bus_operator, bus_number, scheduled_stop_time
   - [ ] Delivery at designated bus stop locations
   - [ ] Coordination with bus schedule

3. **Factory/Workplace Delivery**
   - [ ] Company ID and internal delivery point required
   - [ ] Internal location mapping (building, floor, department)
   - [ ] Access control validation
   - [ ] Bulk order support

4. **Delivery Type Detection**
   - [ ] Auto-detect delivery type from order data
   - [ ] Different validation rules per type
   - [ ] Specialized routing for delivery partners

---

## 🔧 Key Implementation

### **Delivery Details JSONB Structure**
```json
{
  "type": "TRAIN|BUS|FACTORY|STANDARD",
  "address": {...},
  "instructions": "...",
  "train_details": {
    "train_number": "12345",
    "coach_number": "A1",
    "seat_number": "42",
    "station_code": "MAS",
    "scheduled_arrival_time": "2025-11-06T10:30:00Z"
  },
  "bus_details": {
    "bus_operator": "BMTC",
    "bus_number": "500",
    "scheduled_stop_time": "2025-11-06T14:00:00Z"
  },
  "factory_details": {
    "company_id": "uuid",
    "internal_delivery_point": "Building A, Floor 3, Cafeteria"
  }
}
```

### **Validation Service**
```java
@Service
public class SpecializedDeliveryService {
    
    public void validateTrainDelivery(OrderCreateRequest request) {
        if (request.getDeliveryDetails().get("type").equals("TRAIN")) {
            Map<String, Object> trainDetails = 
                (Map<String, Object>) request.getDeliveryDetails().get("train_details");
            
            // Validate station code
            if (!stationService.isValidStation((String) trainDetails.get("station_code"))) {
                throw new InvalidStationException("Invalid station code");
            }
            
            // Validate timing
            LocalDateTime arrivalTime = LocalDateTime.parse(
                (String) trainDetails.get("scheduled_arrival_time"));
            if (arrivalTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Train delivery requires at least 1 hour notice");
            }
        }
    }
    
    public void validateFactoryDelivery(OrderCreateRequest request) {
        if (request.getDeliveryDetails().get("type").equals("FACTORY")) {
            Map<String, Object> factoryDetails = 
                (Map<String, Object>) request.getDeliveryDetails().get("factory_details");
            
            UUID companyId = UUID.fromString((String) factoryDetails.get("company_id"));
            Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
            
            // Validate internal delivery point
            if (!company.hasDeliveryPoint((String) factoryDetails.get("internal_delivery_point"))) {
                throw new ValidationException("Invalid delivery point for this company");
            }
        }
    }
}
```

---

## 📋 Definition of Done

- [ ] Train delivery fields added
- [ ] Bus delivery fields added
- [ ] Factory delivery fields added
- [ ] Validation for each type
- [ ] Delivery type detection
- [ ] Tests for all scenarios
- [ ] Code reviewed
