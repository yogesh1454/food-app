package com.teadelivery.ordercatalog.order.exception;

/**
 * Exception thrown when menu item is not available or out of stock
 */
public class MenuItemUnavailableException extends RuntimeException {
    
    private final Long menuItemId;
    private final String itemName;
    private final String reason;
    
    public MenuItemUnavailableException(String message, Long menuItemId, String itemName, String reason) {
        super(message);
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.reason = reason;
    }
    
    public Long getMenuItemId() {
        return menuItemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public String getReason() {
        return reason;
    }
}
