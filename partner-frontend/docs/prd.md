---

## Product Requirements Document: Tea & Snacks Delivery Aggregator (India)

**Document Version:** 1.2
**Date:** July 22, 2025
**Prepared By:** Gemini

---

### 1. Introduction

This Product Requirements Document (PRD) outlines the features and functionalities for a new digital platform: a Tea & Snacks Delivery Aggregator. The platform will connect customers with local tea and snack vendors, facilitating order placement and delivery. Unlike traditional food delivery services, this platform will *not* involve proprietary tea stalls or ingredient procurement; rather, it will focus on aggregating existing vendors. A key differentiation will be the strategic targeting of diverse audience segments: **individual customers**, office workers, and specific, captive audience segments like passenger trains, buses, factories, and workshops.

**1.1. Purpose:**
To define the requirements for developing a robust, scalable, and user-friendly mobile application and web platform that enables seamless ordering and delivery of tea and snacks in India, with a focus on diverse delivery environments.

**1.2. Scope:**
The scope of this document covers the initial Minimum Viable Product (MVP) features necessary to launch and test the core value proposition across identified target segments.

**1.3. Target Audience (Users):**
* **Individual Customers (B2C):** General consumers at home or other personal locations seeking convenient delivery of tea and snacks.
* **Office Workers (B2C/B2B):** Individuals in office environments.
* **Moving Passengers (B2C):**
    * **Train Passengers:** Individuals travelling on passenger trains.
    * **Bus Passengers:** Individuals travelling on long-distance/intercity buses.
* **Factory/Workshop Employees (B2B):** Individuals working in industrial or workshop environments.
* **Vendors:** Tea stalls, snack shops, bakeries, and small food businesses that partner with the platform.
* **Delivery Partners:** Individuals responsible for picking up orders from vendors and delivering them to customers.
* **Admin Panel Users:** Internal team members managing the platform operations, vendors, customers, and delivery logistics.

### 2. Goals & Business Objectives

* **Become the preferred platform for tea and snack delivery for individual customers and in targeted captive segments.**
* **Achieve profitability within [X] months** by leveraging commission-based revenue, delivery fees, and value-added services.
* **Onboard [Y] quality tea and snack vendors** in key operational zones within the first year.
* **Ensure an average delivery time of [Z] minutes** for standard orders.
* **Maintain a customer satisfaction rating (CSAT) of [A]%** or higher.
* **Expand market reach** by successfully establishing delivery operations in [Specific Cities/Regions].

### 2.1. Problem Statement & User Pain Points with Existing Apps for Tea/Snacks

Existing general food delivery applications often fall short in adequately addressing the specific needs and nuances of the tea and snacks market, leading to several user pain points:

* **Limited Specialization & Discovery:**
    * **Lack of Dedicated Tea/Snack Focus:** Existing apps are broad, making it hard to specifically discover dedicated tea stalls or specialized snack vendors. Users often have to wade through full meal restaurants.
    * **Poor Search/Filter Options:** Inadequate filters for specific tea types (e.g., Masala Chai, Filter Coffee, Green Tea) or local snack varieties, making discovery difficult.
* **Delivery Challenges for Specific Contexts:**
    * **Inefficient for Captive Audiences:** Current models are not designed for the unique logistics of delivering to moving trains, buses, or within secured factory/workshop premises, leading to unfulfilled demand in these high-potential segments.
    * **Timeliness & Hotness for Beverages:** Ensuring hot tea/coffee delivery quickly and efficiently, especially from distant vendors, is a common struggle.
* **Cost & Value Perception:**
    * **High Delivery Fees for Small Orders:** For a quick tea or snack, the delivery fees on general apps can often be disproportionately high compared to the order value, making it less appealing.
    * **Lack of Value Deals:** Fewer bundling options or combo deals specifically for tea and snacks.
