# Tea & Snacks Delivery Aggregator Detailed Architecture Document

## 1\. Introduction

This document provides a comprehensive and detailed architectural blueprint for the Tea & Snacks Delivery Aggregator project. It covers the design of backend systems, shared services, data models, API specifications, and infrastructure concerns. This document serves as the single source of truth for all architectural decisions, guiding AI-driven development to ensure consistency, scalability, and maintainability.

**Relationship to Frontend Architecture:**
If the project includes a significant user interface, a separate Frontend Architecture Document will detail the frontend-specific design and MUST be used in conjunction with this document. Core technology stack choices documented herein (see "Tech Stack") are definitive for the entire project, including any frontend components.

### 1.1. Starter Template or Existing Project

No starter template or existing codebase will be used. This is a greenfield project, and the architecture will be designed from scratch, requiring manual setup for all tooling and configuration.

### 1.2. Change Log

| Date | Version | Description | Author |
|---|---|---|---|
| July 27, 2025 | 1.0 | Initial detailed architecture document based on PRD, Tech Stack Template, and user feedback | Gemini (AI Assistant) |

## 2\. High Level Architecture

This section establishes the foundational aspects of the architecture.

### 2.1. Technical Summary

The "Tea & Snacks Delivery Aggregator" system will adopt a **microservices architectural style**, enabling independent development, deployment, and scaling of services. Key components will include dedicated services for user management, vendor management, order processing, and delivery logistics, communicating primarily via REST APIs for synchronous interactions and Apache Kafka for asynchronous operations. The primary technology choices are Java (Spring Boot) for backend, React Native/React for frontend, and AWS for cloud infrastructure. The architecture prioritizes scalability, modularity, and resilience to efficiently aggregate existing tea and snack vendors in India.

### 2.2. High Level Overview

1.  **Main Architectural Style:** Microservices. This choice is driven by the need for scalability, modularity, and resilience, allowing different parts of the system to evolve independently and handle varying loads.
2.  **Repository Structure Decision:** **Polyrepo** (multiple repositories, one per service). This supports independent deployment, development lifecycles, and clear service ownership.
3.  **Service Architecture Decision:** A **service-oriented architecture (SOA)** with clearly defined microservices, each encapsulating a specific business capability (e.g., User Service, Vendor Service, Order Service, Delivery Service). For initial development, modules and databases will be kept as close as possible based on domain responsibility to facilitate faster development. The architecture will remain flexible to allow for more granular modularity and service decomposition as the application grows in popularity.
4.  **Primary User Interaction Flow or Data Flow:**
      * **Customer:** Customer logs in -\> browses vendors/menus -\> places order -\> payment processed -\> order sent to vendor -\> delivery initiated -\> customer tracks order -\> order delivered.
      * **Vendor:** Vendor logs in -\> manages menu/availability -\> receives orders -\> accepts/rejects orders -\> updates order status.
      * **Delivery Partner:** Delivery partner logs in -\> receives delivery requests -\> picks up order -\> delivers order -\> updates delivery status.
5.  **Key Architectural Decisions and their Rationale:**
      * **Microservices:** To achieve high scalability, fault isolation, and allow independent team development and deployment. This aligns with the PRD's requirement for a scalable platform.
      * **API-driven communication (RESTful APIs):** For clear contracts between services and enabling various client applications (mobile, web). This will be the primary synchronous communication method.
      * **Asynchronous communication (Apache Kafka):** To decouple services, improve responsiveness, and handle high throughput. This is crucial for operations like notifications and delivery updates, ensuring resilience even if consuming services are temporarily unavailable.
      * **Domain-driven initial modularity:** To enable faster initial development by keeping related modules and their data close, while maintaining flexibility for future decomposition as the application scales.

### 2.3. High Level Project Diagram

