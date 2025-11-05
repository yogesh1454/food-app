package com.teadelivery.ordercatalog.common.exception;

public class BranchNotFoundException extends RuntimeException {
    
    public BranchNotFoundException(String message) {
        super(message);
    }
    
    public BranchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
