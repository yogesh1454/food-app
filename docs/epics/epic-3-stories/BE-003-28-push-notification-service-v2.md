# BE-003-28: Push Notification Service

**Story ID:** BE-003-28  
**Story Points:** 5  
**Priority:** High (P1)  
**Sprint:** 19  
**Epic:** BE-003  
**Dependencies:** BE-003-27 (Customer Status API)

---

## 📖 User Story

**As a** backend developer  
**I want** a push notification service for customers  
**So that** customers receive real-time updates about their orders

---

## ✅ Acceptance Criteria

1. **Customer Notifications**
   - [ ] Order confirmed
   - [ ] Restaurant accepted
   - [ ] Food being prepared
   - [ ] Rider assigned
   - [ ] Rider picked up
   - [ ] Rider nearby
   - [ ] Order delivered

2. **Implementation**
   - [ ] Firebase Cloud Messaging (FCM) integration
   - [ ] APNS for iOS
   - [ ] Notification templates
   - [ ] User preferences (opt-in/opt-out)

---

## 🎯 Definition of Done

- [ ] Notification service implemented
- [ ] FCM/APNS working
- [ ] All notification types sent
- [ ] Tests passing
