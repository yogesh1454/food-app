package com.teadelivery.ordercatalog.common.fsm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * State Audit Service
 * Records state transitions for audit trail
 */
@Service
@Slf4j
public class StateAuditService {
    
    /**
     * Record state transition
     */
    public void recordTransition(
        UUID entityId,
        String entityType,
        String fromState,
        String toState,
        String trigger
    ) {
        log.info("Recording state transition: entityId={}, entityType={}, from={}, to={}, trigger={}",
            entityId, entityType, fromState, toState, trigger);
        
        // TODO: Implement actual audit recording to database
        // This will be implemented when we create the audit repositories
    }
}