```mermaid
graph TD
    UserClient[Customer (Web/Mobile)] -->|Places Order (REST)| Gateway(API Gateway)
    VendorClient[Vendor Portal] -->|Manages Menu/Orders (REST)| Gateway
    DeliveryClient[Delivery App] -->|Updates Delivery Status (REST)| Gateway

    Gateway -- REST/HTTP --> UserService
    Gateway -- REST/HTTP --> VendorService
    Gateway -- REST/HTTP --> OrderService
    Gateway -- REST/HTTP --> DeliveryService
    Gateway -- REST/HTTP --> PaymentService

    UserService -- JDBC/JPA --> UserDB[(PostgreSQL)]
    VendorService -- JDBC/JPA --> VendorDB[(PostgreSQL)]
    OrderService -- JDBC/JPA --> OrderDB[(PostgreSQL)]
    DeliveryService -- JDBC/JPA --> DeliveryDB[(PostgreSQL)]
    PaymentService -- REST --> PaymentGateway[External Payment Gateway - UPI + Cash]

    OrderService -- Kafka Topic: order_events --> NotificationService
    DeliveryService -- Kafka Topic: delivery_events --> NotificationService
    NotificationService --> UserClient[via FCM/SendGrid/Gupshup]
    NotificationService --> VendorClient[via FCM/SendGrid/Gupshup]
    NotificationService --> DeliveryClient[via FCM/SendGrid/Gupshup]

    CatalogService[Catalog/Menu Service] -- JDBC/JPA --> CatalogDB[(PostgreSQL)]
    CatalogService -- Indexes --> Elasticsearch
    CatalogService -- Caches --> Redis

    ReportingService[Reporting & Analytics Service] -- Reads from --> OrderDB
    ReportingService -- Reads from --> VendorDB
    ReportingService -- Reads from --> UserDB
    ReportingService -- Stores Reports --> ReportingDB[(PostgreSQL)]

    SearchRecommendationService[Order Search & Recommendation] -- Search Index --> Elasticsearch
    SearchRecommendationService -- Recommendation Data --> VectorDB
    SearchRecommendationService -- Order Data --> OrderDB
```

### 2.4. Architectural and Design Patterns

  * **Architectural Style Pattern:**
      * **Choice:** **Microservices** - Decouple components into small, independent services.
      * **Rationale:** Chosen for its ability to enable independent development, deployment, and scaling, essential for a platform with diverse functionalities and a growing user base in India.
  * **Data Consistency Pattern:**
      * **Recommendation:** **Saga Pattern** - This pattern will be used to manage consistency across multiple services for complex business processes like order placement, which involves payment, inventory updates, and delivery initiation. It ensures data integrity in a distributed environment without tight coupling. Eventual consistency will be acceptable for certain non-critical data.
  * **Communication Pattern:**
      * **Choice:** **RESTful APIs for synchronous interactions, combined with Apache Kafka for asynchronous interactions.**
      * **Rationale:** RESTful APIs facilitate immediate request-response for operations like placing orders or fetching vendor details. Apache Kafka provides a robust, scalable, and fault-tolerant messaging system for events like order updates, notifications, and delivery status changes, decoupling services and improving overall system responsiveness. gRPC will not be used for now.

## 3\. Tech Stack

This section details the definitive technology selections for the project, based on the "FoodStore1-TechStack template" provided and user confirmations.

### 3.1. Cloud Infrastructure

  * **Provider:** AWS
  * **Key Services:**
      * API Gateway: AWS API Gateway
      * Container Orchestration: AWS EKS (Elastic Kubernetes Service)
      * Object Storage: AWS S3
  * **Deployment Regions:** To be determined, likely multiple regions within India for optimal latency, data residency, and disaster recovery.

### 3.2. Technology Stack Table

