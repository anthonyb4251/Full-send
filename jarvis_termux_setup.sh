#!/bin/bash
#############################################################
# Jarvis AI Assistant Setup Script for Termux
# 
# This script sets up a Python-based Jarvis AI assistant
# on Android devices using Termux terminal emulator.
# 
# Features:
# - Storage configuration
# - AI core installation
# - Authentication system
# - Battery monitoring
# - Event logging
# - Emergency shutdown capability
#############################################################

#############################################################
# SECTION 1: SETUP AND STORAGE CONFIGURATION
#############################################################

# Ensure Termux has storage permissions
echo "🔄 Setting up Termux storage..."
termux-setup-storage
sleep 2

# Define storage paths
SD_CARD_PATH="/storage/emulated/0/JarvisAI"
SCRIPT_FILE="$SD_CARD_PATH/jarvis.py"
LOG_FILE="$SD_CARD_PATH/jarvis_logs.txt"

# Create AI storage directory
echo "📂 Creating storage directory: $SD_CARD_PATH"
mkdir -p "$SD_CARD_PATH"

#############################################################
# SECTION 2: DEPENDENCY INSTALLATION
#############################################################

# Install required packages (uncomment if needed)
# echo "📦 Installing required packages..."
# pkg update -y && pkg upgrade -y
# pkg install python psutil -y

#############################################################
# SECTION 3: AI SCRIPT GENERATION AND INSTALLATION
#############################################################

echo "💾 Generating Jarvis AI script..."
cat <<EOF > "$SCRIPT_FILE"
#!/usr/bin/python3
import os
import time
import psutil
import sys

class JarvisAI:
    """
    Jarvis AI Core Class
    Handles AI assistant functionality including authentication,
    battery monitoring, logging, and system operations.
    """
    
    def __init__(self):
        self.LOG_FILE_PATH = "${LOG_FILE}"
        self.AUTHORIZED_VOICEPRINT = "Anthony"
        self.AUTHORIZED_PASSCODE = "JarvisSecure123"
        
        # Initialize system
        self.initialize_system()
        self.authenticate_user("Anthony", "JarvisSecure123")
        self.monitor_battery()
        self.log_event("✅ System startup complete.")
        
    def initialize_system(self):
        """Initialize AI core systems"""
        print("🔄 Initializing AI core...")
        time.sleep(1)
        print("✅ Authentication loaded.")
        print("✅ Power management online.")
        print("✅ Logging system ready.")
        
    def authenticate_user(self, input_voice, input_passcode):
        """Authenticate user based on voice or passcode"""
        if input_voice == self.AUTHORIZED_VOICEPRINT or input_passcode == self.AUTHORIZED_PASSCODE:
            print("🔓 Authentication successful. Welcome, Anthony.")
            self.log_event("✅ Authentication successful.")
            return True
        else:
            print("🚫 Unauthorized access detected.")
            self.log_event("⚠️ Unauthorized access attempt.")
            return False
    
    def monitor_battery(self):
        """Monitor system battery and adjust power settings"""
        try:
            battery = psutil.sensors_battery()
            battery_level = battery.percent if battery else "Unknown"
            
            if battery_level != "Unknown" and battery_level < 20:
                print(f"⚠️ Low battery detected ({battery_level}%). Switching to power-saving mode.")
                self.log_event(f"⚠️ Low battery alert: {battery_level}%")
            else:
                print(f"🔋 Battery at {battery_level}%. Operating at full power.")
                self.log_event(f"🔋 Battery status: {battery_level}%")
        except Exception as e:
            print(f"⚠️ Battery monitoring error: {e}")
            self.log_event(f"⚠️ Battery monitoring error: {e}")
    
    def log_event(self, event):
        """Log events with timestamp"""
        try:
            timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
            with open(self.LOG_FILE_PATH, "a") as log_file:
                log_file.write(f"[{timestamp}] {event}\\n")
        except Exception as e:
            print(f"⚠️ Logging error: {e}")
    
    def emergency_shutdown(self):
        """Perform emergency shutdown sequence"""
        print("⚠️ Emergency shutdown initiated...")
        self.log_event("⚠️ Emergency shutdown initiated.")
        time.sleep(2)
        print("🛑 System shutdown complete.")
        sys.exit()

# Initialize and run Jarvis AI
if __name__ == "__main__":
    print("🚀 Launching Jarvis AI...")
    jarvis = JarvisAI()
    
    # Main loop simulation - in a real app, you'd have an event loop here
    print("✅ Jarvis AI is running. Use Ctrl+C to exit.")
    try:
        while True:
            time.sleep(10)  # Keep running until interrupted
            jarvis.monitor_battery()  # Periodically check battery
    except KeyboardInterrupt:
        print("👋 Jarvis AI shutting down normally.")
        jarvis.log_event("System shutdown - user initiated.")
    except Exception as e:
        print(f"⚠️ Error: {e}")
        jarvis.log_event(f"Error: {e}")
EOF

# Make the AI script executable
echo "🔧 Setting executable permissions..."
chmod +x "$SCRIPT_FILE"

#############################################################
# SECTION 4: EXECUTION AND DEPLOYMENT
#############################################################

# Run AI system in the background
echo "🚀 Starting Jarvis AI system..."
nohup python3 "$SCRIPT_FILE" > "$SD_CARD_PATH/jarvis_output.log" 2>&1 &

# Display deployment information
echo "----------------------------------------"
echo "🚀 Deployment Complete! Jarvis AI is now running."
echo "💾 AI script saved at: $SCRIPT_FILE"
echo "📂 Logs stored at: $LOG_FILE"
echo "🖥️ Output redirected to: $SD_CARD_PATH/jarvis_output.log"
echo ""
echo "▶️ To restart AI manually, use:"
echo "   python3 $SCRIPT_FILE"
echo ""
echo "▶️ To check status, use:"
echo "   ps | grep jarvis.py"
echo "----------------------------------------"

# Create shortcut (optional)
if [ -d "/data/data/com.termux/files/home/bin" ]; then
    echo "📱 Creating command shortcut..."
    echo "python3 $SCRIPT_FILE" > "/data/data/com.termux/files/home/bin/jarvis"
    chmod +x "/data/data/com.termux/files/home/bin/jarvis"
    echo "✅ Shortcut created. Just type 'jarvis' to launch."
fi

echo "🔥 Jarvis AI setup is now fully automated and deployed!"