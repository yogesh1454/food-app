# 13\. Coding Standards

These standards are MANDATORY for AI agents.

## Core Standards

  - **Languages & Runtimes:** Java , JVM.
  - **Style & Linting:** Google Java Format will be enforced via Maven/Gradle plugin and CI. SpotBugs for static analysis.
  - **Incremental Story Planning:**The restructuring follows your guidance perfectly - small, incremental stories that each deliver working functionality. Each story is now focused on a single feature and can be developed, tested, and deployed independently.
  - **Test Organization:** Tests must reside in `src/test/java` and mirror the package structure of the source code. Test class names should end with `Test` or `IT` (for Integration Tests).

## Critical Rules

  - **Logging:** Never use `System.out.println()` or `e.printStackTrace()`. Always use the configured SLF4J logger.
  - **Error Handling:** All external API calls and critical business logic operations must be wrapped in `try-catch` blocks that handle expected exceptions and log unexpected ones at `ERROR` level.
  - **API Responses:** All REST API responses from backend services must adhere to the OpenAPI specification and use the standard success/error response formats defined.
  - **Database Access:** All database interactions must go through the Repository pattern (Spring Data JPA interfaces), never direct JDBC or `EntityManager` usage in service logic.
  - **Configuration:** Sensitive information (API keys, database credentials) must *never* be hardcoded. Always use Spring Boot's externalized configuration and environment variables/secrets management.
  - **Immutability:** Prefer immutable objects for DTOs and value objects to avoid unexpected side effects.
  - **Asynchronous Communication:** All inter-service communication via Kafka should use strongly typed DTOs and adhere to predefined Avro/JSON Schema for messages.
  - **Modularity:** Within OCVMS, ensure clear separation between `order`, `catalog`, `vendor`, and `reporting` modules. Avoid direct coupling between business logic of these modules.
  - **Controller:** 
    - Use Clear, RESTful Naming and HTTP Methods
    - Keep Controller Thin (Delegate to Services): Your controller should:
      - Accept requests
      - Validate inputs
      - Call service layer
      - Return responses
      - Don't put business logic in controllers.
    - Use DTOs Instead of Entities in Controllers
    - Validate Input Using @Valid and @Validated
    - Handle Errors Gracefully with @ControllerAdvice
    - Return Standard HTTP Response Codes
    - Use @Sf4j for logging, Not System.out.println()
    - Secure Your Endpoints: Use Spring Security with annotations like: @PreAuthorize("hasRole('ADMIN')")
    - Use Pagination for Lists
- **Use Library:** Use Library whereever needed
  - Use lombok library for builder, getter, data, setter
  - Use @Sl4f library for logging
  - Use OpenAPI for API documentation to be shown on swagger UI
  - Use MapStruct for Mappers
