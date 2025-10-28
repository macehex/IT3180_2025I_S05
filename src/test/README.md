# Unit Tests Guide

This directory contains comprehensive unit tests for the Building Management System. The tests are organized to cover all major components of the application.

##  Test Structure

```
src/test/java/com/example/quanlytoanha/
├── model/                  # Model layer tests
│   ├── RoleTest.java      # Role enum tests
│   ├── UserTest.java      # User abstract class tests
│   └── InvoiceTest.java   # Invoice model tests
├── service/               # Business logic tests
│   ├── AuthServiceTest.java         # Authentication service tests
│   ├── InvoiceServiceTest.java      # Invoice service tests
│   └── ValidationExceptionTest.java # Exception handling tests
├── session/               # Session management tests
│   └── SessionManagerTest.java     # Session management tests
├── utils/                 # Utility class tests
│   └── PasswordUtilTest.java       # Password utility tests
└── TestSuite.java         # Complete test suite runner
```

## Test Coverage

### Model Tests
- **RoleTest**: Tests the Role enum functionality including ID mapping and validation
- **UserTest**: Tests the abstract User class, permission management, and property setters/getters
- **InvoiceTest**: Tests Invoice model creation, property management, and detail handling

### Service Tests
- **AuthServiceTest**: Tests authentication logic, login/logout functionality, and permission loading
- **InvoiceServiceTest**: Tests invoice calculations, payment processing, and transaction management
- **ValidationExceptionTest**: Tests custom exception handling and error propagation

### Session Management Tests
- **SessionManagerTest**: Tests singleton pattern, session persistence, and user state management

### Utility Tests
- **PasswordUtilTest**: Tests password hashing, verification, and security features

##  Running Tests

### Run all test with pretty output (recommended for simple browser view)
for linux/macOS:
```bash
./run-tests.sh
```
for windows:
```cmd
run-tests.bat
```
Open build/reports/tests/test/index.html in your browser to view detailed results.

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.example.quanlytoanha.utils.PasswordUtilTest"
```

### Run Test Suite
```bash
./gradlew test --tests "com.example.quanlytoanha.TestSuite"
```

### Run Tests with Detailed Output
```bash
./gradlew test --info
```

### Generate Test Report
```bash
./gradlew test
# Reports will be generated in build/reports/tests/test/index.html
```

##  Test Technologies Used

- **JUnit 5**: Primary testing framework
- **Mockito**: Mocking framework for isolating dependencies
- **JUnit Platform Suite**: For organizing and running test suites

##  Test Patterns and Best Practices

### 1. Arrange-Act-Assert (AAA) Pattern
All tests follow the AAA pattern:
```java
@Test
public void testExample() {
    // Arrange (Given)
    String input = "test";
    
    // Act (When)
    String result = someMethod(input);
    
    // Assert (Then)
    assertEquals("expected", result);
}
```

### 2. Descriptive Test Names
Test methods use `@DisplayName` annotations for clear, readable descriptions.

### 3. Mocking External Dependencies
Services are tested in isolation using Mockito mocks for DAO dependencies.

### 4. Edge Case Testing
Tests include edge cases like null inputs, empty collections, and boundary conditions.

### 5. Exception Testing
Proper testing of exception scenarios using `assertThrows()`.

##  Mock Usage Examples

### Mocking DAOs in Service Tests
```java
@BeforeEach
public void setUp() {
    mockUserDAO = mock(UserDAO.class);
    mockPermissionDAO = mock(PermissionDAO.class);
    
    // Inject mocks using reflection
    authService = new AuthService();
    injectMock(authService, "userDAO", mockUserDAO);
}
```

### Static Method Mocking
```java
try (MockedStatic<UserDAO> mockedUserDAO = Mockito.mockStatic(UserDAO.class)) {
    mockedUserDAO.when(() -> UserDAO.updateLastLogin(userId)).thenReturn(true);
    // Test code here
}
```

##  Test Metrics

The test suite covers:
- **Model Classes**: Core business entities and their behaviors
- **Service Layer**: Business logic and data processing
- **Utility Classes**: Helper functions and security features
- **Session Management**: User state and authentication persistence
- **Error Handling**: Exception scenarios and validation

##  Test Configuration

### JUnit 5 Configuration
Tests are configured to run with:
- Java 21 preview features enabled
- UTF-8 encoding
- Platform test runner for suite execution

### Mockito Configuration
- Core mocking capabilities
- JUnit 5 integration
- Static method mocking support

##  Adding New Tests

When adding new tests:

1. **Follow naming conventions**: `ClassNameTest.java`
2. **Use descriptive display names**: `@DisplayName("Should do something when condition")`
3. **Group related tests**: Use `@Nested` classes for logical grouping
4. **Mock external dependencies**: Isolate the unit under test
5. **Test edge cases**: Include null, empty, and boundary conditions
6. **Update TestSuite**: Add new test classes to the suite

##  Debugging Tests

### Running Tests in Debug Mode
```bash
./gradlew test --debug-jvm
```

### Viewing Test Output
Test outputs and logs are available in:
- Console output during test execution
- `build/reports/tests/test/` for HTML reports
- `build/test-results/test/` for XML results

##  Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Gradle Test Documentation](https://docs.gradle.org/current/userguide/java_testing.html)

---
