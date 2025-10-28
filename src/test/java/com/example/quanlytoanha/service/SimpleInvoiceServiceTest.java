package com.example.quanlytoanha.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified unit tests for InvoiceService class
 * These tests focus on testing the singleton pattern and basic functionality
 */
public class SimpleInvoiceServiceTest {

    @Test
    @DisplayName("Should return singleton instance")
    public void testGetInstance() {
        // When
        InvoiceService instance1 = InvoiceService.getInstance();
        InvoiceService instance2 = InvoiceService.getInstance();
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should handle service instantiation")
    public void testServiceInstantiation() {
        // When
        InvoiceService service = InvoiceService.getInstance();
        
        // Then
        assertNotNull(service);
    }
}