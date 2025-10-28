package com.example.quanlytoanha.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationException class
 */
public class ValidationExceptionTest {

    @Test
    @DisplayName("Should create ValidationException with message")
    public void testValidationExceptionWithMessage() {
        // Given
        String errorMessage = "Validation failed for field";

        // When
        ValidationException exception = new ValidationException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create ValidationException with message only")
    public void testValidationExceptionMessageOnly() {
        // Given
        String errorMessage = "Validation failed";

        // When
        ValidationException exception = new ValidationException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should be throwable")
    public void testValidationExceptionThrowable() {
        // Given
        String errorMessage = "Test validation error";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            throw new ValidationException(errorMessage);
        });
        
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null message")
    public void testValidationExceptionWithNullMessage() {
        // When
        ValidationException exception = new ValidationException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty message")
    public void testValidationExceptionWithEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        ValidationException exception = new ValidationException(emptyMessage);

        // Then
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should inherit from Exception")
    public void testValidationExceptionInheritance() {
        // Given
        ValidationException exception = new ValidationException("Test");

        // Then
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should preserve stack trace")
    public void testValidationExceptionStackTrace() {
        // When
        ValidationException exception = new ValidationException("Test error");

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}