* **Vendor Onboarding & Quality:**
    * **Limited Local Vendor Presence:** Many small, popular local tea stalls or snack vendors are not onboarded on major platforms due to complexity or high commission structures, limiting choice.
    * **Inconsistent Quality Control:** General apps may not have specific vetting processes for hygiene or quality pertinent to quick-serve beverage and snack outlets.
* **User Experience Gaps:**
    * **Cluttered Interfaces:** General food apps can be overwhelming with too many options irrelevant to a simple tea/snack craving.
    * **Poor Communication:** Challenges in direct communication with vendors for specific requests (e.g., less sugar in tea).

By addressing these pain points, our platform aims to offer a more tailored, efficient, and cost-effective solution for tea and snack delivery across diverse user segments.

### 3. User Stories & Personas

**3.1. Customer Personas & Stories:**

* **Individual Customer (General B2C):**
    * "As an individual at home, I want to easily find and order hot tea and fresh snacks from nearby vendors for a quick treat or a sudden craving."
    * "I want the ordering process to be simple and fast, with clear visibility of delivery fees and estimated delivery times."
    * "I want to see current offers and discounts from nearby tea and snack vendors."
* **Office Worker (B2C/B2B):**
    * "As an office worker, I want to easily order hot tea and a quick snack during my afternoon break so I don't have to leave my desk."
    * "I want to be able to place group orders with my colleagues."
* **Train Passenger:**
    * "As a train passenger, I want to pre-order hot tea and snacks to be delivered to my seat at the next major station stop, so I can enjoy refreshments during my journey."
    * "I want to track my order and receive notifications when my train is approaching the delivery station."
* **Bus Traveller:**
    * "As a bus traveller, I want to order tea and snacks for delivery at a scheduled bus stop, so I can quickly grab a refreshment during my journey break."
    * "I want clear instructions on where to pick up my order at the bus stop."
* **Factory/Workshop Employee:**
    * "As a factory employee, I want to receive my pre-ordered tea and snacks at our designated break time and location within the factory premises, so I can make the most of my break."
    * "I want my company to be able to place bulk orders easily for our team."

**3.2. Vendor Persona & Stories:**

* **Local Tea Stall Owner:**
    * "As a tea stall owner, I want to receive orders easily through a dedicated device/app, so I can quickly prepare them."
    * "I want to manage my menu, availability, and pricing on the platform."
    * "I want to see my earnings and performance metrics from orders placed through the app."
* **Bakery/Snack Shop Owner:**
    * "As a snack shop owner, I want to get new customers through the platform without having to manage my own delivery fleet."
    * "I want transparent information on commissions charged per order."

**3.3. Delivery Partner Persona & Stories:**

* **Delivery Rider:**
    * "As a delivery partner, I want to receive clear order details and optimal delivery routes on my app, so I can complete deliveries efficiently."
    * "I want to easily communicate with customers and vendors if there are any issues."
    * "I want to track my earnings and delivery history."

### 4. Functional Requirements

**4.1. Customer Application (Mobile & Web)**

* **User Management:**
    * Registration/Login (Email, Phone, Social Media).
    * Profile Management (Name, Address, Payment Info).
    * Order History.
    * Referral Program integration.
* **Discovery & Ordering:**
    * Location-based vendor search (auto-detect or manual input).
    * Category-based Browse (Tea, Coffee, Savory Snacks, Sweet Snacks, Combos).
    * Search with filters (e.g., ratings, price range, dietary options).
    * Vendor profiles with menu, pricing, ratings, operating hours.
    * Customizable order items (e.g., sugar/milk for tea).
    * Shopping Cart functionality.
    * Add/remove items from cart.
    * Apply promo codes/discounts.
* **Payment:**
    * Multiple payment options (Credit/Debit Card, Net Banking, UPI, Digital Wallets).
    * Secure payment gateway integration.
* **Order Tracking:**
    * Real-time order status updates (Order Placed, Preparing, Picked Up, On the Way, Delivered).
    * Live map tracking of delivery partner.
    * Push notifications for order status changes.
