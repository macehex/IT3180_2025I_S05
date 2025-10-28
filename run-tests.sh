#!/bin/bash

# Test Runner Script for Quan Ly Toa Nha Application
# This script provides various options for running the unit tests

echo "=============================================="
echo "  Quan Ly Toa Nha - Test Runner"
echo "=============================================="
echo ""

# Function to display usage
show_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  all        - Run all tests (default)"
    echo "  suite      - Run the complete test suite"
    echo "  model      - Run only model tests"
    echo "  service    - Run only service tests"
    echo "  utils      - Run only utility tests"
    echo "  session    - Run only session tests"
    echo "  coverage   - Run tests with coverage report"
    echo "  clean      - Clean and run all tests"
    echo "  help       - Show this help message"
    echo ""
}

# Function to run all tests
run_all_tests() {
    echo "Running all tests..."
    ./gradlew test
}

# Function to run test suite
run_test_suite() {
    echo "Running complete test suite..."
    ./gradlew test --tests "com.example.quanlytoanha.TestSuite"
}

# Function to run model tests only
run_model_tests() {
    echo "Running model tests..."
    ./gradlew test --tests "com.example.quanlytoanha.model.*"
}

# Function to run service tests only
run_service_tests() {
    echo "Running service tests..."
    ./gradlew test --tests "com.example.quanlytoanha.service.*"
}

# Function to run utility tests only
run_utils_tests() {
    echo "Running utility tests..."
    ./gradlew test --tests "com.example.quanlytoanha.utils.*"
}

# Function to run session tests only
run_session_tests() {
    echo "Running session tests..."
    ./gradlew test --tests "com.example.quanlytoanha.session.*"
}

# Function to run tests with coverage
run_coverage_tests() {
    echo "Running tests with coverage report..."
    ./gradlew test jacocoTestReport
    echo ""
    echo "Coverage report generated at: build/reports/jacoco/test/html/index.html"
}

# Function to clean and run tests
run_clean_tests() {
    echo "Cleaning and running all tests..."
    ./gradlew clean test
}

# Function to display test results
show_results() {
    echo ""
    echo "=============================================="
    echo "  Test Results"
    echo "=============================================="
    echo "Test report: build/reports/tests/test/index.html"
    echo "Test results: build/test-results/test/"
    echo ""

    if [ -f "build/reports/tests/test/index.html" ]; then
        echo "Test report generated successfully!"
        echo "Open build/reports/tests/test/index.html in your browser to view detailed results."
    else
        echo "Test report not found. Tests may have failed to run."
    fi
}

# Main script logic
case "${1:-all}" in
    "all")
        run_all_tests
        ;;
    "suite")
        run_test_suite
        ;;
    "model")
        run_model_tests
        ;;
    "service")
        run_service_tests
        ;;
    "utils")
        run_utils_tests
        ;;
    "session")
        run_session_tests
        ;;
    "coverage")
        run_coverage_tests
        ;;
    "clean")
        run_clean_tests
        ;;
    "help")
        show_usage
        exit 0
        ;;
    *)
        echo "Unknown option: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac

# Show results after running tests
if [ $? -eq 0 ]; then
    echo ""
    echo "Tests completed successfully!"
    show_results
else
    echo ""
    echo "Tests failed! Check the output above for details."
    echo "Detailed error report: build/reports/tests/test/index.html"
fi