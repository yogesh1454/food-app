package com.teadelivery.ordercatalog.order.checkout.service;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutRequest;
import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutResponse;
import com.teadelivery.ordercatalog.order.checkout.dto.CommitCheckoutRequest;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSession;
import com.teadelivery.ordercatalog.menu.dto.MenuItemResponse;
import com.teadelivery.ordercatalog.menu.service.MenuService;
import com.teadelivery.ordercatalog.order.dto.CreateOrderRequest;
import com.teadelivery.ordercatalog.order.dto.OrderItemRequest;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.order.service.OrderService;
import com.teadelivery.ordercatalog.vendor.model.VendorBranch;
import com.teadelivery.ordercatalog.vendor.repository.VendorBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main checkout service - orchestrates the checkout flow
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutService {
    
    private final MenuService menuService;
    private final VendorBranchRepository vendorBranchRepository;
    private final PriceCalculationService priceCalculationService;
    private final SessionManagementService sessionManagementService;
    private final OrderService orderService;
    
    /**
     * Calculate checkout and create session
     * This is the main entry point for checkout calculation
     */
    @Transactional(readOnly = true)
    public CheckoutResponse calculateCheckout(CheckoutRequest request) {
        log.info("Processing checkout for user: {}, vendor branch: {}", 
            request.getUserId(), request.getVendorBranchId());
        
        List<CheckoutResponse.CheckoutError> errors = new ArrayList<>();
        
        // Step 1: Validate vendor branch
        VendorBranch vendorBranch = validateVendorBranch(request.getVendorBranchId(), errors);
        if (vendorBranch == null) {
            return buildErrorResponse(errors);
        }
        
        // Step 2: Validate and get menu items with prices
        List<CheckoutResponse.CheckoutItem> checkoutItems = 
            validateAndBuildCheckoutItems(request, vendorBranch, errors);
        
        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }
        
        // Step 3: Calculate delivery fee
        CheckoutResponse.DeliveryDetails deliveryDetails = 
            calculateDeliveryDetails(request, vendorBranch);
        
        // Step 4: Apply discount (if coupon provided)
        CheckoutResponse.DiscountDetails discountDetails = 
            applyDiscount(request, checkoutItems);
        
        // Step 5: Calculate final pricing
        CheckoutResponse.PricingDetails pricing = 
            priceCalculationService.calculatePricing(checkoutItems, discountDetails, deliveryDetails);
        
        // Step 6: Build validation results
        CheckoutResponse.ValidationResults validations = buildValidationResults(
            checkoutItems, vendorBranch, true, true
        );
        
        // Step 7: Create checkout session
        CheckoutSession session = buildCheckoutSession(
            request, vendorBranch, checkoutItems, pricing, validations
        );
        
        String sessionId = sessionManagementService.createSession(session);
        
        // Step 8: Build and return response
        return buildSuccessResponse(
            sessionId, vendorBranch, checkoutItems, pricing, validations, session.getExpiresAt()
        );
    }
    
    /**
     * Get existing checkout session
     */
    public CheckoutResponse getCheckoutSession(String sessionId) {
        return sessionManagementService.getSession(sessionId)
            .map(this::convertSessionToResponse)
            .orElseThrow(() -> new IllegalArgumentException("Checkout session not found or expired: " + sessionId));
    }
    
    /**
     * Validate vendor branch
     */
    private VendorBranch validateVendorBranch(Long branchId, List<CheckoutResponse.CheckoutError> errors) {
        return vendorBranchRepository.findById(branchId)
            .filter(VendorBranch::getIsActive)
            .orElseGet(() -> {
                errors.add(buildError(
                    "VENDOR_NOT_FOUND",
                    "Vendor branch not found or inactive",
                    "vendorBranchId"
                ));
                return null;
            });
    }
    
    /**
     * Validate cart items and build checkout items with current prices
     */
    private List<CheckoutResponse.CheckoutItem> validateAndBuildCheckoutItems(
        CheckoutRequest request,
        VendorBranch vendorBranch,
        List<CheckoutResponse.CheckoutError> errors
    ) {
        List<CheckoutResponse.CheckoutItem> checkoutItems = new ArrayList<>();
        
        for (CheckoutRequest.CartItemRequest cartItem : request.getItems()) {
            try {
                // Get menu item with current price
                MenuItemResponse menuItem = menuService.getMenuItem(cartItem.getMenuItemId());
                
                // Validate item belongs to vendor branch
                if (!menuItem.getBranchId().equals(request.getVendorBranchId())) {
                    errors.add(buildError(
                        "ITEM_NOT_FROM_VENDOR",
                        "Item does not belong to selected vendor",
                        "items[].menuItemId",
                        Map.of("itemId", cartItem.getMenuItemId())
                    ));
                    continue;
                }
                
                // Check if item is available
                if (!menuItem.getIsAvailable()) {
                    errors.add(buildError(
                        "ITEM_NOT_AVAILABLE",
                        menuItem.getName() + " is currently not available",
                        "items[].menuItemId",
                        Map.of("itemId", cartItem.getMenuItemId(), "itemName", menuItem.getName())
                    ));
                    continue;
                }
                
                // Check stock using MenuService
                // TODO: Add stock check integration
                // For now, assume items are in stock
                
                // Build checkout item
                BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                
                checkoutItems.add(CheckoutResponse.CheckoutItem.builder()
                    .menuItemId(menuItem.getMenuItemId())
                    .name(menuItem.getName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .customizations(cartItem.getCustomizations())
                    .isAvailable(true)
                    .stockQuantity(null) // TODO: Add stock quantity to MenuItemResponse
                    .build());
                    
            } catch (Exception e) {
                log.error("Error validating item: {}", cartItem.getMenuItemId(), e);
                errors.add(buildError(
                    "ITEM_VALIDATION_ERROR",
                    "Error validating item: " + e.getMessage(),
                    "items[].menuItemId",
                    Map.of("itemId", cartItem.getMenuItemId())
                ));
            }
        }
        
        return checkoutItems;
    }
    
    /**
     * Calculate delivery details
     */
    private CheckoutResponse.DeliveryDetails calculateDeliveryDetails(
        CheckoutRequest request,
        VendorBranch vendorBranch
    ) {
        // TODO: Implement actual distance calculation using vendor branch location
        // For now, use default values
        double distance = 3.5; // km
        String deliveryZone = "ZONE_1";
        
        return priceCalculationService.calculateDeliveryFee(distance, deliveryZone);
    }
    
    /**
     * Apply discount if coupon code provided
     */
    private CheckoutResponse.DiscountDetails applyDiscount(
        CheckoutRequest request,
        List<CheckoutResponse.CheckoutItem> items
    ) {
        if (request.getCouponCode() == null || request.getCouponCode().isBlank()) {
            return null;
        }
        
        // TODO: Integrate with PromotionService to validate coupon
        // For now, apply a simple percentage discount
        BigDecimal itemTotal = priceCalculationService.calculateItemTotal(items);
        
        // Example: 50% discount with max cap of 100
        BigDecimal discountValue = new BigDecimal("50");
        BigDecimal maxDiscount = new BigDecimal("100.00");
        BigDecimal appliedDiscount = priceCalculationService.calculateDiscount(
            itemTotal, "PERCENTAGE", discountValue, maxDiscount
        );
        
        return CheckoutResponse.DiscountDetails.builder()
            .couponCode(request.getCouponCode())
            .discountType("PERCENTAGE")
            .discountValue(discountValue)
            .maxDiscount(maxDiscount)
            .appliedDiscount(appliedDiscount)
            .build();
    }
    
    /**
     * Build validation results
     */
    private CheckoutResponse.ValidationResults buildValidationResults(
        List<CheckoutResponse.CheckoutItem> items,
        VendorBranch vendorBranch,
        boolean deliveryAddressValid,
        boolean deliveryZoneServiceable
    ) {
        boolean allItemsAvailable = items.stream()
            .allMatch(CheckoutResponse.CheckoutItem::getIsAvailable);
        
        return CheckoutResponse.ValidationResults.builder()
            .allItemsAvailable(allItemsAvailable)
            .deliveryAddressValid(deliveryAddressValid)
            .deliveryZoneServiceable(deliveryZoneServiceable)
            .vendorAcceptingOrders(vendorBranch.getIsActive())
            .paymentMethodSupported(true)
            .build();
    }
    
    /**
     * Build checkout session for Redis storage
     */
    private CheckoutSession buildCheckoutSession(
        CheckoutRequest request,
        VendorBranch vendorBranch,
        List<CheckoutResponse.CheckoutItem> items,
        CheckoutResponse.PricingDetails pricing,
        CheckoutResponse.ValidationResults validations
    ) {
        return CheckoutSession.builder()
            .userId(request.getUserId())
            .vendorBranchId(vendorBranch.getBranchId())
            .vendorId(vendorBranch.getVendor().getVendorId().toString())
            .vendorName(vendorBranch.getVendor().getCompanyName())
            .items(request.getItems())
            .deliveryAddress(request.getDeliveryAddress())
            .deliveryLocation(request.getDeliveryLocation())
            .pricing(pricing)
            .paymentMethod(request.getPaymentMethod())
            .scheduledDeliveryTime(request.getScheduledDeliveryTime())
            .contactlessDelivery(request.getContactlessDelivery())
            .leaveAtDoor(request.getLeaveAtDoor())
            .deliveryInstructions(request.getDeliveryInstructions())
            .validations(validations)
            .metadata(new HashMap<>())
            .build();
    }
    
    /**
     * Build success response
     */
    private CheckoutResponse buildSuccessResponse(
        String sessionId,
        VendorBranch vendorBranch,
        List<CheckoutResponse.CheckoutItem> items,
        CheckoutResponse.PricingDetails pricing,
        CheckoutResponse.ValidationResults validations,
        LocalDateTime expiresAt
    ) {
        return CheckoutResponse.builder()
            .checkoutSessionId(sessionId)
            .status(CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT)
            .expiresAt(expiresAt)
            .vendor(buildVendorInfo(vendorBranch))
            .items(items)
            .pricing(pricing)
            .deliveryEstimate(buildDeliveryEstimate())
            .validations(validations)
            .build();
    }
    
    /**
     * Build error response
     */
    private CheckoutResponse buildErrorResponse(List<CheckoutResponse.CheckoutError> errors) {
        return CheckoutResponse.builder()
            .checkoutSessionId(null)
            .status(CheckoutResponse.CheckoutStatus.VALIDATION_FAILED)
            .errors(errors)
            .build();
    }
    
    /**
     * Build vendor info
     */
    private CheckoutResponse.VendorInfo buildVendorInfo(VendorBranch vendorBranch) {
        return CheckoutResponse.VendorInfo.builder()
            .vendorId(vendorBranch.getVendor().getVendorId().toString())
            .vendorName(vendorBranch.getVendor().getCompanyName())
            .vendorBranchId(vendorBranch.getBranchId())
            .branchName(vendorBranch.getBranchName())
            .estimatedPrepTime(25) // TODO: Get from vendor settings
            .isAcceptingOrders(vendorBranch.getIsActive())
            .build();
    }
    
    /**
     * Build delivery estimate
     */
    private CheckoutResponse.DeliveryEstimate buildDeliveryEstimate() {
        int prepTime = 25;
        int deliveryDuration = 20;
        
        return CheckoutResponse.DeliveryEstimate.builder()
            .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(prepTime + deliveryDuration))
            .estimatedPrepTime(prepTime)
            .estimatedDeliveryDuration(deliveryDuration)
            .totalEstimatedTime(prepTime + deliveryDuration)
            .build();
    }
    
    /**
     * Build error object
     */
    private CheckoutResponse.CheckoutError buildError(String code, String message, String field) {
        return buildError(code, message, field, null);
    }
    
    private CheckoutResponse.CheckoutError buildError(
        String code, 
        String message, 
        String field,
        Map<String, Object> metadata
    ) {
        return CheckoutResponse.CheckoutError.builder()
            .code(code)
            .message(message)
            .field(field)
            .severity("ERROR")
            .metadata(metadata)
            .build();
    }
    
    /**
     * Convert session to response
     */
    private CheckoutResponse convertSessionToResponse(CheckoutSession session) {
        // Rebuild checkout items from session
        List<CheckoutResponse.CheckoutItem> items = session.getItems().stream()
            .map(cartItem -> {
                try {
                    MenuItemResponse menuItem = menuService.getMenuItem(cartItem.getMenuItemId());
                    BigDecimal subtotal = menuItem.getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                    
                    return CheckoutResponse.CheckoutItem.builder()
                        .menuItemId(menuItem.getMenuItemId())
                        .name(menuItem.getName())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(menuItem.getPrice())
                        .subtotal(subtotal)
                        .customizations(cartItem.getCustomizations())
                        .isAvailable(menuItem.getIsAvailable())
                        .stockQuantity(null) // TODO: Add stock quantity
                        .build();
                } catch (Exception e) {
                    log.error("Error loading menu item: {}", cartItem.getMenuItemId(), e);
                    return null;
                }
            })
            .filter(item -> item != null)
            .collect(Collectors.toList());
        
        VendorBranch vendorBranch = vendorBranchRepository.findById(session.getVendorBranchId())
            .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));
        
        return CheckoutResponse.builder()
            .checkoutSessionId(session.getCheckoutSessionId())
            .status(session.getStatus())
            .expiresAt(session.getExpiresAt())
            .vendor(buildVendorInfo(vendorBranch))
            .items(items)
            .pricing(session.getPricing())
            .deliveryEstimate(buildDeliveryEstimate())
            .validations(session.getValidations())
            .build();
    }
    
    /**
     * Commit checkout session to create an order
     */
    @Transactional
    public Order commitCheckout(CommitCheckoutRequest request) {
        log.info("Committing checkout session: {}", request.getCheckoutSessionId());
        
        // Get and validate session
        CheckoutSession session = sessionManagementService.getSession(request.getCheckoutSessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Checkout session not found or expired: " + request.getCheckoutSessionId()));
        
        if (session.getStatus() != CheckoutResponse.CheckoutStatus.READY_FOR_COMMIT) {
            throw new IllegalStateException(
                "Checkout session is not ready for commit. Status: " + session.getStatus());
        }
        
        // Convert checkout session to order request
        CreateOrderRequest orderRequest = convertSessionToOrderRequest(session, request);
        
        // Create order
        Order order = orderService.createOrder(session.getUserId(), orderRequest);
        
        // Update session status to COMMITTED
        sessionManagementService.updateSessionStatus(
            request.getCheckoutSessionId(), 
            CheckoutResponse.CheckoutStatus.COMMITTED
        );
        
        log.info("Checkout committed successfully. Order ID: {}, Session: {}", 
            order.getOrderId(), request.getCheckoutSessionId());
        
        return order;
    }
    
    /**
     * Convert checkout session to order request
     */
    private CreateOrderRequest convertSessionToOrderRequest(
        CheckoutSession session,
        CommitCheckoutRequest commitRequest
    ) {
        // Convert cart items to order items
        List<OrderItemRequest> orderItems = session.getItems().stream()
            .map(cartItem -> OrderItemRequest.builder()
                .menuItemId(cartItem.getMenuItemId())
                .quantity(cartItem.getQuantity())
                .customizations(cartItem.getCustomizations())
                .specialInstructions(cartItem.getSpecialInstructions())
                .build())
            .collect(Collectors.toList());
        
        // Convert delivery address
        CreateOrderRequest.DeliveryAddressRequest addressRequest = 
            CreateOrderRequest.DeliveryAddressRequest.builder()
                .addressLine1(session.getDeliveryAddress().getAddressLine1())
                .addressLine2(session.getDeliveryAddress().getAddressLine2())
                .landmark(session.getDeliveryAddress().getLandmark())
                .city(session.getDeliveryAddress().getCity())
                .state(session.getDeliveryAddress().getState())
                .pincode(session.getDeliveryAddress().getPincode())
                .addressType(session.getDeliveryAddress().getAddressType())
                .label(session.getDeliveryAddress().getLabel())
                .build();
        
        // Convert payment info
        CreateOrderRequest.PaymentRequest paymentRequest = CreateOrderRequest.PaymentRequest.builder()
            .method(commitRequest.getPaymentMethod() != null ? 
                commitRequest.getPaymentMethod() : session.getPaymentMethod())
            .transactionId(commitRequest.getPaymentTransactionId())
            .build();
        
        // Convert pricing info from session
        CreateOrderRequest.PricingRequest pricingRequest = CreateOrderRequest.PricingRequest.builder()
            .itemTotal(session.getPricing().getItemTotal())
            .deliveryCharges(session.getPricing().getDeliveryCharges())
            .platformFee(session.getPricing().getPlatformFee())
            .gst(session.getPricing().getGst())
            .discount(session.getPricing().getDiscount())
            .totalAmount(session.getPricing().getTotalAmount())
            .build();
        
        // Convert delivery location
        CreateOrderRequest.LocationRequest locationRequest = null;
        if (session.getDeliveryLocation() != null) {
            locationRequest = CreateOrderRequest.LocationRequest.builder()
                .latitude(session.getDeliveryLocation().getLatitude().doubleValue())
                .longitude(session.getDeliveryLocation().getLongitude().doubleValue())
                .build();
        }
        
        // Get vendor info from branch
        VendorBranch vendorBranch = vendorBranchRepository.findById(session.getVendorBranchId())
            .orElseThrow(() -> new IllegalStateException("Vendor branch not found"));
        
        return CreateOrderRequest.builder()
            .vendorId(UUID.fromString(vendorBranch.getVendor().getVendorId().toString()))
            .vendorName(vendorBranch.getVendor().getCompanyName())
            .vendorBranchId(session.getVendorBranchId())
            .items(orderItems)
            .deliveryAddress(addressRequest)
            .deliveryLocation(locationRequest)
            .deliveryInstructions(session.getDeliveryInstructions())
            .specialInstructions(session.getDeliveryInstructions())
            .contactlessDelivery(session.getContactlessDelivery())
            .leaveAtDoor(session.getLeaveAtDoor())
            .payment(paymentRequest)
            .pricing(pricingRequest)
            .build();
    }
}
