@echo off
setlocal enabledelayedexpansion

echo 📦 Creating Jarvis AI Installation Package
echo ==========================================

REM Build the APK first
echo 🏗️  Building APK...
call gradlew.bat assembleDebug

REM Check if APK was created
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
if not exist "%APK_PATH%" (
    echo ❌ APK build failed. Cannot create package.
    exit /b 1
)

REM Create package directory
set PACKAGE_DIR=jarvis-ai-installation-package
if exist "%PACKAGE_DIR%" rmdir /s /q "%PACKAGE_DIR%"
mkdir "%PACKAGE_DIR%"

REM Copy APK with friendly name
copy "%APK_PATH%" "%PACKAGE_DIR%\jarvis-ai.apk"

REM Create installation instructions
(
echo 📱 JARVIS AI ANDROID INSTALLATION GUIDE
echo =====================================
echo.
echo 🎯 WHAT YOU HAVE:
echo - jarvis-ai.apk - The Jarvis AI Android application
echo.
echo 🚀 INSTALLATION STEPS:
echo.
echo 1. ENABLE UNKNOWN SOURCES:
echo    - Go to Android Settings
echo    - Search for "Install unknown apps" or "Unknown sources"
echo    - Enable installation from unknown sources for your file manager
echo.
echo 2. TRANSFER APK TO YOUR DEVICE:
echo    - Copy jarvis-ai.apk to your Android device
echo    - You can use USB cable, email, cloud storage, etc.
echo.
echo 3. INSTALL THE APK:
echo    - On your Android device, navigate to jarvis-ai.apk
echo    - Tap the file
echo    - Android will ask for permission to install
echo    - Tap "Install" and follow the prompts
echo.
echo 4. LAUNCH THE APP:
echo    - Find "Jarvis AI" in your app drawer
echo    - Tap to launch
echo    - Grant any requested permissions ^(storage access^)
echo.
echo ✨ FEATURES:
echo - AI System interface with dark theme
echo - Battery monitoring with power-saving mode
echo - System status monitoring
echo - Alert mode functionality
echo - Event logging to device storage
echo - Emergency shutdown capability
echo.
echo 🎉 ENJOY YOUR JARVIS AI ASSISTANT!
) > "%PACKAGE_DIR%\INSTALLATION-INSTRUCTIONS.txt"

REM Create README
(
echo 📱 JARVIS AI ANDROID APP - INSTALLATION PACKAGE
echo ==============================================
echo.
echo This package contains everything you need to install and run the Jarvis AI Android application.
echo.
echo 📦 PACKAGE CONTENTS:
echo - jarvis-ai.apk - The Android application
echo - INSTALLATION-INSTRUCTIONS.txt - Step-by-step installation guide
echo - README.txt - This file
echo.
echo 🚀 QUICK START:
echo 1. Read INSTALLATION-INSTRUCTIONS.txt
echo 2. Copy jarvis-ai.apk to your Android device
echo 3. Enable "Install from unknown sources"
echo 4. Install the APK
echo 5. Launch Jarvis AI from your app drawer
echo.
echo 🎉 Enjoy your Jarvis AI assistant!
echo.
echo Version: 1.0
echo Build Date: %date% %time%
) > "%PACKAGE_DIR%\README.txt"

REM Create the final zip package
set ZIP_NAME=jarvis-ai-android-installer.zip
if exist "%ZIP_NAME%" del "%ZIP_NAME%"

REM Use PowerShell to create zip
powershell -command "Compress-Archive -Path '%PACKAGE_DIR%\*' -DestinationPath '%ZIP_NAME%'"

if exist "%ZIP_NAME%" (
    echo.
    echo 🎉 INSTALLATION PACKAGE CREATED SUCCESSFULLY!
    echo 📦 Package: %ZIP_NAME%
    echo 📋 Package Contents:
    echo    - jarvis-ai.apk ^(Android application^)
    echo    - Installation instructions
    echo    - README file
    echo.
    echo 🚀 TO INSTALL:
    echo 1. Download and extract %ZIP_NAME%
    echo 2. Follow the instructions in INSTALLATION-INSTRUCTIONS.txt
    echo 3. Install jarvis-ai.apk on your Android device
    echo.
    echo ✅ Ready for distribution!
) else (
    echo ❌ Failed to create zip package
    exit /b 1
)

endlocal
