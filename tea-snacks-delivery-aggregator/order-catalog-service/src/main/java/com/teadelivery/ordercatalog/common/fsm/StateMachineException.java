package com.teadelivery.ordercatalog.common.fsm;

/**
 * State Machine Exception
 * Thrown when a state machine operation fails
 */
public class StateMachineException extends RuntimeException {
    
    public StateMachineException(String message) {
        super(message);
    }
    
    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
