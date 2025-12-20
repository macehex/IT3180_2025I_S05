./#!/bin/bash
# Build script for JavaFX app with multiple options
# This script temporarily works around Spring Security module issues

echo "JavaFX App Builder - Multiple Options"
echo "====================================="

# Function to build fat JAR (easiest option)
build_fat_jar() {
    echo "Building Fat JAR (easiest option)..."
    
    # Use the new fatJarNoModule task that properly handles module-info.java
    ./gradlew clean fatJarNoModule
    
    if [ $? -eq 0 ]; then
        echo "Fat JAR built successfully!"
        echo "Location: build/libs/quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar"
        echo "Run with: java -jar build/libs/quan-ly-toa-nha-1.0-SNAPSHOT-fat-nomodule.jar"
    else
        echo "Fat JAR build failed"
    fi
}

# Function to try simplified jlink
build_jlink() {
    echo "Trying simplified jlink..."
    ./gradlew clean jlink

    if [ $? -eq 0 ]; then
        echo "jlink build successful!"
        echo "Location: build/image/"
        echo "Run with: ./build/image/bin/app"
    else
        echo "jlink build failed - use fat JAR instead"
    fi
}

# Function to build regular distribution
build_distribution() {
    echo "Building standard distribution..."

    # Temporarily rename module-info.java
    if [ -f "src/main/java/module-info.java" ]; then
        mv src/main/java/module-info.java src/main/java/module-info.java.bak
    fi

    ./gradlew clean distZip distTar

    # Restore module-info.java
    if [ -f "src/main/java/module-info.java.bak" ]; then
        mv src/main/java/module-info.java.bak src/main/java/module-info.java
    fi

    if [ $? -eq 0 ]; then
        echo "Distribution built successfully!"
        echo "Location: build/distributions/"
        ls -la build/distributions/
    fi
}

# Main menu
echo "Choose build option:"
echo "1) Fat JAR (recommended - works everywhere)"
echo "2) Try jlink (native image)"
echo "3) Standard distribution"
echo "4) All options"

read -p "Enter choice (1-4): " choice

case $choice in
    1)
        build_fat_jar
        ;;
    2)
        build_jlink
        ;;
    3)
        build_distribution
        ;;
    4)
        echo "Building all options..."
        build_fat_jar
        echo ""
        build_distribution
        echo ""
        build_jlink
        ;;
    *)
        echo "Invalid choice. Building Fat JAR (safest option)..."
        build_fat_jar
        ;;
esac

echo ""
echo "Build process completed!"