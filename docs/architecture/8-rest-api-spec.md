# 8\. REST API Spec

```yaml
openapi: 3.0.0
info:
  title: Tea & Snacks Delivery Aggregator API
  version: 1.0.0
  description: API for managing users, orders, vendors, delivery, payments, and search for the Tea & Snacks Delivery Aggregator platform.
servers:
  - url: https://api.teasansnacks.com/v1
    description: Production Server
  - url: https://dev.teasansnacks.com/v1
    description: Development Server
tags:
  - name: Users
    description: User management operations
  - name: Auth
    description: Authentication operations
  - name: Orders
    description: Order management operations
  - name: Vendors
    description: Vendor management operations
  - name: Menu
    description: Menu item operations
  - name: Delivery
    description: Delivery operations
  - name: Payments
    description: Payment processing operations
  - name: Search
    description: Search and discovery operations
paths:
  /auth/register:
    post:
      summary: Register a new user
      tags:
        - Auth
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
      responses:
        '201':
          description: User registered successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '400':
          description: Invalid input
  /auth/login:
    post:
      summary: User login
      tags:
        - Auth
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: Login successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          description: Invalid credentials
  /users/{userId}:
    get:
      summary: Get user profile by ID
      tags:
        - Users
      parameters:
        - in: path
          name: userId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the user to retrieve
      security:
        - bearerAuth: []
      responses:
        '200':
          description: User profile retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '404':
          description: User not found
  /vendors:
    post:
      summary: Register a new vendor
      tags:
        - Vendors
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/VendorRegisterRequest'
      responses:
        '201':
          description: Vendor registered successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Vendor'
  /vendors/{vendorId}/menu:
    put:
      summary: Update a vendor's menu
      tags:
        - Menu
        - Vendors
      parameters:
        - in: path
          name: vendorId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the vendor
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/MenuItemRequest'
      responses:
        '200':
          description: Menu updated successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MenuItem'
  /orders:
    post:
      summary: Place a new order
      tags:
        - Orders
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/OrderCreateRequest'
      responses:
        '201':
          description: Order placed successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Order'
  /orders/{orderId}/status:
    put:
      summary: Update order status (Vendor/Admin/Delivery Partner)
      tags:
        - Orders
      parameters:
        - in: path
          name: orderId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the order
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/OrderStatusUpdateRequest'
      responses:
        '200':
          description: Order status updated
  /payments/process:
    post:
      summary: Initiate payment for an order
      tags:
        - Payments
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PaymentInitiateRequest'
      responses:
        '200':
          description: Payment initiated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PaymentResponse'
  /search/vendors:
    get:
      summary: Search for vendors
      tags:
        - Search
      parameters:
        - in: query
          name: q
          schema:
            type: string
          description: Search query for vendor name/description
        - in: query
          name: latitude
          schema:
            type: number
            format: float
          description: Latitude for proximity search
        - in: query
          name: longitude
          schema:
            type: number
            format: float
          description: Longitude for proximity search
        - in: query
          name: radius
          schema:
            type: number
            format: float
            default: 5
          description: Search radius in kilometers
        - in: query
          name: category
          schema:
            type: string
          description: Filter by menu item category
        - in: query
          name: minRating
          schema:
            type: number
            format: float
            minimum: 0
            maximum: 5
          description: Minimum average rating
      responses:
        '200':
          description: List of vendors matching criteria
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/VendorSearchResponse'
  /search/items:
    get:
      summary: Search for menu items
      tags:
        - Search
      parameters:
        - in: query
          name: q
          schema:
            type: string
          description: Search query for item name/description
        - in: query
          name: vendorId
          schema:
            type: string
            format: uuid
          description: Filter by specific vendor
        - in: query
          name: category
          schema:
            type: string
          description: Filter by item category
        - in: query
          name: maxPrice
          schema:
            type: number
            format: float
          description: Maximum price
      responses:
        '200':
          description: List of menu items matching criteria
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MenuItemSearchResponse'
  /recommendations/items/{userId}:
    get:
      summary: Get recommended menu items for a user
      tags:
        - Search
      parameters:
        - in: path
          name: userId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the user for recommendations
      security:
        - bearerAuth: []
      responses:
        '200':
          description: List of recommended menu items
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MenuItemSearchResponse'
  /delivery-partners/{partnerId}/location:
    put:
      summary: Update delivery partner's real-time location
      tags:
        - Delivery
      parameters:
        - in: path
          name: partnerId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the delivery partner
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LocationUpdate'
      responses:
        '200':
          description: Location updated successfully
  /deliveries/{deliveryId}/status:
    put:
      summary: Update delivery status by delivery partner
      tags:
        - Delivery
      parameters:
        - in: path
          name: deliveryId
          schema:
            type: string
            format: uuid
          required: true
          description: ID of the delivery
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                status:
                  type: string
                  enum: [PICKED_UP, ON_THE_WAY, DELIVERED, FAILED_DELIVERY]
              required:
                - status
      responses:
        '200':
          description: Delivery status updated
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  schemas:
    Address:
      type: object
      properties:
        street:
          type: string
        city:
          type: string
        state:
          type: string
        zip_code:
          type: string
        latitude:
          type: number
          format: float
        longitude:
          type: number
          format: float
    RegisterRequest:
      type: object
      properties:
        username:
          type: string
        password:
          type: string
        email:
          type: string
        phone_number:
          type: string
        user_type:
          type: string
          enum: [CUSTOMER, VENDOR, DELIVERY_PARTNER]
      required:
        - username
        - password
        - email
        - phone_number
        - user_type
    LoginRequest:
      type: object
      properties:
        username:
          type: string
        password:
          type: string
      required:
        - username
        - password
    AuthResponse:
      type: object
      properties:
        access_token:
          type: string
        token_type:
          type: string
          default: Bearer
        expires_in:
          type: integer
    User:
      type: object
      properties:
        user_id:
          type: string
          format: uuid
        username:
          type: string
        email:
          type: string
        phone_number:
          type: string
        user_type:
          type: string
          enum: [CUSTOMER, VENDOR, DELIVERY_PARTNER, ADMIN]
        first_name:
          type: string
        last_name:
          type: string
        address:
          $ref: '#/components/schemas/Address'
        created_at:
          type: string
          format: date-time
        updated_at:
          type: string
          format: date-time
        status:
          type: string
          enum: [ACTIVE, INACTIVE, SUSPENDED]
        company_id:
          type: string
          format: uuid
        internal_delivery_point:
          type: string
    VendorRegisterRequest:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
        address:
          $ref: '#/components/schemas/Address'
        phone_number:
          type: string
        email:
          type: string
        user_id:
          type: string
          format: uuid
      required:
        - name
        - address
        - phone_number
        - email
        - user_id
    Vendor:
      type: object
      properties:
        vendor_id:
          type: string
          format: uuid
        user_id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
        address:
          $ref: '#/components/schemas/Address'
        phone_number:
          type: string
        email:
          type: string
        rating:
          type: number
          format: float
        status:
          type: string
          enum: [ACTIVE, INACTIVE, PENDING_APPROVAL]
        operating_hours:
          type: array
          items:
            type: object
            properties:
              day:
                type: string
              open:
                type: string
                pattern: "^([01]\\d|2[0-3]):([0-5]\\d)$"
              close:
                type: string
                pattern: "^([01]\\d|2[0-3]):([0-5]\\d)$"
        is_open:
          type: boolean
        created_at:
          type: string
          format: date-time
        updated_at:
          type: string
          format: date-time
    MenuItemRequest:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
        price:
          type: number
          format: float
        category:
          type: string
        image_url:
          type: string
          format: url
        is_available:
          type: boolean
        preparation_time_minutes:
          type: integer
      required:
        - name
        - price
        - category
        - is_available
    MenuItem:
      type: object
      properties:
        menu_item_id:
          type: string
          format: uuid
        vendor_id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
        price:
          type: number
          format: float
        category:
          type: string
        image_url:
          type: string
          format: url
        is_available:
          type: boolean
        preparation_time_minutes:
          type: integer
        created_at:
          type: string
          format: date-time
        updated_at:
          type: string
          format: date-time
    OrderItemRequest:
      type: object
      properties:
        menu_item_id:
          type: string
          format: uuid
        quantity:
          type: integer
          minimum: 1
        notes:
          type: string
      required:
        - menu_item_id
        - quantity
    OrderCreateRequest:
      type: object
      properties:
        vendor_id:
          type: string
          format: uuid
        items:
          type: array
          items:
            $ref: '#/components/schemas/OrderItemRequest'
        delivery_address:
          $ref: '#/components/schemas/Address'
        special_instructions:
          type: string
        train_number:
          type: string
        coach_number:
          type: string
        seat_number:
          type: string
        station_code:
          type: string
        scheduled_arrival_time:
          type: string
          format: date-time
        bus_operator:
          type: string
        bus_number:
          type: string
        scheduled_stop_time:
          type: string
          format: date-time
        company_id:
          type: string
          format: uuid
        internal_delivery_point:
          type: string
      required:
        - vendor_id
        - items
        - delivery_address
    Order:
      type: object
      properties:
        order_id:
          type: string
          format: uuid
        customer_id:
          type: string
          format: uuid
        vendor_id:
          type: string
          format: uuid
        delivery_partner_id:
          type: string
          format: uuid
        order_status:
          type: string
          enum: [PENDING, ACCEPTED, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, CANCELLED, REJECTED]
        total_amount:
          type: number
          format: float
        payment_status:
          type: string
          enum: [PENDING, PAID, FAILED, REFUNDED]
        delivery_address:
          $ref: '#/components/schemas/Address'
        ordered_at:
          type: string
          format: date-time
        estimated_delivery_time:
          type: string
          format: date-time
        delivered_at:
          type: string
          format: date-time
        special_instructions:
          type: string
        train_number:
          type: string
        coach_number:
          type: string
        seat_number:
          type: string
        station_code:
          type: string
        scheduled_arrival_time:
          type: string
          format: date-time
        bus_operator:
          type: string
        bus_number:
          type: string
        scheduled_stop_time:
          type: string
          format: date-time
        company_id:
          type: string
          format: uuid
        internal_delivery_point:
          type: string
        items:
          type: array
          items:
            $ref: '#/components/schemas/OrderItem'
    OrderItem:
      type: object
      properties:
        order_item_id:
          type: string
          format: uuid
        order_id:
          type: string
          format: uuid
        menu_item_id:
          type: string
          format: uuid
        quantity:
          type: integer
        price_at_order:
          type: number
          format: float
        notes:
          type: string
    OrderStatusUpdateRequest:
      type: object
      properties:
        status:
          type: string
          enum: [ACCEPTED, REJECTED, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, CANCELLED]
        delivery_partner_id:
          type: string
          format: uuid
          description: Only applicable when status transitions to ON_THE_WAY and assigned to DP
      required:
        - status
    PaymentInitiateRequest:
      type: object
      properties:
        order_id:
          type: string
          format: uuid
        payment_method:
          type: string
          enum: [UPI, CASH] # Card/Wallet can be added later
      required:
        - order_id
        - payment_method
    PaymentResponse:
      type: object
      properties:
        transaction_id:
          type: string
          format: uuid
        order_id:
          type: string
          format: uuid
        status:
          type: string
          enum: [PENDING, SUCCESS, FAILED]
        amount:
          type: number
          format: float
        redirect_url:
          type: string
          format: url
          description: For UPI redirects
    VendorSearchResponse:
      type: object
      properties:
        vendor_id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
        address:
          $ref: '#/components/schemas/Address'
        rating:
          type: number
          format: float
        is_open:
          type: boolean
        distance_km:
          type: number
          format: float
    MenuItemSearchResponse:
      type: object
      properties:
        menu_item_id:
          type: string
          format: uuid
        vendor_id:
          type: string
          format: uuid
        vendor_name:
          type: string
        name:
          type: string
        description:
          type: string
        price:
          type: number
          format: float
        category:
          type: string
        image_url:
          type: string
          format: url
        is_available:
          type: boolean
        preparation_time_minutes:
          type: integer
    LocationUpdate:
      type: object
      properties:
        latitude:
          type: number
          format: float
        longitude:
          type: number
          format: float
      required:
        - latitude
        - longitude
```
