 PRD: Order & Delivery FSM Flow

1. Overview
Project Name:
Food Delivery Platform – Order & Delivery State Machines

Purpose:
Define and document the functional and non‑functional requirements for the Order and Delivery Finite State Machines (FSMs), including integration points with PostgreSQL, Kafka, and Redis.

2. Objectives
Maintain Consistency: Ensure every order and delivery request moves through a well‑defined, auditable lifecycle.

Scalability: Support thousands of concurrent orders and deliveries with minimal latency.

Extensibility: Allow easy addition of new triggers, states, and side‑effects (e.g., notifications, metrics).

Integration: Seamlessly integrate with REST APIs, Kafka topics (event bus), PostgreSQL (durable state), and Redis (caching, TTL for delayed events).

3. Scope
In Scope
Definition of Order FSM (NEW → … → DELIVERED / CANCELLED / REJECTED)

Definition of Delivery FSM (PENDING → … → DELIVERED)

Callbacks/hooks for assignment, notifications, metrics

Kafka topics and message formats for state‑change events

Persistence model in PostgreSQL

Cache design in Redis (e.g., TTL for auto‑cancel)

Error and retry handling

API endpoints for triggering FSM transitions

Out of Scope
Rider mobile app UI

Payment gateway integration

Detailed UI wireframes

Analytics dashboards

4. Stakeholders
Role

Responsibility

Product Manager

Define features, prioritize backlog

Backend Engineers

Implement FSMs, persistence, APIs

DevOps / SRE

Kafka, PostgreSQL, Redis setup

QA Engineers

Test FSM transitions & integrations

Delivery Team Lead

Define business rules for assignment

Notifications Service

Consume Kafka events, send alerts

5. Technology Stack
Language/Framework: Java + Spring Boot

FSM Library: Stateless4j

Database: PostgreSQL (Order & Delivery tables)

Messaging: Apache Kafka

Cache & Delayed Events: Redis (TTL, keyspace notifications)

Monitoring: Prometheus / Grafana (via Micrometer)

6. Functional Requirements
6.1 Order FSM
6.1.1 States
NEW

ACCEPTED

PREPARING

READY_FOR_PICKUP

PICKED_UP

DELIVERED

CANCELLED (terminal)

REJECTED (terminal)

6.1.2 Triggers
Trigger

From State(s)

To State

Side‑Effects / Callbacks

ACCEPT_ORDER

NEW

ACCEPTED

Publish order.accepted on Kafka

REJECT_ORDER

NEW

REJECTED

Publish order.rejected on Kafka

CANCEL_ORDER

NEW, ACCEPTED

CANCELLED

Publish order.cancelled on Kafka

START_PREPARATION

ACCEPTED

PREPARING

–

FINISH_PREPARATION

PREPARING

READY_FOR_PICKUP

assignDeliveryAgent(orderId)

cache TTL for auto‑cancel in Redis?

PICK_ORDER

READY_FOR_PICKUP

PICKED_UP

Publish order.picked_up

COMPLETE_DELIVERY

PICKED_UP

DELIVERED

Publish order.delivered

6.1.3 Auto‑Cancel Logic
When: Order in NEW state > 5 minutes

How: Redis key order:{id}:auto_cancel with TTL = 5m; on expiry, Redis keyspace notification invokes consumer that fires CANCEL_ORDER if still NEW.

6.2 Delivery FSM
6.2.1 States
PENDING

ASSIGNED

REACHED_RESTAURANT

PICKED_UP

OUT_FOR_DELIVERY

DELIVERED (terminal)

6.2.2 Triggers
Trigger

From State(s)

To State

Side‑Effects / Callbacks

ASSIGN_RIDER

PENDING

ASSIGNED

Publish delivery.assigned on Kafka

REACH_RESTAURANT

ASSIGNED

REACHED_RESTAURANT

–

PICK_ORDER

REACHED_RESTAURANT

PICKED_UP

Publish delivery.picked_up

