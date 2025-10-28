package com.example.quanlytoanha.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for User class
 */
public class UserTest {

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new Resident(
            1,
            "testuser",
            "test@example.com",
            "Test User",
            Role.RESIDENT,
            Timestamp.from(Instant.now()),
            null,
            "123456789",
            1,
            101,
            new java.util.Date(),
            "123456789012",
            "Chủ hộ"
        );
    }

    @Test
    @DisplayName("Should create user with valid data")
    public void testUserCreation() {
        assertNotNull(testUser);
        assertEquals(1, testUser.getUserId());
        assertEquals("testuser", testUser.getUsername());
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("Test User", testUser.getFullName());
        assertEquals(Role.RESIDENT, testUser.getRole());
        assertEquals("123456789", testUser.getPhoneNumber());
    }

    @Test
    @DisplayName("Should set and get password")
    public void testPasswordSetGet() {
        String password = "hashedPassword123";
        testUser.setPassword(password);
        assertEquals(password, testUser.getPassword());
    }

    @Test
    @DisplayName("Should set permissions from list")
    public void testSetPermissionsFromList() {
        // Given
        List<Permission> permissions = Arrays.asList(
            createPermission("READ_PROFILE"),
            createPermission("VIEW_INVOICES"),
            createPermission("MAKE_PAYMENTS")
        );

        // When
        testUser.setPermissions(permissions);

        // Then
        assertEquals(3, testUser.getPermissions().size());
        assertTrue(testUser.hasPermission("READ_PROFILE"));
        assertTrue(testUser.hasPermission("VIEW_INVOICES"));
        assertTrue(testUser.hasPermission("MAKE_PAYMENTS"));
    }

    @Test
    @DisplayName("Should check permission correctly")
    public void testHasPermission() {
        // Given
        List<Permission> permissions = Arrays.asList(
            createPermission("READ_PROFILE"),
            createPermission("VIEW_INVOICES")
        );
        testUser.setPermissions(permissions);

        // When & Then
        assertTrue(testUser.hasPermission("READ_PROFILE"));
        assertTrue(testUser.hasPermission("VIEW_INVOICES"));
        assertFalse(testUser.hasPermission("DELETE_USER"));
        assertFalse(testUser.hasPermission("ADMIN_ACCESS"));
    }

    @Test
    @DisplayName("Should return false for permission check when no permissions set")
    public void testHasPermissionWhenNoPermissions() {
        // Given - no permissions set
        
        // When & Then
        assertFalse(testUser.hasPermission("ANY_PERMISSION"));
        assertFalse(testUser.hasPermission("READ_PROFILE"));
    }

    @Test
    @DisplayName("Should handle null permissions list")
    public void testSetNullPermissions() {
        // When
        testUser.setPermissions(null);

        // Then
        assertFalse(testUser.hasPermission("ANY_PERMISSION"));
    }

    @Test
    @DisplayName("Should handle empty permissions list")
    public void testSetEmptyPermissions() {
        // Given
        List<Permission> emptyPermissions = Arrays.asList();

        // When
        testUser.setPermissions(emptyPermissions);

        // Then
        assertEquals(0, testUser.getPermissions().size());
        assertFalse(testUser.hasPermission("ANY_PERMISSION"));
    }

    @Test
    @DisplayName("Should update user properties")
    public void testUserPropertyUpdates() {
        // When
        testUser.setEmail("newemail@example.com");
        testUser.setPhoneNumber("987654321");
        testUser.setFullName("Updated Name");

        // Then
        assertEquals("newemail@example.com", testUser.getEmail());
        assertEquals("987654321", testUser.getPhoneNumber());
        assertEquals("Updated Name", testUser.getFullName());
    }

    @Test
    @DisplayName("Should set and get last login timestamp")
    public void testLastLoginTimestamp() {
        // Given
        Timestamp lastLogin = Timestamp.from(Instant.now());

        // When
        testUser.setLastLogin(lastLogin);

        // Then
        assertEquals(lastLogin, testUser.getLastLogin());
    }

    @Test
    @DisplayName("Should handle case-sensitive permission names")
    public void testPermissionCaseSensitivity() {
        // Given
        List<Permission> permissions = Arrays.asList(
            createPermission("READ_PROFILE")
        );
        testUser.setPermissions(permissions);

        // When & Then
        assertTrue(testUser.hasPermission("READ_PROFILE"));
        assertFalse(testUser.hasPermission("read_profile"));
        assertFalse(testUser.hasPermission("READ_Profile"));
    }

    @Test
    @DisplayName("Should not allow duplicate permissions")
    public void testDuplicatePermissions() {
        // Given
        List<Permission> permissions = Arrays.asList(
            createPermission("READ_PROFILE"),
            createPermission("READ_PROFILE"),
            createPermission("VIEW_INVOICES")
        );

        // When
        testUser.setPermissions(permissions);

        // Then
        assertEquals(2, testUser.getPermissions().size()); // Set removes duplicates
        assertTrue(testUser.hasPermission("READ_PROFILE"));
        assertTrue(testUser.hasPermission("VIEW_INVOICES"));
    }

    // Helper method
    private Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setPermissionName(name);
        return permission;
    }
}