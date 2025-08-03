package com.teadelivery.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify test framework is working.
 */
class SimpleTest {

    @Test
    @DisplayName("Should pass basic test")
    void shouldPassBasicTest() {
        // Given
        int expected = 2;
        int actual = 1 + 1;

        // When & Then
        assertEquals(expected, actual, "Basic math should work");
    }

    @Test
    @DisplayName("Should handle string operations")
    void shouldHandleStringOperations() {
        // Given
        String expected = "Hello World";
        String actual = "Hello" + " " + "World";

        // When & Then
        assertEquals(expected, actual, "String concatenation should work");
    }
} 