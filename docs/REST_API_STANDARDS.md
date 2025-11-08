# REST API Standards & Best Practices

**Version:** 2.0.0  
**Last Updated:** November 8, 2025  
**Applies To:** All microservices in Tea Snacks Delivery Aggregator

---

## Table of Contents
1. [Overview](#overview)
2. [API Design Principles](#api-design-principles)
3. [URL Structure](#url-structure)
4. [HTTP Methods](#http-methods)
5. [Request/Response Format](#requestresponse-format)
6. [Status Codes](#status-codes)
7. [Error Handling](#error-handling)
8. [Validation](#validation)
9. [Documentation (Swagger/OpenAPI)](#documentation-swaggeropenapi)
10. [Controller Standards](#controller-standards)
11. [Database & Entity Standards](#database--entity-standards)
12. [Testing Standards](#testing-standards)
13. [Examples](#examples)

---

## Overview

This document defines the REST API standards for all microservices in the Tea Snacks Delivery Aggregator platform. Following these standards ensures consistency, maintainability, and excellent developer experience.

### Key Principles
- **Self-Documenting**: APIs should be discoverable and understandable through Swagger UI
- **Consistent**: Same patterns across all services
- **Simple**: Clean controller code without boilerplate
- **Robust**: Comprehensive error handling
- **Validated**: Input validation at API boundary

---

## API Design Principles

### 1. RESTful Resource Modeling
- Use nouns for resources, not verbs
- Use plural nouns for collections
- Nest resources to show relationships

```
✅ Good:
GET    /api/v1/vendors/{vendorId}
POST   /api/v1/vendors/{vendorId}/branches
GET    /api/v1/branches/{branchId}

❌ Bad:
GET    /api/v1/getVendor/{id}
POST   /api/v1/createBranch
GET    /api/v1/vendor/branch/{id}
```

### 2. API Versioning
- Use URL path versioning: `/api/v1/`, `/api/v2/`
- Version at the API level, not resource level
- Maintain backward compatibility within major versions

### 3. ID Strategy & Type Consistency

**CRITICAL: Entity ID types MUST match database schema**

#### Primary Key Guidelines
- **Use `BIGSERIAL` (Long)** for most entities:
  - Vendors, Branches, Menu Items, Documents
  - Better performance, human-readable, easier debugging
  - JPA: `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  
- **Use `UUID`** only for:
  - User identifiers (cross-service references)
  - Distributed entities requiring global uniqueness
  - High-volume transactional data (orders, payments)
  - External system integrations

#### Type Consistency Rules
1. **Entity ↔ Database**: ID type MUST match migration script
   ```java
   // Migration: BIGSERIAL → Entity: Long
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long menuItemId;
   ```

2. **Entity ↔ DTO**: Response DTOs MUST match entity ID type
   ```java
   // Entity has Long → DTO must have Long
   public class MenuItemResponse {
       private Long menuItemId;  // ✅ Matches entity
   }
   ```

3. **Repository**: Generic type MUST match entity ID
   ```java
   // Entity ID is Long → Repository uses Long
   public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
       Optional<MenuItem> findByMenuItemIdAndIsDeletedFalse(Long menuItemId);
   }
   ```

4. **Service Methods**: Parameter types MUST match entity ID
   ```java
   // Entity ID is Long → Service uses Long
   public MenuItemResponse getMenuItem(Long menuItemId) { }
   ```

5. **Controller**: Path variables MUST match entity ID type
   ```java
   // Entity ID is Long → Controller uses Long
   @GetMapping("/{menuItemId}")
   public MenuItemResponse get(@PathVariable Long menuItemId) { }
   ```

#### Common Pitfalls to Avoid
❌ **DON'T mix UUID and Long**
```java
// Migration uses BIGSERIAL but entity uses UUID
@Id
@GeneratedValue(strategy = GenerationType.UUID)  // ❌ WRONG!
private UUID menuItemId;
```

❌ **DON'T use mismatched repository types**
```java
// Entity has Long but repository uses UUID
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> { }  // ❌ WRONG!
```

✅ **DO maintain consistency across all layers**
```
Migration (BIGSERIAL) → Entity (Long) → DTO (Long) → Repository (Long) → Service (Long) → Controller (Long)
```

---

## URL Structure

### Base URL Pattern
```
{protocol}://{host}:{port}/api/{version}/{resource}
```

### Examples
```
http://localhost:8082/api/v1/vendors
http://localhost:8082/api/v1/vendors/123
http://localhost:8082/api/v1/vendors/123/branches
http://localhost:8082/api/v1/branches/456
```

### Path Parameters
- Use for resource identification
- Always required
- Use descriptive names

```java
@GetMapping("/vendors/{vendorId}")
public VendorResponse getVendor(@PathVariable Long vendorId) { }
```

### Query Parameters
- Use for filtering, sorting, pagination
- Always optional
- Provide sensible defaults

```java
@GetMapping("/branches")
public List<BranchResponse> getBranches(
    @RequestParam(required = false) String city,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) { }
```

---

## HTTP Methods

| Method | Purpose | Idempotent | Safe | Request Body | Response Body |
|--------|---------|------------|------|--------------|---------------|
| GET    | Retrieve resource(s) | ✅ | ✅ | ❌ | ✅ |
| POST   | Create resource | ❌ | ❌ | ✅ | ✅ |
| PUT    | Update entire resource | ✅ | ❌ | ✅ | ✅ |
| PATCH  | Partial update | ❌ | ❌ | ✅ | ✅ |
| DELETE | Remove resource | ✅ | ❌ | ❌ | ❌/✅ |

### Usage Guidelines

#### POST - Create
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public VendorResponse createVendor(@Valid @RequestBody VendorRequest request) {
    return vendorService.create(request);
}
```

#### GET - Retrieve
```java
@GetMapping("/{id}")
public VendorResponse getVendor(@PathVariable Long id) {
    return vendorService.getById(id);
}
```

#### PUT - Update
```java
@PutMapping("/{id}")
public VendorResponse updateVendor(
    @PathVariable Long id,
    @Valid @RequestBody VendorUpdateRequest request
) {
    return vendorService.update(id, request);
}
```

#### DELETE - Remove
```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteVendor(@PathVariable Long id) {
    vendorService.delete(id);
}
```

---

## Request/Response Format

### Content Type
- **Request**: `application/json`
- **Response**: `application/json`
- **Character Encoding**: UTF-8

### Request Body Structure
```json
{
  "companyName": "Chai Express Pvt Ltd",
  "brandName": "Chai Express",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "9876543210"
}
```

### Response Body Structure
```json
{
  "vendorId": 1,
  "companyName": "Chai Express Pvt Ltd",
  "brandName": "Chai Express",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "9876543210",
  "createdAt": "2024-11-08T12:30:45",
  "updatedAt": "2024-11-08T12:30:45"
}
```

### Naming Conventions
- Use `camelCase` for JSON properties
- Use descriptive names
- Avoid abbreviations unless widely understood
- Be consistent across all APIs

---

## Status Codes

### Success Codes
| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 202 | Accepted | Async operation accepted |
| 204 | No Content | Successful DELETE |

### Client Error Codes
| Code | Meaning | Usage |
|------|---------|-------|
| 400 | Bad Request | Validation failure, malformed request |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource already exists, constraint violation |
| 422 | Unprocessable Entity | Business logic validation failure |
| 429 | Too Many Requests | Rate limit exceeded |

### Server Error Codes
| Code | Meaning | Usage |
|------|---------|-------|
| 500 | Internal Server Error | Unexpected server error |
| 502 | Bad Gateway | Upstream service error |
| 503 | Service Unavailable | Service temporarily down |
| 504 | Gateway Timeout | Upstream service timeout |

---

## Error Handling

### Global Exception Handler
All services **MUST** implement a `@RestControllerAdvice` for consistent error handling.

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        
        return ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("Bad Request")
            .message("Validation failed")
            .validationErrors(errors)
            .build();
    }
}
```

### Standard Error Response
```json
{
  "timestamp": "2024-11-08T12:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Vendor not found",
  "path": "/api/v1/vendors/999"
}
```

### Validation Error Response
```json
{
  "timestamp": "2024-11-08T12:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/vendors",
  "validationErrors": {
    "companyEmail": "must be a well-formed email address",
    "companyName": "must not be blank"
  }
}
```

---

## Validation

### Input Validation
Use Jakarta Bean Validation annotations on DTOs:

```java
public class VendorRegistrationRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String companyEmail;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String companyPhone;
    
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panNumber;  // Optional
}
```

### Controller Validation
Always use `@Valid` annotation:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public VendorResponse create(@Valid @RequestBody VendorRequest request) {
    return service.create(request);
}
```

---

## Documentation (Swagger/OpenAPI)

### Configuration
Every service must have OpenAPI configuration:

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Service Name API")
                .version("1.0.0")
                .description("Service description")
                .contact(new Contact()
                    .name("Team Name")
                    .email("team@example.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.example.com").description("Production")
            ));
    }
}
```

### Controller Documentation
Use comprehensive Swagger annotations:

```java
@RestController
@RequestMapping("/api/v1/vendors")
@Tag(name = "Vendor Management", description = "APIs for vendor operations")
public class VendorController {
    
    @Operation(
        summary = "Register a new vendor",
        description = "Creates a new vendor account with company details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Vendor successfully registered",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = VendorResponse.class),
                examples = @ExampleObject(
                    value = "{\"vendorId\": 1, \"companyName\": \"Chai Express\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                schema = @Schema(implementation = ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Vendor already exists",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse registerVendor(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Vendor registration details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = VendorRegistrationRequest.class),
                    examples = @ExampleObject(
                        value = "{\"companyName\": \"Chai Express\", \"companyEmail\": \"contact@chai.com\"}"
                    )
                )
            )
            @Valid @RequestBody VendorRegistrationRequest request) {
        return vendorService.registerVendor(request);
    }
}
```

### DTO Documentation
Document all DTO fields:

```java
@Schema(description = "Vendor registration request")
public class VendorRegistrationRequest {
    
    @Schema(description = "Company legal name", example = "Chai Express Pvt Ltd", required = true)
    @NotBlank
    private String companyName;
    
    @Schema(description = "Brand/display name", example = "Chai Express")
    private String brandName;
    
    @Schema(description = "Company email address", example = "contact@chaiexpress.com", required = true)
    @NotBlank
    @Email
    private String companyEmail;
}
```

---

## Controller Standards

### DO ✅

1. **Remove ResponseEntity Wrappers**
   ```java
   // Use @ResponseStatus for status codes
   @PostMapping
   @ResponseStatus(HttpStatus.CREATED)
   public VendorResponse create(@Valid @RequestBody VendorRequest request) {
       return service.create(request);
   }
   ```

2. **Use Global Exception Handling**
   ```java
   // Let @RestControllerAdvice handle exceptions
   @GetMapping("/{id}")
   public VendorResponse get(@PathVariable Long id) {
       return service.getById(id);  // Throws VendorNotFoundException
   }
   ```

3. **Comprehensive Swagger Documentation**
   ```java
   @Operation(summary = "...", description = "...")
   @ApiResponses(value = { ... })
   ```

4. **Validate All Inputs**
   ```java
   @PostMapping
   public Response create(@Valid @RequestBody Request request) { }
   ```

5. **Use Descriptive Parameter Names**
   ```java
   @Parameter(description = "Vendor ID", example = "1", required = true)
   @PathVariable Long vendorId
   ```

### DON'T ❌

1. **Don't Use ResponseEntity**
   ```java
   // ❌ Bad
   public ResponseEntity<VendorResponse> get(@PathVariable Long id) {
       return ResponseEntity.ok(service.get(id));
   }
   
   // ✅ Good
   public VendorResponse get(@PathVariable Long id) {
       return service.get(id);
   }
   ```

2. **Don't Handle Exceptions in Controllers**
   ```java
   // ❌ Bad
   try {
       return service.get(id);
   } catch (Exception e) {
       return ResponseEntity.status(500).body(null);
   }
   
   // ✅ Good - Let GlobalExceptionHandler handle it
   return service.get(id);
   ```

3. **Don't Skip Validation**
   ```java
   // ❌ Bad
   public Response create(@RequestBody Request request) { }
   
   // ✅ Good
   public Response create(@Valid @RequestBody Request request) { }
   ```

4. **Don't Use Generic Names**
   ```java
   // ❌ Bad
   @GetMapping("/{id}")
   public Response get(@PathVariable Long id) { }
   
   // ✅ Good
   @GetMapping("/{vendorId}")
   public VendorResponse getVendor(@PathVariable Long vendorId) { }
   ```

---

## Examples

### Complete Controller Example

```java
@RestController
@RequestMapping("/api/v1/vendors")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vendor Management", description = "Vendor CRUD operations")
public class VendorController {
    
    private final VendorService vendorService;
    
    @Operation(summary = "Create vendor", description = "Register a new vendor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = VendorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse create(@Valid @RequestBody VendorRequest request) {
        log.info("Creating vendor: {}", request.getCompanyName());
        return vendorService.create(request);
    }
    
    @Operation(summary = "Get vendor", description = "Retrieve vendor by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found",
            content = @Content(schema = @Schema(implementation = VendorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{vendorId}")
    public VendorResponse get(
            @Parameter(description = "Vendor ID", example = "1")
            @PathVariable Long vendorId) {
        log.info("Getting vendor: {}", vendorId);
        return vendorService.getById(vendorId);
    }
    
    @Operation(summary = "Update vendor", description = "Update vendor details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(schema = @Schema(implementation = VendorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{vendorId}")
    public VendorResponse update(
            @Parameter(description = "Vendor ID", example = "1")
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        log.info("Updating vendor: {}", vendorId);
        return vendorService.update(vendorId, request);
    }
    
    @Operation(summary = "Delete vendor", description = "Remove vendor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{vendorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Vendor ID", example = "1")
            @PathVariable Long vendorId) {
        log.info("Deleting vendor: {}", vendorId);
        vendorService.delete(vendorId);
    }
}
```

---

## Database & Entity Standards

### Flyway Migration Best Practices

#### 1. Flyway as Single Source of Truth
- **NEVER** modify migration files after they've been applied
- Use Flyway for ALL schema changes (not Hibernate DDL)
- Set `spring.jpa.hibernate.ddl-auto=validate` (NOT `update`)

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Only validate, don't modify schema
  flyway:
    baseline-on-migrate: true
```

#### 2. Migration File Naming
```
V{version}__{description}.sql

Examples:
V1__Create_vendors_table.sql
V2__Create_vendor_branches_table.sql
V3__Add_menu_version_to_branches.sql
```

#### 3. ID Column Standards
```sql
-- ✅ Use BIGSERIAL for auto-increment Long IDs
CREATE TABLE menu_items (
    menu_item_id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id),
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

-- ✅ Use UUID only when necessary
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id)
);
```

#### 4. Foreign Key Consistency
- Foreign keys MUST match referenced column type
- Use `BIGINT` for references to `BIGSERIAL` columns
- Use `UUID` for references to `UUID` columns

```sql
-- ✅ Correct: branch_id references BIGSERIAL
CREATE TABLE menu_items (
    menu_item_id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES vendor_branches(branch_id)
);

-- ❌ Wrong: Type mismatch
CREATE TABLE menu_items (
    menu_item_id BIGSERIAL PRIMARY KEY,
    branch_id UUID NOT NULL  -- ❌ branch_id is BIGINT in vendor_branches!
);
```

### JPA Entity Best Practices

#### 1. Entity ID Mapping
```java
// ✅ For BIGSERIAL columns
@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_id")
    private Long menuItemId;
}

// ✅ For UUID columns (when needed)
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;
}
```

#### 2. Foreign Key Mapping
```java
@Entity
public class MenuItem {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private VendorBranch branch;  // ✅ Reference entity, not ID
}
```

#### 3. JSONB Column Mapping
```java
@Entity
public class MenuItem {
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> images;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}

// Add dependency: io.hypersistence:hypersistence-utils-hibernate-63
```

### Database Recreation Script

Maintain a script to recreate databases for clean testing:

```bash
#!/bin/bash
# infrastructure/scripts/recreate-databases.sh

CONTAINER_NAME="tea-snacks-postgres"
DB_USER="tea_snacks_user"
MAIN_DB="tea_snacks_db"

databases=(
    "order_catalog_db"
    "order_catalog_test_db"
    "user_management_db"
    # ... other databases
)

for db in "${databases[@]}"; do
    docker exec $CONTAINER_NAME psql -U $DB_USER -d $MAIN_DB -c "DROP DATABASE IF EXISTS $db;"
    docker exec $CONTAINER_NAME psql -U $DB_USER -d $MAIN_DB -c "CREATE DATABASE $db OWNER $DB_USER;"
done
```

---

## Testing Standards

### E2E Test Configuration

#### 1. Dedicated Test Database
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_catalog_test_db
    username: tea_snacks_user
    password: tea_snacks_password
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Same as production
```

#### 2. Test Class Configuration
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // ✅ Share state across tests
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // ✅ Control execution order
public class VendorBranchOnboardingE2ETest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private Long vendorId;  // ✅ Instance variable for state
    private Long branchId;
    
    @BeforeAll
    public void setup() {
        // ✅ Clean database before all tests
        jdbcTemplate.execute("TRUNCATE TABLE branch_documents CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE menu_items CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE vendor_branches CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE vendors CASCADE");
    }
}
```

#### 3. Test Execution Order
```java
@Test
@Order(1)
@DisplayName("UC-V001: Register Vendor - Success")
public void testRegisterVendor_Success() throws Exception {
    // Test creates vendor and stores vendorId
    vendorId = extractedVendorId;
}

@Test
@Order(2)
@DisplayName("UC-B001: Create Branch - Success")
public void testCreateBranch_Success() throws Exception {
    // Test uses vendorId from previous test
    mockMvc.perform(post("/api/v1/vendors/" + vendorId + "/branches")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
}
```

#### 4. User Context Simulation
```java
// Controller accepts optional header for testing
@PostMapping
public VendorResponse registerVendor(
    @Valid @RequestBody VendorRegistrationRequest request,
    @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
    
    UUID userId = userIdHeader != null 
        ? UUID.fromString(userIdHeader)
        : UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    
    return vendorService.registerVendor(request, userId);
}

// Test sends different user IDs
mockMvc.perform(post("/api/v1/vendors")
    .header("X-User-Id", "660e8400-e29b-41d4-a716-446655440001")
    .contentType(MediaType.APPLICATION_JSON)
    .content(requestJson))
    .andExpect(status().isCreated());
```

### Test Assertions Best Practices

```java
// ✅ Assert response structure
.andExpect(status().isCreated())
.andExpect(jsonPath("$.vendorId").exists())
.andExpect(jsonPath("$.companyName").value("Chai Express"))
.andExpect(jsonPath("$.onboardingStatus").value("PENDING"));

// ✅ Extract IDs for subsequent tests
MvcResult result = mockMvc.perform(post(...))
    .andExpect(status().isCreated())
    .andReturn();

String responseString = result.getResponse().getContentAsString();
JsonNode rootNode = objectMapper.readTree(responseString);
vendorId = rootNode.path("vendorId").asLong();

// ✅ Test error scenarios
mockMvc.perform(post("/api/v1/vendors")
    .contentType(MediaType.APPLICATION_JSON)
    .content(invalidRequestJson))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.validationErrors.companyEmail").exists());
```

---

## Checklist for New APIs

### API Design
- [ ] RESTful URL structure
- [ ] Appropriate HTTP methods
- [ ] Human-readable IDs where applicable (Long for most entities)
- [ ] Input validation with `@Valid`
- [ ] No `ResponseEntity` wrappers
- [ ] `@ResponseStatus` for non-200 success codes

### Documentation
- [ ] Comprehensive `@Operation` documentation
- [ ] All `@ApiResponses` documented (200, 201, 400, 404, 409, 500)
- [ ] Request/response examples in Swagger
- [ ] Parameter descriptions with `@Parameter`
- [ ] DTO field documentation with `@Schema`

### Error Handling
- [ ] Global exception handler configured
- [ ] Consistent error response format
- [ ] Validation error responses with field-level details
- [ ] Logging at appropriate levels

### Database & Entities
- [ ] **ID type consistency**: Migration (BIGSERIAL) → Entity (Long) → DTO (Long) → Repository (Long) → Service (Long) → Controller (Long)
- [ ] Flyway migration files created and tested
- [ ] `hibernate.ddl-auto=validate` configured
- [ ] Entity annotations match database schema
- [ ] Foreign key types match referenced columns
- [ ] JSONB columns properly mapped with `@Type(JsonBinaryType.class)`

### Testing
- [ ] Dedicated test database configured
- [ ] E2E tests with `@TestInstance(PER_CLASS)` for state persistence
- [ ] Database cleanup in `@BeforeAll`
- [ ] Test execution order with `@Order` annotations
- [ ] User context simulation (X-User-Id header)
- [ ] All success scenarios tested
- [ ] All error scenarios tested (validation, not found, conflict)
- [ ] 100% test pass rate before commit

---

## Swagger UI Access

After starting the service:
- **Swagger UI**: `http://localhost:{port}/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:{port}/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:{port}/v3/api-docs.yaml`

---

## References

- [REST API Tutorial](https://restfulapi.net/)
- [HTTP Status Codes](https://httpstatuses.com/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Spring REST Docs](https://spring.io/guides/gs/rest-service/)
- [Jakarta Bean Validation](https://beanvalidation.org/)

---

**Document Owner**: Architecture Team  
**Review Cycle**: Quarterly  
**Next Review**: February 2025
