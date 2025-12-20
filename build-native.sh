#!/bin/bash

# KnullCI Native Build Script
# Builds a native executable using GraalVM

set -e

echo "🚀 Building KnullCI Native Image..."
echo ""

# Check if GraalVM is installed
if ! command -v native-image &> /dev/null; then
    echo "❌ GraalVM native-image not found!"
    echo ""
    echo "Install GraalVM:"
    echo "  brew install --cask graalvm-jdk@21"
    echo ""
    echo "Set JAVA_HOME:"
    echo "  export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
    echo "  export PATH=\$JAVA_HOME/bin:\$PATH"
    echo ""
    exit 1
fi

echo "✅ GraalVM detected: $(native-image --version | head -n 1)"
echo ""

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean

# Build with native profile
echo "🔨 Compiling native image (this may take several minutes)..."
mvn -Pnative -DskipTests package

if [ -f "target/knull" ]; then
    echo ""
    echo "✅ Native executable created successfully!"
    echo ""
    echo "📦 Executable location: target/knull"
    echo "📊 Size: $(du -h target/knull | cut -f1)"
    echo ""
    echo "🎯 Run with: ./target/knull"
    echo ""
else
    echo ""
    echo "❌ Build failed - native executable not found"
    exit 1
fi
