# BE-003-04: Branch Operating Hours & Status Management

**Story ID:** BE-003-04  
**Story Points:** 5  
**Priority:** High  
**Sprint:** 6  
**Epic:** BE-003 (Order & Catalog Management Service)  
**Dependencies:** BE-003-03 (Branch Onboarding)

---

## 📖 User Story

**As a** branch manager  
**I want** to manage operating hours and online/offline status  
**So that** customers see when we're available and I can control order receipt

---

## ✅ Acceptance Criteria

1. **Operating Hours Management**
   - [ ] Set operating hours for each day of the week
   - [ ] Support multiple time slots per day (e.g., 9AM-12PM, 5PM-9PM)
   - [ ] Support 24/7 operation
   - [ ] Support closed days
   - [ ] Validation prevents invalid time ranges (end time > start time)
   - [ ] Operating hours stored as JSONB array in vendor_branches table
   - [ ] No overlapping time slots per day

2. **Online/Offline Status**
   - [ ] Branch manager can manually toggle is_open status
   - [ ] System automatically sets is_open based on operating hours
   - [ ] Override capability: can go offline during operating hours
   - [ ] Status changes publish Kafka events
   - [ ] Status cached in Redis (5-min TTL)

3. **Order Acceptance Rules**
   - [ ] Orders only accepted when is_open = true AND is_active = true
   - [ ] Orders rejected with clear message when branch is offline
   - [ ] Operating hours displayed to customers before order placement
   - [ ] Estimated preparation time considered for order cutoff

4. **API Endpoints**
   - [ ] PUT /api/v1/branches/{branchId}/operating-hours - Set operating hours
   - [ ] GET /api/v1/branches/{branchId}/operating-hours - Get operating hours
   - [ ] PUT /api/v1/branches/{branchId}/status - Toggle online/offline
   - [ ] GET /api/v1/branches/{branchId}/availability - Check current availability

5. **Scheduled Jobs**
   - [ ] Auto-update is_open status every 5 minutes based on operating hours
   - [ ] Timezone: Asia/Kolkata (IST)
   - [ ] Handle daylight saving time (if applicable)

---

## 🔧 Technical Implementation

### **Operating Hours JSONB Structure**
```json
{
  "MONDAY": [
    {"open": "09:00", "close": "12:00"},
    {"open": "17:00", "close": "21:00"}
  ],
  "TUESDAY": [{"open": "09:00", "close": "21:00"}],
  "WEDNESDAY": [{"open": "09:00", "close": "21:00"}],
  "THURSDAY": [{"open": "09:00", "close": "21:00"}],
  "FRIDAY": [{"open": "09:00", "close": "21:00"}],
  "SATURDAY": [{"open": "09:00", "close": "21:00"}],
  "SUNDAY": []  // Closed
}
```

### **Service Implementation**
```java
@Service
@Slf4j
public class BranchAvailabilityService {
    
    @Transactional
    public BranchResponse updateOperatingHours(UUID branchId, 
                                              OperatingHoursRequest request,
                                              UUID requestingUserId) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        // Authorization
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        // Validate no overlapping slots
        if (request.hasOverlappingSlots()) {
            throw new ValidationException("Operating hours have overlapping time slots");
        }
        
        // Validate time ranges
        for (DayOfWeek day : DayOfWeek.values()) {
            List<TimeSlot> slots = request.getHoursForDay(day);
            for (TimeSlot slot : slots) {
                if (!slot.isValidTimeRange()) {
                    throw new ValidationException("Invalid time range for " + day);
                }
            }
        }
        
        // Update operating hours
        branch.setOperatingHours(request.toJsonb());
        
        // Auto-update is_open based on current time
        updateBranchOpenStatus(branch);
        
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchStatus(branchId);
        
        // Publish event
        eventPublisher.publishOperatingHoursUpdated(updatedBranch);
        
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional
    public BranchResponse toggleBranchStatus(UUID branchId, 
                                            BranchStatusRequest request,
                                            UUID requestingUserId) {
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        if (!branch.getVendor().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Not authorized");
        }
        
        Boolean oldStatus = branch.getIsOpen();
        branch.setIsOpen(request.getIsOpen());
        
        VendorBranch updatedBranch = branchRepository.save(branch);
        
        // Invalidate cache
        cacheService.evictBranchStatus(branchId);
        
        // Publish event
        eventPublisher.publishBranchStatusChanged(updatedBranch, oldStatus, request.getIsOpen());
        
        return BranchMapper.toResponse(updatedBranch);
    }
    
    @Transactional(readOnly = true)
    public BranchAvailabilityResponse checkAvailability(UUID branchId) {
        // Try cache first
        BranchAvailabilityResponse cached = cacheService.getBranchAvailability(branchId);
        if (cached != null) {
            return cached;
        }
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Kolkata")).getDayOfWeek();
        
        List<TimeSlot> todayHours = branch.getOperatingHoursForDay(today);
        boolean isWithinOperatingHours = todayHours.stream()
            .anyMatch(slot -> slot.isOpenAt(now));
        
        String currentStatus = determineStatus(branch.getIsOpen(), isWithinOperatingHours);
        
        BranchAvailabilityResponse response = BranchAvailabilityResponse.builder()
            .branchId(branchId)
            .isOpen(branch.getIsOpen())
            .isActive(branch.getIsActive())
            .isWithinOperatingHours(isWithinOperatingHours)
            .currentStatus(currentStatus)
            .todayHours(todayHours)
            .nextOpenTime(findNextOpenTime(branch, now, today))
            .nextCloseTime(findNextCloseTime(todayHours, now))
            .build();
        
        // Cache for 5 minutes
        cacheService.cacheBranchAvailability(branchId, response);
        
        return response;
    }
    
    private void updateBranchOpenStatus(VendorBranch branch) {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Kolkata")).getDayOfWeek();
        
        List<TimeSlot> todayHours = branch.getOperatingHoursForDay(today);
        boolean shouldBeOpen = todayHours.stream()
            .anyMatch(slot -> slot.isOpenAt(now));
        
        branch.setIsOpen(shouldBeOpen);
    }
    
    private String determineStatus(Boolean isOpen, boolean isWithinOperatingHours) {
        if (!isWithinOperatingHours) {
            return "CLOSED";
        }
        return isOpen ? "OPEN" : "OFFLINE";
    }
}
```

