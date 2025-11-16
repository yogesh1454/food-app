Order Commitment API Specification: /api/v1/orders (Transactional Step 2)

1. API Overview

This document specifies the requirements for the Order Commitment API, which is the second and transactional step of the two-step checkout flow.

Its primary purpose is to take the validated and calculated state from a previous Checkout Session, execute the required payment transaction, and create a persistent, final order record. This is a mission-critical, idempotent transaction.

Resource

Method

Path

Action

HTTP Status

Order

POST

/api/v1/orders

Commits the pre-calculated Checkout Session, executes payment, and creates the final order record.

201 Created (Success)

2. Dependencies and External Services

The Checkout Service orchestrating this API relies on the following external domain services:

Dependency

Purpose

Key Operations

Payment Service

Executes payment transactions (Wallet deduction, GPay processing, COD registration).

deductBalance, processGpayTransaction, registerCodTransaction

Order Service

Persists the final order record and manages long-term order state.

createOrder

Vendor/Menu Domain

Provides last-second validation (e.g., vendor still open).

checkVendorStatus

3. Request and Response Schemas

3.1. Request Body (Input)

The request is minimal, relying entirely on the checkoutSessionId generated in the /checkout/calculate step.

Field

Type

Required

Description

Constraints

checkoutSessionId

String

Yes

ID of the temporary session containing all validated cart, price, and payment details.

Must exist and be in READY_FOR_COMMIT state.

paymentToken

String

Conditionally

Required if the payment method specified in the session is GPay. This is the tokenized payment payload.



3.2. Success Response (HTTP 201 Created)

Sent upon successful payment execution and order persistence.

Field

Type

Description

orderId

String

Unique identifier of the newly created order record.

status

String

Initial status of the order (PENDING_CONFIRMATION).

totalAmount

Decimal

The final amount charged/committed.

4. Detailed Business Logic and Activity List (6-Step Atomic Process)

The following activities must be executed atomically and sequentially. Any failure at steps 1, 2, or 3 must result in an appropriate error response and a rollback/release of the session lock.

Step

Activity

Domain Interaction

Success/Failure Conditions

1. Session Lock

Retrieve the Checkout Session using checkoutSessionId and apply a lock.

Checkout Service (Internal)

Success: Status updated from READY_FOR_COMMIT to IN_PROGRESS. Failure: Session is expired, not found (404), or already IN_PROGRESS/COMMITTED (409 Conflict).

2. Final Validation

Perform final checks before executing payment.

Vendor/Menu Domain

Checks: Vendor is still open and the session is not excessively stale (e.g., older than 5 minutes). Failure: Vendor is closed (409 Conflict).

3. Execute Payment

Call the appropriate external Payment Service method based on the session details.

Payment Service

A. Wallet: Call deductBalance. Failure triggers 402 Payment Required (Insufficient Funds).







B. GPay: Call processGpayTransaction. Failure triggers 402 Payment Required (Token/Gateway failure).







C. COD: Call registerCodTransaction. Always successful (registers PENDING payment).

4. Order Creation

If payment is successful/committed (or PENDING for COD), create the final order record.

Order Service

Pass all validated session data and the resulting paymentTransactionId. Initial status must be PENDING_CONFIRMATION. Failure: Order Service unavailable (503).

5. Session Cleanup

Mark the session as completed and set for deletion.

Checkout Service (Internal)

Update session status to COMMITTED.

6. Notification

Publish an asynchronous message to initiate fulfillment.

Messaging Queue (e.g., Pub/Sub)

Publish OrderPlacedEvent to notify the Vendor Domain to begin preparing the order.

5. Error Handling and Status Codes

The API must return standard HTTP status codes and provide clear, machine-readable error bodies.

Status Code

Error Scenario

Recommended Error Description (for Response Body)

400 Bad Request

Missing required parameters, e.g., checkoutSessionId is not provided.

ERR_INVALID_INPUT

402 Payment Required

Payment Execution Failed (Step 3).

ERR_INSUFFICIENT_FUNDS (Wallet), ERR_PAYMENT_GATEWAY_FAILURE (GPay), or similar.

404 Not Found

The provided checkoutSessionId does not exist.

ERR_SESSION_NOT_FOUND

409 Conflict

State Conflict (Step 1 or 2).

ERR_SESSION_ALREADY_COMMITTED (Lock failure), ERR_VENDOR_CLOSED, ERR_SESSION_EXPIRED.

503 Service Unavailable

A critical downstream service (e.g., Order Service, Payment Service) is unavailable or times out.

ERR_ORDER_SERVICE_UNAVAILABLE or ERR_PAYMENT_SERVICE_UNAVAILABLE.

500 Internal Server Error

Unexpected server-side failure.