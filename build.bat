@echo off
setlocal enabledelayedexpansion

echo === Jarvis AI Android Application Build Script ===
echo This script will build the Jarvis AI APK for Android.
echo.

rem Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install JDK 8 or higher and try again.
    exit /b 1
)

rem Clean previous build if requested
if "%1"=="clean" (
    echo Cleaning previous build...
    call gradlew.bat clean
)

rem Build the debug APK
echo Building APK...
call gradlew.bat assembleDebug

rem Check if build was successful
if %ERRORLEVEL% EQU 0 (
    set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
    if exist "!APK_PATH!" (
        echo Build successful!
        echo APK location: !APK_PATH!
        echo.
        echo === Installation Instructions ===
        echo 1. Transfer the APK to your Android device
        echo 2. On your device, navigate to the APK file and tap on it
        echo 3. Follow the on-screen instructions to install
        echo 4. Make sure to allow 'Install from Unknown Sources' if prompted
        echo.
        echo === ADB Installation (for developers) ===
        echo If you have ADB installed, you can install directly with:
        echo adb install !APK_PATH!
    ) else (
        echo Error: Built APK not found at expected location
        exit /b 1
    )
) else (
    echo Build failed. Please check the error messages above.
    exit /b 1
)

endlocal
