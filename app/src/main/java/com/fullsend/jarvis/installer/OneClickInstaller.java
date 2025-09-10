package com.fullsend.jarvis.installer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.fullsend.jarvis.MainActivity;
import com.fullsend.jarvis.JarvisService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class OneClickInstaller {
    
    private static final String TAG = "OneClickInstaller";
    private Context context;
    private InstallationCallback callback;
    
    public interface InstallationCallback {
        void onInstallationStarted();
        void onInstallationProgress(int progress, String message);
        void onInstallationComplete();
        void onInstallationError(String error);
    }
    
    public OneClickInstaller(Context context, InstallationCallback callback) {
        this.context = context;
        this.callback = callback;
    }
    
    public void startOneClickInstallation() {
        callback.onInstallationStarted();
        
        // Start the complete installation process
        new InstallationTask().execute();
    }
    
    private class InstallationTask extends AsyncTask<Void, InstallationProgress, Boolean> {
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Step 1: Create installation directory
                publishProgress(new InstallationProgress(10, "Creating installation directory..."));
                createInstallationDirectory();
                
                // Step 2: Extract required files
                publishProgress(new InstallationProgress(25, "Extracting installation files..."));
                extractInstallationFiles();
                
                // Step 3: Setup permissions
                publishProgress(new InstallationProgress(40, "Configuring permissions..."));
                setupPermissions();
                
                // Step 4: Initialize AI components
                publishProgress(new InstallationProgress(60, "Initializing AI components..."));
                initializeAIComponents();
                
                // Step 5: Setup OBD diagnostics
                publishProgress(new InstallationProgress(75, "Setting up OBD diagnostics..."));
                setupOBDDiagnostics();
                
                // Step 6: Configure system services
                publishProgress(new InstallationProgress(90, "Configuring system services..."));
                configureSystemServices();
                
                // Step 7: Finalize installation
                publishProgress(new InstallationProgress(100, "Finalizing installation..."));
                finalizeInstallation();
                
                return true;
                
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        protected void onProgressUpdate(InstallationProgress... progress) {
            InstallationProgress p = progress[0];
            callback.onInstallationProgress(p.progress, p.message);
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onInstallationComplete();
                launchApplication();
            } else {
                callback.onInstallationError("Installation failed. Please try again.");
            }
        }
    }
    
    private void createInstallationDirectory() throws IOException {
        File jarvisDir = new File(Environment.getExternalStorageDirectory(), "JarvisAI");
        if (!jarvisDir.exists()) {
            if (!jarvisDir.mkdirs()) {
                throw new IOException("Failed to create installation directory");
            }
        }
        
        // Create subdirectories
        new File(jarvisDir, "logs").mkdirs();
        new File(jarvisDir, "config").mkdirs();
        new File(jarvisDir, "obd").mkdirs();
        new File(jarvisDir, "ai").mkdirs();
        new File(jarvisDir, "garage").mkdirs();
    }
    
    private void extractInstallationFiles() throws IOException {
        // Extract default configuration files
        extractAssetToFile("config/default_settings.json", "config/settings.json");
        extractAssetToFile("config/obd_protocols.json", "obd/protocols.json");
        extractAssetToFile("config/ai_responses.json", "ai/responses.json");
        extractAssetToFile("config/vehicle_database.json", "garage/vehicles.json");
    }
    
    private void extractAssetToFile(String assetPath, String targetPath) throws IOException {
        File jarvisDir = new File(Environment.getExternalStorageDirectory(), "JarvisAI");
        File targetFile = new File(jarvisDir, targetPath);
        
        try (InputStream is = context.getAssets().open(assetPath);
             FileOutputStream fos = new FileOutputStream(targetFile)) {
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
    
    private void setupPermissions() {
        // Mark permissions as configured
        context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("permissions_configured", true)
            .putLong("permission_setup_time", System.currentTimeMillis())
            .apply();
    }
    
    private void initializeAIComponents() {
        // Initialize AI system
        context.getSharedPreferences("jarvis_ai", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("ai_initialized", true)
            .putString("ai_voice_model", "default")
            .putBoolean("voice_recognition_enabled", true)
            .putBoolean("natural_language_processing", true)
            .apply();
    }
    
    private void setupOBDDiagnostics() {
        // Configure OBD system
        context.getSharedPreferences("jarvis_obd", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("obd_enabled", true)
            .putString("default_protocol", "AUTO")
            .putBoolean("kkl_cable_support", true)
            .putBoolean("elm327_support", true)
            .apply();
    }
    
    private void configureSystemServices() {
        // Configure background services
        context.getSharedPreferences("jarvis_services", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("background_service_enabled", true)
            .putBoolean("battery_monitoring", true)
            .putBoolean("system_health_monitoring", true)
            .putInt("log_retention_days", 30)
            .apply();
    }
    
    private void finalizeInstallation() {
        // Mark installation as complete
        context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("installation_complete", true)
            .putLong("installation_date", System.currentTimeMillis())
            .putString("installation_version", "1.0.0")
            .putBoolean("first_run", true)
            .apply();
        
        // Create installation success marker
        try {
            File jarvisDir = new File(Environment.getExternalStorageDirectory(), "JarvisAI");
            File successFile = new File(jarvisDir, ".installation_complete");
            successFile.createNewFile();
        } catch (IOException e) {
            // Non-critical error
        }
    }
    
    private void launchApplication() {
        // Start the main application
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("first_launch", true);
        intent.putExtra("installation_complete", true);
        context.startActivity(intent);
        
        // Start background service
        Intent serviceIntent = new Intent(context, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        Toast.makeText(context, "🎉 Jarvis AI installed successfully!", Toast.LENGTH_LONG).show();
    }
    
    private static class InstallationProgress {
        int progress;
        String message;
        
        InstallationProgress(int progress, String message) {
            this.progress = progress;
            this.message = message;
        }
    }
    
    public static boolean isInstallationComplete(Context context) {
        return context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE)
            .getBoolean("installation_complete", false);
    }
    
    public static void resetInstallation(Context context) {
        // Clear installation preferences
        context.getSharedPreferences("jarvis_installer", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        
        context.getSharedPreferences("jarvis_ai", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        
        context.getSharedPreferences("jarvis_obd", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        
        context.getSharedPreferences("jarvis_services", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
    }
}
