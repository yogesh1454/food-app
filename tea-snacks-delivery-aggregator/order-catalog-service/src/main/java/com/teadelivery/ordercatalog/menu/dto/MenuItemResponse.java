package com.teadelivery.ordercatalog.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    
    private Long menuItemId;
    private Long branchId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Boolean isAvailable;
    private Integer preparationTimeMinutes;
    private Map<String, Object> images;
    private Map<String, Object> metadata;
    private String[] tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