| Category | Technology | Version | Purpose | Rationale |
|---|---|---|---|---|
| **Frontend & Mobile** | React Native | `~0.74.x` (or latest stable) | Customer Mobile App, Vendor Mobile App, Delivery Partner App | Enables cross-platform development with a single codebase, leveraging native capabilities. |
| | React | `~18.3.x` (or latest stable) | Customer Web App, Vendor Web Dashboard, Admin Web Panel | Popular, component-based library for building dynamic user interfaces, to be used if React Native is not feasible for specific web components. |
| **Backend Development** | Java (Spring Boot) | `3.3.x` (or latest stable) | Primary backend language and framework for all microservices | Robust, enterprise-grade, large ecosystem, and suitable for building scalable microservices with a strong emphasis on developer productivity. |
| **API Gateway** | AWS API Gateway | N/A | Manage API traffic, routing, and security | Cloud-native, scalable, integrates seamlessly with AWS ecosystem, providing features like throttling, caching, and authentication. |
| **Authentication** | JWT (JSON Web Tokens), OAuth 2.0 | N/A | Secure user authentication and authorization | Industry standards for secure access control, providing a robust mechanism for token-based authentication and delegated authorization. |
| **Role-Based Access Control (RBAC)** | Spring Security | N/A | Implement granular access control within services | Comprehensive security framework for Spring Boot applications, enabling fine-grained control over resource access based on user roles and permissions. |
| **User Management Service (UMS)** | Spring Boot + JPA/PostgreSQL | `3.3.x` (Spring Boot), `16.x` (PostgreSQL) | Manage user accounts, profiles, and authentication details | Relational database for structured user data with ACID properties, JPA for robust Object-Relational Mapping (ORM). |
| **Order Management Service (OMS)** | Spring Boot + Apache Kafka | `3.3.x` (Spring Boot), `3.x` (Kafka) | Handle order creation, processing, and lifecycle | Kafka for high-throughput, fault-tolerant message queuing, essential for handling order events and ensuring eventual consistency across services. |
| **Payment Service (PMS)** | Spring Boot | `3.3.x` | Process payments and manage payment transactions | Backend logic for integrating with various payment methods and handling payment callbacks. |
| **Payment Gateway Integration** | UPI + Cash | N/A | Facilitate payment transactions in India | Supports local Indian payment methods, crucial for targeting the specified market. |
| **Vendor Management Service (VMS)** | Spring Boot + Apache Kafka | `3.3.x` (Spring Boot), `3.x` (Kafka) | Manage vendor information, menus, and availability | Kafka for asynchronous updates and communication with other services, ensuring real-time consistency of vendor data. |
| **Delivery Management Service (DMS)** | Spring Boot + MapmyIndia API | `3.3.x` (Spring Boot) | Manage delivery assignments, tracking, and logistics | MapmyIndia API for mapping and geolocation services, specifically tailored and optimized for Indian geographic data. |
| **Notification Service** | Spring Boot | `3.3.x` | Orchestrate and send various notifications | Centralized service for sending notifications via multiple channels, decoupling notification logic from core business services. |
| **Push Notifications** | FCM (Firebase Cloud Messaging) | N/A | Deliver push notifications to customer, vendor, and delivery partner mobile devices | Free, robust, and widely adopted service with native support for Android & iOS, providing reliable message delivery. |
| **Email Notifications** | SendGrid | N/A | Send transactional and promotional email notifications | Easy integration, robust API, and a solid free tier suitable for initial scale. |
| **SMS/WhatsApp Notifications** | Gupshup | N/A | Send SMS and WhatsApp messages for critical updates and alerts | Local pricing, simple setup in India, and works well for business messaging, providing efficient communication. |
| **Catalog/Menu Service** | Spring Boot + Elasticsearch + Redis | `3.3.x` (Spring Boot), `8.x` (Elasticsearch), `7.x` (Redis) | Manage vendor menus and product catalogs | Elasticsearch for fast, flexible full-text search and complex queries on menu items; Redis for high-performance caching of frequently accessed menu data to reduce database load. |
| **Reporting & Analytics Service** | PostgreSQL | `16.x` | Generate reports and analytics from various data sources | Relational database suitable for structured reporting data, allowing for complex queries and aggregations. |
| **Order Search & Recommendation** | PostgreSQL + Elasticsearch + VectorDB (e.g., Pinecone/Weaviate) | `16.x` (PostgreSQL), `8.x` (Elasticsearch), To be determined (VectorDB) | Enable search functionality for past orders and provide personalized recommendations | PostgreSQL for transactional data, Elasticsearch for full-text search, VectorDB for personalized recommendations. |
| **Mapping/Geolocation Integration** | MapmyIndia API | N/A | Provide map services and location-based functionalities | Tailored for Indian geographic data and services, ensuring accurate and localized mapping capabilities for delivery logistics. |
| **SMS/Email Integration** | SendGrid, Gupshup | N/A | Facilitate communication via SMS and email | Chosen for ease of integration and local relevance. |
| **Railway/Bus Schedules Integration** | Manual ingestion | N/A | Incorporate public transport schedules for captive audience segments | Initial approach to integrate schedules, possibly evolving to API integrations if public APIs become available or are deemed necessary. |
| **Cloud Provider** | AWS | N/A | Core cloud infrastructure | Leading cloud provider with comprehensive services. |
| **Containerization** | Docker | `26.x` (or latest stable) | Package applications into isolated containers | Standard for containerization, ensures consistent environments. |
| **Orchestration** | EKS (Kubernetes) | `1.29` (or latest stable) | Manage and scale containerized applications | Robust, scalable container orchestration platform. |
| **CI/CD** | Jenkins | `2.463` (or latest stable) | Automate build, test, and deployment pipelines | Widely used, highly customizable automation server. |
| **Infrastructure as Code (IaC)** | Terraform | `1.8.x` (or latest stable) | Provision and manage infrastructure | Declarative IaC tool, supports multi-cloud environments. |
| **Primary Relational Database** | PostgreSQL | `16.x` | Store structured data for various services | Robust, open-source relational database. |
| **Distributed Cache** | Redis | `7.2.x` (or latest stable) | In-memory data store for caching | High-performance, versatile caching solution. |
| **Message Broker** | Apache Kafka | `3.x` (or latest stable) | High-throughput, fault-tolerant messaging system | Essential for inter-service communication and event streaming in microservices. |
| **Object Storage** | AWS S3 | N/A | Store static assets, user-generated content, backups | Scalable, durable object storage. |
| **Monitoring** | Prometheus + Grafana | `2.x` (Prometheus), `11.x` (Grafana) | System and application monitoring, visualization | Powerful open-source tools for metrics collection and dashboards. |
| **Centralized Logging** | OpenObserver or ELK Stack | N/A | Aggregate and analyze logs from all services | Choice based on simplicity of build, with preference for OpenObserver if straightforward. |
| **Distributed Tracing** | OpenTelemetry | N/A | Trace requests across multiple services | Vendor-neutral API/SDK for instrumentation, supports various backends. |
| **Alerting** | Grafana Alerting | N/A | Configure and manage alerts based on metrics | Integrates with Grafana dashboards for proactive issue detection. |
| **Encryption** | TLS (HTTPS) | N/A | Secure data in transit | Standard for encrypted communication over networks. |
| **DDoS & API Security** | Rate Limiting at API Gateway | N/A | Protect against Denial-of-Service attacks and API abuse | Implemented at the API Gateway level for effective control. |
| **Data Privacy & Compliance** | DPDP Act (India), PCI DSS, SOC2 | N/A | Ensure legal and regulatory compliance for data handling | To be addressed in later stages of development, with initial focus on fundamental security measures. |

