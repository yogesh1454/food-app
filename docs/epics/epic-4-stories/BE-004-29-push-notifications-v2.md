# BE-004-29: Push Notification Service

**Story ID:** BE-004-29  
**Story Points:** 5  
**Priority:** Medium (P2)  
**Sprint:** 19  
**Epic:** BE-004  
**Dependencies:** BE-004-27 (Customer Status Abstraction)

---

## 📖 User Story

**As a** customer  
**I want** to receive push notifications when my order status changes  
**So that** I stay informed without constantly checking the app

---

## ✅ Acceptance Criteria

### 1. FCM Integration
- [ ] Firebase Admin SDK integrated ⏳
- [ ] FCM server key configured ⏳
- [ ] Notification sending service ⏳
- [ ] Error handling and retries ⏳

### 2. Device Token Management
- [ ] POST /api/v1/customers/{customerId}/devices/register ⏳
- [ ] Store device tokens in database ⏳
- [ ] Support multiple devices per customer ⏳
- [ ] Token refresh handling ⏳

### 3. Notification Templates
- [ ] Order Confirmed notification ⏳
- [ ] Preparing notification ⏳
- [ ] Rider Assigned notification ⏳
- [ ] Out for Delivery notification ⏳
- [ ] Delivered notification ⏳
- [ ] Cancelled notification ⏳

### 4. Notification Preferences
- [ ] Enable/disable notifications ⏳
- [ ] Notification type preferences ⏳
- [ ] Quiet hours support ⏳

### 5. Delivery Tracking
- [ ] Track sent notifications ⏳
- [ ] Track delivered notifications ⏳
- [ ] Track failed notifications ⏳
- [ ] Retry failed notifications ⏳

### 6. Testing
- [ ] Unit tests ⏳
- [ ] Integration tests with FCM ⏳
- [ ] Mock FCM for testing ⏳

---

## 🔧 Technical Implementation

### **FCM Service**

```java
@Service
@Slf4j
public class FCMNotificationService {
    
    private final FirebaseMessaging firebaseMessaging;
    
    public void sendNotification(UUID customerId, String title, String body) {
        List<String> tokens = getDeviceTokens(customerId);
        
        for (String token : tokens) {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .build();
            
            try {
                String response = firebaseMessaging.send(message);
                log.info("Notification sent: {}", response);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send notification", e);
            }
        }
    }
}
```

### **Notification Templates**

```java
public enum NotificationTemplate {
    ORDER_CONFIRMED(
        "Order Confirmed! 🎉",
        "Restaurant is preparing your food. Estimated time: 20 minutes"
    ),
    PREPARING(
        "Food Being Prepared 👨‍🍳",
        "Your delicious meal is being prepared with care"
    ),
    RIDER_ASSIGNED(
        "Delivery Partner Assigned 🏍️",
        "{riderName} is heading to the restaurant"
    ),
    OUT_FOR_DELIVERY(
        "On the Way! 🚚",
        "Your order will arrive in approximately {eta} minutes"
    ),
    DELIVERED(
        "Delivered! ✅",
        "Your order has been delivered. Enjoy your meal!"
    ),
    CANCELLED(
        "Order Cancelled ❌",
        "Your order was cancelled. Refund will be processed in 3-5 days"
    );
}
```

### **Device Token Registration**

```java
@RestController
@RequestMapping("/api/v1/customers/{customerId}/devices")
public class DeviceController {
    
    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(
        @PathVariable UUID customerId,
        @RequestBody RegisterDeviceRequest request
    ) {
        deviceService.registerDevice(
            customerId, 
            request.getDeviceToken(),
            request.getDeviceType()
        );
        return ResponseEntity.ok().build();
    }
}
```

---

## 🎯 Definition of Done

**Implementation Status: 0% Complete** ⏳ (Last updated: Nov 9, 2025)

### Pending Implementation
- [ ] Firebase Admin SDK setup ⏳
- [ ] FCMNotificationService ⏳
- [ ] Device token management ⏳
- [ ] Notification templates ⏳
- [ ] Device registration API ⏳
- [ ] Notification preferences ⏳
- [ ] Delivery tracking ⏳
- [ ] Integration tests ⏳

**Dependencies:**
- Requires Firebase project setup
- Requires FCM server key
- Requires mobile app integration

**External Setup Required:**
1. Create Firebase project
2. Generate FCM server key
3. Add google-services.json to mobile app
4. Implement device token registration in mobile app