START_DELIVERY

PICKED_UP

OUT_FOR_DELIVERY

–

COMPLETE_DELIVERY

OUT_FOR_DELIVERY

DELIVERED

Publish delivery.delivered

6.2.3 Coupling with Order FSM
Upon OrderState.FINISH_PREPARATION → READY_FOR_PICKUP, the OrderService callback:

Creates a new Delivery record in DB with state PENDING.

Fires DeliveryTrigger.ASSIGN_RIDER via DeliveryStateMachine.

Correlate by order_id foreign key in Delivery table.

7. Data Model
7.1 Order Table


sql
CopyEdit

CREATE TABLE orders (   id UUID PRIMARY KEY,   customer_id UUID NOT NULL,   restaurant_id UUID NOT NULL,   total_amount DECIMAL(10,2),   state VARCHAR(32) NOT NULL,   created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),   updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() ); 

7.2 Delivery Table


sql
CopyEdit

CREATE TABLE deliveries (   id UUID PRIMARY KEY,   order_id UUID REFERENCES orders(id),   rider_id UUID,   state VARCHAR(32) NOT NULL,   assigned_at TIMESTAMP,   completed_at TIMESTAMP,   created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),   updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() ); 

8. Integration & APIs
8.1 REST Endpoints
Method

URI

Payload

Description

POST

/orders

{ items[], totalAmount }

Create new order (state=NEW)

POST

/orders/{id}/trigger

trigger=ACCEPT_ORDER

Fire a trigger on Order FSM

GET

/orders/{id}

—

Fetch current state & details

POST

/deliveries/{id}/trigger

trigger=REACH_RESTAURANT

Fire a trigger on Delivery FSM

GET

/deliveries/{id}

—

Fetch current delivery state

8.2 Kafka Topics
Topic

Key

Value Schema

Emitted By

order-events

order_id

{ orderId, trigger, newState, timestamp }

OrderService

delivery-events

delivery_id

{ deliveryId, trigger, newState, timestamp }

DeliveryService

assign-delivery

order_id

{ orderId, restaurantLocation }

OrderService (on READY)

9. Non‑Functional Requirements
Throughput: ≥ 1,000 state changes per second

Latency: < 50 ms per transition (in-memory FSM + DB write)

Durability: All state changes persisted in PostgreSQL within < 100 ms

Resilience:

Retry on transient DB/Kafka failures (3 attempts, exponential backoff)

Fallback to dead‑letter queue if assignment fails

Monitoring & Alerting:

Track metrics: transitions/sec, error rates, cold starts

Alerts if average transition latency > 200 ms or error rate > 1%

10. Testing & Validation
Unit Tests:

Cover each FSM’s transitions (valid & invalid)

Verify callbacks fire appropriately

Integration Tests:

REST endpoints → FSM → DB → Kafka emission

Redis TTL auto‑cancel flow

Load Tests:

Simulate 10K concurrent orders

Measure end‑to‑end latency & throughput

11. Milestones & Timeline
Milestone

Owner

ETA

Define and review PRD

Product + Eng

Day 0

Implement Order FSM + persistence

Backend Eng

Day 7

Integrate Order REST API & Kafka

Backend Eng

Day 10

Implement Delivery FSM + assignment hook

Backend Eng

Day 14

Redis auto-cancel & TTL flow

Backend Eng

Day 16

End‑to‑end integration & load testing

QA + SRE

Day 20

Documentation & handoff

Product Eng

Day 22

12. Acceptance Criteria
All FSM transitions execute only when permitted; invalid triggers raise exceptions.

State changes are persisted and publish correct Kafka messages.

Redis‑based auto‑cancel fires within ±5 seconds around the 5‑minute mark.

Delivery assignment service selects an available rider and updates Delivery FSM.

System handles 10K concurrent orders without exceeding latency & error thresholds.

TL;DR


We will start will Stateless4j and if needed will move to SCXML (State Chart XML) if needed.

 