## 4\. Detailed Microservice Design

This section provides a deeper dive into the design of each core microservice, including their responsibilities, key APIs, and data interactions.

### 4.1. User Management Service (UMS)

  * **Responsibilities:** User registration, login, profile management (view, edit), password reset, authentication token generation (JWT), user role management, account activation/deactivation.
  * **Key Data Model:** `User` (UserID, Email, PhoneNumber, PasswordHash, Salt, FirstName, LastName, Roles, Status, AddressID, CreatedAt, UpdatedAt).
  * **APIs (REST):**
      * `POST /users/register`: Register a new user.
      * `POST /users/login`: Authenticate user and return JWT.
      * `GET /users/{userId}`: Retrieve user profile.
      * `PUT /users/{userId}`: Update user profile.
      * `POST /users/password/reset`: Initiate password reset.
      * `PUT /users/password/change`: Change password.
      * `GET /users/{userId}/addresses`: Get user addresses.
      * `POST /users/{userId}/addresses`: Add new address.
  * **Database:** PostgreSQL (Primary UserDB).

### 4.2. Vendor Management Service (VMS)

  * **Responsibilities:** Vendor registration, profile management (business details, operating hours, location), menu management (add, edit, delete items, pricing), availability toggling, vendor ratings/reviews.
  * **Key Data Models:** `Vendor` (VendorID, Name, Description, AddressID, ContactInfo, OperatingHours, Status, Rating, CreatedAt, UpdatedAt), `MenuItem` (ItemID, VendorID, Name, Description, Price, Category, IsAvailable).
  * **APIs (REST):**
      * `POST /vendors/register`: Register a new vendor.
      * `GET /vendors/{vendorId}`: Retrieve vendor profile.
      * `PUT /vendors/{vendorId}`: Update vendor profile.
      * `GET /vendors/{vendorId}/menu`: Retrieve vendor's menu.
      * `POST /vendors/{vendorId}/menu`: Add menu item.
      * `PUT /vendors/{vendorId}/menu/{itemId}`: Update menu item.
      * `DELETE /vendors/{vendorId}/menu/{itemId}`: Delete menu item.
      * `PUT /vendors/{vendorId}/status`: Update vendor availability.
  * **Database:** PostgreSQL (Primary VendorDB).
  * **Kafka Events (Producer):** `vendor_updated` (when vendor details or menu change), `vendor_status_changed`.

### 4.3. Catalog/Menu Service

  * **Responsibilities:** Provide comprehensive catalog search and Browse, facilitate menu item retrieval, support filtering and sorting, integrate with Elasticsearch for fast search, and Redis for caching.
  * **Key Data Model:** Replicates `MenuItem` from VMS, `Category`, `Tag`.
  * **APIs (REST):**
      * `GET /catalog/search`: Search menu items by keywords, category, vendor.
      * `GET /catalog/vendors/{vendorId}/menu`: Get a specific vendor's menu.
      * `GET /catalog/items/{itemId}`: Get details of a single menu item.
      * `GET /catalog/categories`: Get available food categories.
  * **Database:** PostgreSQL (replicates data from VendorDB for read efficiency), Elasticsearch (for search index), Redis (for caching popular menus/items).
  * **Kafka Events (Consumer):** `vendor_updated` (to update Elasticsearch index and Redis cache), `menu_item_updated`.

