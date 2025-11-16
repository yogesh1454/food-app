# BE-003-17: Base FSM Framework with Stateless4j

**Story ID:** BE-003-17  
**Story Points:** 8  
**Priority:** Critical (P0)  
**Sprint:** 15  
**Epic:** BE-003  
**Dependencies:** BE-003-14, BE-003-15, BE-003-16

---

## 📖 User Story

**As a** backend developer  
**I want** to implement a base FSM framework using Stateless4j  
**So that** Order and Delivery FSMs can be built with consistent state management

---

## ✅ Acceptance Criteria

1. **Stateless4j Integration**
   - [ ] Stateless4j dependency added to project
   - [ ] Base FSM configuration class created
   - [ ] State and trigger enums defined
   - [ ] FSM instance factory implemented

2. **State Management**
   - [ ] Load state from database/cache
   - [ ] Persist state after transitions
   - [ ] Cache state in Redis
   - [ ] Handle concurrent state transitions

3. **Event Publishing**
   - [ ] Publish state change events to Kafka
   - [ ] Include previous and new state
   - [ ] Include trigger and metadata
   - [ ] Handle event publishing failures

4. **Audit Trail**
   - [ ] Record all state transitions in audit table
   - [ ] Include actor information
   - [ ] Include timestamp and metadata
   - [ ] Support querying audit history

5. **Error Handling**
   - [ ] Handle invalid state transitions
   - [ ] Handle database failures
   - [ ] Handle cache failures
   - [ ] Implement retry logic

6. **Testing**
   - [ ] Unit tests for state transitions
   - [ ] Integration tests for persistence
   - [ ] Concurrent transition tests
   - [ ] Failure scenario tests

---

## 🔧 Technical Implementation

### **Dependencies**

```gradle
// build.gradle
dependencies {
    implementation 'com.github.oxo42:stateless4j:2.6.0'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.kafka:spring-kafka'
}
```

### **Base FSM Interface**

```java
public interface FiniteStateMachine<TState, TTrigger> {
    
    /**
     * Fire a trigger to transition state
     */
    void fire(UUID entityId, TTrigger trigger);
    
    /**
     * Fire a trigger with parameters
     */
    void fire(UUID entityId, TTrigger trigger, Map<String, Object> parameters);
    
    /**
     * Get current state
     */
    TState getState(UUID entityId);
    
    /**
     * Check if trigger is permitted in current state
     */
    boolean canFire(UUID entityId, TTrigger trigger);
    
    /**
     * Get permitted triggers for current state
     */
    List<TTrigger> getPermittedTriggers(UUID entityId);
}
```

### **Base FSM Implementation**

