package com.fullsend.jarvis.installer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.fullsend.jarvis.MainActivity;
import com.fullsend.jarvis.JarvisService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    }

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
            publishProgress(10, "Creating directories...");
            createInstallationDirectories();

            publishProgress(25, "Extracting configuration files...");
            extractConfigFiles();

            publishProgress(50, "Initializing AI components...");
            initializeAIComponents();

            publishProgress(70, "Setting up OBD diagnostics...");
            setupOBDDiagnostics();

            publishProgress(85, "Configuring background services...");
            configureSystemServices();

            publishProgress(100, "Finalizing installation...");
            finalizeInstallation();

            mainHandler.post(() -> {
                callback.onInstallationComplete();
                launchAppAndServices();
            });

        } catch (Exception e) {
            mainHandler.post(() -> callback.onInstallationError("Installation failed: " + e.getMessage()));
        }
    }

    private void publishProgress(int progress, String message) {
        mainHandler.post(() -> callback.onInstallationProgress(progress, message));
    }

    private void createInstallationDirectories() throws Exception {
        File baseDir = new File(context.getFilesDir(), "JarvisAI");
        if (!baseDir.exists() && !baseDir.mkdirs())
            throw new Exception("Failed to create base directory");

        new File(baseDir, "logs").mkdirs();
        new File(baseDir, "config").mkdirs();
        new File(baseDir, "obd").mkdirs();
        new File(baseDir, "ai").mkdirs();
        new File(baseDir, "garage").mkdirs();
    }

    private void extractConfigFiles() throws Exception {
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

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
        }
    }

    private void initializeAIComponents() {
        SharedPreferences prefs = context.getSharedPreferences("jarvis_ai", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("ai_initialized", true)
                .putBoolean("voice_recognition_enabled", true)
                .putBoolean("nlp_enabled", true)
                .apply();
    }

    private void setupOBDDiagnostics() {
        SharedPreferences prefs = context.getSharedPreferences("jarvis_obd", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("obd_enabled", true)
                .putString("default_protocol", "AUTO")
                .putBoolean("kkl_support", true)
                .putBoolean("elm327_support", true)
                .apply();
    }

    private void configureSystemServices() {
        SharedPreferences prefs = context.getSharedPreferences("jarvis_services", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("service_enabled", true)
                .putInt("log_retention_days", 30)
                .apply();
    }

    private void finalizeInstallation() throws Exception {
        SharedPreferences prefs = context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("installation_complete", true)
                .putLong("installation_date", System.currentTimeMillis())
                .apply();

        File successFile = new File(context.getFilesDir(), "JarvisAI/.installation_complete");
        if (!successFile.exists()) successFile.createNewFile();
    }

    private void launchAppAndServices() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(mainIntent);

        Intent serviceIntent = new Intent(context, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent);
        else
            context.startService(serviceIntent);

        Toast.makeText(context, "🎉 Jarvis AI installed successfully!", Toast.LENGTH_LONG).show();
    }

    public static boolean isInstallationComplete(Context context) {
        return context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE)
                .getBoolean("installation_complete", false);
    }

    public static void resetInstallation(Context context) {
        context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("jarvis_ai", Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("jarvis_obd", Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("jarvis_services", Context.MODE_PRIVATE).edit().clear().apply();
    }
}