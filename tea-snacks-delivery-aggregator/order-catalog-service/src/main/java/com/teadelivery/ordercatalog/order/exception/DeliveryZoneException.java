package com.teadelivery.ordercatalog.order.exception;

/**
 * Exception thrown when delivery location is outside service area
 */
public class DeliveryZoneException extends RuntimeException {
    
    private final Double latitude;
    private final Double longitude;
    private final Long vendorBranchId;
    
    public DeliveryZoneException(String message, Double latitude, Double longitude, Long vendorBranchId) {
        super(message);
        this.latitude = latitude;
        this.longitude = longitude;
        this.vendorBranchId = vendorBranchId;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public Long getVendorBranchId() {
        return vendorBranchId;
    }
}
