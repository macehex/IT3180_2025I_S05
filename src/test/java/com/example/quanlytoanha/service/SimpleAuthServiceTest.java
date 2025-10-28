package com.example.quanlytoanha.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simplified unit tests for AuthService class
 * These tests focus on basic functionality without complex mocking
 */
public class SimpleAuthServiceTest {

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Should create AuthService instance")
    public void testAuthServiceCreation() {
        assertNotNull(authService);
    }

    @Test
    @DisplayName("Should handle login with null username gracefully")
    public void testLoginWithNullUsername() {
        // When
        var result = authService.login(null, "password");
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle login with empty username gracefully")
    public void testLoginWithEmptyUsername() {
        // When
        var result = authService.login("", "password");
        
        // Then
        assertNull(result);
    }
}