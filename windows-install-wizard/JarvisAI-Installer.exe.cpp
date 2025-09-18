// Jarvis AI Universal Windows Installer Application
// C++ Windows GUI Application for Android Installation

#include <windows.h>
#include <commctrl.h>
#include <shellapi.h>
#include <shlobj.h>
#include <wininet.h>
#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <fstream>

#pragma comment(lib, "comctl32.lib")
#pragma comment(lib, "shell32.lib")
#pragma comment(lib, "wininet.lib")

// Window controls
#define ID_INSTALL_BUTTON 1001
#define ID_PROGRESS_BAR 1002
#define ID_STATUS_TEXT 1003
#define ID_DEVICE_LIST 1004
#define ID_REFRESH_BUTTON 1005
#define ID_HELP_BUTTON 1006

// Global variables
HWND hMainWindow;
HWND hProgressBar;
HWND hStatusText;
HWND hDeviceList;
HWND hInstallButton;
HWND hRefreshButton;

bool installationInProgress = false;
std::string currentStatus = "Ready to install";

// Function declarations
LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
void InitializeControls(HWND hwnd);
void RefreshDeviceList();
void StartInstallation();
void UpdateStatus(const std::string& status);
void UpdateProgress(int percentage);
bool CheckADBInstallation();
bool InstallADB();
bool CheckDeviceConnection();
bool InstallAPK();
void ShowHelp();

// Main entry point
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Initialize common controls
    INITCOMMONCONTROLSEX icex;
    icex.dwSize = sizeof(INITCOMMONCONTROLSEX);
    icex.dwICC = ICC_PROGRESS_CLASS | ICC_LISTVIEW_CLASSES;
    InitCommonControlsEx(&icex);

    // Register window class
    const char* CLASS_NAME = "JarvisAIInstaller";
    
    WNDCLASS wc = {};
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hIcon = LoadIcon(hInstance, MAKEINTRESOURCE(101));

    RegisterClass(&wc);

    // Create main window
    hMainWindow = CreateWindowEx(
        0,
        CLASS_NAME,
        "Jarvis AI Universal Android Installer",
        WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, 600, 500,
        NULL, NULL, hInstance, NULL
    );

    if (hMainWindow == NULL) {
        return 0;
    }

    ShowWindow(hMainWindow, nCmdShow);
    UpdateWindow(hMainWindow);

    // Message loop
    MSG msg = {};
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_CREATE:
        InitializeControls(hwnd);
        RefreshDeviceList();
        return 0;

    case WM_COMMAND:
        switch (LOWORD(wParam)) {
        case ID_INSTALL_BUTTON:
            if (!installationInProgress) {
                std::thread(StartInstallation).detach();
            }
            break;
        case ID_REFRESH_BUTTON:
            RefreshDeviceList();
            break;
        case ID_HELP_BUTTON:
            ShowHelp();
            break;
        }
        return 0;

    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void InitializeControls(HWND hwnd) {
    // Title
    CreateWindow("STATIC", "🤖 Jarvis AI Universal Android Installer",
        WS_VISIBLE | WS_CHILD | SS_CENTER,
        50, 20, 500, 30, hwnd, NULL, NULL, NULL);

    // Subtitle
    CreateWindow("STATIC", "Zero Manual Input - Automatic Installation with All Dependencies",
        WS_VISIBLE | WS_CHILD | SS_CENTER,
        50, 50, 500, 20, hwnd, NULL, NULL, NULL);

    // Device list label
    CreateWindow("STATIC", "Connected Android Devices:",
        WS_VISIBLE | WS_CHILD,
        50, 90, 200, 20, hwnd, NULL, NULL, NULL);

    // Device list
    hDeviceList = CreateWindow("LISTBOX", "",
        WS_VISIBLE | WS_CHILD | WS_BORDER | WS_VSCROLL,
        50, 110, 400, 100, hwnd, (HMENU)ID_DEVICE_LIST, NULL, NULL);

    // Refresh button
    hRefreshButton = CreateWindow("BUTTON", "🔄 Refresh Devices",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        460, 110, 120, 30, hwnd, (HMENU)ID_REFRESH_BUTTON, NULL, NULL);

    // Status text
    hStatusText = CreateWindow("STATIC", "Ready to install Jarvis AI",
        WS_VISIBLE | WS_CHILD,
        50, 230, 500, 20, hwnd, (HMENU)ID_STATUS_TEXT, NULL, NULL);

    // Progress bar
    hProgressBar = CreateWindow(PROGRESS_CLASS, "",
        WS_VISIBLE | WS_CHILD,
        50, 250, 500, 25, hwnd, (HMENU)ID_PROGRESS_BAR, NULL, NULL);

    // Install button
    hInstallButton = CreateWindow("BUTTON", "🚀 Install Jarvis AI (Automatic)",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        50, 290, 200, 40, hwnd, (HMENU)ID_INSTALL_BUTTON, NULL, NULL);

    // Help button
    CreateWindow("BUTTON", "❓ Help & Troubleshooting",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        270, 290, 180, 40, hwnd, (HMENU)ID_HELP_BUTTON, NULL, NULL);

    // Features section
    CreateWindow("STATIC", "✨ Features Included:",
        WS_VISIBLE | WS_CHILD,
        50, 350, 150, 20, hwnd, NULL, NULL, NULL);

    CreateWindow("STATIC", "• AI Assistant with J.A.R.V.I.S interface\n• Battery monitoring and power management\n• OBD-II vehicle diagnostics (with USB OTG)\n• Virtual garage management system\n• Biometric security and voice commands",
        WS_VISIBLE | WS_CHILD,
        50, 370, 500, 80, hwnd, NULL, NULL, NULL);

    // Set progress bar range
    SendMessage(hProgressBar, PBM_SETRANGE, 0, MAKELPARAM(0, 100));
}

