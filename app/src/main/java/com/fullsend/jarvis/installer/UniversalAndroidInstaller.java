package com.fullsend.jarvis.installer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.fullsend.jarvis.MainActivity;
import com.fullsend.jarvis.JarvisService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OneClickInstaller {

    private final Context context;
    private final InstallationCallback callback;
    private final Executor executor;
    private final Handler mainHandler;

    public interface InstallationCallback {
        void onInstallationStarted();
        void onInstallationProgress(int progress, String message);
        void onInstallationComplete();
        void onInstallationError(String error);
        void onAutoFixApplied(String fix);
    }

    // Device profile (from Universal Installer)
    public static class DeviceProfile {
        public String manufacturer;
        public String model;
        public String androidVersion;
        public int apiLevel;
        public String architecture;
        public boolean isRooted;
        public boolean hasUSBOTG;
        public boolean hasBiometrics;
        public long availableStorage;
        public List<String> supportedFeatures = new ArrayList<>();
    }

    private DeviceProfile deviceProfile = new DeviceProfile();

    public OneClickInstaller(Context context, InstallationCallback callback) {
        this.context = context;
        this.callback = callback;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void startOneClickInstallation() {
        callback.onInstallationStarted();
        executor.execute(this::runInstallation);
    }

    private void runInstallation() {
        try {
            publishProgress(5, "Detecting device environment...");
            detectEnvironment();

            publishProgress(15, "Resolving dependencies...");
            resolveDependencies();

            publishProgress(30, "Configuring permissions...");
            configurePermissions();

            publishProgress(50, "Enabling features...");
            enableFeatures();

            publishProgress(70, "Creating directories and extracting files...");
            createApplicationDirectories();
            extractConfigurationAssets();

            publishProgress(90, "Finalizing installation...");
            applyFinalOptimizations();

            publishProgress(100, "Installation complete!");
            mainHandler.post(() -> {
                callback.onInstallationComplete();
                launchAppAndServices();
            });

        } catch (Exception e) {
            handleInstallationError(e);
        }
    }

    private void publishProgress(int progress, String message) {
        mainHandler.post(() -> callback.onInstallationProgress(progress, message));
    }

    // -----------------------
    // Environment Detection
    // -----------------------
    private void detectEnvironment() {
        deviceProfile.manufacturer = Build.MANUFACTURER;
        deviceProfile.model = Build.MODEL;
        deviceProfile.androidVersion = Build.VERSION.RELEASE;
        deviceProfile.apiLevel = Build.VERSION.SDK_INT;
        deviceProfile.architecture = detectArchitecture();
        deviceProfile.isRooted = detectRootStatus();
        deviceProfile.hasUSBOTG = detectUSBOTGSupport();
        deviceProfile.hasBiometrics = detectBiometricSupport();
        deviceProfile.availableStorage = getAvailableStorage();
        detectSupportedFeatures();
    }

    private String detectArchitecture() {
        String[] abis = Build.SUPPORTED_ABIS;
        if (abis.length > 0) {
            String primaryAbi = abis[0];
            if (primaryAbi.contains("arm64")) return "ARM64";
            if (primaryAbi.contains("arm")) return "ARM32";
            if (primaryAbi.contains("x86_64")) return "x86_64";
            if (primaryAbi.contains("x86")) return "x86";
        }
        return "Unknown";
    }

    private boolean detectRootStatus() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean detectUSBOTGSupport() {
        return context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_USB_HOST);
    }

    private boolean detectBiometricSupport() {
        return context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT) ||
               Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private long getAvailableStorage() {
        File dir = context.getFilesDir();
        return dir.getUsableSpace();
    }

    private void detectSupportedFeatures() {
        if (context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_MICROPHONE)) {
            deviceProfile.supportedFeatures.add("microphone");
        }
        if (context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_LOCATION)) {
            deviceProfile.supportedFeatures.add("location");
        }
        if (context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA)) {
            deviceProfile.supportedFeatures.add("camera");
        }
    }

    // -----------------------
    // Dependency & Permissions
    // -----------------------
    private void resolveDependencies() throws Exception {
        if (deviceProfile.availableStorage < 50 * 1024 * 1024) {
            autoFixStorageSpace();
        }
        if (deviceProfile.apiLevel < 23) {
            throw new Exception("Android 6.0+ required. Current: " + deviceProfile.androidVersion);
        }
    }

    private void configurePermissions() {
        // Placeholder: implement runtime permissions if required
    }

    // -----------------------
    // Feature Enablement
    // -----------------------
    private void enableFeatures() {
        // Core AI
        // OBD
        if (deviceProfile.hasUSBOTG) deviceProfile.supportedFeatures.add("OBD Diagnostics");
        // Biometric
        if (deviceProfile.hasBiometrics) deviceProfile.supportedFeatures.add("Biometric");
        // Voice
        if (deviceProfile.supportedFeatures.contains("microphone")) deviceProfile.supportedFeatures.add("Voice Commands");
        // Location
        if (deviceProfile.supportedFeatures.contains("location")) deviceProfile.supportedFeatures.add("Location Services");
    }

    // -----------------------
    // Directories & Assets
    // -----------------------
    private void createApplicationDirectories() throws Exception {
        File jarvisDir = new File(context.getFilesDir(), "JarvisAI");
        if (!jarvisDir.exists() && !jarvisDir.mkdirs()) throw new Exception("Failed to create directories");
        new File(jarvisDir, "logs").mkdirs();
        new File(jarvisDir, "config").mkdirs();
        new File(jarvisDir, "obd").mkdirs();
        new File(jarvisDir, "ai").mkdirs();
        new File(jarvisDir, "garage").mkdirs();
    }

    private void extractConfigurationAssets() throws Exception {
        copyAssetToFile("config/default_settings.json", "config/settings.json");
        copyAssetToFile("config/obd_protocols.json", "obd/protocols.json");
        copyAssetToFile("config/ai_responses.json", "ai/responses.json");
        copyAssetToFile("config/vehicle_database.json", "garage/vehicles.json");
    }

    private void copyAssetToFile(String assetPath, String targetPath) throws Exception {
        File baseDir = new File(context.getFilesDir(), "JarvisAI");
        File targetFile = new File(baseDir, targetPath);
        try (InputStream is = context.getAssets().open(assetPath);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024]; int len;
            while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
        }
    }

    // -----------------------
    // Final Optimizations
    // -----------------------
    private void applyFinalOptimizations() {
        // Placeholder for device-specific optimizations
    }

    private void handleInstallationError(Exception e) {
        String autoFix = attemptAutoFix(e);
        if (autoFix != null) {
            mainHandler.post(() -> callback.onAutoFixApplied(autoFix));
            startOneClickInstallation(); // retry
        } else {
            mainHandler.post(() -> callback.onInstallationError(e.getMessage()));
        }
    }

    private String attemptAutoFix(Exception e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("storage")) {
            try { autoFixStorageSpace(); return "Cleared cache to free storage"; } catch (Exception ignored) {}
        }
        return null;
    }

    private void autoFixStorageSpace() throws Exception {
        File cache = context.getCacheDir();
        deleteRecursive(cache);
        File extCache = context.getExternalCacheDir();
        if (extCache != null) deleteRecursive(extCache);
    }

    private void deleteRecursive(File fileOrDir) {
        if (fileOrDir == null) return;
        if (fileOrDir.isDirectory()) for (File child : fileOrDir.listFiles()) deleteRecursive(child);
        fileOrDir.delete();
    }

    // -----------------------
    // Launch Application
    // -----------------------
    private void launchAppAndServices() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(mainIntent);

        Intent serviceIntent = new Intent(context, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(serviceIntent);
        else context.startService(serviceIntent);

        Toast.makeText(context, "🎉 Jarvis AI installed successfully!", Toast.LENGTH_LONG).show();
    }

    // -----------------------
    // Installation Check
    // -----------------------
    public static boolean isInstallationComplete(Context context) {
        File marker = new File(context.getFilesDir(), "JarvisAI/.installation_complete");
        return marker.exists();
    }
}