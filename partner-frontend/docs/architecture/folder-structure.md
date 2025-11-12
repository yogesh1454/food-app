## Spring Boot Microservice Folder and File Structure Best Practices

A well-organized folder and file structure is crucial for the maintainability, scalability, and readability of Spring Boot microservices, especially as projects grow. This suggested structure aims for clarity, separation of concerns, and ease of navigation.

-----

### Core Principles for Microservice Structure

1.  **Package by Feature/Domain:** Instead of packaging by layer (e.g., `com.example.app.controller`, `com.example.app.service`), group related components by their business domain or feature. This makes it easier to understand and work on a specific part of the service.
2.  **Flat vs. Nested:** Avoid overly deep nesting. A reasonable depth (3-5 levels) is generally good.
3.  **Clear Naming:** Use clear, consistent, and descriptive names for packages and files.
4.  **Configuration Centralization:** Keep configuration files organized and easily accessible.
5.  **Test Alongside Code:** Place tests in a parallel structure that mirrors the main source code.

-----

### Recommended Folder and File Structure

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── <microservice-name>
│   │   │               ├── Application.java               # Main Spring Boot application class
│   │   │               │
│   │   │               ├── config                         # Global application configuration, security, web config
│   │   │               │   ├── WebConfig.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── ObjectMapperConfig.java    # Jackson ObjectMapper customizations
│   │   │               │
│   │   │               ├── exception                      # Custom exceptions and global exception handler
│   │   │               │   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   │   │               │   ├── NotFoundException.java
│   │   │               │   └── CustomValidationException.java
│   │   │               │
│   │   │               ├── common                         # Utilities, constants, common DTOs/enums shared across features
│   │   │               │   ├── UtilService.java
│   │   │               │   └── Constants.java
│   │   │               │
│   │   │               ├── auth                           # Authentication/Authorization related components (if applicable)
│   │   │               │   ├── UserDetailsService.java
│   │   │               │   └── JwtRequestFilter.java
│   │   │               │
│   │   │               ├── product                      # Feature/Domain 1: Product Management
│   │   │               │   ├── api                        # Public-facing interfaces (e.g., Feign clients) for this domain
│   │   │               │   │   └── ProductClient.java       # Feign client for external product service
│   │   │               │   │
│   │   │               │   ├── controller                 # REST controllers for product
│   │   │               │   │   └── ProductController.java
│   │   │               │   │
│   │   │               │   ├── service                    # Business logic for product
│   │   │               │   │   └── ProductService.java
│   │   │               │   │
│   │   │               │   ├── repository                 # Data access for product
│   │   │               │   │   └── ProductRepository.java
│   │   │               │   │
│   │   │               │   ├── model                      # JPA entities, domain models (e.g., Product.java)
│   │   │               │   │   └── Product.java
│   │   │               │   │
│   │   │               │   ├── dto                        # Data Transfer Objects for product (request/response)
│   │   │               │   │   ├── ProductRequest.java
│   │   │               │   │   └── ProductResponse.java
│   │   │               │   │
│   │   │               │   └── mapper                     # MapStruct mappers for product
│   │   │               │       └── ProductMapper.java
│   │   │               │
│   │   │               └── order                        # Feature/Domain 2: Order Management
│   │   │                   ├── controller
│   │   │                   │   └── OrderController.java
│   │   │                   │
│   │   │                   ├── service
│   │   │                   │   └── OrderService.java
│   │   │                   │
│   │   │                   ├── repository
│   │   │                   │   └── OrderRepository.java
│   │   │                   │
│   │   │                   ├── model
│   │   │                   │   └── Order.java
│   │   │                   │
│   │   │                   └── dto
│   │   │                       ├── OrderRequest.java
│   │   │                       └── OrderResponse.java
│   │   │
│   │   └── resources
│   │       ├── application.yml                    # Main application properties
│   │       ├── application-dev.yml                # Development specific properties
│   │       ├── application-prod.yml               # Production specific properties
│   │       ├── logback-spring.xml                 # Logging configuration
│   │       └── db
│   │           └── migration                      # Database migration scripts (e.g., Flyway/Liquibase)
│   │               ├── V1__create_tables.sql
│   │               └── V2__add_data.sql
│   │
│   └── test
│       ├── java
│       │   └── com
│       │       └── example
│       │           └── <microservice-name>
│       │               ├── ApplicationTests.java
│       │               ├── config
│       │               │   └── TestConfig.java
│       │               │
│       │               ├── product
│       │               │   ├── controller
│       │               │   │   └── ProductControllerTest.java
│       │               │   │
│       │               │   ├── service
│   	│               │   │   └── ProductServiceTest.java
│   	│               │   │
│   	│               │   └── repository
│   	│               │       └── ProductRepositoryTest.java
│       │               │
│       │               └── order
│       │                   └── service
│       │                       └── OrderServiceTest.java
│       │
│       └── resources
│           └── application-test.yml               # Test specific properties
│
└── pom.xml                                      # Maven build file (or build.gradle for Gradle)
```

-----

### Explanation of Sections

  * **`src/main/java/com/example/<microservice-name>`**: This is the root package for your microservice.

      * **`Application.java`**: The main Spring Boot class with the `@SpringBootApplication` annotation.
      * **`config`**:
          * Contains classes for application-wide configuration, such as `WebMvcConfigurer` implementations, Spring Security configuration (`WebSecurityConfigurerAdapter` or security filter chains), and any custom bean definitions that are broadly applicable (e.g., custom `ObjectMapper` for Jackson).
      * **`exception`**:
          * Holds custom exception classes specific to your application and the global exception handler (`@ControllerAdvice`) to ensure consistent error responses across all REST endpoints.
      * **`common`**:
          * For utility classes, helper methods, common enums, or DTOs that are used across multiple domains/features within the same microservice.
      * **`auth`**:
          * If your microservice handles its own authentication/authorization (e.g., JWT filters, custom `UserDetailsService`), this is the place for those components.
      * **`<feature-name>` (e.g., `product`, `order`)**:
          * This is where the "package by feature" principle comes into play. Each top-level package here represents a distinct business domain or feature within your microservice.
          * **`api`**: Used for defining API interfaces consumed by other services (e.g., Feign clients). This helps separate the client-side view of an external API.
          * **`controller`**: Contains the REST controllers that expose the API endpoints for this specific feature.
          * **`service`**: Holds the core business logic for the feature. Services often orchestrate calls to repositories and other services.
          * **`repository`**: Contains data access interfaces (e.g., Spring Data JPA repositories) for interacting with the database.
          * **`model`**: Houses your JPA entities or core domain model classes. Keep these clean and free of business logic where possible (anemic domain model vs. rich domain model is a choice here).
          * **`dto`**: Data Transfer Objects used for request and response payloads between the API layer and the service layer. Often good candidates for Java Records.
          * **`mapper`**: Dedicated package for MapStruct mapper interfaces, responsible for converting between DTOs and models/entities.

  * **`src/main/resources`**:

      * **`application.yml` (or `.properties`)**: The main configuration file. Use environment-specific files (`application-dev.yml`, `application-prod.yml`) for different environments.
      * **`logback-spring.xml`**: Configuration for the logging framework.
      * **`db/migration`**: If using database migration tools like Flyway or Liquibase, this is the standard location for your SQL migration scripts.

  * **`src/test/java/com/example/<microservice-name>`**:

      * Mirrors the `main/java` structure for tests. This makes it easy to find the tests for a specific class or feature.
      * **`ApplicationTests.java`**: Basic integration test for the Spring Boot application context.
      * **`config`**: Test-specific configurations (e.g., H2 database for integration tests).
      * Tests for controllers, services, and repositories are placed within their respective feature folders.

  * **`src/test/resources`**:

      * **`application-test.yml`**: Test-specific properties, often overriding main properties for test environments (e.g., pointing to an in-memory database).

  * **`pom.xml` (Maven) or `build.gradle` (Gradle)**:

      * The project's build definition file, managing dependencies, plugins, and build lifecycle.

-----