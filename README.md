# Jarvis AI Android Application

## Overview
Jarvis AI is an Android application that brings AI assistant functionality to your Android device. This project converts the previously Termux-based scripts into a native Android application with a modern interface.

## Features
- Clean, modern UI themed like a J.A.R.V.I.S interface
- System status monitoring and notifications
- Battery level monitoring with power-saving mode for low battery
- Event logging to external storage
- Alert mode activation
- Emergency shutdown capability

## Building the APK

### Prerequisites
- Java JDK 8 or higher
- Android SDK (automatically downloaded by Gradle if not present)
- Internet connection (for downloading dependencies)

### Build Steps
1. Clone this repository
   ```
   git clone https://github.com/anthonyb4251/Full-send.git
   cd Full-send
   ```

2. Make the Gradle wrapper executable
   ```
   chmod +x gradlew  # On Linux/Mac
   ```

3. Build the debug APK
   ```
   ./gradlew assembleDebug
   ```
   or on Windows:
   ```
   gradlew.bat assembleDebug
   ```

4. The APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Installing the APK

### Using ADB (Android Debug Bridge)
1. Enable Developer options and USB debugging on your Android device
2. Connect your device to your computer
3. Run the following command:
   ```
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Manual Installation
1. Transfer the APK to your Android device
2. On your device, navigate to the APK file
3. Tap on the file and follow the on-screen instructions to install
4. You may need to allow installation from unknown sources in your device settings

## Required Permissions
- **Storage Access**: The app needs permission to read and write files for storing logs and configuration
- **Battery Stats**: For monitoring battery level and enabling power-saving mode
- **Foreground Service**: To keep the AI assistant running in the background

## How to Use
1. Launch the Jarvis AI application
2. Grant the requested permissions
3. The AI system will initialize automatically
4. Use the "Activate Alert Mode" button to switch the system to alert status
5. Monitor your battery status and system logs on the main screen
6. Use the "Emergency Shutdown" button to completely shut down the AI assistant

## Development
This Android application replaces the previous Termux-based scripts with native Android components, providing a more integrated and user-friendly experience while maintaining the same core functionality.

## Automatic Builds
The APK is automatically built using GitHub Actions. Download the latest APK from the [Releases](https://github.com/anthonyb4251/Full-send/releases) section.

### Quick Download
Get the latest APK: [Download Jarvis AI APK](https://github.com/anthonyb4251/Full-send/releases/latest)

*APK builds automatically when pushing to build-apk branch - BUILD TRIGGERED*