### **Scheduled Job for Auto-Update**
```java
@Component
@Slf4j
public class BranchStatusScheduler {
    
    private final BranchRepository branchRepository;
    private final BranchAvailabilityService availabilityService;
    
    @Scheduled(cron = "0 */5 * * * *")  // Every 5 minutes
    public void updateBranchStatuses() {
        log.debug("Running scheduled branch status update");
        
        List<VendorBranch> activeBranches = branchRepository.findByIsActiveTrue();
        
        for (VendorBranch branch : activeBranches) {
            try {
                availabilityService.updateBranchOpenStatus(branch);
            } catch (Exception e) {
                log.error("Error updating branch status: {}", branch.getBranchId(), e);
            }
        }
    }
}
```

### **Controller**
```java
@RestController
@RequestMapping("/api/v1/branches")
@Slf4j
public class BranchAvailabilityController {
    
    private final BranchAvailabilityService availabilityService;
    
    @PutMapping("/{branchId}/operating-hours")
    public ResponseEntity<ApiResponse<BranchResponse>> updateOperatingHours(
            @PathVariable UUID branchId,
            @Valid @RequestBody OperatingHoursRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        BranchResponse response = availabilityService.updateOperatingHours(
            branchId, request, userPrincipal.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Operating hours updated successfully"));
    }
    
    @GetMapping("/{branchId}/operating-hours")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOperatingHours(
            @PathVariable UUID branchId) {
        
        VendorBranch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new BranchNotFoundException("Branch not found"));
        
        return ResponseEntity.ok(ApiResponse.success(branch.getOperatingHours()));
    }
    
    @PutMapping("/{branchId}/status")
    public ResponseEntity<ApiResponse<BranchResponse>> toggleStatus(
            @PathVariable UUID branchId,
            @Valid @RequestBody BranchStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        BranchResponse response = availabilityService.toggleBranchStatus(
            branchId, request, userPrincipal.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "Branch status updated successfully"));
    }
    
    @GetMapping("/{branchId}/availability")
    public ResponseEntity<ApiResponse<BranchAvailabilityResponse>> checkAvailability(
            @PathVariable UUID branchId) {
        
        BranchAvailabilityResponse response = availabilityService.checkAvailability(branchId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 🧪 Testing Requirements

### **Unit Tests**
- [ ] TimeSlot.isValidTimeRange()
- [ ] TimeSlot.isOpenAt()
- [ ] OperatingHoursRequest.hasOverlappingSlots()
- [ ] BranchAvailabilityService.updateOperatingHours()
- [ ] BranchAvailabilityService.checkAvailability()
- [ ] BranchStatusScheduler.updateBranchStatuses()

### **Integration Tests**
- [ ] PUT /api/v1/branches/{id}/operating-hours - success
- [ ] PUT /api/v1/branches/{id}/operating-hours - overlapping slots error
- [ ] PUT /api/v1/branches/{id}/status - toggle online/offline
- [ ] GET /api/v1/branches/{id}/availability - check status
- [ ] Redis cache invalidation on status change
- [ ] Scheduled job updates status correctly

---

## 📋 Definition of Done

- [ ] Operating hours model created with validation
- [ ] Availability service implemented
- [ ] Cache service for branch status
- [ ] All controller endpoints implemented
- [ ] Scheduled job for auto-status updates
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] Redis caching working correctly
- [ ] Kafka events published on status changes
- [ ] Code reviewed and approved

---

## 📚 References

- [Coding Standards](/docs/architecture/13-coding-standards.md)
- [Caching Strategy](/docs/architecture/2-high-level-architecture.md)
