package com.teadelivery.ordercatalog.order.checkout.service;

import com.teadelivery.ordercatalog.order.checkout.dto.CheckoutRequest;
import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import com.teadelivery.ordercatalog.order.checkout.dto.CommitCheckoutRequest;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSession;
import com.teadelivery.ordercatalog.order.checkout.model.CheckoutSessionStatus;
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
         * 
         * Note: Not using @Transactional to allow graceful handling of exceptions
         * from other services (like MenuService) without transaction rollback issues
         */
        public OrderDetailsResponse calculateCheckout(CheckoutRequest request) {
                log.info("=== CHECKOUT STARTED ===");
                log.info("Processing checkout for user: {}, vendor branch: {}",
                                request.getUserId(), request.getVendorBranchId());
                log.debug("Full request: {}", request);

                List<OrderDetailsResponse.CheckoutError> errors = new ArrayList<>();

                try {
                        // Step 1: Validate vendor branch
                        log.info("Step 1: Validating vendor branch ID: {}", request.getVendorBranchId());
                        VendorBranch vendorBranch = validateVendorBranch(request.getVendorBranchId(), errors);
                        if (vendorBranch == null) {
                                log.warn("Vendor branch validation failed. Errors: {}", errors);
                                throw new com.teadelivery.ordercatalog.order.checkout.exception.CheckoutValidationException(
                                                "Vendor validation failed", errors);
                        }
                        log.info("Vendor branch validated: {} (Active: {}, Open: {})",
                                        vendorBranch.getBranchName(), vendorBranch.getIsActive(),
                                        vendorBranch.getIsOpen());
                        log.info("Vendor: {} (ID: {})", vendorBranch.getVendor().getCompanyName(),
                                        vendorBranch.getVendor().getVendorId());

                        // Step 2: Validate and get menu items with prices
                        log.info("Step 2: Validating {} cart items", request.getItems().size());
                        List<OrderDetailsResponse.CheckoutItem> checkoutItems = validateAndBuildCheckoutItems(request,
                                        vendorBranch,
                                        errors);

                        if (!errors.isEmpty()) {
                                log.warn("Item validation failed. Errors: {}", errors);
                                throw new com.teadelivery.ordercatalog.order.checkout.exception.CheckoutValidationException(
                                                "Item validation failed", errors);
                        }
                        log.info("All items validated successfully. Total items: {}", checkoutItems.size());

                        // Step 3: Calculate delivery fee
                        log.info("Step 3: Calculating delivery details");
                        OrderDetailsResponse.DeliveryChargeDetails deliveryDetails = calculateDeliveryDetails(request,
                                        vendorBranch);
                        log.info("Delivery details calculated: distance={}, zone={}, baseFee={}, distanceFee={}",
                                        deliveryDetails.getDistance(), deliveryDetails.getDeliveryZone(),
                                        deliveryDetails.getBaseFee(), deliveryDetails.getDistanceFee());

                        // Step 4: Apply discount (if coupon provided)
                        log.info("Step 4: Checking for discount/coupon");
                        OrderDetailsResponse.DiscountDetails discountDetails = applyDiscount(request, checkoutItems);
                        if (discountDetails != null) {
                                log.info("Discount applied: {} ({})", discountDetails.getAppliedDiscount(),
                                                discountDetails.getCouponCode());
                        }

                        // Step 5: Calculate final pricing
                        log.info("Step 5: Calculating final pricing");
                        OrderDetailsResponse.PricingDetails pricing = priceCalculationService.calculatePricing(
                                        checkoutItems,
                                        discountDetails, deliveryDetails);
                        log.info("Final pricing calculated. Total: {}", pricing.getTotalAmount());

                        // Step 6: Build validation results
                        log.info("Step 6: Building validation results");
                        OrderDetailsResponse.ValidationResults validations = buildValidationResults(
                                        checkoutItems, vendorBranch, true, true);
                        log.info("Validations: {}", validations);

                        // Step 7: Create checkout session
                        log.info("Step 7: Creating checkout session in Redis");
                        CheckoutSession session = buildCheckoutSession(
                                        request, vendorBranch, checkoutItems, pricing, validations);

                        String sessionId = sessionManagementService.createSession(session);
                        log.info("Checkout session created with ID: {}", sessionId);

                        // Step 8: Build and return response
                        log.info("Step 8: Building success response");
                        OrderDetailsResponse response = buildSuccessResponse(
                                        sessionId, vendorBranch, checkoutItems, pricing, validations,
                                        session.getExpiresAt());
                        log.info("=== CHECKOUT COMPLETED SUCCESSFULLY ===");
                        return response;

                } catch (Exception e) {
                        log.error("=== CHECKOUT FAILED WITH EXCEPTION ===", e);
                        log.error("Exception type: {}", e.getClass().getName());
                        log.error("Exception message: {}", e.getMessage());
                        throw e;
                }
        }

        /**
         * Get existing checkout session
         */
        public OrderDetailsResponse getCheckoutSession(String sessionId) {
                return sessionManagementService.getSession(sessionId)
                                .map(this::convertSessionToResponse)
                                .orElseThrow(() -> {
                                        log.warn("Checkout session not found or expired: {}", sessionId);
                                        return new com.teadelivery.ordercatalog.order.checkout.exception.CheckoutSessionNotFoundException(
                                                        "Checkout session not found or expired: " + sessionId);
                                });
        }

        /**
         * Validate vendor branch
         * Uses JOIN FETCH to eagerly load vendor to avoid LazyInitializationException
         */
        private VendorBranch validateVendorBranch(Long branchId, List<OrderDetailsResponse.CheckoutError> errors) {
                return vendorBranchRepository.findByIdWithVendor(branchId)
                                .filter(VendorBranch::getIsActive)
                                .orElseGet(() -> {
                                        errors.add(buildError(
                                                        "VENDOR_NOT_FOUND",
                                                        "Vendor branch not found or inactive",
                                                        "vendorBranchId"));
                                        return null;
                                });
        }

        /**
         * Validate cart items and build checkout items with current prices
         */
        private List<OrderDetailsResponse.CheckoutItem> validateAndBuildCheckoutItems(
                        CheckoutRequest request,
                        VendorBranch vendorBranch,
                        List<OrderDetailsResponse.CheckoutError> errors) {
                List<OrderDetailsResponse.CheckoutItem> checkoutItems = new ArrayList<>();

                for (CheckoutRequest.CartItemRequest cartItem : request.getItems()) {
                        try {
                                log.debug("Validating cart item: menuItemId={}, quantity={}",
                                                cartItem.getMenuItemId(), cartItem.getQuantity());

                                // Get menu item with current price
                                MenuItemResponse menuItem = menuService.getMenuItem(cartItem.getMenuItemId());
                                log.debug("Menu item found: {} (price: {}, available: {})",
                                                menuItem.getName(), menuItem.getPrice(), menuItem.getIsAvailable());

                                // Validate item belongs to vendor branch
                                if (!menuItem.getBranchId().equals(request.getVendorBranchId())) {
                                        errors.add(buildError(
                                                        "ITEM_NOT_FROM_VENDOR",
                                                        "Item does not belong to selected vendor",
                                                        "items[].menuItemId",
                                                        Map.of("itemId", cartItem.getMenuItemId())));
                                        continue;
                                }

                                // Check if item is available
                                if (!menuItem.getIsAvailable()) {
                                        errors.add(buildError(
                                                        "ITEM_NOT_AVAILABLE",
                                                        menuItem.getName() + " is currently not available",
                                                        "items[].menuItemId",
                                                        Map.of("itemId", cartItem.getMenuItemId(), "itemName",
                                                                        menuItem.getName())));
                                        continue;
                                }

                                // Check stock using MenuService
                                // TODO: Add stock check integration
                                // For now, assume items are in stock

                                // Build checkout item
                                BigDecimal subtotal = menuItem.getPrice()
                                                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                                checkoutItems.add(OrderDetailsResponse.CheckoutItem.builder()
                                                .menuItemId(menuItem.getMenuItemId())
                                                .name(menuItem.getName())
                                                .quantity(cartItem.getQuantity())
                                                .unitPrice(menuItem.getPrice())
                                                .subtotal(subtotal)
                                                .customizations(cartItem.getCustomizations())
                                                .isAvailable(true)
                                                .stockQuantity(null) // TODO: Add stock quantity to MenuItemResponse
                                                .build());

                        } catch (com.teadelivery.ordercatalog.common.exception.MenuItemNotFoundException e) {
                                // Menu item not found - add specific error
                                log.warn("Caught MenuItemNotFoundException for item: {}", cartItem.getMenuItemId());
                                errors.add(buildError(
                                                "ITEM_NOT_FOUND",
                                                "Menu item not found",
                                                "items[].menuItemId",
                                                Map.of("itemId", cartItem.getMenuItemId())));
                                log.warn("Error added to list. Total errors so far: {}", errors.size());
                        } catch (Exception e) {
                                // Unexpected error - add generic validation error
                                log.error("Caught unexpected exception for item: {}", cartItem.getMenuItemId(), e);
                                errors.add(buildError(
                                                "ITEM_VALIDATION_ERROR",
                                                "Error validating item: " + e.getMessage(),
                                                "items[].menuItemId",
                                                Map.of("itemId", cartItem.getMenuItemId())));
                                log.error("Error added to list. Total errors so far: {}", errors.size());
                        }
                }

                return checkoutItems;
        }

        /**
         * Calculate delivery details
         */
        private OrderDetailsResponse.DeliveryChargeDetails calculateDeliveryDetails(
                        CheckoutRequest request,
                        VendorBranch vendorBranch) {
                // TODO: Implement actual distance calculation using vendor branch location
                // For now, use default values
                double distance = 3.5; // km
                String deliveryZone = "ZONE_1";

                return priceCalculationService.calculateDeliveryFee(distance, deliveryZone);
        }

        /**
         * Apply discount if coupon code provided
         */
        private OrderDetailsResponse.DiscountDetails applyDiscount(
                        CheckoutRequest request,
                        List<OrderDetailsResponse.CheckoutItem> items) {
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
                                itemTotal, "PERCENTAGE", discountValue, maxDiscount);

                return OrderDetailsResponse.DiscountDetails.builder()
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
        private OrderDetailsResponse.ValidationResults buildValidationResults(
                        List<OrderDetailsResponse.CheckoutItem> items,
                        VendorBranch vendorBranch,
                        boolean deliveryAddressValid,
                        boolean deliveryZoneServiceable) {
                boolean allItemsAvailable = items.stream()
                                .allMatch(OrderDetailsResponse.CheckoutItem::getIsAvailable);

                return OrderDetailsResponse.ValidationResults.builder()
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
                        List<OrderDetailsResponse.CheckoutItem> items,
                        OrderDetailsResponse.PricingDetails pricing,
                        OrderDetailsResponse.ValidationResults validations) {
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
        private OrderDetailsResponse buildSuccessResponse(
                        String sessionId,
                        VendorBranch vendorBranch,
                        List<OrderDetailsResponse.CheckoutItem> items,
                        OrderDetailsResponse.PricingDetails pricing,
                        OrderDetailsResponse.ValidationResults validations,
                        LocalDateTime expiresAt) {
                return OrderDetailsResponse.builder()
                                .checkoutSessionId(sessionId)
                                .status(CheckoutSessionStatus.READY_FOR_COMMIT)
                                .statusDisplayName(CheckoutSessionStatus.READY_FOR_COMMIT.getDisplayName())
                                .expiresAt(expiresAt)
                                .vendor(buildVendorInfo(vendorBranch))
                                .items(items)
                                .totalItemCount(items.stream().mapToInt(OrderDetailsResponse.CheckoutItem::getQuantity)
                                                .sum())
                                .pricing(pricing)
                                .delivery(buildDeliveryInfo())
                                .validations(validations)
                                .build();
        }

        /**
         * Build error response
         */
        private OrderDetailsResponse buildErrorResponse(List<OrderDetailsResponse.CheckoutError> errors) {
                return OrderDetailsResponse.builder()
                                .checkoutSessionId(null)
                                .status(CheckoutSessionStatus.VALIDATION_FAILED)
                                .errors(errors)
                                .build();
        }

        /**
         * Build vendor info
         */
        private OrderDetailsResponse.VendorInfo buildVendorInfo(VendorBranch vendorBranch) {
                return OrderDetailsResponse.VendorInfo.builder()
                                .vendorId(vendorBranch.getVendor().getVendorId())
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
        private OrderDetailsResponse.DeliveryInfo buildDeliveryInfo() {
                int prepTime = 25;
                int deliveryDuration = 20;
                int totalTime = prepTime + deliveryDuration;

                return OrderDetailsResponse.DeliveryInfo.builder()
                                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(totalTime))
                                .estimatedPrepTime(prepTime)
                                .estimatedDeliveryDuration(deliveryDuration)
                                .totalEstimatedTime(totalTime)
                                .deliveryTimeRange(totalTime + "-" + (totalTime + 15) + " mins")
                                .build();
        }

        /**
         * Build error object
         */
        private OrderDetailsResponse.CheckoutError buildError(String code, String message, String field) {
                return buildError(code, message, field, null);
        }

        private OrderDetailsResponse.CheckoutError buildError(
                        String code,
                        String message,
                        String field,
                        Map<String, Object> metadata) {
                return OrderDetailsResponse.CheckoutError.builder()
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
        private OrderDetailsResponse convertSessionToResponse(CheckoutSession session) {
                // Rebuild checkout items from session
                List<OrderDetailsResponse.CheckoutItem> items = session.getItems().stream()
                                .map(cartItem -> {
                                        try {
                                                MenuItemResponse menuItem = menuService
                                                                .getMenuItem(cartItem.getMenuItemId());
                                                BigDecimal subtotal = menuItem.getPrice()
                                                                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                                                return OrderDetailsResponse.CheckoutItem.builder()
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

                return OrderDetailsResponse.builder()
                                .checkoutSessionId(session.getCheckoutSessionId())
                                .status(session.getStatus())
                                .statusDisplayName(session.getStatus().getDisplayName())
                                .expiresAt(session.getExpiresAt())
                                .vendor(buildVendorInfo(vendorBranch))
                                .items(items)
                                .totalItemCount(items.stream().mapToInt(OrderDetailsResponse.CheckoutItem::getQuantity)
                                                .sum())
                                .pricing(session.getPricing())
                                .delivery(buildDeliveryInfo())
                                .validations(session.getValidations())
                                .build();
        }

}