### 4.4. Order Management Service (OMS)

  * **Responsibilities:** Order creation, order status updates, order history retrieval, order validation (e.g., item availability), interaction with Payment Service and Delivery Service.
  * **Key Data Models:** `Order` (OrderID, UserID, VendorID, TotalAmount, Status, OrderItems, DeliveryAddressID, PaymentStatus, CreatedAt, UpdatedAt), `OrderItem` (ItemID, Quantity, PriceAtTimeOfOrder).
  * **APIs (REST):**
      * `POST /orders`: Create a new order.
      * `GET /orders/{orderId}`: Retrieve order details.
      * `PUT /orders/{orderId}/status`: Update order status (e.g., accepted, rejected, prepared).
      * `GET /users/{userId}/orders`: Get user's order history.
  * **Database:** PostgreSQL (Primary OrderDB).
  * **Kafka Events (Producer):** `order_created`, `order_status_updated`, `order_payment_successful`, `order_payment_failed`.
  * **Kafka Events (Consumer):** `payment_status_updated`, `delivery_status_updated`, `inventory_updated` (if an inventory service is introduced later).

### 4.5. Payment Service (PMS)

  * **Responsibilities:** Process payments, handle payment callbacks from UPI/Cash, manage payment status, and record payment transactions.
  * **Key Data Model:** `PaymentTransaction` (TransactionID, OrderID, UserID, Amount, Status, PaymentMethod, TransactionDetails, CreatedAt).
  * **APIs (REST):**
      * `POST /payments/initiate`: Initiate a payment for an order.
      * `POST /payments/callback`: Handle payment gateway callbacks (e.g., UPI success/failure).
      * `GET /payments/{transactionId}`: Retrieve payment status.
  * **Database:** PostgreSQL (part of OrderDB or a separate PaymentDB for domain separation).
  * **External Integrations:** UPI (via SDK/API) for online payments, Cash-on-Delivery handling.
  * **Kafka Events (Producer):** `payment_status_updated` (for OMS).

### 4.6. Delivery Management Service (DMS)

  * **Responsibilities:** Assign delivery partners to orders, track deliveries in real-time, update delivery status, calculate delivery routes, manage delivery partner availability.
  * **Key Data Models:** `Delivery` (DeliveryID, OrderID, DeliveryPartnerID, Status, PickupLocation, DropoffLocation, EstimatedDeliveryTime, ActualDeliveryTime), `DeliveryPartner` (PartnerID, Name, ContactInfo, VehicleDetails, Status, CurrentLocation).
  * **APIs (REST):**
      * `POST /deliveries/assign`: Assign a delivery partner to an order.
      * `GET /deliveries/{deliveryId}/track`: Get real-time delivery status and location.
      * `PUT /deliveries/{deliveryId}/status`: Update delivery status (e.g., picked up, in transit, delivered).
      * `GET /delivery-partners/{partnerId}/availability`: Get partner availability.
      * `PUT /delivery-partners/{partnerId}/location`: Update partner's current location.
  * **Database:** PostgreSQL (Primary DeliveryDB).
  * **External Integrations:** MapmyIndia API for geocoding, routing, and real-time location tracking.
  * **Kafka Events (Producer):** `delivery_status_updated` (for OMS and Notification Service).
  * **Kafka Events (Consumer):** `order_ready_for_delivery` (from OMS).

### 4.7. Notification Service

  * **Responsibilities:** Send notifications (push, email, SMS/WhatsApp) to users, vendors, and delivery partners based on system events. Manages notification templates and preferences.
  * **Key Data Model:** `NotificationLog` (LogID, UserID/EntityID, Channel, Type, Message, Status, Timestamp), `NotificationTemplate`.
  * **APIs (REST - internal for manual triggers):**
      * `POST /notifications/send`: Send a generic notification.
  * **Database:** No primary database, possibly uses a small internal PostgreSQL for logs/templates.
  * **External Integrations:** FCM, SendGrid, Gupshup.
  * **Kafka Events (Consumer):** `order_status_updated`, `payment_status_updated`, `delivery_status_updated`, `vendor_status_changed`, etc.

### 4.8. Reporting & Analytics Service

  * **Responsibilities:** Generate business reports (e.g., sales, vendor performance, delivery efficiency), provide dashboards, enable data analysis.
  * **Key Data Models:** Aggregated data models derived from other services.
  * **APIs (REST):**
      * `GET /reports/sales`: Retrieve sales reports.
      * `GET /reports/vendor-performance`: Get vendor-specific performance data.
  * **Database:** PostgreSQL (dedicated for analytical queries and aggregated data). Reads directly from other service databases or consumes Kafka events to build its own data views.

