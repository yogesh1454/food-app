# BE-003-11: Sales Reporting & Analytics

**Story ID:** BE-003-11  
**Story Points:** 8  
**Priority:** High  
**Sprint:** 8  
**Epic:** BE-003  
**Dependencies:** BE-003-08 (Order Status)

---

## 📖 User Story

**As a** branch manager/admin  
**I want** to view sales reports and analytics  
**So that** I can track business performance and make data-driven decisions

---

## ✅ Acceptance Criteria

1. **Daily Sales Report**
   - [ ] Total orders count
   - [ ] Total revenue
   - [ ] Average order value
   - [ ] Orders by status
   - [ ] Top selling items

2. **Vendor Performance**
   - [ ] Orders completed vs cancelled
   - [ ] Average preparation time
   - [ ] Customer ratings
   - [ ] Revenue trends

3. **Order Analytics**
   - [ ] Orders by time of day
   - [ ] Orders by day of week
   - [ ] Peak hours identification
   - [ ] Category-wise sales

4. **API Endpoints**
   - [ ] GET /api/v1/reports/daily-sales
   - [ ] GET /api/v1/reports/vendor-performance/{vendorId}
   - [ ] GET /api/v1/reports/order-analytics

---

## 🔧 Key Implementation

### **Reporting Service**
```java
@Service
public class ReportingService {
    
    public DailySalesReport getDailySalesReport(UUID branchId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByBranchAndOrderedAtBetween(
            branchId, startOfDay, endOfDay);
        
        BigDecimal totalRevenue = orders.stream()
            .filter(o -> o.getOrderStatus().equals("DELIVERED"))
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalOrders = orders.size();
        BigDecimal avgOrderValue = totalOrders > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        Map<String, Long> ordersByStatus = orders.stream()
            .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));
        
        List<TopSellingItem> topItems = getTopSellingItems(orders, 10);
        
        return DailySalesReport.builder()
            .date(date)
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .averageOrderValue(avgOrderValue)
            .ordersByStatus(ordersByStatus)
            .topSellingItems(topItems)
            .build();
    }
    
    private List<TopSellingItem> getTopSellingItems(List<Order> orders, int limit) {
        Map<UUID, TopSellingItem> itemStats = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                UUID itemId = item.getMenuItem().getMenuItemId();
                TopSellingItem stats = itemStats.getOrDefault(itemId, 
                    new TopSellingItem(item.getMenuItem().getName(), 0, BigDecimal.ZERO));
                
                stats.setQuantitySold(stats.getQuantitySold() + item.getQuantity());
                stats.setRevenue(stats.getRevenue().add(
                    item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity()))));
                
                itemStats.put(itemId, stats);
            }
        }
        
        return itemStats.values().stream()
            .sorted(Comparator.comparing(TopSellingItem::getQuantitySold).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

---

## 📋 Definition of Done

- [ ] Daily sales report implemented
- [ ] Vendor performance metrics
- [ ] Order analytics
- [ ] All endpoints working
- [ ] Tests passing
- [ ] Code reviewed
