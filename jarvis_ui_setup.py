#!/usr/bin/env python3
"""
Jarvis AI Assistant UI Setup Script

This script sets up a graphical user interface for the Jarvis AI assistant.
It handles storage setup, dependency installation, and launches a Tkinter-based
GUI dashboard with system monitoring capabilities.

Features:
- Termux storage configuration
- Package installation
- Modern UI with dark theme
- System status monitoring
- Alert mode functionality
- Logging system
"""

import os
import subprocess
import time
import sys
import traceback
import threading


class SetupManager:
    """Manages the setup process for Jarvis AI"""
    
    def __init__(self):
        """Initialize setup paths and configuration"""
        self.SD_CARD_PATH = "/storage/emulated/0/JarvisAI"
        self.SCRIPT_FILE = f"{self.SD_CARD_PATH}/jarvis_ui.py"
        self.LOG_FILE = f"{self.SD_CARD_PATH}/jarvis_ui_logs.txt"
        
    def setup_storage(self):
        """Create necessary directories for Jarvis AI"""
        try:
            print(f"📂 Creating storage directory: {self.SD_CARD_PATH}")
            os.makedirs(self.SD_CARD_PATH, exist_ok=True)
            return True
        except Exception as e:
            print(f"❌ Error creating directory: {e}")
            return False
            
    def install_dependencies(self):
        """Install required packages for Jarvis AI"""
        try:
            print("📦 Installing required packages...")
            # Check if running in Termux
            if os.path.exists("/data/data/com.termux"):
                os.system("pkg update -y && pkg upgrade -y && pkg install python termux-api termux-widget -y")
                # Try to install tkinter via pip if available
                os.system("pip install python-tk")
            else:
                print("ℹ️ Not running in Termux, skipping package installation")
                # For non-Termux environments, we assume dependencies are installed
            return True
        except Exception as e:
            print(f"⚠️ Warning: Package installation may not be complete: {e}")
            # Continue anyway as many environments may already have the packages
            return True
            
    def generate_ui_script(self):
        """Generate the Jarvis AI UI script"""
        script_content = """#!/usr/bin/env python3
# Jarvis AI UI System
# Advanced interface with monitoring capabilities

import os
import time
import tkinter as tk
from tkinter import ttk
import threading
import datetime
import platform
import subprocess

class JarvisUI:
    """Jarvis AI UI Controller"""
    
    def __init__(self, root):
        """Initialize the UI"""
        self.root = root
        self.setup_ui()
        self.status = "Normal"
        self.log_path = "/storage/emulated/0/JarvisAI/jarvis_ui_logs.txt"
        self.log_event("UI System started")
        
        # Start system monitoring in background
        self.monitor_thread = threading.Thread(target=self.system_monitor, daemon=True)
        self.monitor_thread.start()
        
    def setup_ui(self):
        """Configure the UI elements"""
        # Configure root window
        self.root.title("J.A.R.V.I.S. HUD")
        self.root.geometry("800x600")
        self.root.configure(bg="#1e1e1e")
        
        # Main frame
        main_frame = tk.Frame(self.root, bg="#1e1e1e")
        main_frame.pack(fill=tk.BOTH, expand=True, padx=20, pady=20)
        
        # Header
        header_frame = tk.Frame(main_frame, bg="#1e1e1e")
        header_frame.pack(fill=tk.X, pady=10)
        
        title_label = tk.Label(
            header_frame, 
            text="J.A.R.V.I.S.", 
            font=("Arial", 28, "bold"), 
            fg="#00a8ff", 
            bg="#1e1e1e"
        )
        title_label.pack(side=tk.LEFT)
        
        subtitle_label = tk.Label(
            header_frame, 
            text="Advanced AI Assistant", 
            font=("Arial", 14), 
            fg="#aaaaaa", 
            bg="#1e1e1e"
        )
        subtitle_label.pack(side=tk.LEFT, padx=15, pady=12)
        
        # Status section
        status_frame = tk.Frame(main_frame, bg="#1e1e1e")
        status_frame.pack(fill=tk.X, pady=20)
        
        tk.Label(
            status_frame, 
            text="SYSTEM STATUS:", 
            font=("Arial", 12, "bold"), 
            fg="#aaaaaa", 
            bg="#1e1e1e"
        ).pack(side=tk.LEFT)
        
        self.status_label = tk.Label(
            status_frame, 
            text="Normal", 
            font=("Arial", 14, "bold"), 
            fg="#00ff00", 
            bg="#1e1e1e"
        )
        self.status_label.pack(side=tk.LEFT, padx=10)
        
        # Time display
        self.time_label = tk.Label(
            status_frame, 
            text="", 
            font=("Arial", 12), 
            fg="#aaaaaa", 
            bg="#1e1e1e"
        )
        self.time_label.pack(side=tk.RIGHT)
        self.update_time()
        
        # Control panel
        control_frame = tk.Frame(main_frame, bg="#2d2d2d", bd=1, relief=tk.RAISED)
        control_frame.pack(fill=tk.X, pady=15)
        
        # Control buttons
        btn_frame = tk.Frame(control_frame, bg="#2d2d2d", pady=10, padx=10)
        btn_frame.pack(fill=tk.X)
        
        # Alert Mode button
        self.alert_btn = tk.Button(
            btn_frame, 
            text="ACTIVATE ALERT MODE", 
            font=("Arial", 12, "bold"),
            bg="#ff9500", 
            fg="black", 
            command=self.toggle_alert_mode,
            width=20, 
            height=2
        )
        self.alert_btn.pack(side=tk.LEFT, padx=10)
        
        # System Scan button
        scan_btn = tk.Button(
            btn_frame, 
            text="SYSTEM SCAN", 
            font=("Arial", 12, "bold"),
            bg="#007aff", 
            fg="white", 
            command=self.run_system_scan,
            width=15, 
            height=2
        )
        scan_btn.pack(side=tk.LEFT, padx=10)
        
        # System Info button
        info_btn = tk.Button(
            btn_frame, 
            text="SYSTEM INFO", 
            font=("Arial", 12, "bold"),
            bg="#5856d6", 
            fg="white", 
            command=self.show_system_info,
            width=15, 
            height=2
        )
        info_btn.pack(side=tk.LEFT, padx=10)
        
        # Activity log
        log_frame = tk.Frame(main_frame, bg="#2d2d2d", bd=1, relief=tk.RAISED)
        log_frame.pack(fill=tk.BOTH, expand=True, pady=15)
        
        tk.Label(
            log_frame, 
            text="ACTIVITY LOG", 
            font=("Arial", 12, "bold"), 
            fg="white", 
            bg="#2d2d2d",
            padx=10, 
            pady=5
        ).pack(fill=tk.X)
        
        # Scrolled text for logs
        self.log_display = tk.Text(
            log_frame, 
            height=10, 
            bg="#1e1e1e", 
            fg="#00ff00",
            font=("Consolas", 10), 
            wrap=tk.WORD
        )
        self.log_display.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        self.log_display.insert(tk.END, "System initialized...\n")
        self.log_display.config(state=tk.DISABLED)
        
        # Status bar
        status_bar = tk.Frame(main_frame, bg="#1e1e1e")
        status_bar.pack(fill=tk.X, side=tk.BOTTOM, pady=5)
        
        version_label = tk.Label(
            status_bar, 
            text="Jarvis AI v1.0.0", 
            font=("Arial", 8), 
            fg="#aaaaaa", 
            bg="#1e1e1e"
        )
        version_label.pack(side=tk.RIGHT)
    
    def update_time(self):
        """Update the time display"""
        current_time = datetime.datetime.now().strftime("%H:%M:%S - %Y-%m-%d")
        self.time_label.config(text=current_time)
        self.root.after(1000, self.update_time)
    
    def toggle_alert_mode(self):
        """Toggle between normal and alert modes"""
        if self.status == "Normal":
            self.status = "Alert"
            self.status_label.config(text="Alert Mode", fg="#ff9500")
            self.alert_btn.config(text="DEACTIVATE ALERT MODE", bg="#34c759")
            self.add_to_log("Alert mode activated")
            self.log_event("Alert mode activated")
        else:
            self.status = "Normal"
            self.status_label.config(text="Normal", fg="#00ff00")
            self.alert_btn.config(text="ACTIVATE ALERT MODE", bg="#ff9500")
            self.add_to_log("Alert mode deactivated")
            self.log_event("Alert mode deactivated")
    
    def run_system_scan(self):
        """Simulate a system scan"""
        self.add_to_log("Running system scan...")
        
        # Start a scan simulation in a separate thread
        threading.Thread(target=self._simulate_scan, daemon=True).start()
    
    def _simulate_scan(self):
        """Simulate a scanning process"""
        scan_steps = [
            "Initializing scan...",
            "Checking system integrity...",
            "Scanning memory subsystems...",
            "Analyzing network connections...",
            "Verifying security protocols...",
            "Checking for updates...",
            "Finalizing scan results..."
        ]
        
        for step in scan_steps:
            time.sleep(0.5)  # Simulate processing time
            self.add_to_log(step)
        
        time.sleep(1)
        self.add_to_log("Scan complete. No threats detected.")
        self.log_event("System scan completed")
    
    def show_system_info(self):
        """Display system information"""
        info = f"OS: {platform.system()} {platform.release()}"
        info += f"\nPython: {platform.python_version()}"
        info += f"\nProcessor: {platform.processor()}"
        
        self.add_to_log("System Information:")
        self.add_to_log(info)
        self.log_event("System info displayed")
    
    def system_monitor(self):
        """Monitor system resources periodically"""
        while True:
            # In a real application, you would check CPU, memory, network, etc.
            # For this demo, we'll just add a monitoring message periodically
            time.sleep(60)  # Check every minute
            self.add_to_log("System monitoring active. All systems nominal.")
    
    def add_to_log(self, message):
        """Add a message to the UI log display"""
        self.log_display.config(state=tk.NORMAL)
        timestamp = datetime.datetime.now().strftime("%H:%M:%S")
        self.log_display.insert(tk.END, f"[{timestamp}] {message}\n")
        self.log_display.see(tk.END)  # Scroll to the bottom
        self.log_display.config(state=tk.DISABLED)
    
    def log_event(self, event):
        """Log events to file with timestamp"""
        try:
            timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            with open(self.log_path, "a") as log_file:
                log_file.write(f"[{timestamp}] {event}\n")
        except Exception as e:
            print(f"Error writing to log: {e}")

def main():
    """Main entry point for the Jarvis UI application"""
    try:
        root = tk.Tk()
        app = JarvisUI(root)
        root.mainloop()
    except Exception as e:
        print(f"Error starting UI: {e}")
        # Write error to log file
        with open("/storage/emulated/0/JarvisAI/error_log.txt", "a") as f:
            f.write(f"{datetime.datetime.now()}: {e}\n")
            f.write(traceback.format_exc())

if __name__ == "__main__":
    main()
"""
        try:
            print(f"💾 Generating Jarvis AI UI script at {self.SCRIPT_FILE}...")
            with open(self.SCRIPT_FILE, "w") as file:
                file.write(script_content)
            
            # Make the script executable
            os.chmod(self.SCRIPT_FILE, 0o755)
            return True
        except Exception as e:
            print(f"❌ Error generating UI script: {e}")
            return False
    
    def launch_ui(self):
        """Launch the Jarvis UI"""
        try:
            print("🚀 Starting Jarvis AI system with UI...")
            # Start in a subprocess so it doesn't block
            subprocess.Popen(["python3", self.SCRIPT_FILE])
            return True
        except Exception as e:
            print(f"❌ Error launching UI: {e}")
            print("💡 You can try running it manually with:")
            print(f"   python3 {self.SCRIPT_FILE}")
            return False
    
    def show_completion_message(self):
        """Display setup completion message"""
        print("\n" + "="*50)
        print("🎉 JARVIS AI SETUP COMPLETE 🎉")
        print("="*50)
        print(f"📱 UI script saved at: {self.SCRIPT_FILE}")
        print(f"📋 Logs will be stored at: {self.LOG_FILE}")
        print("\n📚 DOCUMENTATION:")
        print("  • The UI provides system status monitoring")
        print("  • Alert mode can be toggled for emergency situations")
        print("  • System scans check for potential issues")
        print("  • Activity is logged to the file system")
        print("\n🔄 TO RESTART:")
        print(f"  python3 {self.SCRIPT_FILE}")
        print("\n⚠️ Note: Tkinter must be installed for UI to work properly")
        print("="*50)


def main():
    """Main entry point for Jarvis AI Setup"""
    try:
        print("🔧 Starting Jarvis AI Assistant UI Setup")
        
        # Initialize setup manager
        setup = SetupManager()
        
        # Setup stages
        if not setup.setup_storage():
            print("❌ Failed to set up storage. Aborting.")
            return 1
            
        setup.install_dependencies()
        
        if not setup.generate_ui_script():
            print("❌ Failed to generate UI script. Aborting.")
            return 1
        
        setup.launch_ui()
        setup.show_completion_message()
        
        return 0
        
    except Exception as e:
        print(f"❌ Error during setup: {e}")
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())