### 4.9. Order Search & Recommendation Service

  * **Responsibilities:** Provide full-text search capabilities for past orders for customers and vendors, generate personalized order recommendations.
  * **Key Data Models:** Search index (Elasticsearch), User/Item embeddings (VectorDB).
  * **APIs (REST):**
      * `GET /search/orders`: Search user/vendor orders.
      * `GET /recommendations/orders`: Get personalized order recommendations for a user.
  * **Database:** Elasticsearch (for search index), VectorDB (e.g., Pinecone, Weaviate for recommendations), reads from OrderDB for source data.
  * **Kafka Events (Consumer):** `order_completed` (to update recommendation models and search index).

## 5\. Data Storage & Management

### 5.1. Database Design (PostgreSQL)

  * **Primary Relational Database:** PostgreSQL will be used for transactional data storage for most microservices. Each service will typically have its own dedicated database instance or a schema within a shared PostgreSQL cluster to maintain data isolation and allow independent schema evolution.
  * **Key Design Principles:**
      * **Microservice per Database:** Each microservice owns its data. Direct database access from other services is highly discouraged. Communication must be via APIs or Kafka events.
      * **Normalization:** Follow normalization principles to reduce data redundancy and improve data integrity.
      * **Indexing:** Appropriate indexing strategy for frequently queried columns to optimize read performance.
      * **Connection Pooling:** Use connection pooling (e.g., HikariCP with Spring Boot) to manage database connections efficiently.
      * **Schema Migrations:** Use tools like Flyway or Liquibase for version-controlled database schema migrations.

### 5.2. Caching (Redis)

  * **Purpose:** Redis will be used as a distributed in-memory data store for caching frequently accessed data, reducing load on primary databases and improving response times.
  * **Use Cases:**
      * Caching popular menu items in the Catalog Service.
      * Storing session data or authentication tokens.
      * Rate limiting API requests.
      * Leaderboards or real-time counters.

### 5.3. Search (Elasticsearch)

  * **Purpose:** Elasticsearch will power full-text search capabilities for menu items, orders, and potentially vendor listings.
  * **Use Cases:**
      * Customer searching for specific tea/snack items.
      * Vendor searching their past orders.
      * Admin panel search functionalities.
  * **Integration:** Data will be pushed from relevant microservices (e.g., Catalog Service, Order Service) into Elasticsearch via Kafka consumers or direct API calls for indexing.

### 5.4. Vector Database

  * **Purpose:** A VectorDB (e.g., Pinecone, Weaviate, or a PostgreSQL extension like pgvector) will be used for storing embeddings and enabling semantic search and personalized recommendations.
  * **Use Cases:**
      * Recommending tea/snack items to users based on their past orders and preferences.
      * Suggesting complementary items.

### 5.5. Object Storage (AWS S3)

  * **Purpose:** AWS S3 will be used for storing unstructured data, such as:
      * Vendor images (e.g., menu item photos, vendor logos).
      * User profile pictures.
      * Backup files.
      * Static content (though CDN is not required initially).

## 6\. API Design & Communication

### 6.1. RESTful APIs

  * **Standard:** RESTful principles will be adhered to for all synchronous communication between client applications (mobile/web) and microservices, and for internal service-to-service communication where immediate response is required.
  * **Design Principles:**
      * **Resource-Oriented:** APIs will be designed around resources (e.g., `/users`, `/orders`, `/vendors`).
      * **Stateless:** Each request from client to server must contain all of the information needed to understand the request.
      * **Standard HTTP Methods:** Use GET, POST, PUT, DELETE, PATCH appropriately.
      * **JSON Payloads:** Request and response bodies will be JSON.
      * **Versioned APIs:** APIs will be versioned (e.g., `/v1/users`) to allow for backward compatibility.
      * **Clear Error Handling:** Standardized error response formats with appropriate HTTP status codes.
  * **API Gateway:** AWS API Gateway will serve as the single entry point for all external API requests, handling authentication, authorization, request routing, throttling, and caching.

### 6.2. Asynchronous Communication (Apache Kafka)

  * **Purpose:** Apache Kafka will be the central message broker for asynchronous communication and event streaming between microservices.
  * **Use Cases:**
      * **Event Sourcing:** Capturing state changes as a sequence of immutable events (e.g., `OrderCreated`, `OrderStatusUpdated`).
      * **Inter-service Communication:** Decoupling services for operations that don't require immediate synchronous responses (e.g., a payment success event triggering a notification).
      * **Data Replication/Synchronization:** Feeding data to search indexes (Elasticsearch) or analytical databases.
  * **Key Kafka Topics:**
      * `order_events`: For all order-related events (creation, status updates, cancellations).
      * `payment_events`: For payment transaction status (success, failure).
      * `delivery_events`: For delivery status updates (picked up, in transit, delivered).
      * `user_events`: For user registration, profile updates.
      * `vendor_events`: For vendor registration, profile updates, menu changes.
  * **Schema Registry:** A schema registry (e.g., Confluent Schema Registry) will be used to manage Avro or Protobuf schemas for Kafka messages, ensuring data compatibility and evolution.