```java
@Slf4j
public abstract class BaseStateMachine<TState extends Enum<TState>, TTrigger extends Enum<TTrigger>> 
    implements FiniteStateMachine<TState, TTrigger> {
    
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
     * Subclasses must provide FSM configuration
     */
    protected abstract StateMachineConfig<TState, TTrigger> configure();
    
    /**
     * Subclasses must load state from persistence
     */
    protected abstract TState loadStateFromDatabase(UUID entityId);
    
    /**
     * Subclasses must persist state to database
     */
    protected abstract void persistStateToDatabase(UUID entityId, TState state);
    
    /**
     * Subclasses must provide entity type for events
     */
    protected abstract String getEntityType();
    
    @Override
    public void fire(UUID entityId, TTrigger trigger) {
        fire(entityId, trigger, Collections.emptyMap());
    }
    
    @Override
    public void fire(UUID entityId, TTrigger trigger, Map<String, Object> parameters) {
        log.info("Firing trigger: entityId={}, trigger={}", entityId, trigger);
        
        // Load current state
        TState currentState = getState(entityId);
        
        // Create FSM instance
        StateMachine<TState, TTrigger> stateMachine = createStateMachine(currentState);
        
        // Check if transition is permitted
        if (!stateMachine.canFire(trigger)) {
            throw new InvalidStateTransitionException(
                String.format("Cannot fire trigger %s in state %s", trigger, currentState)
            );
        }
        
        try {
            // Fire trigger
            stateMachine.fire(trigger);
            
            // Get new state
            TState newState = stateMachine.getState();
            
            // Persist state
            persistState(entityId, newState);
            
            // Record audit
            recordAudit(entityId, currentState, newState, trigger, parameters);
            
            // Publish event
            publishStateChangeEvent(entityId, currentState, newState, trigger, parameters);
            
            log.info("State transition successful: entityId={}, {} -> {}", 
                entityId, currentState, newState);
            
        } catch (Exception e) {
            log.error("State transition failed: entityId={}, trigger={}", 
                entityId, trigger, e);
            throw new StateTransitionException("State transition failed", e);
        }
    }
    
    @Override
    public TState getState(UUID entityId) {
        // Try cache first
        Optional<TState> cachedState = cacheService.getState(entityId);
        if (cachedState.isPresent()) {
            return cachedState.get();
        }
        
        // Load from database
        TState state = loadStateFromDatabase(entityId);
        
        // Cache for future use
        cacheService.cacheState(entityId, state);
        
        return state;
    }
    
    @Override
    public boolean canFire(UUID entityId, TTrigger trigger) {
        TState currentState = getState(entityId);
        StateMachine<TState, TTrigger> stateMachine = createStateMachine(currentState);
        return stateMachine.canFire(trigger);
    }
    
    @Override
    public List<TTrigger> getPermittedTriggers(UUID entityId) {
        TState currentState = getState(entityId);
        StateMachine<TState, TTrigger> stateMachine = createStateMachine(currentState);
        return new ArrayList<>(stateMachine.getPermittedTriggers());
    }
    
    protected StateMachine<TState, TTrigger> createStateMachine(TState initialState) {
        StateMachineConfig<TState, TTrigger> config = configure();
        return new StateMachine<>(initialState, config);
    }
    
    protected void persistState(UUID entityId, TState state) {
        // Persist to database
        persistStateToDatabase(entityId, state);
        
        // Update cache
        cacheService.cacheState(entityId, state);
    }
    
    protected void recordAudit(
        UUID entityId,
        TState previousState,
        TState newState,
        TTrigger trigger,
        Map<String, Object> parameters
    ) {
        StateAuditRecord audit = StateAuditRecord.builder()
            .entityId(entityId)
            .entityType(getEntityType())
            .previousState(previousState.name())
            .newState(newState.name())
            .trigger(trigger.name())
            .actorType(getActorType(parameters))
            .actorId(getActorId(parameters))
            .metadata(parameters)
            .build();
        
        auditService.recordTransition(audit);
    }
    
    protected void publishStateChangeEvent(
        UUID entityId,
        TState previousState,
        TState newState,
        TTrigger trigger,
        Map<String, Object> parameters
    ) {
        StateChangeEvent event = StateChangeEvent.builder()
            .entityId(entityId)
            .entityType(getEntityType())
            .previousState(previousState.name())
            .newState(newState.name())
            .trigger(trigger.name())
            .timestamp(Instant.now())
            .metadata(parameters)
            .build();
        
        eventPublisher.publishStateChange(event);
    }
    
    protected String getActorType(Map<String, Object> parameters) {
        return (String) parameters.getOrDefault("actorType", "SYSTEM");
    }
    
    protected UUID getActorId(Map<String, Object> parameters) {
        Object actorId = parameters.get("actorId");
        return actorId != null ? UUID.fromString(actorId.toString()) : null;
    }
}
```

### **State Cache Service**

```java
@Service
@Slf4j
public class StateCacheService<TState> {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String STATE_KEY_PREFIX = "fsm:state:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    
    public void cacheState(UUID entityId, TState state) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            redisTemplate.opsForValue().set(key, state.toString(), CACHE_TTL);
            log.debug("Cached state: entityId={}, state={}", entityId, state);
        } catch (Exception e) {
            log.error("Failed to cache state: entityId={}", entityId, e);
        }
    }
    
    public Optional<TState> getState(UUID entityId) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            String stateStr = (String) redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(stateStr)
                .map(s -> (TState) Enum.valueOf(getStateClass(), s));
        } catch (Exception e) {
            log.error("Failed to get state from cache: entityId={}", entityId, e);
            return Optional.empty();
        }
    }
    
    public void invalidateState(UUID entityId) {
        String key = STATE_KEY_PREFIX + entityId;
        try {
            redisTemplate.delete(key);
            log.debug("Invalidated state cache: entityId={}", entityId);
        } catch (Exception e) {
            log.error("Failed to invalidate state cache: entityId={}", entityId, e);
        }
    }
    
    private Class<TState> getStateClass() {
        // This would be injected or determined at runtime
        throw new UnsupportedOperationException("Subclass must provide state class");
    }
}
```

