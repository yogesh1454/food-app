package com.teadelivery.ordercatalog.order.exception;

import java.time.LocalTime;

/**
 * Exception thrown when vendor is closed or not accepting orders
 */
public class VendorClosedException extends RuntimeException {
    
    private final Long vendorBranchId;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    
    public VendorClosedException(String message, Long vendorBranchId, LocalTime openTime, LocalTime closeTime) {
        super(message);
        this.vendorBranchId = vendorBranchId;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
    
    public Long getVendorBranchId() {
        return vendorBranchId;
    }
    
    public LocalTime getOpenTime() {
        return openTime;
    }
    
    public LocalTime getCloseTime() {
        return closeTime;
    }
}