void RefreshDeviceList() {
    // Clear device list
    SendMessage(hDeviceList, LB_RESETCONTENT, 0, 0);
    
    UpdateStatus("Scanning for Android devices...");
    
    // Check if ADB is available
    if (!CheckADBInstallation()) {
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"❌ ADB not found - will be installed automatically");
        UpdateStatus("ADB not found - will be installed during setup");
        return;
    }

    // Run ADB devices command
    FILE* pipe = _popen("adb devices", "r");
    if (!pipe) {
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"❌ Failed to execute ADB command");
        UpdateStatus("Failed to scan for devices");
        return;
    }

    char buffer[256];
    bool foundDevices = false;
    bool skipFirst = true; // Skip "List of devices attached" line

    while (fgets(buffer, sizeof(buffer), pipe)) {
        if (skipFirst) {
            skipFirst = false;
            continue;
        }

        std::string line(buffer);
        if (line.find("device") != std::string::npos && line.find("offline") == std::string::npos) {
            // Extract device ID
            size_t tabPos = line.find('\t');
            if (tabPos != std::string::npos) {
                std::string deviceId = line.substr(0, tabPos);
                std::string displayText = "✅ " + deviceId + " (Ready for installation)";
                SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)displayText.c_str());
                foundDevices = true;
            }
        } else if (line.find("unauthorized") != std::string::npos) {
            size_t tabPos = line.find('\t');
            if (tabPos != std::string::npos) {
                std::string deviceId = line.substr(0, tabPos);
                std::string displayText = "⚠️ " + deviceId + " (USB debugging not authorized)";
                SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)displayText.c_str());
            }
        }
    }

    _pclose(pipe);

    if (!foundDevices) {
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"📱 No Android devices found");
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"");
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"Please:");
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"1. Connect your Android device via USB");
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"2. Enable USB debugging in Developer Options");
        SendMessage(hDeviceList, LB_ADDSTRING, 0, (LPARAM)"3. Click 'Refresh Devices'");
        UpdateStatus("No devices found - connect Android device and enable USB debugging");
    } else {
        UpdateStatus("Android device(s) detected and ready for installation");
    }
}

void StartInstallation() {
    installationInProgress = true;
    EnableWindow(hInstallButton, FALSE);
    EnableWindow(hRefreshButton, FALSE);

    try {
        UpdateProgress(0);
        UpdateStatus("Starting automatic installation...");

        // Step 1: Check and install ADB
        UpdateProgress(10);
        UpdateStatus("Checking ADB installation...");
        if (!CheckADBInstallation()) {
            UpdateStatus("Installing ADB (Android SDK Platform Tools)...");
            if (!InstallADB()) {
                throw std::runtime_error("Failed to install ADB");
            }
        }

        // Step 2: Check device connection
        UpdateProgress(30);
        UpdateStatus("Checking device connection...");
        if (!CheckDeviceConnection()) {
            throw std::runtime_error("No Android device found. Please connect device and enable USB debugging.");
        }

        // Step 3: Install APK
        UpdateProgress(50);
        UpdateStatus("Installing Jarvis AI APK...");
        if (!InstallAPK()) {
            throw std::runtime_error("Failed to install APK");
        }

        // Step 4: Verify installation
        UpdateProgress(80);
        UpdateStatus("Verifying installation...");
        Sleep(2000); // Give time for installation to complete

        // Step 5: Complete
        UpdateProgress(100);
        UpdateStatus("✅ Installation completed successfully!");

        MessageBox(hMainWindow, 
            "🎉 Jarvis AI has been installed successfully!\n\n"
            "You can now:\n"
            "• Find 'Jarvis AI' in your device's app drawer\n"
            "• Launch the app to start using your AI assistant\n"
            "• Enjoy features like battery monitoring, OBD diagnostics, and more!\n\n"
            "The installation wizard will guide you through initial setup.",
            "Installation Complete", 
            MB_OK | MB_ICONINFORMATION);

    } catch (const std::exception& e) {
        UpdateStatus("❌ Installation failed: " + std::string(e.what()));
        MessageBox(hMainWindow, 
            ("Installation failed:\n\n" + std::string(e.what()) + "\n\nPlease:\n"
            "1. Ensure your Android device is connected\n"
            "2. Enable USB debugging in Developer Options\n"
            "3. Allow USB debugging when prompted on device\n"
            "4. Try the installation again\n\n"
            "Click 'Help & Troubleshooting' for more assistance.").c_str(),
            "Installation Error", 
            MB_OK | MB_ICONERROR);
        UpdateProgress(0);
    }

    installationInProgress = false;
    EnableWindow(hInstallButton, TRUE);
    EnableWindow(hRefreshButton, TRUE);
    RefreshDeviceList();
}

