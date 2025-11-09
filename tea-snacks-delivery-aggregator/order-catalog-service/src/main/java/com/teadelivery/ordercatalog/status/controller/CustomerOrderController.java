package com.teadelivery.ordercatalog.status.controller;

import com.teadelivery.ordercatalog.status.dto.CustomerStatusResponseDTO;
import com.teadelivery.ordercatalog.status.service.CustomerStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Customer Order Controller
 * APIs for customers to track orders with simplified status
 * As per BE-004-28
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
@Slf4j
@Validated
@Tag(name = "Customer Order Tracking", description = "APIs for customers to track their orders")
public class CustomerOrderController {
    
    private final CustomerStatusService customerStatusService;
    
    public CustomerOrderController(CustomerStatusService customerStatusService) {
        this.customerStatusService = customerStatusService;
    }
    
    /**
     * Get order status for customer
     */
    @GetMapping("/{orderId}/status")
    @Operation(
        summary = "Get order status",
        description = "Get simplified order status with ETA, progress, and rider info"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "403", description = "Order does not belong to customer")
    })
    public ResponseEntity<CustomerStatusResponseDTO> getOrderStatus(
        @PathVariable UUID customerId,
        @PathVariable UUID orderId
    ) {
        log.info("Getting order status: customerId={}, orderId={}", customerId, orderId);
        
        CustomerStatusResponseDTO response = customerStatusService.getOrderStatus(
            customerId, orderId);
        
        return ResponseEntity.ok(response);
    }
}
