#!/bin/bash

# Air Quality Tracker API - Run Script
# This script builds and runs the Spring Boot application

echo "=================================================="
echo "   Global Air Quality Tracker API - Startup"
echo "=================================================="
echo ""

# Navigate to project directory
cd "$(dirname "$0")"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java is not installed or not in PATH"
    echo "   Please install Java 11 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ ERROR: Java 11 or higher is required"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven is not installed or not in PATH"
    echo "   Please install Maven: brew install maven"
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"
echo ""

# Check if JAR already exists
if [ -f "target/air-quality-tracker-api.jar" ]; then
    echo "📦 Found existing JAR file"
    read -p "   Do you want to rebuild? (y/n): " rebuild
    if [ "$rebuild" = "y" ] || [ "$rebuild" = "Y" ]; then
        echo ""
        echo "🔨 Building application..."
        mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "❌ Build failed!"
            exit 1
        fi
    fi
else
    echo "🔨 Building application for the first time..."
    echo "   This may take a few minutes..."
    echo ""
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

echo ""
echo "=================================================="
echo "   Starting Air Quality Tracker API"
echo "=================================================="
echo ""
echo "📍 API will be available at: http://localhost:8080"
echo "📍 Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "📍 Health Check: http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""
echo "=================================================="
echo ""

# Run the application
java -jar target/air-quality-tracker-api.jar
