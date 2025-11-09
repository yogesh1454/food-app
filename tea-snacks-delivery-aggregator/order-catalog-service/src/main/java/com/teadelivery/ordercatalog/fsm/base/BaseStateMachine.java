package com.teadelivery.ordercatalog.fsm.base;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Base State Machine
 * Abstract base class for all FSM implementations
 * 
 * @param <TState> State enum type
 * @param <TTrigger> Trigger enum type
 */
@Slf4j
public abstract class BaseStateMachine<TState extends Enum<TState>, TTrigger extends Enum<TTrigger>> {
    
    protected final StateCacheService<TState> cacheService;
    protected final StateAuditService auditService;
    protected final EventPublisher eventPublisher;
    
    public BaseStateMachine(
        StateCacheService<TState> cacheService,
        StateAuditService auditService,
        EventPublisher eventPublisher
    ) {
        this.cacheService = cacheService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Configure the state machine with states and transitions
     */
    protected abstract StateMachineConfig<TState, TTrigger> configure();
    
    /**
     * Load current state from database
     */
    protected abstract TState loadStateFromDatabase(UUID entityId);
    
    /**
     * Persist state to database
     */
    protected abstract void persistStateToDatabase(UUID entityId, TState state);
    
    /**
     * Get entity type for logging and auditing
     */
    protected abstract String getEntityType();
    
    /**
     * Fire a trigger to transition state
     */
    public void fire(UUID entityId, TTrigger trigger) {
        log.info("Firing trigger: entityId={}, entityType={}, trigger={}", 
            entityId, getEntityType(), trigger);
        
        try {
            // Load current state
            TState currentState = loadState(entityId);
            
            // Create state machine instance
            StateMachine<TState, TTrigger> stateMachine = 
                new StateMachine<>(currentState, configure());
            
            // Check if transition is permitted
            if (!stateMachine.canFire(trigger)) {
                throw new InvalidStateTransitionException(
                    String.format("Cannot fire trigger %s in state %s for %s %s",
                        trigger, currentState, getEntityType(), entityId)
                );
            }
            
            // Fire trigger
            stateMachine.fire(trigger);
            
            // Get new state
            TState newState = stateMachine.getState();
            
            // Persist new state
            saveState(entityId, currentState, newState, trigger);
            
            log.info("State transition successful: entityId={}, from={}, to={}, trigger={}",
                entityId, currentState, newState, trigger);
            
        } catch (Exception e) {
            log.error("State transition failed: entityId={}, trigger={}", 
                entityId, trigger, e);
            throw new StateMachineException(
                "State transition failed for " + getEntityType() + " " + entityId, e
            );
        }
    }
    
    /**
     * Get current state
     */
    public TState getState(UUID entityId) {
        return loadState(entityId);
    }
    
    /**
     * Check if trigger can be fired
     */
    public boolean canFire(UUID entityId, TTrigger trigger) {
        try {
            TState currentState = loadState(entityId);
            StateMachine<TState, TTrigger> stateMachine = 
                new StateMachine<>(currentState, configure());
            return stateMachine.canFire(trigger);
        } catch (Exception e) {
            log.error("Error checking if trigger can fire: entityId={}, trigger={}", 
                entityId, trigger, e);
            return false;
        }
    }
    
    /**
     * Load state (from cache or database)
     */
    private TState loadState(UUID entityId) {
        // Try cache first
        TState cachedState = cacheService.getState(entityId);
        if (cachedState != null) {
            log.debug("State loaded from cache: entityId={}, state={}", entityId, cachedState);
            return cachedState;
        }
        
        // Load from database
        TState state = loadStateFromDatabase(entityId);
        
        // Cache it
        cacheService.cacheState(entityId, state);
        
        log.debug("State loaded from database: entityId={}, state={}", entityId, state);
        return state;
    }
    
    /**
     * Save state (to database and cache)
     */
    private void saveState(UUID entityId, TState fromState, TState toState, TTrigger trigger) {
        // Persist to database
        persistStateToDatabase(entityId, toState);
        
        // Update cache
        cacheService.cacheState(entityId, toState);
        
        // Record audit trail
        auditService.recordTransition(
            entityId,
            getEntityType(),
            fromState != null ? fromState.name() : null,
            toState.name(),
            trigger.name()
        );
        
        // Publish event
        eventPublisher.publishStateChange(
            entityId,
            getEntityType(),
            fromState != null ? fromState.name() : null,
            toState.name(),
            trigger.name()
        );
    }
}
