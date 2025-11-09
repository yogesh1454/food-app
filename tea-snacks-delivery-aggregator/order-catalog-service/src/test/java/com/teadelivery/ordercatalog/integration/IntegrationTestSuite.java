package com.teadelivery.ordercatalog.integration;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Integration Test Suite
 * Runs all integration tests in the integration package
 * 
 * To run: ./gradlew test --tests IntegrationTestSuite
 */
@Suite
@SuiteDisplayName("Order Catalog Service - Integration Test Suite")
@SelectPackages("com.teadelivery.ordercatalog.integration")
public class IntegrationTestSuite {
    // Test suite configuration
    // All tests in the integration package will be executed
}
