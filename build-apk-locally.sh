#!/bin/bash

# Local APK Build Script for Jarvis AI
# This script builds the APK on your local machine

echo "🚀 Jarvis AI - Local APK Builder"
echo "================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install JDK 17 or higher."
    echo "Download from: https://adoptium.net/"
    exit 1
fi

# Check Java version
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo "❌ Java 17 or higher required. Current version: $java_version"
    exit 1
fi

echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

# Set Android SDK path (user can modify this)
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME not set. Trying common locations..."
    
    # Common Android SDK locations
    possible_paths=(
        "$HOME/Android/Sdk"
        "$HOME/Library/Android/sdk"
        "C:/Users/$USER/AppData/Local/Android/Sdk"
        "/opt/android-sdk"
    )
    
    for path in "${possible_paths[@]}"; do
        if [ -d "$path" ]; then
            export ANDROID_HOME="$path"
            echo "✅ Found Android SDK at: $ANDROID_HOME"
            break
        fi
    done
    
    if [ -z "$ANDROID_HOME" ]; then
        echo "❌ Android SDK not found. Please:"
        echo "1. Install Android Studio from: https://developer.android.com/studio"
        echo "2. Or set ANDROID_HOME environment variable to your SDK path"
        exit 1
    fi
else
    echo "✅ Android SDK found at: $ANDROID_HOME"
fi

# Make gradlew executable
echo "🔧 Making gradlew executable..."
chmod +x gradlew

# Clean previous build
echo "🧹 Cleaning previous build..."
./gradlew clean

# Build APK
echo "🏗️  Building APK..."
./gradlew assembleDebug

# Check if APK was created
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    # Create release directory
    mkdir -p release
    
    # Copy APK with friendly name
    cp "$APK_PATH" "release/jarvis-ai.apk"
    
    # Create zip
    cd release
    zip -r ../jarvis-ai-android.zip jarvis-ai.apk
    cd ..
    
    echo ""
    echo "🎉 SUCCESS! APK built successfully!"
    echo "📱 APK Location: release/jarvis-ai.apk"
    echo "📦 Zip Location: jarvis-ai-android.zip"
    echo ""
    echo "📋 Installation Instructions:"
    echo "1. Copy jarvis-ai.apk to your Android device"
    echo "2. Enable 'Install from unknown sources' in Settings"
    echo "3. Tap the APK file to install"
    echo ""
    echo "APK Size: $(du -h release/jarvis-ai.apk | cut -f1)"
else
    echo "❌ Build failed. APK not found at: $APK_PATH"
    echo "Check the build output above for errors."
    exit 1
fi