* **Ratings & Reviews:**
    * Ability to rate vendors and delivery partners post-delivery.
    * Write text reviews.
* **Customer Support:**
    * In-app chat support.
    * FAQ section.

**4.2. Vendor Application/Dashboard**

* **Order Management:**
    * Receive new order notifications (audible alert).
    * Accept/Reject orders.
    * Update order status (e.g., Preparing, Ready for Pickup).
    * View order details (items, customer info, delivery address).
    * Order history.
* **Menu Management:**
    * Add/Edit/Remove menu items, prices, descriptions, images.
    * Mark items as out of stock.
    * Set/update operating hours.
* **Financials:**
    * View daily/weekly/monthly earnings reports.
    * Track commission details.
    * Request payouts.
* **Vendor Profile:**
    * Manage business information, contact details.
* **Communication:**
    * Chat with delivery partners and admin support.

**4.3. Delivery Partner Application**

* **Order Acceptance:**
    * Receive new delivery request notifications.
    * Accept/Reject delivery requests.
    * View order details (pickup and delivery locations, order items, customer contact).
* **Navigation:**
    * In-app navigation to vendor and customer locations (Google Maps API integration).
* **Status Updates:**
    * Update delivery status (e.g., Picked Up, En Route, Delivered).
    * Mark order as delivered with proof (e.g., photo optional, customer signature).
* **Earnings & History:**
    * View daily/weekly earnings.
    * Track delivery history.
* **Communication:**
    * In-app chat/call with customers and vendors.
* **Availability:**
    * Go online/offline to accept/decline orders.

**4.4. Admin Panel (Web-based)**

* **Dashboard:**
    * Overview of active orders, deliveries, registered users, vendors, delivery partners.
    * Key performance metrics.
* **User Management:**
    * Manage customer accounts (view, block, unblock).
    * Manage vendor accounts (onboard, approve, suspend, edit details).
    * Manage delivery partner accounts (onboard, activate, track, manage payouts).
* **Order Management:**
    * Monitor all active and past orders.
    * Manual assignment of delivery partners (if needed).
    * Handle order cancellations and refunds.
* **Content Management:**
    * Manage promo codes, discounts, and campaigns.
    * Manage in-app advertising slots.
* **Reporting & Analytics:**
    * Generate reports on sales, popular items, delivery times, customer feedback.
    * Track commission payouts and revenue.
* **Customer Support:**
    * View and resolve customer/vendor/delivery partner queries.
* **System Configuration:**
    * Manage delivery zones, surge pricing rules.
    * Set commission rates.

**4.5. Specialized Requirements for Captive Segments:**

* **Passenger Trains:**
    * **Customer App:**
        * Option to select "Train Delivery."
        * Input fields for Train Name/Number, Coach Number, Seat Number.
        * Selection of specific station stop for delivery.
        * Pre-order window (e.g., minimum 2 hours before station arrival).
        * Notifications for "Train Approaching Delivery Station."
    * **Vendor & Delivery Partner Apps:**
        * Clear display of train delivery orders, including train details and expected arrival time at the station.
        * Specific drop-off instructions for station delivery.
    * **Admin Panel:**
        * Integration with railway schedules (if possible, through publicly available APIs or manual updates).
        * Tool to manage station-specific delivery hubs/partners.
        * Tracking of train delays to adjust delivery windows.
* **Buses (Long-distance/Intercity):**
    * **Customer App:**
        * Option for "Bus Delivery."
        * Input fields for Bus Operator, Bus Number, Boarding Point, Destination.
        * Selection of designated rest stop for delivery.
        * Pre-order window.
        * Notifications for "Bus Approaching Delivery Stop."
    * **Vendor & Delivery Partner Apps:**
        * Clear display of bus delivery orders, bus details, and expected arrival at the stop.
        * Specific drop-off instructions for bus stop delivery.
    * **Admin Panel:**
        * Management of bus operator partnerships and designated delivery stops.
        * Tools to handle potential bus schedule variations.
