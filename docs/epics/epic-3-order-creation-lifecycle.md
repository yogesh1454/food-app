# Epic: Order Creation & Lifecycle Management

**Epic ID:** BE-003-Order-Lifecycle
**Parent Epic:** BE-003 (Order Catalog Management)
**Priority:** Critical (P0)

---

## 📖 Overview

This epic covers the complete lifecycle of customer order creation, validation, and initial fulfillment within the `order-catalog-service`. It includes the core process of a customer placing an order, comprehensive validation, accurate calculation of the total bill (including various fees, surcharges, promotions, and discounts within the `order-catalog-service` itself), management of order items and add-ons, flexible shipping/delivery options, and initial order status management. This epic also addresses the ability to update an order after placement under specific conditions. Excluded from this epic are advanced search functionalities for vendors and menu items, which will be addressed in separate epics.

---

## ✅ User Stories

*   **As a** customer,
    **I want to** easily browse menus, add items and add-ons to my cart, and place an order, including applying promotions and selecting shipping options,
    **So that I can** purchase food from my preferred branches with confidence and flexibility.

*   **As a** customer,
    **I want to** receive clear confirmation and status updates for my order, and be able to modify or cancel it under specific conditions,
    **So that I can** track its progress and manage my purchases effectively.

*   **As a** `VendorBranch` owner,
    **I want to** receive new orders with all necessary details, accurate pricing (including promotions and fees), and clear shipping instructions,
    **So that I can** efficiently prepare and fulfill them and manage changes if necessary.

---

## 📋 Acceptance Criteria

### 1. Order Creation & Validation

*   Customer can create an order with multiple menu items and associated add-ons.
*   Order creation validates that the selected `VendorBranch` is active and currently open.
*   All selected menu items and add-ons must exist, be available, and belong to the chosen branch.
*   Quantities for all items and add-ons must be positive integers.
*   Delivery/Shipping address details are captured and validated for completeness and deliverability within the branch's service area.
*   Order is initially created with a `PENDING` order status and `PENDING` payment status.
*   The system enforces a minimum order amount (if applicable).
*   The system enforces a maximum number of items per order (if applicable).

### 2. Total Bill Calculation (within Order Catalog Service)

*   The system accurately calculates the subtotal based on `MenuItem` prices (snapshot `priceAtOrder`), add-on prices, and quantities.
*   A dedicated module within the `order-catalog-service` calculates and applies various charges in a defined order of precedence:
    *   Add-on costs are added to item prices.
    *   **Promotions, Offers, and Discounts** are applied to eligible items or the subtotal.
    *   Surcharges (e.g., small order fee, peak hour fee).
    *   Platform fees.
    *   Delivery/Shipping fees (can be a fixed value, dynamic based on distance/time, or from external input from `delivery-management-service`).
    *   Taxes are calculated on the final amount after all other charges and discounts.
*   The final `totalAmount` is stored in the `Order` record, with a breakdown of all applied fees, discounts, and taxes.

### 3. Order Item & Add-on Management

*   Each `OrderItem` accurately captures the `quantity` and `priceAtOrder` (price snapshot at the time of order).
*   `OrderItem` supports special instructions and associated add-ons (each with its quantity and price snapshot).
*   There is a unique constraint ensuring one entry per menu item per order.\
*   `OrderItem`s and their add-ons are linked to the `MenuItem` for reference.

### 4. Order Status & Lifecycle

*   Order can transition through various statuses (e.g., `PENDING`, `ACCEPTED`, `REJECTED`, `PREPARING`, `READY_FOR_PICKUP`, `DELIVERING`, `DELIVERED`, `CANCELLED`, `MODIFIED`).
*   Customers can view the current status of their orders.
*   Mechanisms for **order cancellation** are provided (with restrictions based on current status and time windows).
*   **Order modification** is supported for `PENDING` orders, allowing changes to items, quantities, add-ons, and delivery details. Modifications trigger re-calculation of the total bill and potential re-validation.
*   The system tracks an `Order`'s modification history.

### 5. Event Publishing

*   An `order.created` event is published upon successful initial order creation, including relevant details (customer, branch, items, add-ons, calculated total, shipping info, applied promotions).
*   Events for status changes (e.g., `order.status.updated`, `order.payment.successful`, `order.cancelled`, `order.modified`) are published to inform other services.

### 6. API Endpoints