### **State Audit Service**

```java
@Service
@Slf4j
@Transactional
public class StateAuditService {
    
    private final StateAuditRepository auditRepository;
    
    public void recordTransition(StateAuditRecord audit) {
        try {
            StateAuditEntity entity = StateAuditEntity.builder()
                .entityId(audit.getEntityId())
                .entityType(audit.getEntityType())
                .previousState(audit.getPreviousState())
                .newState(audit.getNewState())
                .trigger(audit.getTrigger())
                .actorType(audit.getActorType())
                .actorId(audit.getActorId())
                .transitionedAt(Instant.now())
                .metadata(audit.getMetadata())
                .build();
            
            auditRepository.save(entity);
            log.debug("Recorded state transition: entityId={}, {} -> {}", 
                audit.getEntityId(), audit.getPreviousState(), audit.getNewState());
        } catch (Exception e) {
            log.error("Failed to record state transition: entityId={}", 
                audit.getEntityId(), e);
            // Don't fail the transaction if audit fails
        }
    }
    
    public List<StateAuditRecord> getAuditHistory(UUID entityId) {
        return auditRepository.findByEntityIdOrderByTransitionedAtDesc(entityId)
            .stream()
            .map(this::toAuditRecord)
            .collect(Collectors.toList());
    }
    
    private StateAuditRecord toAuditRecord(StateAuditEntity entity) {
        return StateAuditRecord.builder()
            .entityId(entity.getEntityId())
            .entityType(entity.getEntityType())
            .previousState(entity.getPreviousState())
            .newState(entity.getNewState())
            .trigger(entity.getTrigger())
            .actorType(entity.getActorType())
            .actorId(entity.getActorId())
            .transitionedAt(entity.getTransitionedAt())
            .metadata(entity.getMetadata())
            .build();
    }
}
```

### **Event Publisher**

```java
@Service
@Slf4j
public class EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String STATE_CHANGE_TOPIC = "state-change-events";
    
    public void publishStateChange(StateChangeEvent event) {
        try {
            ListenableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(
                    STATE_CHANGE_TOPIC, 
                    event.getEntityId().toString(), 
                    event
                );
            
            future.addCallback(
                result -> log.info("Published state change event: entityId={}, state={}", 
                    event.getEntityId(), event.getNewState()),
                ex -> log.error("Failed to publish state change event: entityId={}", 
                    event.getEntityId(), ex)
            );
        } catch (Exception e) {
            log.error("Error publishing state change event", e);
            // Don't fail the transaction if event publishing fails
        }
    }
}
```

---

## 📋 Testing Requirements

### **Unit Tests**
- [ ] Test state transitions with valid triggers
- [ ] Test invalid state transitions
- [ ] Test concurrent state transitions
- [ ] Test cache hit/miss scenarios
- [ ] Test audit recording
- [ ] Test event publishing

### **Integration Tests**
- [ ] Test complete FSM lifecycle
- [ ] Test database persistence
- [ ] Test cache integration
- [ ] Test Kafka event publishing
- [ ] Test failure recovery

---

## 📚 References

- [Architecture Decisions](../../business-flows/01_ARCHITECTURE_DECISIONS.md)
- [Order FSM Design](../../business-flows/02_ORDER_FSM_DESIGN.md)
- [Delivery FSM Design](../../business-flows/03_DELIVERY_FSM_DESIGN.md)

---

## 🎯 Definition of Done

- [ ] Stateless4j integrated
- [ ] Base FSM framework implemented
- [ ] State cache service implemented
- [ ] Audit service implemented
- [ ] Event publisher implemented
- [ ] Unit tests passing with > 80% coverage
- [ ] Integration tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