* **Factories & Workshops (B2B):**
    * **Customer App (for employees/B2B portal for company):**
        * Option for "Corporate/Factory Delivery."
        * Selection of company/factory and specific delivery point within premises.
        * Ability to place individual or bulk orders.
        * Pre-order system aligned with factory break times.
    * **Vendor App:**
        * Clear indication of bulk orders and specific delivery instructions for factories.
    * **Delivery Partner App:**
        * Specific access instructions for factory premises.
        * Clear drop-off points within the factory.
    * **Admin Panel:**
        * Dedicated module for B2B client management (company accounts, billing, customized menus).
        * Ability to set up fixed delivery schedules and designated drop-off points for each factory.
        * Bulk order processing tools.

### 5. Non-Functional Requirements

* **Performance:**
    * Fast load times (under 3 seconds).
    * Ability to handle [X] concurrent users and [Y] orders per hour.
    * Efficient database queries for quick data retrieval.
* **Scalability:**
    * Architecture designed to support future growth in users, vendors, and delivery partners without significant re-architecture.
    * Ability to expand to new cities/regions.
* **Security:**
    * Data encryption for sensitive user information and payment details.
    * Protection against common web vulnerabilities (OWASP Top 10).
    * Secure API endpoints.
    * Compliance with data privacy regulations (e.g., GDPR-like principles for India).
* **Reliability & Availability:**
    * High uptime (e.g., 99.9% availability).
    * Robust error handling and recovery mechanisms.
* **Usability (UX/UI):**
    * Intuitive and easy-to-navigate interface for all user types.
    * Consistent design language across platforms.
    * Minimal steps for core actions (ordering, accepting delivery).
* **Maintainability:**
    * Clean, modular, and well-documented codebase.
    * Easy to update and add new features.
* **Compatibility:**
    * Support for major iOS and Android versions.
    * Cross-browser compatibility for web platforms.

### 6. Out of Scope for MVP

* In-house tea/snack preparation or ingredient sourcing.
* Complex loyalty programs beyond basic discounts/referrals (beyond MVP).
* Integration with external POS systems of vendors (initially).
* Advanced AI/ML-driven personalized recommendations (beyond basic based on history).
* International expansion.

### 7. Key Performance Indicators (KPIs)

* **Customer:**
    * Number of active users (including segment breakdown: individual, office, train, bus, factory).
    * Average Order Value (AOV).
    * Order frequency.
    * Customer Retention Rate.
    * Conversion Rate (from browse to order).
    * Customer Lifetime Value (CLTV).
* **Vendor:**
    * Number of active vendors.
    * Vendor satisfaction.
    * Average orders per vendor.
    * Commission revenue percentage.
* **Delivery:**
    * Average delivery time.
    * Order fulfillment rate.
    * Delivery partner utilization.
    * Cost per delivery.
* **Financial:**
    * Gross Merchandise Value (GMV).
    * Net Order Value (NOV).
    * Revenue (Commissions + Delivery Fees + Subscriptions + Ads).
    * Contribution Margin per order.
    * EBITDA.

### 8. Future Considerations & Phased Rollout

* **Phase 1 (MVP - as detailed above):** Focus on core functionality and establishing presence for **individual customers** and initial B2B/captive segments (e.g., 1-2 factories, 1 major railway station).
* **Phase 2 (Post-MVP):**
    * Introduce advanced personalization features.
    * Expand to more captive segments or geographies.
    * Implement more sophisticated loyalty programs.
    * Integrate with vendor POS systems for seamless order flow.
    * Explore "dark kitchen" partnerships if high demand for specific tea/snacks emerges.
    * Offer analytics dashboards for vendors.
* **Phase 3 (Long-term):**
    * Potential for white-label solutions for corporate clients.
    * API integrations for larger corporate systems.
    * Explore drone delivery or autonomous vehicle delivery in specific, controlled environments.

---