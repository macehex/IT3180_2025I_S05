package com.example.quanlytoanha.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

/**
 * Unit tests for Resident class
 */
public class ResidentTest {

    private Resident resident;

    @BeforeEach
    public void setUp() {
        resident = new Resident();
    }

    @Test
    @DisplayName("Should create resident with default constructor")
    public void testDefaultConstructor() {
        assertNotNull(resident);
        assertEquals(0, resident.getUserId());
        assertEquals(0, resident.getResidentId());
        assertEquals(0, resident.getApartmentId());
    }

    @Test
    @DisplayName("Should create resident with parameterized constructor")
    public void testParameterizedConstructor() {
        // Given
        int userId = 1;
        String username = "resident1";
        String email = "resident@example.com";
        String fullName = "John Doe";
        Role role = Role.RESIDENT;
        Timestamp createdAt = Timestamp.from(Instant.now());
        Timestamp lastLogin = null;
        String phoneNumber = "123456789";
        int residentId = 101;
        int apartmentId = 201;
        Date dateOfBirth = new Date();
        String idCardNumber = "123456789012";
        String relationship = "Chủ hộ";

        // When
        Resident paramResident = new Resident(
            userId, username, email, fullName, role, createdAt, lastLogin, phoneNumber,
            residentId, apartmentId, dateOfBirth, idCardNumber, relationship
        );

        // Then
        assertEquals(userId, paramResident.getUserId());
        assertEquals(username, paramResident.getUsername());
        assertEquals(email, paramResident.getEmail());
        assertEquals(fullName, paramResident.getFullName());
        assertEquals(role, paramResident.getRole());
        assertEquals(phoneNumber, paramResident.getPhoneNumber());
        assertEquals(residentId, paramResident.getResidentId());
        assertEquals(apartmentId, paramResident.getApartmentId());
        assertEquals(dateOfBirth, paramResident.getDateOfBirth());
        assertEquals(idCardNumber, paramResident.getIdCardNumber());
        assertEquals(relationship, paramResident.getRelationship());
    }

    @Test
    @DisplayName("Should set and get resident ID")
    public void testResidentId() {
        // When
        resident.setResidentId(123);

        // Then
        assertEquals(123, resident.getResidentId());
    }

    @Test
    @DisplayName("Should set and get apartment ID")
    public void testApartmentId() {
        // When
        resident.setApartmentId(456);

        // Then
        assertEquals(456, resident.getApartmentId());
    }

    @Test
    @DisplayName("Should set and get date of birth")
    public void testDateOfBirth() {
        // Given
        Date dateOfBirth = new Date();

        // When
        resident.setDateOfBirth(dateOfBirth);

        // Then
        assertEquals(dateOfBirth, resident.getDateOfBirth());
    }

    @Test
    @DisplayName("Should set and get ID card number")
    public void testIdCardNumber() {
        // Given
        String idCardNumber = "123456789012";

        // When
        resident.setIdCardNumber(idCardNumber);

        // Then
        assertEquals(idCardNumber, resident.getIdCardNumber());
    }

    @Test
    @DisplayName("Should set and get relationship")
    public void testRelationship() {
        // Given
        String relationship = "Con cái";

        // When
        resident.setRelationship(relationship);

        // Then
        assertEquals(relationship, resident.getRelationship());
    }

    @Test
    @DisplayName("Should set and get status")
    public void testStatus() {
        // Given
        String status = "RESIDING";

        // When
        resident.setStatus(status);

        // Then
        assertEquals(status, resident.getStatus());
    }

    @Test
    @DisplayName("Should set and get move-in date")
    public void testMoveInDate() {
        // Given
        Date moveInDate = new Date();

        // When
        resident.setMoveInDate(moveInDate);

        // Then
        assertEquals(moveInDate, resident.getMoveInDate());
    }

    @Test
    @DisplayName("Should display dashboard correctly")
    public void testDisplayDashboard() {
        // Given
        resident.setFullName("Test Resident");

        // When & Then (testing that it doesn't throw an exception)
        assertDoesNotThrow(() -> resident.displayDashboard());
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    public void testNullValues() {
        // When
        resident.setDateOfBirth(null);
        resident.setIdCardNumber(null);
        resident.setRelationship(null);
        resident.setStatus(null);
        resident.setMoveInDate(null);

        // Then
        assertNull(resident.getDateOfBirth());
        assertNull(resident.getIdCardNumber());
        assertNull(resident.getRelationship());
        assertNull(resident.getStatus());
        assertNull(resident.getMoveInDate());
    }

    @Test
    @DisplayName("Should handle empty string values")
    public void testEmptyStringValues() {
        // When
        resident.setIdCardNumber("");
        resident.setRelationship("");
        resident.setStatus("");

        // Then
        assertEquals("", resident.getIdCardNumber());
        assertEquals("", resident.getRelationship());
        assertEquals("", resident.getStatus());
    }

    @Test
    @DisplayName("Should inherit User properties correctly")
    public void testUserInheritance() {
        // Given
        resident.setUserId(1);
        resident.setUsername("testuser");
        resident.setEmail("test@example.com");
        resident.setFullName("Test User");
        resident.setPhoneNumber("123456789");

        // Then
        assertEquals(1, resident.getUserId());
        assertEquals("testuser", resident.getUsername());
        assertEquals("test@example.com", resident.getEmail());
        assertEquals("Test User", resident.getFullName());
        assertEquals("123456789", resident.getPhoneNumber());
    }

    @Test
    @DisplayName("Should be instance of User")
    public void testInheritance() {
        assertTrue(resident instanceof User);
    }

    @Test
    @DisplayName("Should handle business methods without exceptions")
    public void testBusinessMethods() {
        // When & Then
        assertDoesNotThrow(() -> resident.payInvoice());
        assertDoesNotThrow(() -> resident.submitServiceRequest());
    }
}