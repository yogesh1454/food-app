package com.teadelivery.ordercatalog.order.dto;

import com.teadelivery.ordercatalog.order.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for order item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private UUID orderItemId;
    private Long menuItemId;
    private String itemName;
    private String itemDescription;
    private String imageUrl;
    private String categoryName;
    private Integer quantity;
    private BigDecimal priceAtOrder;
    private BigDecimal subtotal;
    private String notes;
    private Map<String, Object> customizations;

    /**
     * Convert OrderItem entity to OrderItemResponse DTO
     */
    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .menuItemId(item.getMenuItemId())
                .itemName(item.getItemName())
                .itemDescription(item.getItemDescription())
                .imageUrl(item.getImageUrl())
                .categoryName(item.getCategoryName())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .subtotal(item.getSubtotal())
                .notes(item.getNotes())
                .customizations(item.getCustomizations())
                .build();
    }
}
