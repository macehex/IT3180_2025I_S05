@echo off
setlocal enabledelayedexpansion

:: Test Runner Script for Quan Ly Toa Nha Application
:: This script provides various options for running the unit tests

echo ==============================================
echo   Quan Ly Toa Nha - Test Runner
echo ==============================================
echo.

:: Function to display usage
:show_usage
echo Usage: %~nx0 [option]
echo.
echo Options:
echo   all        - Run all tests ^(default^)
echo   suite      - Run the complete test suite
echo   model      - Run only model tests
echo   service    - Run only service tests
echo   utils      - Run only utility tests
echo   session    - Run only session tests
echo   coverage   - Run tests with coverage report
echo   clean      - Clean and run all tests
echo   help       - Show this help message
echo.
goto :eof

:: Function to run all tests
:run_all_tests
echo Running all tests...
call gradlew test
goto :eof

:: Function to run test suite
:run_test_suite
echo Running complete test suite...
call gradlew test --tests "com.example.quanlytoanha.TestSuite"
goto :eof

:: Function to run model tests only
:run_model_tests
echo Running model tests...
call gradlew test --tests "com.example.quanlytoanha.model.*"
goto :eof

:: Function to run service tests only
:run_service_tests
echo Running service tests...
call gradlew test --tests "com.example.quanlytoanha.service.*"
goto :eof

:: Function to run utility tests only
:run_utils_tests
echo Running utility tests...
call gradlew test --tests "com.example.quanlytoanha.utils.*"
goto :eof

:: Function to run session tests only
:run_session_tests
echo Running session tests...
call gradlew test --tests "com.example.quanlytoanha.session.*"
goto :eof

:: Function to run tests with coverage
:run_coverage_tests
echo Running tests with coverage report...
call gradlew test jacocoTestReport
echo.
echo Coverage report generated at: build/reports/jacoco/test/html/index.html
goto :eof

:: Function to clean and run tests
:run_clean_tests
echo Cleaning and running all tests...
call gradlew clean test
goto :eof

:: Function to display test results
:show_results
echo.
echo ==============================================
echo   Test Results
echo ==============================================
echo Test report: build/reports/tests/test/index.html
echo Test results: build/test-results/test/
echo.

if exist "build/reports/tests/test/index.html" (
    echo Test report generated successfully!
    echo Open build/reports/tests/test/index.html in your browser to view detailed results.
) else (
    echo Test report not found. Tests may have failed to run.
)
goto :eof

:: Main script logic
set "option=%~1"
if "!option!"=="" set "option=all"

if "!option!"=="all" (
    call :run_all_tests
) else if "!option!"=="suite" (
    call :run_test_suite
) else if "!option!"=="model" (
    call :run_model_tests
) else if "!option!"=="service" (
    call :run_service_tests
) else if "!option!"=="utils" (
    call :run_utils_tests
) else if "!option!"=="session" (
    call :run_session_tests
) else if "!option!"=="coverage" (
    call :run_coverage_tests
) else if "!option!"=="clean" (
    call :run_clean_tests
) else if "!option!"=="help" (
    call :show_usage
    exit /b 0
) else (
    echo Unknown option: !option!
    echo.
    call :show_usage
    exit /b 1
)

:: Show results after running tests
if !errorlevel! equ 0 (
    echo.
    echo Tests completed successfully!
    call :show_results
) else (
    echo.
    echo Tests failed! Check the output above for details.
    echo Detailed error report: build/reports/tests/test/index.html
)

endlocal