#!/bin/bash

# Create Installation Package for Jarvis AI
# This script creates a complete installation package

echo "📦 Creating Jarvis AI Installation Package"
echo "=========================================="

# Build the APK first
echo "🏗️  Building APK..."
chmod +x gradlew
./gradlew assembleDebug

# Check if APK was created
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK build failed. Cannot create package."
    exit 1
fi

# Create package directory
PACKAGE_DIR="jarvis-ai-installation-package"
mkdir -p "$PACKAGE_DIR"

# Copy APK with friendly name
cp "$APK_PATH" "$PACKAGE_DIR/jarvis-ai.apk"

# Create installation instructions
cat > "$PACKAGE_DIR/INSTALLATION-INSTRUCTIONS.txt" << 'EOF'
📱 JARVIS AI ANDROID INSTALLATION GUIDE
=====================================

🎯 WHAT YOU HAVE:
- jarvis-ai.apk - The Jarvis AI Android application

🚀 INSTALLATION STEPS:

1. ENABLE UNKNOWN SOURCES:
   - Go to Android Settings
   - Search for "Install unknown apps" or "Unknown sources"
   - Enable installation from unknown sources for your file manager

2. TRANSFER APK TO YOUR DEVICE:
   - Copy jarvis-ai.apk to your Android device
   - You can use USB cable, email, cloud storage, etc.

3. INSTALL THE APK:
   - On your Android device, navigate to jarvis-ai.apk
   - Tap the file
   - Android will ask for permission to install
   - Tap "Install" and follow the prompts

4. LAUNCH THE APP:
   - Find "Jarvis AI" in your app drawer
   - Tap to launch
   - Grant any requested permissions (storage access)

✨ FEATURES:
- AI System interface with dark theme
- Battery monitoring with power-saving mode
- System status monitoring
- Alert mode functionality
- Event logging to device storage
- Emergency shutdown capability

🔧 TROUBLESHOOTING:
- If installation fails, make sure "Install from unknown sources" is enabled
- The app requires Android 6.0 (API 23) or higher
- Allow storage permissions when prompted for full functionality

🎉 ENJOY YOUR JARVIS AI ASSISTANT!
EOF

# Create features documentation
cat > "$PACKAGE_DIR/FEATURES.txt" << 'EOF'
🤖 JARVIS AI FEATURES
==================

🎨 USER INTERFACE:
- Dark theme similar to J.A.R.V.I.S from Iron Man
- Clean, modern Android interface
- Status indicators and controls

🔋 BATTERY MONITORING:
- Real-time battery level display
- Automatic power-saving mode when battery is low
- Battery status notifications

📊 SYSTEM STATUS:
- Normal operation mode
- Alert mode for enhanced monitoring
- System status display

📝 EVENT LOGGING:
- Logs all system events to device storage
- Stores logs in /storage/emulated/0/JarvisAI/
- Persistent logging across app sessions

⚠️ ALERT MODE:
- Switchable alert status
- Visual indicators for system state
- Enhanced monitoring capabilities

🛑 EMERGENCY CONTROLS:
- Emergency shutdown functionality
- Safe system termination
- Confirmation dialogs for safety

🔐 PERMISSIONS REQUIRED:
- Storage access (for logging)
- Battery stats (for monitoring)
- Foreground service (for background operation)

📱 COMPATIBILITY:
- Android 6.0+ (API level 23 or higher)
- Works on phones and tablets
- Optimized for portrait orientation
EOF

# Create README for the package
cat > "$PACKAGE_DIR/README.txt" << 'EOF'
📱 JARVIS AI ANDROID APP - INSTALLATION PACKAGE
==============================================

This package contains everything you need to install and run the Jarvis AI Android application.

📦 PACKAGE CONTENTS:
- jarvis-ai.apk - The Android application
- INSTALLATION-INSTRUCTIONS.txt - Step-by-step installation guide
- FEATURES.txt - Complete feature list
- README.txt - This file

