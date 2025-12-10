# 3\. Tech Stack

This is the DEFINITIVE technology selection section.

## Cloud Infrastructure

  - **Provider:** AWS
  - **Key Services:** AWS API Gateway, AWS RDS (PostgreSQL), AWS ElastiCache (Redis), AWS MSK (Kafka), AWS EKS (Kubernetes), AWS S3, AWS CloudWatch, AWS X-Ray, AWS IAM, AWS WAF.
  - **Deployment Regions:** eu-west-1 (Ireland) or ap-south-1 (Mumbai, India) - *Recommendation: ap-south-1 for primary focus on India market, lower latency for India-based users.*

## Technology Stack Table

| Category | Technology | Version | Purpose | Rationale |
|---|---|---|---|---|
| **Language** | Java | 17 | Primary backend development language | Strong ecosystem, mature frameworks, good performance, widely adopted for enterprise systems. |
| **Runtime** | JVM | - | Java Virtual Machine | Standard runtime for Java applications, robust and well-optimized. |
| **Framework** | Spring Boot | 3.2.x | Backend service development | Rapid development, strong convention over configuration, extensive ecosystem of modules for various needs (JPA, Web, Kafka, Security). |
| **Web Framework** | Spring Web MVC | - | REST API development within Spring Boot | Integrated with Spring Boot, provides robust features for building RESTful services. |
| **ORM/Data Access** | Spring Data JPA (Hibernate) | - | Database interaction | Simplifies data access, object-relational mapping, reduces boilerplate code, supports transactions. |
| **Database** | PostgreSQL | 15.x | Primary transactional and relational data store | Robust, open-source, ACID compliant, excellent support for complex queries, high scalability, and active community. |
| **Caching** | Redis | 7.x | In-memory data store for caching and real-time data | High performance, supports various data structures (hash, list, set), ideal for session management, leaderboards, and frequently accessed data. |
| **Search Engine** | Elasticsearch | 8.x | Full-text search and analytical indexing for SDS | Distributed, highly scalable search and analytics engine, ideal for complex queries, real-time indexing, and high throughput. |
| **Vector Database** | Pgvector (PostgreSQL extension) / or specialized VectorDB | 0.5.x (pgvector) / Latest stable | Store and query vector embeddings for SDS | Efficiently handles vector similarity search for recommendation features, can be integrated with PostgreSQL initially for simplicity or a dedicated solution for scale. |
| **Message Broker** | Apache Kafka | 3.x | Asynchronous inter-service communication | High-throughput, fault-tolerant, scalable event streaming platform, crucial for event-driven architecture and data synchronization. |
| **External APIs (Mapping)** | MapmyIndia API | Latest | Geolocation, mapping, routing for DMS | Indian context, comprehensive mapping data for accurate delivery, route optimization. |
| **External APIs (Payments)** | UPI Gateway (e.g., Razorpay, PayU) | Latest | Payment processing for PMS | Widely adopted digital payment system in India, essential for user convenience. |
| **External APIs (Notifications)** | FCM, SendGrid, Gupshup (SMS/WhatsApp) | Latest | Push notifications, email, SMS, WhatsApp | Comprehensive notification channels to reach users effectively. |
| **Containerization** | Docker | Latest stable | Packaging services | Ensures consistent environments across development and deployment, simplifies dependency management. |
| **Orchestration** | Kubernetes (EKS) | 1.28.x | Container orchestration and management | Automates deployment, scaling, and management of containerized applications, enabling independent scaling of services. |
| **CI/CD** | Jenkins | Latest LTS | Continuous Integration/Continuous Deployment | Automates build, test, and deployment pipelines. |
| **Infrastructure as Code (IaC)** | Terraform | 1.x | Infrastructure provisioning | Declarative infrastructure management, enables repeatable and consistent environment setup. |
| **Monitoring** | Prometheus | Latest stable | Metrics collection | Open-source monitoring system with a powerful query language, ideal for collecting real-time metrics from services. |
| **Visualization/Alerting** | Grafana | Latest stable | Dashboarding and alerting | Integrates with Prometheus to visualize metrics and configure alerts for system health. |
| **Centralized Logging** | OpenObserver / ELK Stack | Latest stable | Log aggregation and analysis | Centralized platform for collecting, parsing, and analyzing logs from all services, crucial for debugging and operational insights. |
| **Distributed Tracing** | OpenTelemetry | Latest stable | End-to-end transaction tracing | Provides visibility into request flow across multiple services, essential for debugging distributed systems. |
| **Build Tool** | Gradle | 8.x | Build automation for Spring Boot projects | Flexible, high-performance build automation system, widely used for Java/Spring projects. |
| **Testing Frameworks** | JUnit 5, Mockito | Latest stable | Unit and integration testing | Standard testing framework for Java, enables robust test suite development. |
| **Security Framework** | Spring Security | - | Authentication and authorization | Comprehensive security framework integrated with Spring Boot, provides robust control over access. |
