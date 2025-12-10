# Order Catalog Service (OCS)

This service manages the food catalog and ordering operations including:
- Vendor company registration and profile management
- Multi-branch onboarding with document verification
- Operating hours and availability management
- Menu item CRUD operations with versioning
- Menu caching and performance optimization
- Inventory tracking and pricing management

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Spring Boot 3.2.x
- PostgreSQL 15.x
- Redis (for caching)
- Gradle 8.x

### Environment Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yogesh1454/food-app.git
   cd tea-snacks-delivery-aggregator
   ```

2. **Configure environment variables:**
   Create `.env` file in the project root:
   ```bash
   # Database
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=tea_snacks_db
   DB_USER=tea_snacks_user
   DB_PASSWORD=tea_snacks_password

   # Redis
   REDIS_HOST=localhost
   REDIS_PORT=6379

   # Kafka
   KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   ```

3. **Start infrastructure services:**
   ```bash
   cd infrastructure/docker
   docker-compose up -d
   ```

### Running the Application

#### Option 1: Using Gradle (Development)
```bash
cd order-catalog-service
./gradlew bootRun
```

The application will start on **http://localhost:8082**

#### Option 2: Using Docker
```bash
docker-compose up order-catalog-service
```

#### Option 3: Building and Running JAR
```bash
./gradlew :order-catalog-service:build
java -jar build/libs/order-catalog-service-*.jar
```

### Verifying the Application

1. **Health Check:**
   ```bash
   curl http://localhost:8082/actuator/health
   ```

2. **API Documentation:**
   - **Swagger UI:** http://localhost:8082/swagger-ui.html
   - **OpenAPI JSON:** http://localhost:8082/v3/api-docs

## 📚 API Documentation

### Swagger UI

The application includes interactive API documentation via Swagger UI. Access it at:

**http://localhost:8082/swagger-ui.html**

Features:
- Browse all available endpoints
- View request/response schemas
- Try endpoints directly from the browser
- See parameter descriptions and validation rules

### Key Endpoints

#### Vendor Management
- `POST /api/v1/vendors` - Register a new vendor
- `GET /api/v1/vendors/{vendorId}` - Get vendor details
- `PUT /api/v1/vendors/{vendorId}` - Update vendor profile

#### Branch Management
- `POST /api/v1/branches/vendors/{vendorId}` - Create a branch
- `GET /api/v1/branches/{branchId}` - Get branch details
- `PUT /api/v1/branches/{branchId}` - Update branch
- `GET /api/v1/branches/{branchId}/availability` - Check branch availability

#### Operating Hours
- `PUT /api/v1/branches/{branchId}/operating-hours` - Set operating hours
- `GET /api/v1/branches/{branchId}/operating-hours` - Get operating hours
- `PUT /api/v1/branches/{branchId}/status` - Toggle online/offline status

#### Menu Items
- `POST /api/v1/menu-items/branches/{branchId}` - Create menu item
- `GET /api/v1/menu-items/{menuItemId}` - Get menu item
- `GET /api/v1/menu-items/branches/{branchId}` - List branch menu
- `PUT /api/v1/menu-items/{menuItemId}` - Update menu item
- `DELETE /api/v1/menu-items/{menuItemId}` - Delete menu item

#### Documents
- `POST /api/v1/branches/{branchId}/documents` - Upload document
- `GET /api/v1/branches/{branchId}/documents` - List documents

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 3.2.x
- **Language:** Java 21
- **Database:** PostgreSQL 15.x with Flyway migrations
- **Caching:** Redis with Spring Data Redis
- **Messaging:** Apache Kafka
- **API Documentation:** Springdoc OpenAPI 3.0
- **ORM:** Hibernate with Spring Data JPA
- **Build Tool:** Gradle

### Database Schema
- `vendors` - Vendor company information
- `vendor_branches` - Branch locations and details
- `branch_documents` - Document verification
- `menu_items` - Menu items per branch

### Key Features
- ✅ Multi-branch vendor architecture
- ✅ JSONB support for flexible data (preferences, images, metadata)
- ✅ Redis caching with version-based keys
- ✅ Automatic cache invalidation on changes
- ✅ Operating hours management with timezone support
- ✅ Menu versioning and soft deletes
- ✅ Comprehensive validation and error handling
- ✅ OpenAPI 3.0 documentation

## 🧪 Testing

Run tests with:
```bash
./gradlew :order-catalog-service:test
```

Build without tests:
```bash
./gradlew :order-catalog-service:build -x test
```

## 📊 Performance

- Menu retrieval (cached): < 50ms
- Menu retrieval (uncached): < 200ms
- Cache hit rate: > 80%
- Database query optimization with indexes

## 🔧 Configuration

Key configuration properties in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tea_snacks_db
    username: tea_snacks_user
    password: tea_snacks_password
  
  redis:
    host: localhost
    port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092

springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

## 📝 Logging

Logs are configured via SLF4J and output to console. Set log levels in `application.yml`:
```yaml
logging:
  level:
    com.teadelivery: INFO
    org.springframework: WARN
```

## 🚨 Troubleshooting

### Application fails to start
- Ensure PostgreSQL is running: `docker-compose up postgres`
- Ensure Redis is running: `docker-compose up redis`
- Check database credentials in `.env` file

### Swagger UI not loading
- Verify application is running on port 8082
- Clear browser cache and try http://localhost:8082/swagger-ui.html
- Check logs for any initialization errors

### Database connection errors
- Verify PostgreSQL container is healthy: `docker ps`
- Check database credentials match in `application.yml`
- Run migrations manually if needed: `./gradlew flywayMigrate`

## 📞 Support

For issues or questions:
1. Check the Swagger UI documentation at http://localhost:8082/swagger-ui.html
2. Review application logs for error details
3. Check database migrations in `src/main/resources/db/migration/`

## 📄 License

Apache 2.0
