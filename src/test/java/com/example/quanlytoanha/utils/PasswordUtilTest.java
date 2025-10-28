package com.example.quanlytoanha.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class
 */
public class PasswordUtilTest {

    @Test
    @DisplayName("Should hash password successfully")
    public void testHashPassword() {
        // Given
        String plainPassword = "testPassword123";
        
        // When
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // Then
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$"));
        assertTrue(hashedPassword.length() > 50);
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    public void testHashPasswordGeneratesDifferentHashes() {
        // Given
        String plainPassword = "samePassword";
        
        // When
        String hash1 = PasswordUtil.hashPassword(plainPassword);
        String hash2 = PasswordUtil.hashPassword(plainPassword);
        
        // Then
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should verify correct password")
    public void testCheckPasswordCorrect() {
        // Given
        String plainPassword = "mySecretPassword";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // When
        boolean result = PasswordUtil.checkPassword(plainPassword, hashedPassword);
        
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject incorrect password")
    public void testCheckPasswordIncorrect() {
        // Given
        String plainPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // When
        boolean result = PasswordUtil.checkPassword(wrongPassword, hashedPassword);
        
        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle null plain password")
    public void testCheckPasswordWithNullPlainPassword() {
        // Given
        String hashedPassword = PasswordUtil.hashPassword("somePassword");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.checkPassword(null, hashedPassword);
        });
    }

    @Test
    @DisplayName("Should handle null hashed password")
    public void testCheckPasswordWithNullHashedPassword() {
        // Given
        String plainPassword = "somePassword";
        
        // When & Then
        assertFalse(PasswordUtil.checkPassword(plainPassword, null));
    }

    @Test
    @DisplayName("Should handle empty passwords")
    public void testCheckPasswordWithEmptyPasswords() {
        // Given
        String emptyPassword = "";
        String hashedEmptyPassword = PasswordUtil.hashPassword(emptyPassword);
        
        // When
        boolean result = PasswordUtil.checkPassword(emptyPassword, hashedEmptyPassword);
        
        // Then
        assertTrue(result);
    }
}