🚀 QUICK START:
1. Read INSTALLATION-INSTRUCTIONS.txt
2. Copy jarvis-ai.apk to your Android device
3. Enable "Install from unknown sources"
4. Install the APK
5. Launch Jarvis AI from your app drawer

💡 NEED HELP?
- Read the full installation instructions
- Check the features documentation
- Make sure your Android version is 6.0 or higher

🎉 Enjoy your Jarvis AI assistant!

Version: 1.0
Build Date: $(date)
EOF

# Get APK info
APK_SIZE=$(du -h "$PACKAGE_DIR/jarvis-ai.apk" | cut -f1)

# Create the final zip package
ZIP_NAME="jarvis-ai-v1.0-release.zip"
cd "$PACKAGE_DIR"
zip -r "../$ZIP_NAME" .
cd ..

# Create a GitHub-style release info file
cat > "RELEASE-INFO.md" << EOF
# 📱 Jarvis AI Android App v1.0 Release

**A sophisticated AI assistant app for Android devices, inspired by J.A.R.V.I.S from Iron Man.**

## 📥 Download

- **jarvis-ai.apk** - Ready-to-install Android application ($APK_SIZE)
- **$ZIP_NAME** - Complete installation package with documentation

## 🚀 Quick Installation

1. **Download** \`jarvis-ai.apk\` 
2. **Enable** "Install from unknown sources" in Android Settings
3. **Install** the APK on your device
4. **Launch** Jarvis AI from your app drawer

## ✨ Key Features

- 🎨 **Dark J.A.R.V.I.S Theme** - Sleek interface inspired by Iron Man's AI
- 🔋 **Battery Monitoring** - Real-time battery status with power-saving mode
- 📊 **System Status** - Monitor system health with normal/alert modes
- 📝 **Event Logging** - Comprehensive logging to device storage
- 🛑 **Emergency Controls** - Safe shutdown and system management
- 🔐 **Secure Operation** - Proper permission handling and data protection

## 📱 Compatibility

- **Minimum:** Android 6.0 (API 23)
- **Recommended:** Android 8.0+ for optimal performance
- **Devices:** Phones and tablets supported
- **Root:** Not required - works on stock Android

## 📋 Installation Requirements

- Android device with "Install from unknown sources" enabled
- ~$APK_SIZE of storage space
- Storage permission (requested during first launch)

---

**Build Information:**
- 📅 **Built:** $(date +'%Y-%m-%d %H:%M:%S')
- 🏗️ **Version:** v1.0-local-build
- 📦 **Package:** $ZIP_NAME

**Need help?** Check the installation guide included in the zip package.
EOF

echo ""
echo "🎉 FULL RELEASE PACKAGE CREATED SUCCESSFULLY!"
echo "================================================"
echo "📦 Release Package: $ZIP_NAME"
echo "📱 APK Size: $APK_SIZE"
echo "📋 Release Contents:"
echo "   ✅ jarvis-ai.apk (Android application)"
echo "   ✅ Installation instructions"
echo "   ✅ Features documentation"
echo "   ✅ README file"
echo "   ✅ Release information"
echo ""
echo "🚀 INSTALLATION GUIDE:"
echo "1. Extract $ZIP_NAME"
echo "2. Read INSTALLATION-INSTRUCTIONS.txt"
echo "3. Transfer jarvis-ai.apk to your Android device"
echo "4. Install the APK (enable unknown sources first)"
echo "5. Launch Jarvis AI from your app drawer"
echo ""
echo "📱 PERFECT FOR:"
echo "   ✅ Galaxy A14 (non-rooted)"
echo "   ✅ Any Android 6.0+ device"
echo "   ✅ Phones and tablets"
echo ""
echo "🎯 YOUR JARVIS AI RELEASE IS READY FOR DISTRIBUTION!"
echo "📥 Share $ZIP_NAME with anyone who wants the app!"
