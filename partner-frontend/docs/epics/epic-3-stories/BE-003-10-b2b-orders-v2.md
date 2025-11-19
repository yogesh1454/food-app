# BE-003-10: B2B Order Processing

**Story ID:** BE-003-10  
**Story Points:** 8  
**Priority:** Medium (P1)  
**Sprint:** 8  
**Epic:** BE-003  
**Dependencies:** BE-003-07 (Order Creation)

---

## 📖 User Story

**As a** corporate customer  
**I want** to place bulk orders for my company with approval workflows  
**So that** employees can order food with company billing

---

## ✅ Acceptance Criteria

1. **Company Association**
   - [ ] Orders linked to company_id
   - [ ] Company billing information captured
   - [ ] Internal delivery points supported
   - [ ] Department/cost center tracking

2. **Bulk Order Support**
   - [ ] Multiple items with large quantities
   - [ ] Scheduled delivery times
   - [ ] Recurring order support
   - [ ] Volume discounts applicable

3. **Approval Workflow**
   - [ ] Orders above threshold require approval
   - [ ] Approval by company admin
   - [ ] Approval status tracking
   - [ ] Notification to approvers

4. **Billing & Invoicing**
   - [ ] Company billing instead of individual payment
   - [ ] Monthly invoice generation
   - [ ] Credit limit checking
   - [ ] Purchase order number support

---

## 🔧 Key Implementation

### **B2B Order Service**
```java
@Service
@Transactional
public class B2BOrderService {
    
    public OrderResponse createB2BOrder(B2BOrderRequest request, UUID userId) {
        Company company = companyRepository.findById(request.getCompanyId())
            .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
        
        // Check credit limit
        BigDecimal orderTotal = calculateTotal(request);
        if (company.getCurrentBalance().add(orderTotal).compareTo(company.getCreditLimit()) > 0) {
            throw new CreditLimitExceededException("Order exceeds company credit limit");
        }
        
        // Check if approval required
        boolean requiresApproval = orderTotal.compareTo(company.getApprovalThreshold()) > 0;
        
        Order order = createOrder(request, userId);
        order.setCompanyId(company.getCompanyId());
        order.setInternalDeliveryPoint(request.getInternalDeliveryPoint());
        
        if (requiresApproval) {
            order.setOrderStatus("PENDING_APPROVAL");
            notificationService.notifyApprovers(company, order);
        }
        
        return orderMapper.toResponse(orderRepository.save(order));
    }
}
```

---

## 📋 Definition of Done

- [ ] Company entity created
- [ ] B2B order flow implemented
- [ ] Credit limit checking
- [ ] Approval workflow
- [ ] Tests passing
- [ ] Code reviewed
