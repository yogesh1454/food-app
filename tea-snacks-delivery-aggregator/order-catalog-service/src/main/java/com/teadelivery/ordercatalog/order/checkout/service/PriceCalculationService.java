package com.teadelivery.ordercatalog.order.checkout.service;

import com.teadelivery.ordercatalog.order.dto.OrderDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for calculating checkout pricing
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal GST_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal BASE_DELIVERY_FEE = new BigDecimal("20.00");
    private static final BigDecimal PER_KM_FEE = new BigDecimal("5.00");

    /**
     * Calculate complete pricing breakdown
     */
    public OrderDetailsResponse.PricingDetails calculatePricing(
            List<OrderDetailsResponse.CheckoutItem> items,
            OrderDetailsResponse.DiscountDetails discountDetails,
            OrderDetailsResponse.DeliveryChargeDetails deliveryDetails) {
        log.debug("Calculating pricing for {} items", items.size());

        // Step 1: Calculate item total
        BigDecimal itemTotal = calculateItemTotal(items);

        // Step 2: Apply discount
        BigDecimal discount = discountDetails != null ? discountDetails.getAppliedDiscount() : BigDecimal.ZERO;
        BigDecimal subtotalAfterDiscount = itemTotal.subtract(discount);

        // Step 3: Calculate delivery charges
        BigDecimal deliveryCharges = deliveryDetails != null
                ? deliveryDetails.getBaseFee().add(deliveryDetails.getDistanceFee())
                : BASE_DELIVERY_FEE;

        // Step 4: Calculate platform fee
        BigDecimal platformFee = calculatePlatformFee(subtotalAfterDiscount, deliveryCharges);

        // Step 5: Calculate GST
        BigDecimal taxableAmount = subtotalAfterDiscount
                .add(deliveryCharges)
                .add(platformFee);
        BigDecimal gst = calculateGst(taxableAmount);
        OrderDetailsResponse.GstDetails gstDetails = buildGstDetails(gst);

        // Step 6: Calculate total
        BigDecimal totalAmount = taxableAmount.add(gst);

        log.info("Pricing calculated - Item: {}, Discount: {}, Delivery: {}, Platform: {}, GST: {}, Total: {}",
                itemTotal, discount, deliveryCharges, platformFee, gst, totalAmount);

        return OrderDetailsResponse.PricingDetails.builder()
                .itemTotal(itemTotal)
                .discount(discount)
                .discountDetails(discountDetails)
                .subtotalAfterDiscount(subtotalAfterDiscount)
                .deliveryCharges(deliveryCharges)
                .deliveryDetails(deliveryDetails)
                .platformFee(platformFee)
                .gst(gst)
                .gstDetails(gstDetails)
                .totalAmount(totalAmount)
                .currency("INR")
                .build();
    }

    /**
     * Calculate item total
     */
    public BigDecimal calculateItemTotal(List<OrderDetailsResponse.CheckoutItem> items) {
        return items.stream()
                .map(OrderDetailsResponse.CheckoutItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate discount amount
     */
    public BigDecimal calculateDiscount(
            BigDecimal itemTotal,
            String discountType,
            BigDecimal discountValue,
            BigDecimal maxDiscount) {
        BigDecimal discount;

        if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            // Percentage discount
            discount = itemTotal
                    .multiply(discountValue.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);

            // Apply max discount cap
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else if ("FLAT".equalsIgnoreCase(discountType)) {
            // Flat discount
            discount = discountValue.min(itemTotal);
        } else {
            discount = BigDecimal.ZERO;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate delivery fee based on distance
     */
    public OrderDetailsResponse.DeliveryChargeDetails calculateDeliveryFee(
            Double distance,
            String deliveryZone) {
        BigDecimal baseFee = BASE_DELIVERY_FEE;
        BigDecimal distanceFee = BigDecimal.valueOf(distance)
                .multiply(PER_KM_FEE)
                .setScale(2, RoundingMode.HALF_UP);

        return OrderDetailsResponse.DeliveryChargeDetails.builder()
                .distance(distance)
                .distanceUnit("km")
                .deliveryZone(deliveryZone != null ? deliveryZone : "ZONE_1")
                .baseFee(baseFee)
                .distanceFee(distanceFee)
                .build();
    }

    /**
     * Calculate platform fee
     */
    private BigDecimal calculatePlatformFee(BigDecimal subtotal, BigDecimal deliveryCharges) {
        return subtotal
                .add(deliveryCharges)
                .multiply(PLATFORM_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate GST
     */
    private BigDecimal calculateGst(BigDecimal taxableAmount) {
        return taxableAmount
                .multiply(GST_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Build GST details (split into CGST and SGST)
     */
    private OrderDetailsResponse.GstDetails buildGstDetails(BigDecimal gst) {
        BigDecimal half = new BigDecimal("2");
        BigDecimal cgst = gst.divide(half, 2, RoundingMode.HALF_UP);
        BigDecimal sgst = gst.divide(half, 2, RoundingMode.HALF_UP);

        return OrderDetailsResponse.GstDetails.builder()
                .cgst(cgst)
                .sgst(sgst)
                .gstRate(5)
                .build();
    }
}
