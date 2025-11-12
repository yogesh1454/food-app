# 4\. Data Models

## User

**Purpose:** Stores information about all users of the platform, including customers, vendors, delivery partners, and administrators.

**Key Attributes:**

  - `user_id`: UUID - Primary identifier for the user.
  - `username`: String - Unique username for login.
  - `password_hash`: String - Hashed password for security.
  - `email`: String - User's email address, unique.
  - `phone_number`: String - User's phone number, unique.
  - `user_type`: Enum (CUSTOMER, VENDOR, DELIVERY\_PARTNER, ADMIN) - Role of the user on the platform.
  - `first_name`: String - User's first name.
  - `last_name`: String - User's last name.
  - `address`: JSONB - User's default delivery address (street, city, state, zip, lat, long).
  - `created_at`: Timestamp - Timestamp of user creation.
  - `updated_at`: Timestamp - Timestamp of last update.
  - `status`: Enum (ACTIVE, INACTIVE, SUSPENDED) - Account status.
  - `company_id`: UUID (Optional) - For B2B users, links to a company profile.
  - `internal_delivery_point`: String (Optional) - For B2B users, specific location within a factory/workshop.

**Relationships:**

  - One-to-many with `Order` (a user can place multiple orders).
  - One-to-one with `Vendor` (if user\_type is VENDOR).
  - One-to-one with `DeliveryPartner` (if user\_type is DELIVERY\_PARTNER).

## Vendor

**Purpose:** Stores information about tea and snack vendors on the platform.

