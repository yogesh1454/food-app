package com.teadelivery.ordercatalog.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Earnings DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsDTO {
    
    private BigDecimal today;
    private BigDecimal thisWeek;
    private BigDecimal thisMonth;
    
    private Integer deliveriesToday;
    private Integer deliveriesThisWeek;
    private Integer deliveriesThisMonth;
}