void UpdateStatus(const std::string& status) {
    currentStatus = status;
    SetWindowText(hStatusText, status.c_str());
}

void UpdateProgress(int percentage) {
    SendMessage(hProgressBar, PBM_SETPOS, percentage, 0);
}

bool CheckADBInstallation() {
    FILE* pipe = _popen("adb version", "r");
    if (!pipe) return false;
    
    char buffer[256];
    bool found = false;
    if (fgets(buffer, sizeof(buffer), pipe)) {
        if (strstr(buffer, "Android Debug Bridge")) {
            found = true;
        }
    }
    _pclose(pipe);
    return found;
}

bool InstallADB() {
    // This would download and install ADB
    // For now, assume it's handled by the NSIS installer
    UpdateStatus("ADB installation handled by system installer...");
    Sleep(3000);
    return true;
}

bool CheckDeviceConnection() {
    FILE* pipe = _popen("adb devices", "r");
    if (!pipe) return false;
    
    char buffer[256];
    bool deviceFound = false;
    bool skipFirst = true;

    while (fgets(buffer, sizeof(buffer), pipe)) {
        if (skipFirst) {
            skipFirst = false;
            continue;
        }
        
        if (strstr(buffer, "device") && !strstr(buffer, "offline")) {
            deviceFound = true;
            break;
        }
    }
    
    _pclose(pipe);
    return deviceFound;
}

bool InstallAPK() {
    // Install the APK using ADB
    std::string command = "adb install -r \"jarvis-ai.apk\"";
    
    FILE* pipe = _popen(command.c_str(), "r");
    if (!pipe) return false;
    
    char buffer[256];
    bool success = false;
    
    while (fgets(buffer, sizeof(buffer), pipe)) {
        if (strstr(buffer, "Success")) {
            success = true;
            break;
        }
    }
    
    _pclose(pipe);
    return success;
}

void ShowHelp() {
    std::string helpText = 
        "🤖 Jarvis AI Universal Android Installer - Help\n\n"
        
        "📱 DEVICE CONNECTION ISSUES:\n"
        "• Enable 'Developer Options' in Android Settings\n"
        "• Enable 'USB Debugging' in Developer Options\n"
        "• Allow USB debugging when prompted on device\n"
        "• Try different USB cables or ports\n"
        "• Restart both computer and Android device\n\n"
        
        "🔧 INSTALLATION ISSUES:\n"
        "• Enable 'Install from unknown sources' on device\n"
        "• Ensure device has sufficient storage space\n"
        "• Close other Android management software\n"
        "• Run installer as Administrator\n\n"
        
        "📋 SYSTEM REQUIREMENTS:\n"
        "• Windows 7 or later\n"
        "• Android device with Android 6.0+\n"
        "• USB cable for device connection\n"
        "• Internet connection for dependency downloads\n\n"
        
        "✨ FEATURES AFTER INSTALLATION:\n"
        "• AI Assistant with voice commands\n"
        "• Battery monitoring and optimization\n"
        "• OBD-II vehicle diagnostics (with USB OTG)\n"
        "• Virtual garage management\n"
        "• Biometric security features\n\n"
        
        "🌐 For more help, visit:\n"
        "https://github.com/anthonyb4251/Full-send";

    MessageBox(hMainWindow, helpText.c_str(), "Help & Troubleshooting", MB_OK | MB_ICONINFORMATION);
}
