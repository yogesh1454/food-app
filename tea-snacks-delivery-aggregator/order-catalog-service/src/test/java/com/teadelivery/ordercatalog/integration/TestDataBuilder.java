package com.teadelivery.ordercatalog.integration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Test Data Builder for Integration Tests
 * 
 * Creates isolated test data for each test.
 * Data is automatically cleaned up via @Transactional rollback.
 * 
 * Usage in tests:
 * <pre>
 * TestVendor vendor = testDataBuilder.createVendor("Test Cafe", 19.0760, 72.8777);
 * TestMenuItem item1 = testDataBuilder.createMenuItem(vendor.branchId, "Chai", 20.00);
 * TestMenuItem item2 = testDataBuilder.createMenuItem(vendor.branchId, "Samosa", 15.00);
 * </pre>
 */
@Component
public class TestDataBuilder {
    
    private final JdbcTemplate jdbcTemplate;
    
    public TestDataBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Create a test vendor branch with default values
     */
    public TestVendor createVendor() {
        return createVendor("Test Vendor - " + UUID.randomUUID().toString().substring(0, 8), 19.0760, 72.8777);
    }
    
    /**
     * Create a test vendor branch at specific location
     */
    public TestVendor createVendor(String branchName, double latitude, double longitude) {
        UUID vendorId = UUID.randomUUID();
        Long branchId = System.currentTimeMillis(); // Use timestamp as branch ID for uniqueness
        
        String sql = """
            INSERT INTO vendor_branches (
                branch_id, vendor_id, branch_name, is_active, 
                latitude, longitude, created_at, updated_at
            ) VALUES (?, ?::uuid, ?, true, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        jdbcTemplate.update(sql, branchId, vendorId.toString(), branchName, 
                          BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
        
        return new TestVendor(branchId, vendorId, branchName, latitude, longitude);
    }
    
    /**
     * Create a test menu item with default values
     */
    public TestMenuItem createMenuItem(Long branchId, String itemName, double price) {
        return createMenuItem(branchId, itemName, "Test item description", price, "SNACKS", true);
    }
    
    /**
     * Create a test menu item with all details
     */
    public TestMenuItem createMenuItem(Long branchId, String itemName, String description, 
                                       double price, String category, boolean isAvailable) {
        Long menuItemId = System.currentTimeMillis() + (long)(Math.random() * 1000); // Unique ID
        
        String sql = """
            INSERT INTO menu_items (
                menu_item_id, branch_id, item_name, description, 
                price, category, is_available, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        
        jdbcTemplate.update(sql, menuItemId, branchId, itemName, description,
                          BigDecimal.valueOf(price), category, isAvailable);
        
        return new TestMenuItem(menuItemId, branchId, itemName, description, price, category, isAvailable);
    }
    
    /**
     * Create a complete test scenario with vendor and menu items
     */
    public TestScenario createCompleteScenario() {
        TestVendor vendor = createVendor();
        TestMenuItem item1 = createMenuItem(vendor.branchId, "Masala Chai", 20.00);
        TestMenuItem item2 = createMenuItem(vendor.branchId, "Samosa", 15.00);
        
        return new TestScenario(vendor, item1, item2);
    }
    
    // Data classes to hold test data
    
    public record TestVendor(
        Long branchId,
        UUID vendorId,
        String branchName,
        double latitude,
        double longitude
    ) {}
    
    public record TestMenuItem(
        Long menuItemId,
        Long branchId,
        String itemName,
        String description,
        double price,
        String category,
        boolean isAvailable
    ) {}
    
    public record TestScenario(
        TestVendor vendor,
        TestMenuItem item1,
        TestMenuItem item2
    ) {}
}

