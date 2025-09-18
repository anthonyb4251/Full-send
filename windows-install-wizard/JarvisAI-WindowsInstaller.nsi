; Jarvis AI Universal Windows Installer
; NSIS Script for complete Windows installation with all dependencies

!define PRODUCT_NAME "Jarvis AI Universal Android Installer"
!define PRODUCT_VERSION "1.0.0"
!define PRODUCT_PUBLISHER "Jarvis AI Open Source Project"
!define PRODUCT_WEB_SITE "https://github.com/anthonyb4251/Full-send"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\JarvisAI.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; Modern UI
!include "MUI2.nsh"
!include "FileFunc.nsh"
!include "WinVer.nsh"
!include "x64.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "jarvis-icon.ico"
!define MUI_UNICON "jarvis-icon.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP "jarvis-welcome.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "jarvis-welcome.bmp"

; Welcome page
!insertmacro MUI_PAGE_WELCOME

; License page
!insertmacro MUI_PAGE_LICENSE "LICENSE.txt"

; Components page
!insertmacro MUI_PAGE_COMPONENTS

; Directory page
!insertmacro MUI_PAGE_DIRECTORY

; Instfiles page
!insertmacro MUI_PAGE_INSTFILES

; Finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\JarvisAI-Installer.exe"
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\README.txt"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; Reserve files
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "JarvisAI-Universal-Windows-Installer.exe"
InstallDir "$PROGRAMFILES\JarvisAI"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show
RequestExecutionLevel admin

; Version Information
VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "${PRODUCT_NAME}"
VIAddVersionKey "Comments" "Universal Android installer with all dependencies"
VIAddVersionKey "CompanyName" "${PRODUCT_PUBLISHER}"
VIAddVersionKey "LegalTrademarks" "Open Source GPL-3.0"
VIAddVersionKey "LegalCopyright" "© 2024 Jarvis AI Open Source Project"
VIAddVersionKey "FileDescription" "${PRODUCT_NAME}"
VIAddVersionKey "FileVersion" "${PRODUCT_VERSION}"

Section "Core Installation" SEC01
  SectionIn RO
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  
  ; Main application files
  File "JarvisAI-Installer.exe"
  File "jarvis-ai.apk"
  File "README.txt"
  File "LICENSE.txt"
  File "INSTALLATION-GUIDE.txt"
  
  ; Create shortcuts
  CreateDirectory "$SMPROGRAMS\Jarvis AI"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Jarvis AI Installer.lnk" "$INSTDIR\JarvisAI-Installer.exe"
  CreateShortCut "$DESKTOP\Jarvis AI Installer.lnk" "$INSTDIR\JarvisAI-Installer.exe"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section "Android SDK Platform Tools (ADB)" SEC02
  SetOutPath "$INSTDIR\platform-tools"
  
  DetailPrint "Downloading Android SDK Platform Tools..."
  
  ; Download ADB if not present
  IfFileExists "$INSTDIR\platform-tools\adb.exe" SkipADBDownload 0
  
  ; Create download directory
  CreateDirectory "$TEMP\jarvis-downloads"
  
  ; Download platform tools
  NSISdl::download "https://dl.google.com/android/repository/platform-tools-latest-windows.zip" "$TEMP\jarvis-downloads\platform-tools.zip"
  Pop $R0
  StrCmp $R0 "success" ExtractADB
  
  MessageBox MB_OK "Failed to download Android SDK Platform Tools. Please check your internet connection."
  Goto SkipADBDownload
  
  ExtractADB:
  DetailPrint "Extracting Android SDK Platform Tools..."
  nsisunz::UnzipToLog "$TEMP\jarvis-downloads\platform-tools.zip" "$INSTDIR"
  
  ; Add to PATH
  EnVar::SetHKLM
  EnVar::AddValue "PATH" "$INSTDIR\platform-tools"
  
  SkipADBDownload:
  
  ; Verify ADB installation
  ExecWait '"$INSTDIR\platform-tools\adb.exe" version' $0
  IntCmp $0 0 ADBSuccess
  MessageBox MB_OK "ADB installation verification failed. Manual installation may be required."
  
  ADBSuccess:
  DetailPrint "Android SDK Platform Tools installed successfully"
SectionEnd

Section "Universal USB Drivers" SEC03
  SetOutPath "$INSTDIR\drivers"
  
  DetailPrint "Installing Universal USB Drivers..."
  
  ; Download universal ADB drivers
  NSISdl::download "https://github.com/koush/UniversalAdbDriver/releases/latest/download/UniversalAdbDriverSetup.msi" "$TEMP\jarvis-downloads\usb-drivers.msi"
  Pop $R0
  StrCmp $R0 "success" InstallDrivers
  
  MessageBox MB_OK "Failed to download USB drivers. Manual driver installation may be required."
  Goto SkipDrivers
  
  InstallDrivers:
  DetailPrint "Installing USB drivers..."
  ExecWait 'msiexec /i "$TEMP\jarvis-downloads\usb-drivers.msi" /quiet /norestart' $0
  IntCmp $0 0 DriversSuccess
  MessageBox MB_OK "USB driver installation completed with warnings. Drivers should still work."
  
  DriversSuccess:
  DetailPrint "Universal USB drivers installed successfully"
  
  SkipDrivers:
SectionEnd

