# E2E Integration Test Guide

**Version:** 1.0.0  
**Last Updated:** November 8, 2024  
**Service:** Order Catalog Service

---

## Overview

This document describes the End-to-End (E2E) integration tests for the Vendor & Branch Onboarding flow. The tests validate the complete workflow from vendor registration to branch operations.

---

## Test Suite: VendorBranchOnboardingE2ETest

**Location:** `src/test/java/com/teadelivery/ordercatalog/vendor/VendorBranchOnboardingE2ETest.java`  
**Based On:** `docs/use-cases/VENDOR_BRANCH_ONBOARDING_USECASES.md`  
**Test Framework:** JUnit 5 + Spring Boot Test + MockMvc  
**Total Test Cases:** 20

---

## Test Coverage

### Vendor Onboarding Tests (8 tests)

| Test # | Use Case | Test Name | Expected Result |
|--------|----------|-----------|-----------------|
| 1 | UC-V001 | `testRegisterNewVendor_Success` | 201 Created |
| 2 | UC-V001 | `testRegisterNewVendor_DuplicateEmail` | 409 Conflict |
| 3 | UC-V001 | `testRegisterNewVendor_InvalidPAN` | 400 Bad Request |
| 4 | UC-V001 | `testRegisterNewVendor_InvalidPhone` | 400 Bad Request |
| 5 | UC-V002 | `testGetVendor_Success` | 200 OK |
| 6 | UC-V002 | `testGetVendor_NotFound` | 404 Not Found |
| 7 | UC-V003 | `testUpdateVendor_Success` | 200 OK |
| 8 | UC-V004 | `testUploadVendorLogo_Success` | 200 OK |

### Branch Onboarding Tests (8 tests)

| Test # | Use Case | Test Name | Expected Result |
|--------|----------|-----------|-----------------|
| 9 | UC-B001 | `testCreateBranch_Success` | 201 Created |
| 10 | UC-B001 | `testCreateBranch_VendorNotFound` | 404 Not Found |
| 11 | UC-B002 | `testGetBranch_Success` | 200 OK |
| 12 | UC-B003 | `testUpdateBranch_Success` | 200 OK |
| 13 | UC-B004 | `testUploadBranchImage_Success` | 200 OK |
| 14 | UC-B005 | `testUploadBranchDocument_Success` | 200 OK |
| 15 | UC-B006 | `testToggleBranchStatus_Open` | 200 OK |
| 16 | UC-B006 | `testToggleBranchStatus_Close` | 200 OK |

### Error Handling Tests (3 tests)

| Test # | Scenario | Test Name | Expected Result |
|--------|----------|-----------|-----------------|
| 17 | Missing branchId | `testUpload_MissingBranchId` | 400 Bad Request |
| 18 | Invalid target | `testUpload_InvalidTarget` | 400 Bad Request |
| 19 | Missing fields | `testRegisterVendor_MissingRequiredFields` | 400 Bad Request |

### Complete Flow Test (1 test)

| Test # | Scenario | Test Name | Expected Result |
|--------|----------|-----------|-----------------|
| 20 | Full workflow | `testCompleteOnboardingFlow` | Summary validation |

---

## Running the Tests

### Run All E2E Tests
```bash
cd tea-snacks-delivery-aggregator/order-catalog-service
../../gradlew test --tests VendorBranchOnboardingE2ETest
```

### Run Specific Test
```bash
../../gradlew test --tests VendorBranchOnboardingE2ETest.testRegisterNewVendor_Success
```

### Run with Detailed Output
```bash
../../gradlew test --tests VendorBranchOnboardingE2ETest --info
```

### Run in Continuous Mode
```bash
../../gradlew test --tests VendorBranchOnboardingE2ETest --continuous
```

---

## Test Execution Flow

The tests execute in a specific order using `@Order` annotation:

```
1. Register Vendor (vendorId captured)
2. Test vendor validations (duplicate email, invalid formats)
3. Get vendor details
4. Update vendor information
5. Upload vendor logo
6. Create branch (branchId captured)
7. Test branch validations
8. Get branch details
9. Update branch information
10. Upload branch images
11. Upload branch documents
12. Toggle branch status
13. Test error scenarios
14. Validate complete flow
```

---

## Test Data

### Vendor Test Data
```json
{
  "companyName": "Chai Express Pvt Ltd",
  "brandName": "Chai Express",
  "legalEntityName": "Chai Express Private Limited",
  "companyEmail": "contact@chaiexpress.com",
  "companyPhone": "9876543210",
  "panNumber": "ABCDE1234F",
  "gstNumber": "29ABCDE1234F1Z5"
}
```

### Branch Test Data
```json
{
  "branchName": "Chai Express - Koramangala",
  "city": "Bangalore",
  "address": {
    "street": "100 Feet Road",
    "area": "Koramangala",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560034"
  },
  "latitude": 12.9352,
  "longitude": 77.6245,
  "branchPhone": "9876543210",
  "branchEmail": "koramangala@chaiexpress.com",
  "branchManagerName": "Rajesh Kumar",
  "branchManagerPhone": "9876543211"
}
```

---

## Assertions

### HTTP Status Code Assertions
```java
.andExpect(status().isCreated())        // 201
.andExpect(status().isOk())             // 200
.andExpect(status().isBadRequest())     // 400
.andExpect(status().isNotFound())       // 404
.andExpect(status().isConflict())       // 409
```

