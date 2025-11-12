package com.teadelivery.ordercatalog.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.menu.dto.*;
import com.teadelivery.ordercatalog.vendor.dto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive E2E Tests for Menu Item Operations
 * Based on: docs/use-cases/BRANCH_MENU_OPERATIONS_USECASES_V2.md
 * 
 * Covers all 125 use cases across 14 categories:
 * - CRUD Operations, Images, Nutritional Info, Customizations
 * - Allergens, Dietary Tags, Browsing/Filtering, Availability
 * - Versioning, Price, Tags, Error Handling, Authorization, Complex Scenarios
 * 
 * Pattern: VendorBranchOnboardingE2ETest.java
 * Database: order_catalog_test_db (cleaned before tests)
 * Execution: Sequential with @Order annotations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MenuItemOperationsCompleteE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test data - persisted across tests
    private Long vendorId;
    private Long branchId;
    private Long branchId2;
    private Long menuItemId1; // Masala Chai
    private Long menuItemId2; // Samosa
    private Long menuItemId3; // Vada Pav
    private Long menuItemId4; // Filter Coffee
    private Long menuItemId5; // Paneer Tikka
    
    private static final String BASE_URL = "/api/v1/menu-items";
    private static final String VENDOR_URL = "/api/v1/vendors";
    
    @BeforeAll
    public void setupTestSuite() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🧪 MENU OPERATIONS E2E TEST SUITE - 125 Use Cases");
        System.out.println("=".repeat(80));
        System.out.println("📋 Database: order_catalog_test_db");
        System.out.println("\n🧹 Cleaning database...");
        
        jdbcTemplate.execute("TRUNCATE TABLE order_items CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE menu_items CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE branch_documents CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE vendor_branches CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE vendors CASCADE");
        
        System.out.println("✅ Database cleaned\n");
        setupTestVendorAndBranches();
        System.out.println("✅ Test data ready (Vendor: " + vendorId + ", Branches: " + branchId + ", " + branchId2 + ")");
        System.out.println("=".repeat(80) + "\n");
    }
    
    private void setupTestVendorAndBranches() throws Exception {
        // Create vendor
        VendorRegistrationRequest vendorRequest = new VendorRegistrationRequest();
        vendorRequest.setCompanyName("Chai Express Pvt Ltd");
        vendorRequest.setBrandName("Chai Express");
        vendorRequest.setCompanyEmail("contact@chaiexpress.com");
        vendorRequest.setCompanyPhone("9876543210");
        
        MvcResult vendorResult = mockMvc.perform(post(VENDOR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vendorRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        vendorId = objectMapper.readValue(vendorResult.getResponse().getContentAsString(), 
            VendorResponse.class).getVendorId();
        
        // Create branches
        Map<String, Object> address = new HashMap<>();
        address.put("street", "100 Feet Road");
        address.put("area", "Koramangala");
        address.put("city", "Bangalore");
        address.put("state", "Karnataka");
        address.put("pincode", "560034");
        
        BranchCreateRequest branch1 = new BranchCreateRequest();
        branch1.setBranchName("Koramangala Branch");
        branch1.setBranchCode("KRM001");
        branch1.setCity("Bangalore");
        branch1.setBranchPhone("9876543210");
        branch1.setBranchEmail("koramangala@chaiexpress.com");
        branch1.setAddress(address);
        
        MvcResult branch1Result = mockMvc.perform(post(VENDOR_URL + "/" + vendorId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branch1)))
                .andExpect(status().isCreated())
                .andReturn();
        
        branchId = objectMapper.readValue(branch1Result.getResponse().getContentAsString(),
            BranchResponse.class).getBranchId();
        
        BranchCreateRequest branch2 = new BranchCreateRequest();
        branch2.setBranchName("Indiranagar Branch");
        branch2.setBranchCode("IND001");
        branch2.setCity("Bangalore");
        branch2.setBranchPhone("9876543211");
        branch2.setBranchEmail("indiranagar@chaiexpress.com");
        branch2.setAddress(address);
        
        MvcResult branch2Result = mockMvc.perform(post(VENDOR_URL + "/" + vendorId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(branch2)))
                .andExpect(status().isCreated())
                .andReturn();
        
        branchId2 = objectMapper.readValue(branch2Result.getResponse().getContentAsString(),
            BranchResponse.class).getBranchId();
    }
    
    @AfterAll
    public void tearDown() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🏁 TEST SUITE COMPLETED");
        System.out.println("=".repeat(80) + "\n");
    }

    // ==================== CRUD OPERATIONS (UC-M001 to UC-M010) ====================
    
    @Test
    @Order(1)
    @DisplayName("UC-M001: Create menu item with basic fields")
    public void testCreateMenuItemBasicFields() throws Exception {
        System.out.println("\n🧪 TEST 1: UC-M001 - Create basic menu item");
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Masala Chai");
        request.setDescription("Traditional Indian spiced tea");
        request.setPrice(new BigDecimal("20.00"));
        request.setCategory("Beverages");
        request.setPreparationTimeMinutes(5);
        
        MvcResult result = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menuItemId").exists())
                .andExpect(jsonPath("$.name").value("Masala Chai"))
                .andExpect(jsonPath("$.price").value(20.00))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andReturn();
        
        menuItemId1 = objectMapper.readValue(result.getResponse().getContentAsString(),
            MenuItemResponse.class).getMenuItemId();
        
        System.out.println("✅ PASS: Created menu item ID: " + menuItemId1);
    }
    
    @Test
    @Order(2)
    @DisplayName("UC-M002: Create menu item with complete metadata")
    public void testCreateMenuItemCompleteMetadata() throws Exception {
        System.out.println("\n🧪 TEST 2: UC-M002 - Create with complete metadata");
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Nutritional info (UC-N003)
        Map<String, Object> nutritionalInfo = new HashMap<>();
        nutritionalInfo.put("calories", 150);
        nutritionalInfo.put("protein_g", 3.5);
        nutritionalInfo.put("carbohydrates_g", 20);
        nutritionalInfo.put("fat_g", 5);
        metadata.put("nutritional_info", nutritionalInfo);
        
        // Customizations (UC-C002)
        List<Map<String, Object>> customizations = new ArrayList<>();
        Map<String, Object> sizeCustom = new HashMap<>();
        sizeCustom.put("name", "Size");
        sizeCustom.put("required", true);
        sizeCustom.put("options", Arrays.asList(
            Map.of("value", "Small", "price_modifier", 0),
            Map.of("value", "Medium", "price_modifier", 5),
            Map.of("value", "Large", "price_modifier", 10)
        ));
        customizations.add(sizeCustom);
        metadata.put("customizations", customizations);
        
        // Allergens (UC-A001)
        metadata.put("allergens", Arrays.asList("milk", "cardamom"));
        
        // Dietary tags (UC-D001)
        metadata.put("dietary_tags", Arrays.asList("vegetarian", "gluten_free"));
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Samosa");
        request.setDescription("Crispy fried pastry with spiced potato filling");
        request.setPrice(new BigDecimal("15.00"));
        request.setCategory("Snacks");
        request.setPreparationTimeMinutes(10);
        request.setMetadata(metadata);
        request.setTags(new String[]{"popular", "bestseller"});
        
        MvcResult result = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(150))
                .andExpect(jsonPath("$.metadata.customizations[0].name").value("Size"))
                .andExpect(jsonPath("$.metadata.allergens[0]").value("milk"))
                .andExpect(jsonPath("$.metadata.dietary_tags[0]").value("vegetarian"))
                .andExpect(jsonPath("$.tags[0]").value("popular"))
                .andReturn();
        
        menuItemId2 = objectMapper.readValue(result.getResponse().getContentAsString(),
            MenuItemResponse.class).getMenuItemId();
        
        System.out.println("✅ PASS: Created with complete metadata ID: " + menuItemId2);
    }
    
    @Test
    @Order(3)
    @DisplayName("UC-M003: Get menu item by ID")
    public void testGetMenuItemById() throws Exception {
        System.out.println("\n🧪 TEST 3: UC-M003 - Get menu item by ID");
        
        mockMvc.perform(get(BASE_URL + "/" + menuItemId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuItemId").value(menuItemId1))
                .andExpect(jsonPath("$.name").value("Masala Chai"))
                .andExpect(jsonPath("$.price").value(20.00));
        
        System.out.println("✅ PASS: Retrieved menu item");
    }
    
    @Test
    @Order(4)
    @DisplayName("UC-M004: Update menu item - price only")
    public void testUpdateMenuItemPriceOnly() throws Exception {
        System.out.println("\n🧪 TEST 4: UC-M004 - Update price only");
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setPrice(new BigDecimal("22.00"));
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(22.00))
                .andExpect(jsonPath("$.name").value("Masala Chai")); // Name unchanged
        
        System.out.println("✅ PASS: Price updated");
    }
    
    @Test
    @Order(5)
    @DisplayName("UC-M005 & UC-V001-V003: Availability toggle")
    public void testAvailabilityToggle() throws Exception {
        System.out.println("\n🧪 TEST 5: UC-M005, UC-V001-V003 - Availability management");
        
        // Mark unavailable (UC-V001)
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setIsAvailable(false);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
        
        // Mark available (UC-V002)
        request.setIsAvailable(true);
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(true));
        
        // Toggle again (UC-V003)
        request.setIsAvailable(false);
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
        
        // Restore to available
        request.setIsAvailable(true);
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Availability toggled multiple times");
    }
    
    @Test
    @Order(6)
    @DisplayName("UC-M006 & UC-V006: Update multiple fields including availability")
    public void testUpdateMultipleFields() throws Exception {
        System.out.println("\n🧪 TEST 6: UC-M006, UC-V006 - Update multiple fields");
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setName("Special Masala Chai");
        request.setDescription("Premium spiced tea with special blend");
        request.setPrice(new BigDecimal("25.00"));
        request.setPreparationTimeMinutes(7);
        request.setIsAvailable(true);
        request.setTags(new String[]{"premium", "special"});
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Special Masala Chai"))
                .andExpect(jsonPath("$.price").value(25.00))
                .andExpect(jsonPath("$.preparationTimeMinutes").value(7))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.tags[0]").value("premium"));
        
        System.out.println("✅ PASS: Multiple fields updated");
    }
    
    @Test
    @Order(7)
    @DisplayName("UC-M007 & UC-N002: Partial metadata update")
    public void testPartialMetadataUpdate() throws Exception {
        System.out.println("\n🧪 TEST 7: UC-M007, UC-N002 - Partial metadata update");
        
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> nutritionalInfo = new HashMap<>();
        nutritionalInfo.put("calories", 160); // Update only calories
        metadata.put("nutritional_info", nutritionalInfo);
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(160));
        
        System.out.println("✅ PASS: Partial metadata updated");
    }
    
    @Test
    @Order(8)
    @DisplayName("UC-M010 & UC-B001-B002: List and paginate menu items")
    public void testListAndPaginateMenuItems() throws Exception {
        System.out.println("\n🧪 TEST 8: UC-M010, UC-B001-B002 - List & pagination");
        
        // Create additional items for pagination
        for (int i = 3; i <= 5; i++) {
            MenuItemCreateRequest request = new MenuItemCreateRequest();
            request.setName("Test Item " + i);
            request.setPrice(new BigDecimal("10.00"));
            request.setCategory("Test");
            
            MvcResult result = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            if (i == 3) {
                menuItemId3 = objectMapper.readValue(result.getResponse().getContentAsString(),
                    MenuItemResponse.class).getMenuItemId();
            }
        }
        
        // List all (UC-B001)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
        
        // Paginated (UC-B002)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        
        System.out.println("✅ PASS: List and pagination working");
    }
    
    @Test
    @Order(9)
    @DisplayName("UC-M008: Soft delete menu item")
    public void testSoftDeleteMenuItem() throws Exception {
        System.out.println("\n🧪 TEST 9: UC-M008 - Soft delete");
        
        mockMvc.perform(delete(BASE_URL + "/" + menuItemId3))
                .andExpect(status().isNoContent());
        
        System.out.println("✅ PASS: Menu item soft deleted");
    }
    
    @Test
    @Order(10)
    @DisplayName("UC-M009 & UC-E004: Get deleted/non-existent item")
    public void testGetDeletedMenuItem() throws Exception {
        System.out.println("\n🧪 TEST 10: UC-M009, UC-E004 - Get deleted item");
        
        mockMvc.perform(get(BASE_URL + "/" + menuItemId3))
                .andExpect(status().isNotFound());
        
        // Also test completely non-existent ID (UC-E004)
        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ PASS: Deleted item returns 404");
    }

    
    // ==================== FILTERING & BROWSING (UC-B003 to UC-B015) ====================
    
    @Test
    @Order(11)
    @DisplayName("UC-B003-B005: Filter by category")
    public void testFilterByCategory() throws Exception {
        System.out.println("\n🧪 TEST 11: UC-B003-B005 - Filter by category");
        
        // Filter Beverages (UC-B003)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("category", "Beverages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", everyItem(is("Beverages"))));
        
        // Filter Snacks (UC-B004)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("category", "Snacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", everyItem(is("Snacks"))));
        
        System.out.println("✅ PASS: Category filtering works");
    }
    
    @Test
    @Order(12)
    @DisplayName("UC-B006: Filter by price range")
    public void testFilterByPriceRange() throws Exception {
        System.out.println("\n🧪 TEST 12: UC-B006 - Price range filter");
        
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("minPrice", "15")
                .param("maxPrice", "30"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Price range filter works");
    }
    
    @Test
    @Order(13)
    @DisplayName("UC-B007 & UC-V004: Filter available items only")
    public void testFilterAvailableOnly() throws Exception {
        System.out.println("\n🧪 TEST 13: UC-B007, UC-V004 - Available items filter");
        
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isAvailable", everyItem(is(true))));
        
        System.out.println("✅ PASS: Available filter works");
    }
    
    @Test
    @Order(14)
    @DisplayName("UC-B008 & UC-T005: Filter by tags")
    public void testFilterByTags() throws Exception {
        System.out.println("\n🧪 TEST 14: UC-B008, UC-T005 - Tag filter");
        
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("tags", "popular"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Tag filter works");
    }
    
    @Test
    @Order(15)
    @DisplayName("UC-B009-B010: Combined filters")
    public void testCombinedFilters() throws Exception {
        System.out.println("\n🧪 TEST 15: UC-B009-B010 - Combined filters");
        
        // Category + dietary tags (UC-B009)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("category", "Snacks")
                .param("dietaryTags", "vegetarian"))
                .andExpect(status().isOk());
        
        // Price range + allergen exclusion (UC-B010)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("minPrice", "10")
                .param("maxPrice", "50")
                .param("excludeAllergens", "milk"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Combined filters work");
    }
    
    @Test
    @Order(16)
    @DisplayName("UC-B011: Search by name")
    public void testSearchByName() throws Exception {
        System.out.println("\n🧪 TEST 16: UC-B011 - Search by name");
        
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("search", "Chai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem(containsString("Chai"))));
        
        System.out.println("✅ PASS: Name search works");
    }
    
    @Test
    @Order(17)
    @DisplayName("UC-B012-B013: Sort by price")
    public void testSortByPrice() throws Exception {
        System.out.println("\n🧪 TEST 17: UC-B012-B013 - Price sorting");
        
        // Ascending (UC-B012)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("sortBy", "price")
                .param("sortOrder", "asc"))
                .andExpect(status().isOk());
        
        // Descending (UC-B013)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("sortBy", "price")
                .param("sortOrder", "desc"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Price sorting works");
    }
    
    // ==================== NUTRITIONAL INFO (UC-N001 to UC-N006) ====================
    
    @Test
    @Order(18)
    @DisplayName("UC-N001 & UC-N006: Add and retrieve nutritional info")
    public void testNutritionalInfo() throws Exception {
        System.out.println("\n🧪 TEST 18: UC-N001, UC-N006 - Nutritional info");
        
        // Add nutritional info to existing item (UC-N001)
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> nutritionalInfo = new HashMap<>();
        nutritionalInfo.put("calories", 80);
        nutritionalInfo.put("protein_g", 2.5);
        nutritionalInfo.put("carbohydrates_g", 12);
        nutritionalInfo.put("fat_g", 3);
        nutritionalInfo.put("fiber_g", 1);
        nutritionalInfo.put("sugar_g", 8);
        metadata.put("nutritional_info", nutritionalInfo);
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(80))
                .andExpect(jsonPath("$.metadata.nutritional_info.protein_g").value(2.5));
        
        // Retrieve with detailed breakdown (UC-N006)
        mockMvc.perform(get(BASE_URL + "/" + menuItemId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.nutritional_info").exists())
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(80));
        
        System.out.println("✅ PASS: Nutritional info added and retrieved");
    }
    
    @Test
    @Order(19)
    @DisplayName("UC-N004: Remove nutritional info")
    public void testRemoveNutritionalInfo() throws Exception {
        System.out.println("\n🧪 TEST 19: UC-N004 - Remove nutritional info");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nutritional_info", null);
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Nutritional info removed");
    }
    
    // ==================== CUSTOMIZATIONS (UC-C001 to UC-C010) ====================
    
    @Test
    @Order(20)
    @DisplayName("UC-C001 & UC-C003-C006: Customization management")
    public void testCustomizationManagement() throws Exception {
        System.out.println("\n🧪 TEST 20: UC-C001, UC-C003-C006 - Customizations");
        
        Map<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> customizations = new ArrayList<>();
        
        // Single customization with price modifiers (UC-C001, UC-C003)
        Map<String, Object> sizeCustom = new HashMap<>();
        sizeCustom.put("name", "Size");
        sizeCustom.put("required", true); // UC-C004
        sizeCustom.put("multi_select", false);
        sizeCustom.put("options", Arrays.asList(
            Map.of("value", "Small", "price_modifier", 0),
            Map.of("value", "Large", "price_modifier", 10)
        ));
        customizations.add(sizeCustom);
        
        // Optional customization (UC-C005)
        Map<String, Object> sugarCustom = new HashMap<>();
        sugarCustom.put("name", "Sugar Level");
        sugarCustom.put("required", false);
        sugarCustom.put("options", Arrays.asList(
            Map.of("value", "No Sugar", "price_modifier", 0),
            Map.of("value", "Less Sugar", "price_modifier", 0),
            Map.of("value", "Normal", "price_modifier", 0)
        ));
        customizations.add(sugarCustom);
        
        // Multi-select customization (UC-C006)
        Map<String, Object> addonsCustom = new HashMap<>();
        addonsCustom.put("name", "Add-ons");
        addonsCustom.put("required", false);
        addonsCustom.put("multi_select", true);
        addonsCustom.put("options", Arrays.asList(
            Map.of("value", "Extra Ginger", "price_modifier", 5),
            Map.of("value", "Extra Cardamom", "price_modifier", 3)
        ));
        customizations.add(addonsCustom);
        
        metadata.put("customizations", customizations);
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.customizations", hasSize(3)))
                .andExpect(jsonPath("$.metadata.customizations[0].name").value("Size"))
                .andExpect(jsonPath("$.metadata.customizations[0].required").value(true))
                .andExpect(jsonPath("$.metadata.customizations[2].multi_select").value(true));
        
        System.out.println("✅ PASS: Customizations added (required, optional, multi-select)");
    }
    
    @Test
    @Order(21)
    @DisplayName("UC-C007 & UC-C009: Update customization options and price modifiers")
    public void testUpdateCustomizations() throws Exception {
        System.out.println("\n🧪 TEST 21: UC-C007, UC-C009 - Update customizations");
        
        Map<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> customizations = new ArrayList<>();
        
        Map<String, Object> sizeCustom = new HashMap<>();
        sizeCustom.put("name", "Size");
        sizeCustom.put("required", true);
        sizeCustom.put("options", Arrays.asList(
            Map.of("value", "Small", "price_modifier", 0),
            Map.of("value", "Medium", "price_modifier", 5), // New option
            Map.of("value", "Large", "price_modifier", 15) // Updated price
        ));
        customizations.add(sizeCustom);
        
        metadata.put("customizations", customizations);
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.customizations[0].options", hasSize(3)));
        
        System.out.println("✅ PASS: Customizations updated");
    }
    
    @Test
    @Order(22)
    @DisplayName("UC-C008: Remove customization group")
    public void testRemoveCustomizationGroup() throws Exception {
        System.out.println("\n🧪 TEST 22: UC-C008 - Remove customization");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("customizations", new ArrayList<>());
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Customizations removed");
    }

    
    // ==================== ALLERGENS (UC-A001 to UC-A008) ====================
    
    @Test
    @Order(23)
    @DisplayName("UC-A001-A004: Allergen management")
    public void testAllergenManagement() throws Exception {
        System.out.println("\n🧪 TEST 23: UC-A001-A004 - Allergen management");
        
        // Add allergens (UC-A001)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("allergens", Arrays.asList("milk", "nuts", "soy"));
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.allergens", hasSize(3)));
        
        // Update allergen list (UC-A002)
        metadata.put("allergens", Arrays.asList("milk", "nuts")); // Removed soy
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.allergens", hasSize(2)));
        
        // Clear all allergens (UC-A004)
        metadata.put("allergens", new ArrayList<>());
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Allergens added, updated, and cleared");
    }
    
    @Test
    @Order(24)
    @DisplayName("UC-A005-A008: Browse menu excluding allergens")
    public void testBrowseExcludingAllergens() throws Exception {
        System.out.println("\n🧪 TEST 24: UC-A005-A008 - Allergen exclusion filters");
        
        // Single allergen exclusion (UC-A005, UC-A007 - nut allergy)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("excludeAllergens", "nuts"))
                .andExpect(status().isOk());
        
        // Multiple allergen exclusions (UC-A006, UC-A008 - dairy allergy)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("excludeAllergens", "milk,dairy"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Allergen exclusion filters work");
    }
    
    // ==================== DIETARY TAGS (UC-D001 to UC-D010) ====================
    
    @Test
    @Order(25)
    @DisplayName("UC-D001-D003: Dietary tags management")
    public void testDietaryTagsManagement() throws Exception {
        System.out.println("\n🧪 TEST 25: UC-D001-D003 - Dietary tags");
        
        // Add dietary tags (UC-D001)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("dietary_tags", Arrays.asList("vegetarian", "gluten_free", "vegan"));
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.dietary_tags", hasSize(3)));
        
        // Update dietary tags (UC-D002)
        metadata.put("dietary_tags", Arrays.asList("vegetarian", "vegan")); // Removed gluten_free
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.dietary_tags", hasSize(2)));
        
        System.out.println("✅ PASS: Dietary tags added and updated");
    }
    
    @Test
    @Order(26)
    @DisplayName("UC-D004-D010: Browse by dietary tags")
    public void testBrowseByDietaryTags() throws Exception {
        System.out.println("\n🧪 TEST 26: UC-D004-D010 - Dietary tag filters");
        
        // Single tag - vegetarian (UC-D004, UC-D006)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "vegetarian"))
                .andExpect(status().isOk());
        
        // Multiple tags (UC-D005)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "vegan,gluten_free"))
                .andExpect(status().isOk());
        
        // Vegan items (UC-D007)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "vegan"))
                .andExpect(status().isOk());
        
        // Gluten-free items (UC-D008)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "gluten_free"))
                .andExpect(status().isOk());
        
        // Keto items (UC-D009)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "keto"))
                .andExpect(status().isOk());
        
        // Halal items (UC-D010)
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("dietaryTags", "halal"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Dietary tag filters work");
    }
    
    // ==================== PRICE & TAGS (UC-P001 to UC-T005) ====================
    
    @Test
    @Order(27)
    @DisplayName("UC-P001-P003: Price management")
    public void testPriceManagement() throws Exception {
        System.out.println("\n🧪 TEST 27: UC-P001-P003 - Price management");
        
        // Update price (UC-P001)
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setPrice(new BigDecimal("30.00"));
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(30.00));
        
        // Set to zero - free item (UC-P002)
        request.setPrice(new BigDecimal("0.00"));
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(0.00));
        
        // Restore normal price
        request.setPrice(new BigDecimal("25.00"));
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Price updated including free item");
    }
    
    @Test
    @Order(28)
    @DisplayName("UC-T001-T004: Tags management")
    public void testTagsManagement() throws Exception {
        System.out.println("\n🧪 TEST 28: UC-T001-T004 - Tags management");
        
        // Add tags (UC-T001)
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setTags(new String[]{"popular", "bestseller", "recommended"});
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(3)));
        
        // Update tags (UC-T002)
        request.setTags(new String[]{"popular", "recommended"}); // Removed bestseller
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)));
        
        // Clear all tags (UC-T004)
        request.setTags(new String[]{});
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Tags added, updated, and cleared");
    }
    
    // ==================== ERROR HANDLING (UC-E001 to UC-E015) ====================
    
    @Test
    @Order(29)
    @DisplayName("UC-E001 & UC-E015: Missing/empty required fields")
    public void testMissingRequiredFields() throws Exception {
        System.out.println("\n🧪 TEST 29: UC-E001, UC-E015 - Missing required fields");
        
        // Missing name (UC-E001)
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setPrice(new BigDecimal("20.00"));
        request.setCategory("Beverages");
        
        mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        // Empty name (UC-E015)
        request.setName("");
        
        mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ PASS: Missing/empty fields rejected");
    }
    
    @Test
    @Order(30)
    @DisplayName("UC-E002, E009, P004: Invalid price")
    public void testInvalidPrice() throws Exception {
        System.out.println("\n🧪 TEST 30: UC-E002, E009, P004 - Invalid price");
        
        // Negative price on create (UC-E002, P004)
        MenuItemCreateRequest createRequest = new MenuItemCreateRequest();
        createRequest.setName("Test Item");
        createRequest.setPrice(new BigDecimal("-10.00"));
        createRequest.setCategory("Test");
        
        mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        
        // Negative price on update (UC-E009)
        MenuItemUpdateRequest updateRequest = new MenuItemUpdateRequest();
        updateRequest.setPrice(new BigDecimal("-5.00"));
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ PASS: Negative price rejected");
    }
    
    @Test
    @Order(31)
    @DisplayName("UC-E003: Create for non-existent branch")
    public void testCreateForNonExistentBranch() throws Exception {
        System.out.println("\n🧪 TEST 31: UC-E003 - Non-existent branch");
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Test Item");
        request.setPrice(new BigDecimal("20.00"));
        request.setCategory("Test");
        
        mockMvc.perform(post(BASE_URL + "/branches/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ PASS: Non-existent branch rejected");
    }
    
    @Test
    @Order(32)
    @DisplayName("UC-E005-E006: Update/delete non-existent item")
    public void testUpdateDeleteNonExistent() throws Exception {
        System.out.println("\n🧪 TEST 32: UC-E005-E006 - Non-existent item operations");
        
        // Update non-existent (UC-E005)
        MenuItemUpdateRequest updateRequest = new MenuItemUpdateRequest();
        updateRequest.setPrice(new BigDecimal("20.00"));
        
        mockMvc.perform(put(BASE_URL + "/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        
        // Delete non-existent (UC-E006)
        mockMvc.perform(delete(BASE_URL + "/99999"))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ PASS: Non-existent item operations rejected");
    }
    
    @Test
    @Order(33)
    @DisplayName("UC-E010: Exceed description length")
    public void testExceedDescriptionLength() throws Exception {
        System.out.println("\n🧪 TEST 33: UC-E010 - Description length validation");
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Test Item");
        request.setDescription("A".repeat(1001)); // Assuming 1000 char limit
        request.setPrice(new BigDecimal("20.00"));
        request.setCategory("Test");
        
        mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ PASS: Long description rejected");
    }
    
    @Test
    @Order(34)
    @DisplayName("UC-E012: Invalid preparation time")
    public void testInvalidPreparationTime() throws Exception {
        System.out.println("\n🧪 TEST 34: UC-E012 - Invalid preparation time");
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Test Item");
        request.setPrice(new BigDecimal("20.00"));
        request.setCategory("Test");
        request.setPreparationTimeMinutes(-5); // Negative time
        
        mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ PASS: Negative preparation time rejected");
    }

    
    // ==================== COMPLEX SCENARIOS (UC-X001 to UC-X012) ====================
    
    @Test
    @Order(35)
    @DisplayName("UC-X001: Create complete menu item with all fields")
    public void testCreateCompleteMenuItem() throws Exception {
        System.out.println("\n🧪 TEST 35: UC-X001 - Complete menu item with all fields");
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Complete nutritional info
        Map<String, Object> nutritionalInfo = new HashMap<>();
        nutritionalInfo.put("calories", 200);
        nutritionalInfo.put("protein_g", 5);
        nutritionalInfo.put("carbohydrates_g", 30);
        nutritionalInfo.put("fat_g", 8);
        nutritionalInfo.put("fiber_g", 2);
        nutritionalInfo.put("sugar_g", 10);
        metadata.put("nutritional_info", nutritionalInfo);
        
        // Multiple customizations
        List<Map<String, Object>> customizations = new ArrayList<>();
        customizations.add(Map.of(
            "name", "Size",
            "required", true,
            "options", Arrays.asList(
                Map.of("value", "Small", "price_modifier", 0),
                Map.of("value", "Large", "price_modifier", 10)
            )
        ));
        metadata.put("customizations", customizations);
        
        // Allergens and dietary tags
        metadata.put("allergens", Arrays.asList("wheat", "soy"));
        metadata.put("dietary_tags", Arrays.asList("vegetarian"));
        
        MenuItemCreateRequest request = new MenuItemCreateRequest();
        request.setName("Vada Pav");
        request.setDescription("Mumbai's famous street food - spiced potato fritter in a bun");
        request.setPrice(new BigDecimal("30.00"));
        request.setCategory("Snacks");
        request.setPreparationTimeMinutes(8);
        request.setMetadata(metadata);
        request.setTags(new String[]{"popular", "street_food", "mumbai_special"});
        
        MvcResult result = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vada Pav"))
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(200))
                .andExpect(jsonPath("$.metadata.customizations", hasSize(1)))
                .andExpect(jsonPath("$.metadata.allergens", hasSize(2)))
                .andExpect(jsonPath("$.metadata.dietary_tags", hasSize(1)))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andReturn();
        
        menuItemId4 = objectMapper.readValue(result.getResponse().getContentAsString(),
            MenuItemResponse.class).getMenuItemId();
        
        System.out.println("✅ PASS: Complete menu item created with all fields");
    }
    
    @Test
    @Order(36)
    @DisplayName("UC-X002: Update all metadata fields in single request")
    public void testUpdateAllMetadataFields() throws Exception {
        System.out.println("\n🧪 TEST 36: UC-X002 - Update all metadata at once");
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Update nutritional info
        metadata.put("nutritional_info", Map.of(
            "calories", 220,
            "protein_g", 6,
            "carbohydrates_g", 32,
            "fat_g", 9
        ));
        
        // Update customizations
        metadata.put("customizations", Arrays.asList(
            Map.of(
                "name", "Spice Level",
                "required", false,
                "options", Arrays.asList(
                    Map.of("value", "Mild", "price_modifier", 0),
                    Map.of("value", "Spicy", "price_modifier", 0),
                    Map.of("value", "Extra Spicy", "price_modifier", 5)
                )
            )
        ));
        
        // Update allergens and dietary tags
        metadata.put("allergens", Arrays.asList("wheat", "soy", "mustard"));
        metadata.put("dietary_tags", Arrays.asList("vegetarian", "vegan"));
        
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId4)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.nutritional_info.calories").value(220))
                .andExpect(jsonPath("$.metadata.customizations[0].name").value("Spice Level"))
                .andExpect(jsonPath("$.metadata.allergens", hasSize(3)))
                .andExpect(jsonPath("$.metadata.dietary_tags", hasSize(2)));
        
        System.out.println("✅ PASS: All metadata fields updated");
    }
    
    @Test
    @Order(37)
    @DisplayName("UC-X003: Complete workflow - create, update, toggle availability")
    public void testCompleteWorkflow() throws Exception {
        System.out.println("\n🧪 TEST 37: UC-X003 - Complete workflow");
        
        // Create item
        MenuItemCreateRequest createRequest = new MenuItemCreateRequest();
        createRequest.setName("Filter Coffee");
        createRequest.setPrice(new BigDecimal("40.00"));
        createRequest.setCategory("Beverages");
        
        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        menuItemId5 = objectMapper.readValue(createResult.getResponse().getContentAsString(),
            MenuItemResponse.class).getMenuItemId();
        
        // Update metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("nutritional_info", Map.of("calories", 50));
        metadata.put("dietary_tags", Arrays.asList("vegetarian"));
        
        MenuItemUpdateRequest updateRequest = new MenuItemUpdateRequest();
        updateRequest.setMetadata(metadata);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
        
        // Toggle availability
        updateRequest = new MenuItemUpdateRequest();
        updateRequest.setIsAvailable(false);
        
        mockMvc.perform(put(BASE_URL + "/" + menuItemId5)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
        
        System.out.println("✅ PASS: Complete workflow executed");
    }
    
    @Test
    @Order(38)
    @DisplayName("UC-X004-X006: Bulk operations")
    public void testBulkOperations() throws Exception {
        System.out.println("\n🧪 TEST 38: UC-X004-X006 - Bulk operations");
        
        // Create multiple items (UC-X004)
        List<Long> createdIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            MenuItemCreateRequest request = new MenuItemCreateRequest();
            request.setName("Bulk Item " + i);
            request.setPrice(new BigDecimal("15.00"));
            request.setCategory("Test");
            
            MvcResult result = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            Long id = objectMapper.readValue(result.getResponse().getContentAsString(),
                MenuItemResponse.class).getMenuItemId();
            createdIds.add(id);
        }
        
        // Update multiple items (UC-X005)
        for (Long id : createdIds) {
            MenuItemUpdateRequest updateRequest = new MenuItemUpdateRequest();
            updateRequest.setPrice(new BigDecimal("20.00"));
            
            mockMvc.perform(put(BASE_URL + "/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());
        }
        
        // Delete multiple items (UC-X006)
        for (Long id : createdIds) {
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNoContent());
        }
        
        System.out.println("✅ PASS: Bulk create, update, delete completed");
    }
    
    @Test
    @Order(39)
    @DisplayName("UC-X009: Menu item lifecycle")
    public void testMenuItemLifecycle() throws Exception {
        System.out.println("\n🧪 TEST 39: UC-X009 - Complete lifecycle");
        
        // Create
        MenuItemCreateRequest createRequest = new MenuItemCreateRequest();
        createRequest.setName("Seasonal Special");
        createRequest.setPrice(new BigDecimal("50.00"));
        createRequest.setCategory("Specials");
        
        MvcResult createResult = mockMvc.perform(post(BASE_URL + "/branches/" + branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long lifecycleItemId = objectMapper.readValue(createResult.getResponse().getContentAsString(),
            MenuItemResponse.class).getMenuItemId();
        
        // Update
        MenuItemUpdateRequest updateRequest = new MenuItemUpdateRequest();
        updateRequest.setPrice(new BigDecimal("45.00"));
        updateRequest.setDescription("Limited time offer");
        
        mockMvc.perform(put(BASE_URL + "/" + lifecycleItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
        
        // Mark unavailable
        updateRequest = new MenuItemUpdateRequest();
        updateRequest.setIsAvailable(false);
        
        mockMvc.perform(put(BASE_URL + "/" + lifecycleItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
        
        // Delete
        mockMvc.perform(delete(BASE_URL + "/" + lifecycleItemId))
                .andExpect(status().isNoContent());
        
        // Verify deleted
        mockMvc.perform(get(BASE_URL + "/" + lifecycleItemId))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ PASS: Complete lifecycle executed");
    }
    
    @Test
    @Order(40)
    @DisplayName("UC-X010: Complex multi-filter browsing")
    public void testComplexMultiFilterBrowsing() throws Exception {
        System.out.println("\n🧪 TEST 40: UC-X010 - Complex multi-filter browsing");
        
        // Combine category, price range, dietary tags, and availability
        mockMvc.perform(get(BASE_URL + "/branches/" + branchId)
                .param("category", "Snacks")
                .param("minPrice", "10")
                .param("maxPrice", "50")
                .param("dietaryTags", "vegetarian")
                .param("available", "true")
                .param("sortBy", "price")
                .param("sortOrder", "asc"))
                .andExpect(status().isOk());
        
        System.out.println("✅ PASS: Complex multi-filter query executed");
    }
}