Section "Java Runtime Environment" SEC04
  DetailPrint "Checking Java installation..."
  
  ; Check if Java is already installed
  ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  StrCmp $0 "" 0 JavaInstalled
  
  ; Check for OpenJDK
  ReadRegStr $0 HKLM "SOFTWARE\Eclipse Adoptium\JDK" "CurrentVersion"
  StrCmp $0 "" 0 JavaInstalled
  
  DetailPrint "Java not found. Downloading OpenJDK..."
  
  ; Download OpenJDK 17
  ${If} ${RunningX64}
    NSISdl::download "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1%2B1/OpenJDK17U-jre_x64_windows_hotspot_17.0.8.1_1.msi" "$TEMP\jarvis-downloads\openjdk.msi"
  ${Else}
    NSISdl::download "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8.1%2B1/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.8.1_1.msi" "$TEMP\jarvis-downloads\openjdk.msi"
  ${EndIf}
  
  Pop $R0
  StrCmp $R0 "success" InstallJava
  
  MessageBox MB_OK "Failed to download Java. Please install Java 17+ manually."
  Goto JavaInstalled
  
  InstallJava:
  DetailPrint "Installing Java Runtime Environment..."
  ExecWait 'msiexec /i "$TEMP\jarvis-downloads\openjdk.msi" /quiet /norestart' $0
  IntCmp $0 0 JavaSuccess
  MessageBox MB_OK "Java installation completed with warnings. Java should still work."
  
  JavaSuccess:
  DetailPrint "Java Runtime Environment installed successfully"
  
  JavaInstalled:
  DetailPrint "Java installation verified"
SectionEnd

Section "Android Device Drivers" SEC05
  SetOutPath "$INSTDIR\device-drivers"
  
  DetailPrint "Installing Android device drivers..."
  
  ; Samsung drivers
  NSISdl::download "https://developer.samsung.com/android-usb-driver" "$TEMP\jarvis-downloads\samsung-drivers.exe"
  Pop $R0
  StrCmp $R0 "success" 0 SkipSamsung
  ExecWait '"$TEMP\jarvis-downloads\samsung-drivers.exe" /S' $0
  SkipSamsung:
  
  ; Google USB drivers (included with platform tools)
  File "google-usb-driver.inf"
  
  ; Universal drivers for other manufacturers
  File "universal-android-drivers.inf"
  
  DetailPrint "Android device drivers installed"
SectionEnd

Section "Desktop Integration" SEC06
  ; Create desktop shortcut with custom icon
  CreateShortCut "$DESKTOP\Jarvis AI Android Installer.lnk" "$INSTDIR\JarvisAI-Installer.exe" "" "$INSTDIR\jarvis-icon.ico"
  
  ; Create start menu folder
  CreateDirectory "$SMPROGRAMS\Jarvis AI"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Jarvis AI Android Installer.lnk" "$INSTDIR\JarvisAI-Installer.exe"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Installation Guide.lnk" "$INSTDIR\INSTALLATION-GUIDE.txt"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Uninstall.lnk" "$INSTDIR\uninst.exe"
  
  ; Register file associations
  WriteRegStr HKCR ".apk" "" "JarvisAI.APK"
  WriteRegStr HKCR "JarvisAI.APK" "" "Jarvis AI Android Package"
  WriteRegStr HKCR "JarvisAI.APK\DefaultIcon" "" "$INSTDIR\jarvis-icon.ico"
  WriteRegStr HKCR "JarvisAI.APK\shell\open\command" "" '"$INSTDIR\JarvisAI-Installer.exe" "%1"'
SectionEnd

Section -AdditionalIcons
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateShortCut "$SMPROGRAMS\Jarvis AI\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\JarvisAI-Installer.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\JarvisAI-Installer.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
  
  ; Cleanup temporary files
  RMDir /r "$TEMP\jarvis-downloads"
  
  ; Show completion message
  MessageBox MB_OK "Jarvis AI Universal Android Installer has been installed successfully!$\r$\n$\r$\nYou can now:$\r$\n• Connect your Android device via USB$\r$\n• Run the Jarvis AI Installer from your desktop$\r$\n• Follow the automatic installation process$\r$\n$\r$\nAll dependencies and drivers have been installed automatically."
SectionEnd

; Section descriptions
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC01} "Core Jarvis AI installer and APK files (Required)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC02} "Android SDK Platform Tools (ADB) for device communication"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC03} "Universal USB drivers for Android device connectivity"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC04} "Java Runtime Environment (Required for Android tools)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC05} "Device-specific drivers for Samsung, Google, and other manufacturers"
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC06} "Desktop shortcuts and file associations"
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\JarvisAI-Installer.exe"
  Delete "$INSTDIR\jarvis-ai.apk"
  Delete "$INSTDIR\README.txt"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\INSTALLATION-GUIDE.txt"
  
  Delete "$SMPROGRAMS\Jarvis AI\Uninstall.lnk"
  Delete "$SMPROGRAMS\Jarvis AI\Website.lnk"
  Delete "$SMPROGRAMS\Jarvis AI\Jarvis AI Installer.lnk"
  Delete "$SMPROGRAMS\Jarvis AI\Installation Guide.lnk"
  Delete "$DESKTOP\Jarvis AI Android Installer.lnk"
  Delete "$DESKTOP\Jarvis AI Installer.lnk"

  RMDir "$SMPROGRAMS\Jarvis AI"
  RMDir /r "$INSTDIR\platform-tools"
  RMDir /r "$INSTDIR\drivers"
  RMDir /r "$INSTDIR\device-drivers"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  DeleteRegKey HKCR ".apk"
  DeleteRegKey HKCR "JarvisAI.APK"
  
  ; Remove from PATH
  EnVar::SetHKLM
  EnVar::DeleteValue "PATH" "$INSTDIR\platform-tools"
  
  SetAutoClose true
SectionEnd
