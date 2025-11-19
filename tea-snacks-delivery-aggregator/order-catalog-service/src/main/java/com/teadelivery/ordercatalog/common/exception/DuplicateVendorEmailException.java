package com.teadelivery.ordercatalog.common.exception;

public class DuplicateVendorEmailException extends RuntimeException {
    
    public DuplicateVendorEmailException(String message) {
        super(message);
    }
    
    public DuplicateVendorEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
