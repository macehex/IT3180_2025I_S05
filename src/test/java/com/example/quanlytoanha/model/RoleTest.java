package com.example.quanlytoanha.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Role enum
 */
public class RoleTest {

    @Test
    @DisplayName("Should return correct role ID for ADMIN")
    public void testAdminRoleId() {
        assertEquals(1, Role.ADMIN.getRoleId());
    }

    @Test
    @DisplayName("Should return correct role ID for ACCOUNTANT")
    public void testAccountantRoleId() {
        assertEquals(2, Role.ACCOUNTANT.getRoleId());
    }

    @Test
    @DisplayName("Should return correct role ID for POLICE")
    public void testPoliceRoleId() {
        assertEquals(3, Role.POLICE.getRoleId());
    }

    @Test
    @DisplayName("Should return correct role ID for RESIDENT")
    public void testResidentRoleId() {
        assertEquals(4, Role.RESIDENT.getRoleId());
    }

    @Test
    @DisplayName("Should return correct role name for ADMIN")
    public void testAdminRoleName() {
        assertEquals("Ban quản trị", Role.ADMIN.getRoleName());
    }

    @Test
    @DisplayName("Should return correct role name for ACCOUNTANT")
    public void testAccountantRoleName() {
        assertEquals("Kế toán", Role.ACCOUNTANT.getRoleName());
    }

    @Test
    @DisplayName("Should return correct role name for POLICE")
    public void testPoliceRoleName() {
        assertEquals("Công an", Role.POLICE.getRoleName());
    }

    @Test
    @DisplayName("Should return correct role name for RESIDENT")
    public void testResidentRoleName() {
        assertEquals("Cư dân", Role.RESIDENT.getRoleName());
    }

    @Test
    @DisplayName("Should return correct role from valid ID")
    public void testFromIdValid() {
        assertEquals(Role.ADMIN, Role.fromId(1));
        assertEquals(Role.ACCOUNTANT, Role.fromId(2));
        assertEquals(Role.POLICE, Role.fromId(3));
        assertEquals(Role.RESIDENT, Role.fromId(4));
    }

    @Test
    @DisplayName("Should throw exception for invalid role ID")
    public void testFromIdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromId(0));
        assertThrows(IllegalArgumentException.class, () -> Role.fromId(5));
        assertThrows(IllegalArgumentException.class, () -> Role.fromId(-1));
        assertThrows(IllegalArgumentException.class, () -> Role.fromId(100));
    }

    @Test
    @DisplayName("Should have four total roles")
    public void testTotalRoles() {
        Role[] roles = Role.values();
        assertEquals(4, roles.length);
    }

    @Test
    @DisplayName("Should maintain role ID uniqueness")
    public void testRoleIdUniqueness() {
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            for (int j = i + 1; j < roles.length; j++) {
                assertNotEquals(roles[i].getRoleId(), roles[j].getRoleId());
            }
        }
    }
}