## 7\. Security & Compliance

### 7.1. Authentication & Authorization

  * **OAuth 2.0 & JWT:** OAuth 2.0 will be used for delegated authorization, with JWTs (JSON Web Tokens) serving as the primary mechanism for transmitting authenticated user identity and authorization claims between services.
  * **Flow:**
    1.  User authenticates with the User Management Service (UMS).
    2.  UMS issues an Access Token (JWT) and a Refresh Token.
    3.  Client applications send the Access Token with every API request to the API Gateway.
    4.  API Gateway validates the JWT.
    5.  Microservices use Spring Security to validate JWT and enforce Role-Based Access Control (RBAC) based on roles/permissions embedded in the token or fetched from UMS.
  * **Token Management:** Secure storage of refresh tokens on the client side (e.g., HTTP-only cookies for web, secure storage for mobile). Access tokens will be short-lived.

### 7.2. Data Encryption

  * **In Transit:** All communication will be encrypted using TLS (HTTPS) for both external APIs (via AWS API Gateway) and internal service-to-service communication within the VPC.
  * **At Rest:** Data stored in PostgreSQL, Elasticsearch, Redis, and S3 will be encrypted at rest using AWS native encryption capabilities (e.g., KMS-managed keys for RDS, S3 encryption).

### 7.3. API Security & DDoS Protection

  * **Rate Limiting:** Implemented at the AWS API Gateway to protect against excessive requests and potential DDoS attacks, ensuring service availability.
  * **WAF (Web Application Firewall):** AWS WAF could be deployed in front of the API Gateway to provide protection against common web exploits (e.g., SQL injection, cross-site scripting).
  * **Input Validation:** Strict input validation will be performed at API endpoints to prevent injection attacks and ensure data integrity.

### 7.4. Data Privacy & Compliance

  * **DPDP Act (India):** Adherence to the Digital Personal Data Protection Act (India) will be a key consideration, especially concerning data collection, storage, processing, and user consent for Indian users.
  * **PCI DSS:** While payment processing is offloaded to UPI gateways, practices related to handling sensitive financial information (if any directly touched) will follow PCI DSS guidelines.
  * **SOC2:** Aim for SOC2 compliance for robust security controls.
  * **Strategy:** Data privacy and compliance will be integrated into the development lifecycle from the beginning, but a full compliance audit and implementation will be a continuous process, to be addressed in later stages of development.

## 8\. Infrastructure & DevOps

### 8.1. Cloud Provider

  * **AWS:** All infrastructure will be provisioned and managed on Amazon Web Services (AWS), leveraging its comprehensive suite of services.

### 8.2. Containerization (Docker)

  * **Docker:** All microservices will be containerized using Docker. This ensures consistency across different environments (development, testing, production) and simplifies deployment and scaling.
  * **Container Images:** Custom Docker images will be built for each microservice and stored in AWS ECR (Elastic Container Registry).

### 8.3. Orchestration (Kubernetes - EKS)

  * **Kubernetes (EKS):** AWS Elastic Kubernetes Service (EKS) will be used for deploying, managing, and scaling the containerized microservices.
  * **Key Kubernetes Features Utilized:**
      * **Deployments:** For managing stateless application instances.
      * **StatefulSets:** For stateful applications (e.g., potentially Kafka, Elasticsearch, though managed services are preferred).
      * **Services:** For load balancing and service discovery.
      * **Ingress:** For external access to services via API Gateway.
      * **Horizontal Pod Autoscaler (HPA):** For automatic scaling based on CPU/memory utilization or custom metrics.
      * **Volumes/Persistent Volumes:** For persistent storage needs.

### 8.4. CI/CD (Jenkins)

  * **Jenkins:** Jenkins will be the primary CI/CD platform for automating the build, test, and deployment processes.
  * **Pipeline Stages:**
    1.  **Code Commit:** Triggered by code pushes to source control (e.g., GitHub/Bitbucket).
    2.  **Build:** Compile code, run unit tests, build Docker images.
    3.  **Image Push:** Push Docker images to AWS ECR.
    4.  **Test:** Run integration tests, end-to-end tests.
    5.  **Deploy (Staging):** Deploy to staging environment (EKS).
    6.  **Approval Gate:** Manual or automated approval for production deployment.
    7.  **Deploy (Production):** Deploy to production environment (EKS).

