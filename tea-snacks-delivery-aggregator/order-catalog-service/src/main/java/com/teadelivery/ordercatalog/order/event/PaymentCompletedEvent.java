package com.teadelivery.ordercatalog.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when payment is completed for an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    
    private UUID orderId;
    private UUID customerId;
    private String transactionId;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime completedAt;
    private String checkoutSessionId;
    private String gatewayResponse;
    
    @Builder.Default
    private String version = "1.0";
}
