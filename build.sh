#!/bin/bash

# Simple build script for Jarvis AI Android application

# Set colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Jarvis AI Android Application Build Script ===${NC}"
echo -e "${YELLOW}This script will build the Jarvis AI APK for Android.${NC}"
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
    echo -e "Please install JDK 8 or higher and try again."
    exit 1
fi

# Make gradlew executable
echo -e "${GREEN}Making Gradle wrapper executable...${NC}"
chmod +x gradlew

# Clean previous build if requested
if [ "$1" == "clean" ]; then
    echo -e "${GREEN}Cleaning previous build...${NC}"
    ./gradlew clean
fi

# Build the debug APK
echo -e "${GREEN}Building APK...${NC}"
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo -e "${GREEN}Build successful!${NC}"
        echo -e "${YELLOW}APK location: ${APK_PATH}${NC}"
        echo
        echo -e "${GREEN}=== Installation Instructions ===${NC}"
        echo -e "1. Transfer the APK to your Android device"
        echo -e "2. On your device, navigate to the APK file and tap on it"
        echo -e "3. Follow the on-screen instructions to install"
        echo -e "4. Make sure to allow 'Install from Unknown Sources' if prompted"
        echo
        echo -e "${GREEN}=== ADB Installation (for developers) ===${NC}"
        echo -e "If you have ADB installed, you can install directly with:"
        echo -e "adb install ${APK_PATH}"
    else
        echo -e "${RED}Error: Built APK not found at expected location${NC}"
        exit 1
    fi
else
    echo -e "${RED}Build failed. Please check the error messages above.${NC}"
    exit 1
fi
