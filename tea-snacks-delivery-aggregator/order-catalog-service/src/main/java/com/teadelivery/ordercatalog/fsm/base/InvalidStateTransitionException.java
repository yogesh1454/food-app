package com.teadelivery.ordercatalog.fsm.base;

/**
 * Invalid State Transition Exception
 * Thrown when an invalid state transition is attempted
 */
public class InvalidStateTransitionException extends RuntimeException {
    
    public InvalidStateTransitionException(String message) {
        super(message);
    }
    
    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