### 8.5. Infrastructure as Code (Terraform)

  * **Terraform:** Terraform will be used to define, provision, and manage all AWS infrastructure (VPC, EKS clusters, RDS instances, S3 buckets, etc.) in a declarative and version-controlled manner.
  * **Benefits:** Ensures consistency, repeatability, and enables faster disaster recovery.

## 9\. Monitoring, Logging & Observability

### 9.1. Monitoring (Prometheus + Grafana)

  * **Prometheus:** Will be used for collecting metrics from all microservices and infrastructure components. Spring Boot Actuator endpoints will expose metrics compatible with Prometheus.
  * **Grafana:** Will be used for visualizing the collected metrics from Prometheus, creating dashboards, and providing real-time operational insights.
  * **Key Metrics:** CPU utilization, memory usage, network I/O, API request rates, error rates, latency, database connection pools, Kafka consumer lag.

### 9.2. Centralized Logging (OpenObserver or ELK Stack)

  * **Choice:** OpenObserver or ELK Stack (Elasticsearch, Logstash, Kibana). Preference is for OpenObserver if it offers a simpler setup and management, otherwise ELK will be deployed.
  * **Purpose:** To aggregate logs from all microservices, allowing for centralized search, analysis, and troubleshooting.
  * **Log Format:** All services will emit structured logs (e.g., JSON format) to facilitate parsing and analysis.

### 9.3. Distributed Tracing (OpenTelemetry)

  * **OpenTelemetry:** Will be implemented for end-to-end distributed tracing across microservices.
  * **Purpose:** To trace individual requests as they flow through multiple services, enabling performance bottlenecks identification and debugging in a complex microservices environment.
  * **Backend:** Traces will be exported to a compatible backend (e.g., Jaeger or AWS X-Ray if fully integrated with AWS).

### 9.4. Alerting (Grafana Alerting)

  * **Grafana Alerting:** Will be configured to send alerts based on predefined thresholds and conditions in Prometheus metrics.
  * **Channels:** Alerts will be sent to relevant teams via email, Slack, or PagerDuty integrations as necessary.

## 10\. Scalability & Resilience

### 10.1. Scalability Strategy

  * **Horizontal Scaling:** Microservices will be designed for horizontal scalability, allowing new instances (pods in Kubernetes) to be added dynamically based on load.
  * **Stateless Services:** Most microservices will be stateless to simplify scaling.
  * **Database Scaling:** PostgreSQL instances will be scaled vertically (initially) and horizontally via read replicas for read-heavy workloads. Sharding can be considered for extreme scale.
  * **Kafka:** Inherently scalable for high-throughput messaging.
  * **Elasticsearch/Redis:** Designed for horizontal scalability.
  * **Auto-scaling:** AWS EKS Horizontal Pod Autoscaler (HPA) and Cluster Autoscaler will be configured to automatically adjust the number of pods and underlying nodes based on demand.

### 10.2. Resilience & Fault Tolerance

  * **Circuit Breakers:** Implement circuit breaker patterns (e.g., with Resilience4j for Spring Boot) to prevent cascading failures between services.
  * **Retries:** Implement intelligent retry mechanisms with exponential backoff for transient failures.
  * **Timeouts:** Configure appropriate timeouts for all inter-service communication.
  * **Bulkheads:** Isolate resources to prevent one component's failure from affecting others.
  * **Graceful Degradation:** Design critical paths to degrade gracefully if non-critical dependencies are unavailable.
  * **Dead Letter Queues (DLQs):** For Kafka, use DLQs to capture messages that cannot be processed successfully, preventing message loss and allowing for manual inspection/reprocessing.
  * **Disaster Recovery (DR):** Terraform-managed infrastructure will facilitate rapid re-provisioning in different AWS regions for DR. Data backups to S3 and cross-region replication for critical databases.

## 11\. Future Considerations

  * **Serverless Components:** Explore AWS Lambda for event-driven, less frequently used functionalities (e.g., background jobs, image processing) to optimize cost and operational overhead.
  * **Analytics Platform:** As data grows, consider a dedicated data warehousing solution (e.g., AWS Redshift) for more complex analytics and business intelligence.
  * **GraphQL API:** Evaluate GraphQL for flexible data fetching for client applications, potentially complementing REST APIs.
  * **Advanced AI/ML:** Further leverage VectorDB and ML models for more sophisticated recommendation engines, dynamic pricing, and demand forecasting.
  * **Edge Computing:** For delivery partners, explore edge computing solutions to improve responsiveness and reduce latency in real-time tracking.