### JSON Response Assertions
```java
.andExpect(jsonPath("$.vendorId").exists())
.andExpect(jsonPath("$.companyName").value("Chai Express Pvt Ltd"))
.andExpect(jsonPath("$.status").value(404))
.andExpect(jsonPath("$.error").value("Not Found"))
.andExpect(jsonPath("$.validationErrors.panNumber").exists())
```

---

## Test Configuration

### Spring Boot Test Configuration
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
```

### Key Features:
- **@SpringBootTest**: Full application context
- **@AutoConfigureMockMvc**: MockMvc for HTTP testing
- **@ActiveProfiles("test")**: Use test profile
- **@TestMethodOrder**: Execute tests in order
- **@Transactional**: Rollback after each test

---

## Expected Test Output

### Successful Test Run
```
✅ UC-V001: Vendor registered successfully with ID: 1
✅ UC-V001: Duplicate email validation working
✅ UC-V001: PAN format validation working
✅ UC-V001: Phone format validation working
✅ UC-V002: Get vendor details working
✅ UC-V002: Vendor not found handling working
✅ UC-V003: Update vendor working
✅ UC-V004: Upload vendor logo working
✅ UC-B001: Branch created successfully with ID: 1
✅ UC-B001: Vendor not found validation working
✅ UC-B002: Get branch details working
✅ UC-B003: Update branch working
✅ UC-B004: Upload branch image working
✅ UC-B005: Upload branch document working
✅ UC-B006: Toggle branch status working
✅ UC-B006: Toggle branch status (close) working
✅ Error: Missing branchId validation working
✅ Error: Invalid target validation working
✅ Error: Missing required fields validation working

========================================
✅ COMPLETE E2E ONBOARDING FLOW TEST
========================================
1. ✅ Vendor Registration
2. ✅ Vendor Details Retrieval
3. ✅ Vendor Update
4. ✅ Vendor Logo Upload
5. ✅ Branch Creation
6. ✅ Branch Details Retrieval
7. ✅ Branch Update
8. ✅ Branch Image Upload
9. ✅ Branch Document Upload
10. ✅ Branch Status Toggle
========================================
✅ ALL USE CASES VALIDATED SUCCESSFULLY!
========================================
```

---

## Troubleshooting

### Test Failures

#### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Start PostgreSQL if needed
docker-compose up -d postgres
```

#### Port Already in Use
```bash
# Check what's using port 8082
lsof -i :8082

# Kill the process if needed
kill -9 <PID>
```

#### Test Data Issues
- Tests use `@Transactional` for automatic rollback
- Each test should be independent
- Shared state (vendorId, branchId) is managed via static variables

---

## Test Maintenance

### Adding New Tests

1. **Follow the naming convention:**
   ```java
   @Test
   @Order(21)
   @DisplayName("UC-XXX: Description - Expected Result")
   public void testFeatureName_Scenario() throws Exception {
       // Arrange
       // Act
       // Assert
   }
   ```

2. **Update test order:**
   - Increment `@Order` value
   - Maintain logical flow

3. **Add to documentation:**
   - Update this guide
   - Update use case document

### Updating Existing Tests

1. **When API changes:**
   - Update request/response structures
   - Update assertions
   - Update test data

2. **When validation rules change:**
   - Update error test cases
   - Update expected error messages

3. **When business logic changes:**
   - Review test scenarios
   - Add new test cases if needed

---

## Best Practices

### DO ✅
- Use descriptive test names
- Test both success and failure scenarios
- Validate error responses
- Use `@Order` for sequential tests
- Clean up test data with `@Transactional`
- Add console output for debugging
- Test complete workflows

### DON'T ❌
- Don't hardcode IDs (except for test data)
- Don't skip error scenarios
- Don't test implementation details
- Don't create interdependent tests (except for sequential flows)
- Don't ignore test failures
- Don't commit commented-out tests

---

## Integration with CI/CD

### GitHub Actions Example
```yaml
- name: Run E2E Tests
  run: |
    cd tea-snacks-delivery-aggregator/order-catalog-service
    ../../gradlew test --tests VendorBranchOnboardingE2ETest
```

### Test Reports
- Location: `build/reports/tests/test/index.html`
- View in browser after running tests

---

## Related Documentation

- [Use Cases](../use-cases/VENDOR_BRANCH_ONBOARDING_USECASES.md)
- [REST API Standards](../REST_API_STANDARDS.md)
- [Swagger Documentation](http://localhost:8082/swagger-ui.html)

---

## Metrics

| Metric | Value |
|--------|-------|
| Total Test Cases | 20 |
| API Endpoints Tested | 13 |
| Use Cases Covered | 6 |
| Error Scenarios | 7 |
| Lines of Test Code | 479 |
| Test Execution Time | ~5-10 seconds |

---

## Future Enhancements

- [ ] Add menu item E2E tests
- [ ] Add performance tests
- [ ] Add security tests
- [ ] Add concurrent operation tests
- [ ] Add data validation tests
- [ ] Add integration with external services
- [ ] Add test data builders
- [ ] Add parameterized tests

---

**Document Owner**: QA Team  
**Review Cycle**: After each sprint  
**Next Review**: November 2024
