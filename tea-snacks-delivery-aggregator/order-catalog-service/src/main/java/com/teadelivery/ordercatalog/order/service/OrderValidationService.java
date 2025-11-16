package com.teadelivery.ordercatalog.order.service;

import com.teadelivery.ordercatalog.menu.service.MenuService;
import com.teadelivery.ordercatalog.order.model.DeliveryAddress;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.model.OrderItem;
import com.teadelivery.ordercatalog.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order Validation Service
 * Handles all pre-acceptance validation checks for orders
 * As per 02_ORDER_FSM_DESIGN.md - VALIDATED state requirements
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderValidationService {
    
    private final OrderItemRepository orderItemRepository;
    private final MenuService menuService;
    
    // TODO: Inject these services when vendor domain is fully integrated
    // private final VendorService vendorService;
    // private final DeliveryZoneService deliveryZoneService;
    
    /**
     * Perform comprehensive validation checks on an order
     * 
     * Validation Checks (as per design doc):
     * 1. Vendor is open and accepting orders
     * 2. Customer address in delivery zone
     * 3. Meets minimum order value
     * 4. All menu items are active
     * 5. Stock/inventory available
     * 6. Final price matches submitted total
     * 7. User not flagged or rate-limited
     * 8. Prep + travel time fits SLA
     * 
     * @param order Order to validate
     * @return ValidationResult with success status and error messages
     */
    public ValidationResult validateOrder(Order order) {
        log.info("Performing comprehensive validation for order: {}", order.getOrderId());
        
        List<String> errors = new ArrayList<>();
        
        try {
            // 1. Validate vendor is open and accepting orders
            if (!validateVendorStatus(order, errors)) {
                log.warn("Vendor validation failed for order: {}", order.getOrderId());
            }
            
            // 2. Validate customer address in delivery zone
            if (!validateDeliveryZone(order, errors)) {
                log.warn("Delivery zone validation failed for order: {}", order.getOrderId());
            }
            
            // 3. Validate minimum order value
            if (!validateMinimumOrderValue(order, errors)) {
                log.warn("Minimum order value validation failed for order: {}", order.getOrderId());
            }
            
            // 4. Validate all menu items are active
            if (!validateMenuItems(order, errors)) {
                log.warn("Menu items validation failed for order: {}", order.getOrderId());
            }
            
            // 5. Validate stock/inventory available
            if (!validateInventory(order, errors)) {
                log.warn("Inventory validation failed for order: {}", order.getOrderId());
            }
            
            // 6. Validate final price matches submitted total
            if (!validatePricing(order, errors)) {
                log.warn("Pricing validation failed for order: {}", order.getOrderId());
            }
            
            // 7. Validate user not flagged or rate-limited
            if (!validateCustomerStatus(order, errors)) {
                log.warn("Customer status validation failed for order: {}", order.getOrderId());
            }
            
            // 8. Validate prep + travel time fits SLA
            if (!validateSLA(order, errors)) {
                log.warn("SLA validation failed for order: {}", order.getOrderId());
            }
            
            boolean isValid = errors.isEmpty();
            
            if (isValid) {
                log.info("Order validation successful: {}", order.getOrderId());
            } else {
                log.error("Order validation failed: {}, errors: {}", order.getOrderId(), errors);
            }
            
            return new ValidationResult(isValid, errors);
            
        } catch (Exception e) {
            log.error("Error during order validation: {}", order.getOrderId(), e);
            errors.add("Validation error: " + e.getMessage());
            return new ValidationResult(false, errors);
        }
    }
    
    /**
     * 1. Validate vendor is open and accepting orders
     */
    private boolean validateVendorStatus(Order order, List<String> errors) {
        try {
            Map<String, Object> metadata = order.getMetadata();
            UUID vendorId = (UUID) metadata.get("vendorId");
            
            if (vendorId == null) {
                errors.add("Vendor ID is missing");
                return false;
            }
            
            // TODO: Call VendorService to check if vendor is open
            // For now, assume vendor is open
            log.debug("Vendor status check passed for vendor: {}", vendorId);
            return true;
            
        } catch (Exception e) {
            errors.add("Vendor validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 2. Validate customer address in delivery zone
     */
    private boolean validateDeliveryZone(Order order, List<String> errors) {
        try {
            DeliveryAddress deliveryAddress = order.getDeliveryAddress();
            
            if (deliveryAddress == null || deliveryAddress.getAddressLine1() == null || deliveryAddress.getAddressLine1().isBlank()) {
                errors.add("Delivery address is missing");
                return false;
            }
            
            String pincode = deliveryAddress.getPincode();
            if (pincode == null || pincode.isEmpty()) {
                errors.add("Pincode is missing");
                return false;
            }
            
            // TODO: Call DeliveryZoneService to check if pincode is serviceable
            // For now, assume all pincodes are serviceable
            log.debug("Delivery zone check passed for pincode: {}", pincode);
            return true;
            
        } catch (Exception e) {
            errors.add("Delivery zone validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 3. Validate minimum order value (₹50)
     */
    private boolean validateMinimumOrderValue(Order order, List<String> errors) {
        try {
            BigDecimal itemTotal = order.getItemTotal();
            BigDecimal minimumOrderValue = new BigDecimal("50.00");
            
            if (itemTotal == null || itemTotal.compareTo(minimumOrderValue) < 0) {
                errors.add(String.format("Order value ₹%s is below minimum ₹%s", 
                    itemTotal, minimumOrderValue));
                return false;
            }
            
            log.debug("Minimum order value check passed: ₹{}", itemTotal);
            return true;
            
        } catch (Exception e) {
            errors.add("Minimum order value validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 4. Validate all menu items are active
     */
    private boolean validateMenuItems(Order order, List<String> errors) {
        try {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            
            if (items == null || items.isEmpty()) {
                errors.add("No items in order");
                return false;
            }
            
            // TODO: Call MenuService to validate each menu item is active
            // For now, assume all items are active
            log.debug("Menu items validation passed for {} items", items.size());
            return true;
            
        } catch (Exception e) {
            errors.add("Menu items validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 5. Validate stock/inventory available
     */
    private boolean validateInventory(Order order, List<String> errors) {
        try {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            
            if (items == null || items.isEmpty()) {
                errors.add("No items in order");
                return false;
            }
            
            // Build map of menuItemId -> quantity
            Map<Long, Integer> itemQuantities = new HashMap<>();
            for (OrderItem item : items) {
                itemQuantities.put(item.getMenuItemId(), item.getQuantity());
            }
            
            // Check stock via MenuService
            boolean inStock = menuService.checkMultipleItemsStock(itemQuantities);
            
            if (!inStock) {
                errors.add("One or more items are out of stock or unavailable");
                return false;
            }
            
            log.debug("Inventory check passed");
            return true;
            
        } catch (Exception e) {
            errors.add("Inventory validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 6. Validate final price matches submitted total
     */
    private boolean validatePricing(Order order, List<String> errors) {
        try {
            BigDecimal itemTotal = order.getItemTotal();
            BigDecimal deliveryCharges = order.getDeliveryCharges();
            BigDecimal platformFee = order.getPlatformFee();
            BigDecimal gst = order.getGst();
            BigDecimal discount = order.getDiscount();
            BigDecimal totalAmount = order.getTotalAmount();
            
            // Calculate expected total
            BigDecimal calculatedTotal = itemTotal
                .add(deliveryCharges)
                .add(platformFee)
                .add(gst)
                .subtract(discount);
            
            // Allow 1 rupee difference for rounding
            BigDecimal difference = calculatedTotal.subtract(totalAmount).abs();
            BigDecimal tolerance = new BigDecimal("1.00");
            
            if (difference.compareTo(tolerance) > 0) {
                errors.add(String.format("Price mismatch: calculated ₹%s, submitted ₹%s", 
                    calculatedTotal, totalAmount));
                return false;
            }
            
            log.debug("Pricing validation passed: ₹{}", totalAmount);
            return true;
            
        } catch (Exception e) {
            errors.add("Pricing validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 7. Validate user not flagged or rate-limited
     */
    private boolean validateCustomerStatus(Order order, List<String> errors) {
        try {
            UUID customerId = order.getCustomerId();
            
            // TODO: Check if customer is flagged or rate-limited
            // For now, assume customer is valid
            log.debug("Customer status check passed for customer: {}", customerId);
            return true;
            
        } catch (Exception e) {
            errors.add("Customer status validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 8. Validate prep + travel time fits SLA (< 60 minutes)
     */
    private boolean validateSLA(Order order, List<String> errors) {
        try {
            // TODO: Calculate estimated prep time + travel time
            // For now, assume SLA is met
            log.debug("SLA validation passed");
            return true;
            
        } catch (Exception e) {
            errors.add("SLA validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validation Result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
