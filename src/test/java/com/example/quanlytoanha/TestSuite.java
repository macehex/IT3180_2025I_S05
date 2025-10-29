package com.example.quanlytoanha;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import com.example.quanlytoanha.utils.PasswordUtilTest;
import com.example.quanlytoanha.model.RoleTest;
import com.example.quanlytoanha.model.UserTest;
import com.example.quanlytoanha.model.InvoiceTest;
import com.example.quanlytoanha.model.ResidentTest;
import com.example.quanlytoanha.dao.ResidentDAOTest;
import com.example.quanlytoanha.session.SessionManagerTest;
import com.example.quanlytoanha.service.SimpleAuthServiceTest;
import com.example.quanlytoanha.service.SimpleInvoiceServiceTest;
import com.example.quanlytoanha.service.ValidationExceptionTest;

/**
 * Test Suite for Quan Ly Toa Nha Application
 * 
 * This suite runs all unit tests for the building management system.
 * It covers utilities, models, services, and session management.
 */
@Suite
@SuiteDisplayName("Quan Ly Toa Nha - Complete Test Suite")
@SelectClasses({
    // Utility Tests
    PasswordUtilTest.class,
    
    // Model Tests
    RoleTest.class,
    UserTest.class,
    InvoiceTest.class,
    ResidentTest.class,
    
    // DAO Tests
    ResidentDAOTest.class,
    
    // Session Management Tests
    SessionManagerTest.class,
    
    // Service Tests
    SimpleAuthServiceTest.class,
    SimpleInvoiceServiceTest.class,
    ValidationExceptionTest.class
})
public class TestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}