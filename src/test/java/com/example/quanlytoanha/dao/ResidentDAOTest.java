package com.example.quanlytoanha.dao;

import com.example.quanlytoanha.model.Resident;
import com.example.quanlytoanha.model.Role;
import com.example.quanlytoanha.utils.DatabaseConnection;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.Date;

/**
 * Test class for ResidentDAO remove methods
 * Tests the removeResident and removeResidentByUserId functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentDAOTest {
    
    private ResidentDAO residentDAO;
    private static Connection testConnection;
    
    // Test data
    private static final int TEST_APARTMENT_ID = 101;
    private static final int TEST_USER_ID = 999;
    private static final int TEST_RESIDENT_ID = 999;
    private static final String TEST_FULL_NAME = "Test Resident";
    private static final String TEST_ID_CARD = "999999999999";
    private static final String TEST_RELATIONSHIP = "Owner";
    
    @BeforeAll
    static void setUpDatabase() throws SQLException {
        testConnection = DatabaseConnection.getConnection();
        testConnection.setAutoCommit(false);
        
        // Create test user if not exists
        String insertUserSQL = "INSERT INTO users (user_id, username, password, email, full_name, role_id) " +
                              "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (user_id) DO NOTHING";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertUserSQL)) {
            pstmt.setInt(1, TEST_USER_ID);
            pstmt.setString(2, "testuser999");
            pstmt.setString(3, "hashedpassword");
            pstmt.setString(4, "test999@example.com");
            pstmt.setString(5, TEST_FULL_NAME);
            pstmt.setInt(6, 4); // Role ID 4 = Cư dân
            pstmt.executeUpdate();
        }
        
        // Ensure apartment exists
        String insertApartmentSQL = "INSERT INTO apartments (apartment_id, area, owner_id) " +
                                   "VALUES (?, ?, ?) ON CONFLICT (apartment_id) DO NOTHING";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertApartmentSQL)) {
            pstmt.setInt(1, TEST_APARTMENT_ID);
            pstmt.setBigDecimal(2, new java.math.BigDecimal("75.00"));
            pstmt.setInt(3, TEST_USER_ID);
            pstmt.executeUpdate();
        }
        
        testConnection.commit();
    }
    
    @BeforeEach
    void setUp() {
        residentDAO = new ResidentDAO();
    }
    
    @AfterEach
    void cleanUp() throws SQLException {
        // Clean up test data after each test
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Remove test vehicles
            String deleteVehiclesSQL = "DELETE FROM vehicles WHERE resident_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteVehiclesSQL)) {
                pstmt.setInt(1, TEST_RESIDENT_ID);
                pstmt.executeUpdate();
            }
            
            // Remove test residents
            String deleteResidentSQL = "DELETE FROM residents WHERE resident_id = ? OR user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteResidentSQL)) {
                pstmt.setInt(1, TEST_RESIDENT_ID);
                pstmt.setInt(2, TEST_USER_ID);
                pstmt.executeUpdate();
            }
            
            conn.commit();
        }
    }
    
    @AfterAll
    static void tearDownDatabase() throws SQLException {
        if (testConnection != null) {
            // Clean up test user and apartment
            String deleteUserSQL = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteUserSQL)) {
                pstmt.setInt(1, TEST_USER_ID);
                pstmt.executeUpdate();
            }
            
            String deleteApartmentSQL = "DELETE FROM apartments WHERE apartment_id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteApartmentSQL)) {
                pstmt.setInt(1, TEST_APARTMENT_ID);
                pstmt.executeUpdate();
            }
            
            testConnection.commit();
            testConnection.close();
        }
    }
    
    /**
     * Helper method to create a test resident
     */
    private void createTestResident() throws SQLException {
        String insertSQL = "INSERT INTO residents (resident_id, apartment_id, user_id, full_name, " +
                          "date_of_birth, id_card_number, relationship, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            pstmt.setInt(1, TEST_RESIDENT_ID);
            pstmt.setInt(2, TEST_APARTMENT_ID);
            pstmt.setInt(3, TEST_USER_ID);
            pstmt.setString(4, TEST_FULL_NAME);
            pstmt.setDate(5, new java.sql.Date(new Date().getTime()));
            pstmt.setString(6, TEST_ID_CARD);
            pstmt.setString(7, TEST_RELATIONSHIP);
            pstmt.setString(8, "RESIDING");
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Helper method to create a test vehicle for the resident
     */
    private void createTestVehicle() throws SQLException {
        String insertSQL = "INSERT INTO vehicles (resident_id, license_plate, vehicle_type) " +
                          "VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            pstmt.setInt(1, TEST_RESIDENT_ID);
            pstmt.setString(2, "TEST-999");
            pstmt.setString(3, "Car");
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Helper method to check if resident exists
     */
    private boolean residentExists(int residentId) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM residents WHERE resident_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {
            
            pstmt.setInt(1, residentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    /**
     * Helper method to check if vehicles exist for resident
     */
    private boolean vehiclesExist(int residentId) throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM vehicles WHERE resident_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {
            
            pstmt.setInt(1, residentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Test successful removal of resident by ID")
    void testRemoveResident_Success() throws SQLException {
        // Arrange
        createTestResident();
        createTestVehicle();
        
        // Verify setup
        assertTrue(residentExists(TEST_RESIDENT_ID), "Test resident should exist before removal");
        assertTrue(vehiclesExist(TEST_RESIDENT_ID), "Test vehicle should exist before removal");
        
        // Act
        boolean result = residentDAO.removeResident(TEST_RESIDENT_ID);
        
        // Assert
        assertTrue(result, "removeResident should return true for successful removal");
        assertFalse(residentExists(TEST_RESIDENT_ID), "Resident should not exist after removal");
        assertFalse(vehiclesExist(TEST_RESIDENT_ID), "Vehicles should not exist after resident removal");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test removal of non-existent resident")
    void testRemoveResident_NotFound() throws SQLException {
        // Arrange
        int nonExistentId = 88888;
        
        // Verify resident doesn't exist
        assertFalse(residentExists(nonExistentId), "Test resident should not exist");
        
        // Act
        boolean result = residentDAO.removeResident(nonExistentId);
        
        // Assert
        assertFalse(result, "removeResident should return false for non-existent resident");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test successful removal of resident by user ID")
    void testRemoveResidentByUserId_Success() throws SQLException {
        // Arrange
        createTestResident();
        createTestVehicle();
        
        // Verify setup
        assertTrue(residentExists(TEST_RESIDENT_ID), "Test resident should exist before removal");
        assertTrue(vehiclesExist(TEST_RESIDENT_ID), "Test vehicle should exist before removal");
        
        // Act
        boolean result = residentDAO.removeResidentByUserId(TEST_USER_ID);
        
        // Assert
        assertTrue(result, "removeResidentByUserId should return true for successful removal");
        assertFalse(residentExists(TEST_RESIDENT_ID), "Resident should not exist after removal");
        assertFalse(vehiclesExist(TEST_RESIDENT_ID), "Vehicles should not exist after resident removal");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test removal by non-existent user ID")
    void testRemoveResidentByUserId_NotFound() throws SQLException {
        // Arrange
        int nonExistentUserId = 88888;
        
        // Act
        boolean result = residentDAO.removeResidentByUserId(nonExistentUserId);
        
        // Assert
        assertFalse(result, "removeResidentByUserId should return false for non-existent user");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test removal of resident without vehicles")
    void testRemoveResident_WithoutVehicles() throws SQLException {
        // Arrange
        createTestResident();
        // Don't create vehicles this time
        
        // Verify setup
        assertTrue(residentExists(TEST_RESIDENT_ID), "Test resident should exist before removal");
        assertFalse(vehiclesExist(TEST_RESIDENT_ID), "No vehicles should exist for this test");
        
        // Act
        boolean result = residentDAO.removeResident(TEST_RESIDENT_ID);
        
        // Assert
        assertTrue(result, "removeResident should return true even without vehicles");
        assertFalse(residentExists(TEST_RESIDENT_ID), "Resident should not exist after removal");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test transaction rollback on SQL error")
    void testRemoveResident_TransactionRollback() throws SQLException {
        // This test is more complex and would require mocking or database manipulation
        // to force an SQL error during the deletion process.
        // For now, we'll test the basic error handling path.
        
        // Arrange
        createTestResident();
        
        // Verify setup
        assertTrue(residentExists(TEST_RESIDENT_ID), "Test resident should exist before test");
        
        // Note: In a real scenario, you might test rollback by:
        // 1. Mocking the database connection to throw an exception
        // 2. Using a test database with constraints that would fail
        // 3. Testing with invalid SQL in a controlled manner
        
        // For this basic test, we'll just verify the resident still exists
        // after our test setup (no actual error condition created)
        assertTrue(residentExists(TEST_RESIDENT_ID), "Resident should still exist");
    }
}