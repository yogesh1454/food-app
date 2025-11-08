# REST API Standards & Best Practices

**Version:** 1.0.0  
**Last Updated:** November 8, 2024  
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
11. [Examples](#examples)

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

### 3. Human-Readable IDs
- Use `BIGSERIAL` (Long) for primary keys where volume is finite
- Use `UUID` for distributed entities or high-volume data
- Examples:
  - Vendors, Branches: `Long` (human-readable: 1, 2, 3...)
  - Orders, Transactions: `UUID` (distributed, high-volume)

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

## Checklist for New APIs

- [ ] RESTful URL structure
- [ ] Appropriate HTTP methods
- [ ] Human-readable IDs where applicable
- [ ] Input validation with `@Valid`
- [ ] No `ResponseEntity` wrappers
- [ ] `@ResponseStatus` for non-200 success codes
- [ ] Comprehensive `@Operation` documentation
- [ ] All `@ApiResponses` documented (200, 201, 400, 404, 409, 500)
- [ ] Request/response examples in Swagger
- [ ] Parameter descriptions
- [ ] DTO field documentation with `@Schema`
- [ ] Global exception handler configured
- [ ] Consistent error response format
- [ ] Logging at appropriate levels
- [ ] Unit tests for controllers
- [ ] Integration tests for endpoints

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