**Key Attributes:**

  - `vendor_id`: UUID - Primary identifier for the vendor.
  - `user_id`: UUID - Foreign key to `User` table (links to vendor's user account).
  - `name`: String - Business name of the vendor.
  - `description`: Text - Brief description of the vendor/cuisine.
  - `address`: JSONB - Vendor's physical address (street, city, state, zip, lat, long).
  - `phone_number`: String - Vendor's contact phone number.
  - `email`: String - Vendor's contact email.
  - `rating`: Decimal - Average rating from customer reviews.
  - `status`: Enum (ACTIVE, INACTIVE, PENDING\_APPROVAL) - Vendor's operational status.
  - `operating_hours`: JSONB - Daily operating hours (e.g., [{day: "MON", open: "09:00", close: "18:00"}]).
  - `is_open`: Boolean - Real-time status if vendor is currently open.
  - `menu_version`: Integer - Incremented on menu updates, used for caching invalidation.
  - `created_at`: Timestamp - Timestamp of vendor creation.
  - `updated_at`: Timestamp - Timestamp of last update.

**Relationships:**

  - One-to-one with `User`.
  - One-to-many with `MenuItem` (a vendor has multiple menu items).
  - One-to-many with `Order` (a vendor receives multiple orders).

## MenuItem

**Purpose:** Stores details of individual tea and snack items offered by vendors.

**Key Attributes:**

  - `menu_item_id`: UUID - Primary identifier.
  - `vendor_id`: UUID - Foreign key to `Vendor` table.
  - `name`: String - Name of the item.
  - `description`: Text - Detailed description of the item.
  - `price`: Decimal - Price of the item.
  - `category`: String - Category of the item (e.g., "Tea", "Coffee", "Snacks", "Biscuits").
  - `image_url`: String (Optional) - URL to item image.
  - `is_available`: Boolean - Whether the item is currently available.
  - `preparation_time_minutes`: Integer - Estimated time to prepare.
  - `created_at`: Timestamp.
  - `updated_at`: Timestamp.

**Relationships:**

  - Many-to-one with `Vendor`.
  - Many-to-many with `Order` (via `OrderItem`).

## Order

**Purpose:** Tracks customer orders from creation to completion.

**Key Attributes:**

  - `order_id`: UUID - Primary identifier.
  - `customer_id`: UUID - Foreign key to `User` table (the customer).
  - `vendor_id`: UUID - Foreign key to `Vendor` table.
  - `delivery_partner_id`: UUID (Optional) - Foreign key to `DeliveryPartner` table.
  - `order_status`: Enum (PENDING, ACCEPTED, PREPARING, READY\_FOR\_PICKUP, ON\_THE\_WAY, DELIVERED, CANCELLED, REJECTED) - Current status of the order.
  - `total_amount`: Decimal - Total amount of the order.
  - `payment_status`: Enum (PENDING, PAID, FAILED, REFUNDED) - Payment status.
  - `delivery_address`: JSONB - Snapshot of the delivery address at time of order.
  - `ordered_at`: Timestamp - Time order was placed.
  - `estimated_delivery_time`: Timestamp (Optional) - Estimated time of delivery.
  - `delivered_at`: Timestamp (Optional) - Actual time of delivery.
  - `special_instructions`: Text (Optional) - Any specific notes from customer.
  - `train_number`: String (Optional) - For train deliveries.
  - `coach_number`: String (Optional) - For train deliveries.
  - `seat_number`: String (Optional) - For train deliveries.
  - `station_code`: String (Optional) - For train deliveries, the station for delivery.
  - `scheduled_arrival_time`: Timestamp (Optional) - For train/bus, estimated arrival at delivery point.
  - `bus_operator`: String (Optional) - For bus deliveries.
  - `bus_number`: String (Optional) - For bus deliveries.
  - `scheduled_stop_time`: Timestamp (Optional) - For bus deliveries, the estimated stop time.
  - `company_id`: UUID (Optional) - For B2B orders.
  - `internal_delivery_point`: String (Optional) - For B2B orders, specific location within factory.

**Relationships:**

  - Many-to-one with `User` (customer).
  - Many-to-one with `Vendor`.
  - Many-to-one with `DeliveryPartner`.
  - One-to-many with `OrderItem`.
  - One-to-one with `PaymentTransaction`.

## OrderItem

**Purpose:** Details individual items within an order.

**Key Attributes:**

  - `order_item_id`: UUID - Primary identifier.
  - `order_id`: UUID - Foreign key to `Order` table.
  - `menu_item_id`: UUID - Foreign key to `MenuItem` table.
  - `quantity`: Integer - Quantity of the item.
  - `price_at_order`: Decimal - Price of the item at the time of order (to handle price changes).
  - `notes`: Text (Optional) - Any specific notes for this item.

**Relationships:**

  - Many-to-one with `Order`.
  - Many-to-one with `MenuItem`.

## PaymentTransaction

**Purpose:** Records all payment attempts and their statuses.

**Key Attributes:**

  - `transaction_id`: UUID - Primary identifier.
  - `order_id`: UUID - Foreign key to `Order` table.
  - `payment_method`: Enum (UPI, CASH, CARD, WALLET) - Method used for payment.
  - `amount`: Decimal - Amount of the transaction.
  - `transaction_status`: Enum (PENDING, SUCCESS, FAILED, REFUNDED) - Status of the payment transaction.
  - `gateway_transaction_id`: String (Optional) - ID from the payment gateway.
  - `transaction_time`: Timestamp - Time of the transaction.
  - `refund_id`: UUID (Optional) - Link to a refund record if applicable.

**Relationships:**

  - One-to-one with `Order`.

## DeliveryPartner

**Purpose:** Manages information about delivery personnel.

**Key Attributes:**

  - `delivery_partner_id`: UUID - Primary identifier.
  - `user_id`: UUID - Foreign key to `User` table (links to delivery partner's user account).
  - `name`: String - Delivery partner's name.
  - `phone_number`: String - Delivery partner's contact number.
  - `email`: String - Delivery partner's email.
  - `vehicle_type`: Enum (BIKE, SCOOTER, BICYCLE, CAR) - Type of vehicle.
  - `current_location`: Point (latitude, longitude) - Real-time last known location.
  - `availability_status`: Enum (ONLINE, OFFLINE, ON\_DELIVERY) - Current availability.
  - `rating`: Decimal - Average rating from customers.
  - `created_at`: Timestamp.
  - `updated_at`: Timestamp.

**Relationships:**

  - One-to-one with `User`.
  - One-to-many with `Order` (an order is assigned to a delivery partner).
