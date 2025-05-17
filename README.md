# Full-Send

A collection of utility scripts and tools for automation, AI assistants, and automotive tuning.

## Contents

This repository contains the following components:

### 1. Jarvis AI Assistant for Termux

A bash script that sets up an AI assistant (Jarvis) in Termux on Android devices. The script:
- Creates necessary directories on the SD card
- Installs a Python-based AI assistant
- Sets up authentication
- Configures battery monitoring
- Implements an event logging system

**File:** `jarvis_termux_setup.sh`

### 2. Jarvis AI Assistant with UI

A Python script that creates a graphical user interface for the Jarvis AI assistant. This script:
- Sets up storage in Termux
- Installs required packages
- Creates a Tkinter-based UI with alert functionality
- Initializes the AI system

**File:** `jarvis_ui_setup.py`

### 3. ME75 ECU Patching Utility

Java code for modifying Engine Control Units (ECUs), particularly for ME75 units. Features include:
- ECU memory mapping
- Boost map modification
- Ignition timing adjustment
- Immobilizer bypass
- Checksum bypass functionality

**File:** `me75_ecu_patching.java`

## Installation & Usage

### Jarvis AI for Termux

1. Install Termux from the Google Play Store or F-Droid
2. Open Termux and run:
   ```bash
   bash jarvis_termux_setup.sh
   ```
3. Allow storage permissions when prompted

### Jarvis AI with UI

1. Install Termux and Python
2. Run:
   ```bash
   python jarvis_ui_setup.py
   ```
3. The UI will launch automatically

### ME75 ECU Patching

This is a code snippet intended to be integrated into a larger Java application. It demonstrates the approach for ECU patching and requires:
- Java development environment
- KWP2000 protocol implementation
- Appropriate hardware for connecting to the ECU

## Disclaimer

- The ECU modification tools are provided for educational purposes only
- Modifying automotive ECUs may void warranties and could be illegal in some jurisdictions
- Use all tools at your own risk

## License

This project is licensed under the GNU General Public License v3.0 - see the LICENSE file for details.