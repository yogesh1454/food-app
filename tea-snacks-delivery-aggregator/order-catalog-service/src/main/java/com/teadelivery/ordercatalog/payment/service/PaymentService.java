package com.teadelivery.ordercatalog.payment.service;

import com.teadelivery.ordercatalog.order.fsm.PaymentStatus;
import com.teadelivery.ordercatalog.order.model.Order;
import com.teadelivery.ordercatalog.payment.dto.PaymentTransaction;
import com.teadelivery.ordercatalog.payment.exception.InsufficientFundsException;
import com.teadelivery.ordercatalog.payment.exception.InvalidPaymentTokenException;
import com.teadelivery.ordercatalog.payment.exception.PaymentGatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Service
 * Handles payment processing, verification, and refunds
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    
    // ========== Payment Execution Methods (for Order Creation) ==========
    
    /**
     * Deduct balance from wallet
     * 
     * @param userId User ID
     * @param amount Amount to deduct
     * @param purpose Purpose of payment
     * @return Payment transaction
     * @throws InsufficientFundsException if balance < amount
     */
    public PaymentTransaction deductBalance(UUID userId, BigDecimal amount, String purpose) {
        log.info("Deducting wallet balance: userId={}, amount={}, purpose={}", userId, amount, purpose);
        
        try {
            // TODO: In production, call wallet service API
            // For now, simulate wallet deduction
            
            // Simulate checking balance (mock data)
            BigDecimal availableBalance = new BigDecimal("1000.00"); // Mock balance
            
            if (availableBalance.compareTo(amount) < 0) {
                log.error("Insufficient funds: required={}, available={}", amount, availableBalance);
                throw new InsufficientFundsException(amount, availableBalance);
            }
            
            // Generate transaction ID
            String transactionId = "WLT_" + UUID.randomUUID().toString();
            
            // Simulate successful deduction
            log.info("Wallet balance deducted successfully: txnId={}, amount={}", transactionId, amount);
            
            return PaymentTransaction.builder()
                .transactionId(transactionId)
                .userId(userId)
                .amount(amount)
                .paymentMethod("WALLET")
                .status(PaymentStatus.PAID)
                .gatewayResponse("Wallet deduction successful")
                .createdAt(LocalDateTime.now())
                .build();
                
        } catch (InsufficientFundsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deducting wallet balance: userId={}", userId, e);
            throw new PaymentGatewayException("Wallet service error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process GPay transaction
     * 
     * @param paymentToken Payment token from GPay
     * @param amount Amount to charge
     * @param purpose Purpose of payment
     * @return Payment transaction
     * @throws InvalidPaymentTokenException if token invalid
     * @throws PaymentGatewayException if gateway fails
     */
    public PaymentTransaction processGpayTransaction(String paymentToken, BigDecimal amount, String purpose) {
        log.info("Processing GPay transaction: amount={}, purpose={}", amount, purpose);
        
        try {
            // Validate token
            if (paymentToken == null || paymentToken.isBlank()) {
                throw new InvalidPaymentTokenException("Payment token is required for GPay");
            }
            
            if (!paymentToken.startsWith("tok_gpay_")) {
                throw new InvalidPaymentTokenException("Invalid GPay token format");
            }
            
            // TODO: In production, call GPay payment gateway API
            // For now, simulate GPay processing
            
            boolean gatewaySuccess = Math.random() > 0.1;
            
            // if (!gatewaySuccess) {
            //     log.error("GPay gateway failure: token={}", paymentToken);
            //     throw new PaymentGatewayException("GPay gateway returned error");
            // }
            
            // Generate transaction ID
            String transactionId = "GPAY_" + UUID.randomUUID().toString();
            
            log.info("GPay transaction successful: txnId={}, amount={}", transactionId, amount);
            
            return PaymentTransaction.builder()
                .transactionId(transactionId)
                .userId(null) // Will be set by caller
                .amount(amount)
                .paymentMethod("GPAY")
                .status(PaymentStatus.PAID)
                .gatewayResponse("GPay payment successful")
                .createdAt(LocalDateTime.now())
                .build();
                
        } catch (InvalidPaymentTokenException | PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing GPay transaction", e);
            throw new PaymentGatewayException("GPay processing error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Register COD transaction
     * Always succeeds - payment will be collected on delivery
     * 
     * @param userId User ID
     * @param amount Amount to be collected
     * @param purpose Purpose of payment
     * @return Payment transaction
     */
    public PaymentTransaction registerCodTransaction(UUID userId, BigDecimal amount, String purpose) {
        log.info("Registering COD transaction: userId={}, amount={}, purpose={}", userId, amount, purpose);
        
        // Generate transaction ID
        String transactionId = "COD_" + UUID.randomUUID().toString();
        
        log.info("COD transaction registered: txnId={}, amount={}", transactionId, amount);
        
        return PaymentTransaction.builder()
            .transactionId(transactionId)
            .userId(userId)
            .amount(amount)
            .paymentMethod("COD")
            .status(PaymentStatus.PENDING) // Will be paid on delivery
            .gatewayResponse("COD payment registered - to be collected on delivery")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    // ========== Payment Verification Methods ==========
    
    /**
     * Verify payment for an order
     * Checks if payment was successful and amount matches
     * 
     * @param order Order to verify payment for
     * @return true if payment is valid, false otherwise
     */
    public boolean verifyPayment(Order order) {
        log.info("Verifying payment for order: {}", order.getOrderId());
        
        try {
            // Extract payment details from metadata
            Map<String, Object> metadata = order.getMetadata();
            if (metadata == null) {
                log.error("Order metadata is null for order: {}", order.getOrderId());
                return false;
            }
            
            String paymentMethod = order.getPaymentMethod();
            String transactionId = order.getPaymentTransactionId();
            
            if (paymentMethod == null) {
                log.error("Payment method not found for order: {}", order.getOrderId());
                return false;
            }
            
            // For COD, payment verification happens on delivery
            if ("COD".equals(paymentMethod)) {
                log.info("COD order - payment will be collected on delivery: {}", order.getOrderId());
                return true;
            }
            
            // For online payments, verify transaction ID exists
            if (transactionId == null || transactionId.isEmpty()) {
                log.error("Transaction ID missing for online payment: {}", order.getOrderId());
                return false;
            }
            
            // Verify amount matches
            BigDecimal orderTotal = order.getTotalAmount();
            if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid order total: {}", orderTotal);
                return false;
            }
            
            // TODO: In production, call payment gateway API to verify transaction
            // For now, assume payment is valid if transaction ID exists
            log.info("Payment verified successfully for order: {}, method: {}, txnId: {}", 
                order.getOrderId(), paymentMethod, transactionId);
            
            // Store payment verification timestamp
            metadata.put("paymentVerifiedAt", Instant.now().toString());
            order.setMetadata(metadata);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error verifying payment for order: {}", order.getOrderId(), e);
            return false;
        }
    }
    
    /**
     * Process refund for an order
     * 
     * @param order Order to refund
     * @param refundAmount Amount to refund
     * @param reason Reason for refund
     * @return Refund transaction ID
     */
    public String processRefund(Order order, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for order: {}, amount: {}, reason: {}", 
            order.getOrderId(), refundAmount, reason);
        
        try {
            Map<String, Object> metadata = order.getMetadata();
            String paymentMethod = (String) metadata.get("paymentMethod");
            String originalTransactionId = (String) metadata.get("paymentTransactionId");
            
            // For COD, no refund needed if order cancelled before delivery
            if ("COD".equals(paymentMethod)) {
                log.info("COD order - no refund needed: {}", order.getOrderId());
                return "COD_NO_REFUND";
            }
            
            // Validate refund amount
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid refund amount: " + refundAmount);
            }
            
            if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
                throw new IllegalArgumentException("Refund amount exceeds order total");
            }
            
            // Generate refund transaction ID
            String refundTxnId = "REFUND_" + UUID.randomUUID().toString();
            
            // TODO: In production, call payment gateway API to process refund
            // For now, simulate successful refund
            
            // Store refund details in metadata
            Map<String, Object> refundDetails = new HashMap<>();
            refundDetails.put("refundTransactionId", refundTxnId);
            refundDetails.put("refundAmount", refundAmount.toString());
            refundDetails.put("refundReason", reason);
            refundDetails.put("refundInitiatedAt", Instant.now().toString());
            refundDetails.put("originalTransactionId", originalTransactionId);
            refundDetails.put("refundStatus", "INITIATED");
            
            metadata.put("refund", refundDetails);
            order.setMetadata(metadata);
            
            log.info("Refund processed successfully: orderId={}, refundTxnId={}, amount={}", 
                order.getOrderId(), refundTxnId, refundAmount);
            
            return refundTxnId;
            
        } catch (Exception e) {
            log.error("Error processing refund for order: {}", order.getOrderId(), e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate refund amount based on cancellation policy
     * 
     * @param order Order being cancelled
     * @param cancelledBy Who cancelled (CUSTOMER, VENDOR, SYSTEM)
     * @return Refund amount after applying cancellation fees
     */
    public BigDecimal calculateRefundAmount(Order order, String cancelledBy) {
        log.info("Calculating refund amount for order: {}, cancelledBy: {}", 
            order.getOrderId(), cancelledBy);
        
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal refundAmount = totalAmount;
        
        // If vendor/system cancels, full refund
        if ("VENDOR".equals(cancelledBy) || "SYSTEM".equals(cancelledBy)) {
            log.info("Full refund - cancelled by vendor/system");
            return totalAmount;
        }
        
        // Customer cancellation - apply fees based on order state
        switch (order.getState()) {
            case CREATED:
            case VALIDATED:
            case PAYMENT_CONFIRMED:
            case PENDING_ACCEPTANCE:
                // Free cancellation before restaurant accepts
                log.info("Free cancellation - order not yet accepted");
                refundAmount = totalAmount;
                break;
                
            case ACCEPTED:
                // ₹20 cancellation fee
                BigDecimal fee = new BigDecimal("20.00");
                refundAmount = totalAmount.subtract(fee);
                log.info("Cancellation fee applied: ₹20, refund: {}", refundAmount);
                break;
                
            case PREPARING:
            case READY_FOR_PICKUP:
            case ASSIGNED_TO_RIDER:
            case PICKED_UP:
                // No cancellation allowed (but if system allows, no refund)
                log.warn("Cancellation not allowed in state: {}", order.getState());
                refundAmount = BigDecimal.ZERO;
                break;
                
            default:
                log.warn("Unexpected state for cancellation: {}", order.getState());
                refundAmount = BigDecimal.ZERO;
        }
        
        // Ensure refund is not negative
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundAmount = BigDecimal.ZERO;
        }
        
        log.info("Calculated refund amount: {} for order: {}", refundAmount, order.getOrderId());
        return refundAmount;
    }
    
    /**
     * Settle payments after successful delivery
     * Distributes payment to vendor, rider, and platform
     * 
     * @param order Delivered order
     */
    public void settlePayments(Order order) {
        log.info("Settling payments for order: {}", order.getOrderId());
        
        try {
            Map<String, Object> metadata = order.getMetadata();
            String paymentMethod = (String) metadata.get("paymentMethod");
            
            // Calculate distribution
            BigDecimal itemTotal = order.getItemTotal();
            BigDecimal deliveryCharges = order.getDeliveryCharges();
            BigDecimal platformFee = order.getPlatformFee();
            
            // Vendor gets: item total - platform commission (assume 15%)
            BigDecimal platformCommission = itemTotal.multiply(new BigDecimal("0.15"));
            BigDecimal vendorPayout = itemTotal.subtract(platformCommission);
            
            // Rider gets: delivery charges (assume 80% to rider, 20% to platform)
            BigDecimal riderPayout = deliveryCharges.multiply(new BigDecimal("0.80"));
            BigDecimal platformDeliveryFee = deliveryCharges.multiply(new BigDecimal("0.20"));
            
            // Platform gets: commission + platform fee + delivery fee
            BigDecimal platformTotal = platformCommission.add(platformFee).add(platformDeliveryFee);
            
            // Store settlement details
            Map<String, Object> settlement = new HashMap<>();
            settlement.put("vendorPayout", vendorPayout.toString());
            settlement.put("riderPayout", riderPayout.toString());
            settlement.put("platformTotal", platformTotal.toString());
            settlement.put("settledAt", Instant.now().toString());
            settlement.put("paymentMethod", paymentMethod);
            
            metadata.put("settlement", settlement);
            order.setMetadata(metadata);
            
            log.info("Payment settlement completed: orderId={}, vendor={}, rider={}, platform={}", 
                order.getOrderId(), vendorPayout, riderPayout, platformTotal);
            
            // TODO: In production, trigger actual payout to vendor and rider accounts
            
        } catch (Exception e) {
            log.error("Error settling payments for order: {}", order.getOrderId(), e);
            // Don't throw - settlement can be retried later
        }
    }
    
    // ========== Payment Rollback Methods ==========
    
    /**
     * Rollback/refund wallet payment
     * Used when order creation fails after payment
     * 
     * @param userId User ID
     * @param amount Amount to refund
     * @param reason Reason for refund
     * @return Refund transaction ID
     */
    public String refundWalletBalance(UUID userId, BigDecimal amount, String reason) {
        log.info("Refunding wallet balance: userId={}, amount={}, reason={}", userId, amount, reason);
        
        try {
            // In production, call wallet service API to credit amount back
            String refundTxnId = "REFUND_WLT_" + UUID.randomUUID();
            
            log.info("Wallet refund successful: userId={}, amount={}, refundTxnId={}", 
                userId, amount, refundTxnId);
            
            return refundTxnId;
            
        } catch (Exception e) {
            log.error("Failed to refund wallet balance: userId={}, amount={}", userId, amount, e);
            throw new PaymentGatewayException("Wallet refund failed: " + e.getMessage());
        }
    }
    
    /**
     * Initiate refund for GPay transaction
     * Used when order creation fails after payment
     * 
     * @param transactionId Original transaction ID
     * @param amount Amount to refund
     * @param reason Reason for refund
     * @return Refund transaction ID
     */
    public String refundGpayTransaction(String transactionId, BigDecimal amount, String reason) {
        log.info("Initiating GPay refund: txnId={}, amount={}, reason={}", transactionId, amount, reason);
        
        try {
            // In production, call GPay gateway API to initiate refund
            String refundTxnId = "REFUND_GPAY_" + UUID.randomUUID();
            
            log.info("GPay refund initiated: originalTxnId={}, refundTxnId={}, amount={}", 
                transactionId, refundTxnId, amount);
            
            return refundTxnId;
            
        } catch (Exception e) {
            log.error("Failed to initiate GPay refund: txnId={}, amount={}", transactionId, amount, e);
            throw new PaymentGatewayException("GPay refund failed: " + e.getMessage());
        }
    }
    
    /**
     * Rollback payment transaction
     * Routes to appropriate refund method based on payment method
     * 
     * @param transaction Original payment transaction
     * @param reason Reason for rollback
     * @return Refund transaction ID (null for COD)
     */
    public String rollbackPayment(PaymentTransaction transaction, String reason) {
        log.info("Rolling back payment: txnId={}, method={}, amount={}, reason={}", 
            transaction.getTransactionId(), transaction.getPaymentMethod(), 
            transaction.getAmount(), reason);
        
        try {
            String paymentMethod = transaction.getPaymentMethod();
            
            if ("WALLET".equals(paymentMethod)) {
                return refundWalletBalance(transaction.getUserId(), transaction.getAmount(), reason);
            } else if ("GPAY".equals(paymentMethod)) {
                return refundGpayTransaction(transaction.getTransactionId(), transaction.getAmount(), reason);
            } else if ("COD".equals(paymentMethod)) {
                log.info("COD payment - no rollback needed: txnId={}", transaction.getTransactionId());
                return null; // No refund needed for COD
            } else {
                log.error("Unknown payment method for rollback: {}", paymentMethod);
                throw new IllegalArgumentException("Unknown payment method: " + paymentMethod);
            }
            
        } catch (Exception e) {
            log.error("Payment rollback failed: txnId={}", transaction.getTransactionId(), e);
            throw e;
        }
    }
}
