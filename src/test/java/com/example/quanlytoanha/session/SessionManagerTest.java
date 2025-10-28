package com.example.quanlytoanha.session;

import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.model.User;
import com.example.quanlytoanha.model.Resident;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Unit tests for SessionManager class
 */
public class SessionManagerTest {

    private SessionManager sessionManager;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // Get fresh instance for each test
        sessionManager = SessionManager.getInstance();
        // Clear any existing session
        sessionManager.logout();
        
        // Create a test user (using Resident as concrete implementation)
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
    @DisplayName("Should return singleton instance")
    public void testGetInstance() {
        // Given & When
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();
        
        // Then
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should initially have no logged in user")
    public void testInitialState() {
        // When & Then
        assertNull(sessionManager.getCurrentUser());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("Should login user successfully")
    public void testLogin() {
        // When
        sessionManager.login(testUser);
        
        // Then
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(testUser, sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Should logout user successfully")
    public void testLogout() {
        // Given
        sessionManager.login(testUser);
        assertTrue(sessionManager.isLoggedIn());
        
        // When
        sessionManager.logout();
        
        // Then
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Should handle multiple login calls")
    public void testMultipleLogins() {
        // Given
        User anotherUser = new Resident(
            2, 
            "anotheruser", 
            "another@example.com", 
            "Another User", 
            Role.RESIDENT, 
            Timestamp.from(Instant.now()), 
            null, 
            "987654321",
            2,
            102,
            new java.util.Date(),
            "987654321098",
            "Chủ hộ"
        );
        
        // When
        sessionManager.login(testUser);
        assertEquals(testUser, sessionManager.getCurrentUser());
        
        sessionManager.login(anotherUser);
        
        // Then
        assertEquals(anotherUser, sessionManager.getCurrentUser());
        assertNotEquals(testUser, sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Should handle login with null user")
    public void testLoginWithNull() {
        // When
        sessionManager.login(null);
        
        // Then
        assertNull(sessionManager.getCurrentUser());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    @DisplayName("Should handle logout when no user is logged in")
    public void testLogoutWhenNotLoggedIn() {
        // Given - no user logged in
        assertFalse(sessionManager.isLoggedIn());
        
        // When
        sessionManager.logout();
        
        // Then
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Should maintain session across multiple calls")
    public void testSessionPersistence() {
        // Given
        sessionManager.login(testUser);
        
        // When & Then
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(testUser, sessionManager.getCurrentUser());
        
        // Multiple calls should return the same result
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(testUser, sessionManager.getCurrentUser());
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(testUser, sessionManager.getCurrentUser());
    }
}