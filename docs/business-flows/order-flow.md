Order Lifecycle States (example)


PLACE ORDER → RESTAURANT PENDING → ACCEPTED → FOOD PREPARING → RIDER ASSIGNED →
READY_FOR_PICKUP → PICKED_UP → EN_ROUTE → DELIVERED 
Include support for:

CANCELLED (by user or restaurant)

FAILED (payment/delivery)

TIMEOUT_RETRY

Each of these has sub-states, timers (SLAs), fallbacks, and needs tracking and retries.



┌───────────────────────────┐
│ 1. Order Placed          │
└─────────────┬─────────────┘
              │ Customer selects items & hits “Place Order”
              ▼
┌───────────────────────────┐
│ 2. Payment Initiated      │
└─────────────┬─────────────┘
              │ Call out to payment gateway
              ▼
┌───────────────────────────┐         ┌───────────────────────────┐
│ 3a. Payment Confirmed     │───►─────│ 3b. Payment Failed        │
└─────────────┬─────────────┘   retry │  (retry or cancel)        │
              │                           └───────────────────────────┘
              ▼
┌───────────────────────────┐
│ 4. Pre‑Acceptance         │
│    Validation             │
└─────────────┬─────────────┘
              │ Synchronously check:
              │   • Restaurant is open & not on break  
              │   • Customer address in delivery zone  
              │   • Meets restaurant’s minimum order value  
              │   • All items active on menu  
              │   • Stock/inventory sufficiency (if tracked)  
              │   • Final-price recomputed ≟ submitted total  
              │   • Payment is reserved, not just initiated  
              │   • User not flagged or rate‑limited  
              │   • Prep + travel time fits SLA  
              ▼
┌───────────────────────────┐         ┌───────────────────────────┐
│ 5a. Validation Passed     │───►─────│ 5b. Validation Failed     │
└─────────────┬─────────────┘   reject│ (inform customer & cancel)│
              │                           └───────────────────────────┘
              ▼
┌───────────────────────────┐
│ 6. Order Queued           │
│    (Kafka/RabbitMQ)       │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ 7. Restaurant Notification│
│    (push/webhook)         │
└─────────────┬─────────────┘
              │
         ┌────▼────┐
         │ 8a. Reject │  ◄── If restaurant actively rejects within timeout
         └────┬────┘
              │
        ┌─────▼─────┐
        │ 8b. Accept │
        └─────┬─────┘
              │
              ▼
┌───────────────────────────┐
│ 9. Preparing              │
│   – start prep timer      │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│10. Ready for Pickup       │
│   – trigger batching      │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│11. Delivery Assignment    │
│   – find & notify riders  │
│   – auto‑escalate on no response│
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│12. Rider Picks Up         │
│   – record timestamp      │
│   – start live tracking   │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│13. In Transit             │
│   – real‑time updates     │
│   – dynamic ETA re‑calc   │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│14. Delivered              │
│   – record delivery time  │
│   – prompt rating & review│
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│15. Order Closed           │
│   – emit final events     │
│   – archive status history│
└───────────────────────────┘
Validation Before Accepting any Order
Restaurant Availability
Operating hours
Ensure the current time falls within the restaurant’s open‑hours window (e.g. 11 AM–11 PM).

Holiday/Break status
Check for any configured holidays or temporary closures (e.g. maintenance).

Max concurrent orders
If the kitchen is at capacity (you can track “orders in prep” count), reject or queue with delay.

2. Serviceability & Geofence
Delivery radius
Customer address must lie inside the restaurant’s delivery zone (use a polygon or radius check).

Minimum order value
Enforce any minimum‑cart thresholds the restaurant requires (e.g. ₹150).

3. Menu & Inventory Validation
Item existence
All requested item IDs must still be active on the menu.

Stock levels (if maintained)
For each item, ensure sufficient inventory (e.g. “today’s special” sold out).

Customization rules
Enforce any item‑specific constraints (e.g. “no extra cheese” might remove add‑on fees).

4. Pricing & Payment Sanity
Final price recalculation
Recompute total against the latest menu price, delivery fees, taxes, surge, coupons.

Payment authorization
Guarantee that the payment gateway has successfully reserved the funds (not merely “initiated”).

5. Fraud & Abuse Checks
User status
Block orders from banned or flagged users.

Order velocity
If a single user or address is placing too many orders in a short window, flag or throttle.

Coupon validity
Verify coupon usage limits, expiry, and minimum‑order criteria.

6. SLA & Timing Constraints
Estimated prep time vs. delivery window
If prep + travel time pushes delivery beyond your promised SLA, you might decline or suggest a later slot.

Batching compatibility
If you’re doing batched delivery, ensure this order can join an upcoming batch (within your batch window).