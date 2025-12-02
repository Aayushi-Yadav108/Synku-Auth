#!/bin/bash

# RecN Auth Service - Quick Start Script

echo "=========================================="
echo "RecN Auth Service - Starting Application"
echo "=========================================="
echo ""

# Check Java version
echo "Checking Java version..."
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi
echo "✅ Maven version: $(mvn -version | head -n 1)"
echo ""

# Check MySQL
echo "Checking MySQL connection..."
if ! command -v mysql &> /dev/null; then
    echo "⚠️  MySQL client not found. Make sure MySQL server is running."
else
    echo "✅ MySQL client found"
fi
echo ""

# Build the project
echo "Building the project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
echo "✅ Build successful!"
echo ""

# Run the application
echo "Starting Auth Service..."
echo "Application will be available at: http://localhost:8081/api/v1"
echo ""
echo "Press Ctrl+C to stop the application"
echo "=========================================="
echo ""

mvn spring-boot:run

