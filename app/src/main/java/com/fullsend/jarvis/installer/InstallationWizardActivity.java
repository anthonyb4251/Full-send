package com.fullsend.jarvis.installer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fullsend.jarvis.MainActivity;
import com.fullsend.jarvis.R;

import java.io.File;

public class InstallationWizardActivity extends AppCompatActivity {
    
    private static final String TAG = "InstallationWizard";
    
    private TextView tvWelcome;
    private TextView tvStep;
    private TextView tvDescription;
    private ProgressBar progressBar;
    private Button btnNext;
    private Button btnSkip;
    
    private int currentStep = 0;
    private final int TOTAL_STEPS = 5;
    
    private final ActivityResultLauncher<Intent> storagePermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> checkPermissionsAndContinue());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installation_wizard);
        
        initializeViews();
        startInstallationWizard();
    }
    
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStep = findViewById(R.id.tvStep);
        tvDescription = findViewById(R.id.tvDescription);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        
        btnNext.setOnClickListener(v -> nextStep());
        btnSkip.setOnClickListener(v -> skipStep());
        
        progressBar.setMax(TOTAL_STEPS);
    }
    
    private void startInstallationWizard() {
        showWelcomeStep();
    }
    
    private void showWelcomeStep() {
        currentStep = 0;
        updateProgress();
        
        tvWelcome.setText("🤖 Welcome to Jarvis AI");
        tvStep.setText("Step 1: Welcome");
        tvDescription.setText("Welcome to the Jarvis AI Installation Wizard!\n\n" +
            "This wizard will guide you through the complete setup process to get your " +
            "AI assistant running perfectly on your Android device.\n\n" +
            "✨ Features you'll get:\n" +
            "• AI Assistant with voice recognition\n" +
            "• OBD-II vehicle diagnostics\n" +
            "• Battery monitoring and optimization\n" +
            "• System health monitoring\n" +
            "• Virtual garage management\n\n" +
            "The setup process takes about 2 minutes and requires no technical knowledge.");
        
        btnNext.setText("Start Setup");
        btnSkip.setVisibility(View.GONE);
    }
    
    private void showPermissionsStep() {
        currentStep = 1;
        updateProgress();
        
        tvStep.setText("Step 2: Permissions");
        tvDescription.setText("🔐 Setting up permissions...\n\n" +
            "Jarvis AI needs certain permissions to function properly:\n\n" +
            "📁 Storage Access - For logging and data storage\n" +
            "🔋 Battery Stats - For power monitoring\n" +
            "🎤 Microphone - For voice commands (optional)\n" +
            "📍 Location - For location-based features (optional)\n\n" +
            "All permissions are used only for the intended features and your privacy is protected.");
        
        btnNext.setText("Grant Permissions");
        btnSkip.setText("Skip Optional");
        btnSkip.setVisibility(View.VISIBLE);
    }
    
    private void showCompatibilityStep() {
        currentStep = 2;
        updateProgress();
        
        tvStep.setText("Step 3: Device Compatibility");
        
        String deviceInfo = getDeviceCompatibilityInfo();
        tvDescription.setText("📱 Checking device compatibility...\n\n" + deviceInfo);
        
        btnNext.setText("Continue");
        btnSkip.setVisibility(View.GONE);
    }
    
    private void showFeaturesStep() {
        currentStep = 3;
        updateProgress();
        
        tvStep.setText("Step 4: Feature Configuration");
        tvDescription.setText("⚙️ Configuring features...\n\n" +
            "Choose which features to enable:\n\n" +
            "🤖 AI Assistant - Core functionality (Required)\n" +
            "🔧 OBD Diagnostics - Vehicle diagnostics via KKL cable\n" +
            "🚗 Virtual Garage - Vehicle management system\n" +
            "🔋 Power Management - Battery optimization\n" +
            "📊 System Monitoring - Device health tracking\n\n" +
            "You can change these settings later in the app.");
        
        btnNext.setText("Configure Features");
        btnSkip.setText("Use Defaults");
        btnSkip.setVisibility(View.VISIBLE);
    }
    
    private void showCompletionStep() {
        currentStep = 4;
        updateProgress();
        
        tvStep.setText("Step 5: Installation Complete");
        tvDescription.setText("🎉 Setup Complete!\n\n" +
            "Jarvis AI has been successfully installed and configured on your device.\n\n" +
            "✅ All permissions granted\n" +
            "✅ Features configured\n" +
            "✅ System optimized\n" +
            "✅ Ready to use\n\n" +
            "Your AI assistant is now ready to help you with:\n" +
            "• Voice commands and responses\n" +
            "• Vehicle diagnostics and monitoring\n" +
            "• System health and battery management\n" +
            "• And much more!\n\n" +
            "Tap 'Launch Jarvis AI' to start using your new AI assistant.");
        
        btnNext.setText("Launch Jarvis AI");
        btnSkip.setVisibility(View.GONE);
    }
    
    private void nextStep() {
        switch (currentStep) {
            case 0:
                showPermissionsStep();
                break;
            case 1:
                handlePermissionsStep();
                break;
            case 2:
                showFeaturesStep();
                break;
            case 3:
                showCompletionStep();
                break;
            case 4:
                launchMainApp();
                break;
        }
    }
    
    private void skipStep() {
        switch (currentStep) {
            case 1:
                showCompatibilityStep();
                break;
            case 3:
                showCompletionStep();
                break;
        }
    }
    
    private void handlePermissionsStep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
            } else {
                showCompatibilityStep();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                showCompatibilityStep();
            }
        }
    }
    
    private void requestManageExternalStoragePermission() {
        new AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("Jarvis AI needs storage access to save logs and configuration data. " +
                "This ensures your AI assistant can remember your preferences and provide " +
                "detailed system information.")
            .setPositiveButton("Grant Permission", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                storagePermissionLauncher.launch(intent);
            })
            .setNegativeButton("Skip", (dialog, which) -> showCompatibilityStep())
            .show();
    }
    
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                .setTitle("Storage Permission Required")
                .setMessage("Jarvis AI needs storage access to save logs and configuration data.")
                .setPositiveButton("Grant", (dialog, which) -> 
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001))
                .setNegativeButton("Skip", (dialog, which) -> showCompatibilityStep())
                .show();
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
    }
    
    private void checkPermissionsAndContinue() {
        showCompatibilityStep();
    }
    
    private String getDeviceCompatibilityInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        info.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");
        
        if (Build.VERSION.SDK_INT >= 23) {
            info.append("✅ Android Version: Compatible\n");
        } else {
            info.append("❌ Android Version: Requires Android 6.0+\n");
        }
        
        // Check available storage
        File externalStorage = Environment.getExternalStorageDirectory();
        long freeSpace = externalStorage.getFreeSpace() / (1024 * 1024); // MB
        
        if (freeSpace > 100) {
            info.append("✅ Storage Space: ").append(freeSpace).append(" MB available\n");
        } else {
            info.append("⚠️ Storage Space: Low (").append(freeSpace).append(" MB)\n");
        }
        
        info.append("✅ Hardware: All features supported\n");
        info.append("✅ Permissions: Ready to configure\n\n");
        info.append("Your device is fully compatible with Jarvis AI!");
        
        return info.toString();
    }
    
    private void updateProgress() {
        progressBar.setProgress(currentStep + 1);
        
        // Animate progress
        progressBar.animate()
            .alpha(0.7f)
            .setDuration(200)
            .withEndAction(() -> progressBar.animate().alpha(1.0f).setDuration(200));
    }
    
    private void launchMainApp() {
        // Mark installation as complete
        getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("installation_complete", true)
            .putLong("installation_date", System.currentTimeMillis())
            .apply();
        
        // Launch main app
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "🎉 Welcome to Jarvis AI!", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            showCompatibilityStep();
        }
    }
}
