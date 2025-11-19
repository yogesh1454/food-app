package com.teadelivery.ordercatalog.common.exception;

public class MenuItemNotFoundException extends RuntimeException {
    
    public MenuItemNotFoundException(String message) {
        super(message);
    }
    
    public MenuItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
