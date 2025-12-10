# 10\. Source Tree

```plaintext
project-root/
├── docs/                               # Architecture, PRD, other documentation
│   └── architecture.md
│   └── prd.md
├── services/
│   ├── api-gateway/                    # API Gateway configuration (e.g., AWS Lambda proxies, OpenAPI spec)
│   │   ├── src/
│   │   └── serverless.yml/cloudformation.yml
│   ├── user-service/                   # User Management Service (UMS)
│   │   ├── src/main/java/com/tsda/ums/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── model/
│   │   ├── build.gradle
│   │   └── Dockerfile
│   ├── order-catalog-vendor-service/   # Consolidated OCVMS
│   │   ├── src/main/java/com/tsda/ocvms/
│   │   │   ├── order/                  # Module for Order management
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   └── model/
│   │   │   ├── catalog/                # Module for Catalog/Menu management
│   │   │   ├── vendor/                 # Module for Vendor management
│   │   │   ├── reporting/              # Module for Reporting
│   │   │   └── config/
│   │   ├── build.gradle
│   │   └── Dockerfile
│   ├── search-discovery-service/       # Dedicated Search & Discovery Service (SDS)
│   │   ├── src/main/java/com/tsda/sds/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/             # Handles interaction with ES/VectorDB
│   │   │   ├── model/
│   │   │   └── kafka/                  # Kafka Consumers for indexing
│   │   ├── build.gradle
│   │   └── Dockerfile
│   ├── delivery-service/               # Delivery Service (DMS)
│   │   ├── src/main/java/com/tsda/dms/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── external/               # MapmyIndia API client
│   │   ├── build.gradle
│   │   └── Dockerfile
│   ├── payment-service/                # Payment Service (PMS)
│   │   ├── src/main/java/com/tsda/pms/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   └── gateway/                # UPI Gateway clients
│   │   ├── build.gradle
│   │   └── Dockerfile
│   └── notification-service/           # Notification Service (NOTIF_SVC)
│       ├── src/main/java/com/tsda/notifications/
│       │   ├── service/
│       │   ├── kafka/                  # Kafka Consumers for events
│       │   └── providers/              # FCM, SendGrid, Gupshup clients
│       ├── build.gradle
│       └── Dockerfile
├── shared/                             # Shared libraries, DTOs, enums, exceptions across services
│   ├── java/
│   │   └── com/tsda/shared/
│   │       ├── dto/
│   │       ├── enums/
│   │       └── exceptions/
│   ├── build.gradle
│   └── pom.xml (for shared dependencies)
├── infrastructure/                     # Terraform configurations
│   ├── environments/
│   │   ├── dev/
│   │   ├── staging/
│   │   └── prod/
│   ├── modules/
│   │   ├── vpc/
│   │   ├── rds/
│   │   ├── eks/
│   │   ├── elasticsearch/
│   │   ├── kafka/
│   │   └── redis/
│   └── main.tf
├── cicd/                               # CI/CD pipeline definitions (e.g., Jenkinsfiles)
│   ├── Jenkinsfile-ums
│   ├── Jenkinsfile-ocvms
│   ├── Jenkinsfile-sds
│   ├── Jenkinsfile-dms
│   ├── Jenkinsfile-pms
│   └── Jenkinsfile-notifications
├── build.gradle                        # Root Gradle build file (for multi-project build)
├── settings.gradle                     # Gradle settings file
└── README.md
```
