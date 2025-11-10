package com.teadelivery.ordercatalog.delivery.fsm.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when rider responds to assignment request
 * Published to: assignment-responses topic
 * Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderAssignmentResponseEvent {
    
    private UUID requestId;
    private UUID deliveryId;
    private UUID riderId;
    private Boolean accepted;
    private String rejectionReason;
    private Instant timestamp;
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String version = "1.0";
}