*   `POST /api/v1/orders`: Create a new order with items, add-ons, promotions, and shipping details.
*   `GET /api/v1/orders/{orderId}`: Retrieve details for a specific order, including a breakdown of charges.
*   `GET /api/v1/orders/customer/{customerId}`: Retrieve all orders for a specific customer.
*   `GET /api/v1/branches/{branchId}/orders`: Retrieve orders for a specific branch.
*   `PUT /api/v1/orders/{orderId}/cancel`: Cancel an order, returning cancellation status.
*   `PUT /api/v1/orders/{orderId}/modify`: Modify a `PENDING` order, accepting changes to items, add-ons, and delivery.
*   `PUT /api/v1/orders/{orderId}/status`: Update order status (for internal use by other services/staff).

---

## 🔧 Technical Implementation

### Order Service Enhancements

*   **Order Creation Logic:** Enhance existing `OrderService` to incorporate all validations (branch status, item/add-on availability, quantity, delivery/shipping address, min/max order). This includes handling add-ons associated with menu items. `order-catalog-service/src/main/java/com/teadelivery/ordercatalog/order/service/OrderService.java`
*   **Order Payment Calculation Module:** Create a new component or service within `order-catalog-service` (e.g., `OrderPricingService` or `BillCalculator`) responsible for applying all add-on costs, promotions, offers, discounts, fees (surcharges, platform, delivery/shipping), and taxes to the order subtotal. This module will be called by `OrderService` during order creation and modification.
    *   Potentially interact with `delivery-management-service` to fetch real-time delivery costs if needed.
*   **Order State Management:** Implement methods for updating order statuses, handling cancellation requests (with business rules), and managing order modification logic, ensuring proper business rules are applied. This includes versioning or logging changes for modified orders.
*   **Promotions/Discounts Integration:** Implement logic within the `OrderPricingService` to validate and apply promotions/discounts provided during order creation. This may involve defining promotion data models.

### Data Model Updates

*   Review and extend the `Order` model for new fields required for detailed pricing components (e.g., `deliveryFee`, `platformFee`, `taxAmount`, `discountAmount`, `surchargeAmount`, `promotionCodeUsed`, `shippingAddress`).
*   Introduce a new entity or embedded object for `AddOn`s linked to `OrderItem`s, capturing `addOnName`, `quantity`, `priceAtOrder`.
*   Consider an `OrderActivity` or `OrderStatusHistory` table/entity to log status changes for auditing and tracking.
*   Consider a mechanism to store a snapshot of all pricing components at the time of order creation/modification.

### Eventing

*   Utilize Kafka for publishing `order.created`, `order.status.updated`, `order.cancelled`, and `order.modified` events. Ensure event schemas are well-defined to include all new details (add-ons, detailed pricing breakdown, promotions).

---

## 🧪 Testing Requirements

*   Unit tests for `OrderService`, `OrderPricingService`, and related components covering all validation rules, add-on handling, promotion/discount application, and calculation logic.
*   Integration tests for order creation, modification, and cancellation, ensuring end-to-end flow with database persistence and event publishing.
*   API tests for all defined endpoints, covering success and error scenarios, including various combinations of promotions, add-ons, and shipping options.
*   Thorough testing of order status transitions and authorization checks for updates.

---

## 📋 Definition of Done

*   All user stories are implemented and tested.
*   All acceptance criteria are met, including promotions, add-ons, shipping, and order modification.
*   `OrderService` and Order Payment Calculation module are implemented with comprehensive logic for all charges and discounts.
*   API endpoints are functional, documented, and support order creation, retrieval, modification, and cancellation.
*   Relevant Kafka events are published correctly with all necessary order details.
*   Unit test coverage > 80% for new and modified code.
*   Integration tests are passing.
*   Code reviewed and approved.

---

## 📚 References

*   [BE-003-05-menu-item-crud-v2.md](/docs/epics/epic-3-stories/BE-003-05-menu-item-crud-v2.md)
*   [BE-003-06-menu-versioning-cache-v2.md](/docs/epics/epic-3-stories/BE-003-06-menu-versioning-cache-v2.md) (Dependency)
*   [BE-003-07-order-creation-v2.md](/docs/epics/epic-3-stories/BE-003-07-order-creation-v2.md)
*   [Database Schema](/docs/architecture/9-database-schema.md)
*   [Core Workflows](/docs/architecture/7-core-workflows.md)
