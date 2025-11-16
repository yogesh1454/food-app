package com.teadelivery.ordercatalog.payment.dto;

import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment transaction result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    
    private String transactionId;
    
    private UUID userId;
    
    private BigDecimal amount;
    
    private String paymentMethod;
    
    private PaymentStatus status;
    
    private String gatewayResponse;
    
    private String errorCode;
    
    private String errorMessage;
    
    private LocalDateTime createdAt;
    
    /**
     * Check if payment was successful
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.PAID || status == PaymentStatus.PENDING;
    }
}
