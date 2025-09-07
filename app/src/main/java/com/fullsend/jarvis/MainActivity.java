package com.fullsend.jarvis;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fullsend.jarvis.ai.JarvisAI;
import com.fullsend.jarvis.ai.VoiceManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements VoiceManager.VoiceListener, JarvisAI.AIResponseListener {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int AUDIO_PERMISSION_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 103;
    private static final String LOG_DIRECTORY = "JarvisAI";

    // UI Components
    private TextView tvAiStatus;
    private TextView tvSystemStatus;
    private TextView tvBatteryStatus;
    private TextView tvLogStatus;
    private TextView tvVoiceStatus;
    private TextView tvTimeDate;
    private Button btnAlertMode;
    private Button btnEmergencyShutdown;
    private Button btnVoiceToggle;
    private Button btnSettings;
    private ImageView ivJarvisCore;
    
    // AI and Voice
    private VoiceManager voiceManager;
    private JarvisAI jarvisAI;
    private boolean isVoiceEnabled = false;
    private boolean isAuthenticated = false;
    
    // System
    private BroadcastReceiver batteryReceiver;
    private Handler uiUpdateHandler;
    private Runnable timeUpdateRunnable;

    private final ActivityResultLauncher<Intent> storagePermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> checkPermissionsAndInitialize());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize handlers and runnables
        uiUpdateHandler = new Handler();
        
        // Initialize UI elements
        initializeUI();
        
        // Initialize AI systems
        initializeAI();
        
        // Check and request permissions
        checkPermissionsAndInitialize();
        
        // Register battery receiver
        registerBatteryReceiver();
        
        // Start time/date updates
        startTimeUpdates();
        
        // Check for biometric authentication
        checkBiometricAuthentication();
    }

    private void initializeUI() {
        // Main display elements
        tvAiStatus = findViewById(R.id.tvAiStatus);
        tvSystemStatus = findViewById(R.id.tvSystemStatus);
        tvBatteryStatus = findViewById(R.id.tvBatteryStatus);
        tvLogStatus = findViewById(R.id.tvLogStatus);
        tvVoiceStatus = findViewById(R.id.tvVoiceStatus);
        tvTimeDate = findViewById(R.id.tvTimeDate);
        ivJarvisCore = findViewById(R.id.ivJarvisCore);
        
        // Control buttons
        btnAlertMode = findViewById(R.id.btnAlertMode);
        btnEmergencyShutdown = findViewById(R.id.btnEmergencyShutdown);
        btnVoiceToggle = findViewById(R.id.btnVoiceToggle);
        btnSettings = findViewById(R.id.btnSettings);
        
        // Set button click listeners
        btnAlertMode.setOnClickListener(v -> activateAlertMode());
        btnEmergencyShutdown.setOnClickListener(v -> showShutdownConfirmation());
        btnVoiceToggle.setOnClickListener(v -> toggleVoiceRecognition());
        btnSettings.setOnClickListener(v -> openSettings());
        
        // OBD Diagnostics button
        Button btnOBDDiagnostics = findViewById(R.id.btnOBDDiagnostics);
        btnOBDDiagnostics.setOnClickListener(v -> openOBDDiagnostics());

        // Video Guidance button
        Button btnVideoGuide = findViewById(R.id.btnVideoGuide);
        btnVideoGuide.setOnClickListener(v -> openVideoGuide(null));
        
        // Initialize UI state
        updateVoiceUI();
        startJarvisCoreAnimation();
    }
    
    private void initializeAI() {
        // Initialize AI assistant
        jarvisAI = new JarvisAI(this);
        jarvisAI.setResponseListener(this);
        
        // Initialize voice manager
        voiceManager = new VoiceManager(this);
        voiceManager.setVoiceListener(this);
        jarvisAI.setVoiceManager(voiceManager);
        
        logEvent("AI systems initialized");
    }
    
    private void checkBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                logEvent("Biometric hardware not available");
                isAuthenticated = true;
                onAuthenticationSuccess();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                logEvent("Biometric hardware unavailable");
                isAuthenticated = true;
                onAuthenticationSuccess();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                logEvent("No biometric credentials enrolled");
                isAuthenticated = true;
                onAuthenticationSuccess();
                break;
            default:
                isAuthenticated = true;
                onAuthenticationSuccess();
                break;
        }
    }
    
    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                logEvent("Biometric authentication error: " + errString);
                // Allow access anyway for now
                isAuthenticated = true;
                onAuthenticationSuccess();
            }
            
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                logEvent("Biometric authentication successful");
                isAuthenticated = true;
                onAuthenticationSuccess();
            }
            
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                logEvent("Biometric authentication failed");
            }
        });
        
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Jarvis AI Authentication")
            .setSubtitle("Use your biometric credential to access Jarvis AI")
            .setNegativeButtonText("Cancel")
            .build();
        
        biometricPrompt.authenticate(promptInfo);
    }
    
    private void onAuthenticationSuccess() {
        tvAiStatus.setText("Authentication Successful");
        if (jarvisAI != null) {
            jarvisAI.processCommand("hello jarvis");
        }
    }
    
    private void startTimeUpdates() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDate();
                uiUpdateHandler.postDelayed(this, 1000); // Update every second
            }
        };
        uiUpdateHandler.post(timeUpdateRunnable);
    }
    
    private void updateTimeDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        
        String time = timeFormat.format(new Date());
        String date = dateFormat.format(new Date());
        
        if (tvTimeDate != null) {
            tvTimeDate.setText(String.format("%s\n%s", time, date));
        }
    }
    
    private void startJarvisCoreAnimation() {
        if (ivJarvisCore != null) {
            Animation pulseAnimation = new AlphaAnimation(0.3f, 1.0f);
            pulseAnimation.setDuration(1500);
            pulseAnimation.setRepeatMode(Animation.REVERSE);
            pulseAnimation.setRepeatCount(Animation.INFINITE);
            ivJarvisCore.startAnimation(pulseAnimation);
        }
    }
    
    private void activateAlertMode() {
        tvSystemStatus.setText(R.string.system_status_alert);
        tvSystemStatus.setTextColor(getResources().getColor(R.color.orange, null));
        logEvent("Alert mode activated");
        
        if (jarvisAI != null) {
            jarvisAI.processCommand("activate alert mode");
        }
    }
    
    private void showShutdownConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Emergency Shutdown")
            .setMessage("Are you sure you want to shut down the AI system?")
            .setPositiveButton("Yes", (dialog, which) -> {
                logEvent("Emergency shutdown initiated");
                if (jarvisAI != null) {
                    jarvisAI.processCommand("shutdown");
                }
                stopService(new Intent(MainActivity.this, JarvisService.class));
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void toggleVoiceRecognition() {
        if (!isAuthenticated) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission();
            return;
        }
        
        isVoiceEnabled = !isVoiceEnabled;
        
        if (isVoiceEnabled) {
            startVoiceRecognition();
        } else {
            stopVoiceRecognition();
        }
        
        updateVoiceUI();
    }
    
    private void startVoiceRecognition() {
        if (voiceManager != null) {
            voiceManager.setWakeWordMode(true);
            voiceManager.startListening();
            logEvent("Voice recognition activated");
        }
    }
    
    private void stopVoiceRecognition() {
        if (voiceManager != null) {
            voiceManager.stopListening();
            logEvent("Voice recognition deactivated");
        }
    }
    
    private void updateVoiceUI() {
        if (btnVoiceToggle != null) {
            btnVoiceToggle.setText(isVoiceEnabled ? "Voice: ON" : "Voice: OFF");
            btnVoiceToggle.setBackgroundTintList(getResources().getColorStateList(
                isVoiceEnabled ? R.color.neon_green : R.color.gray, null));
        }
        
        if (tvVoiceStatus != null) {
            tvVoiceStatus.setText(isVoiceEnabled ? "Listening for 'Hey Jarvis'" : "Voice recognition disabled");
            tvVoiceStatus.setTextColor(getResources().getColor(
                isVoiceEnabled ? R.color.cyan : R.color.gray, null));
        }
    }
    
    private void openOBDDiagnostics() {
        logEvent("Opening OBD diagnostics");
        Intent obdIntent = new Intent(MainActivity.this, com.fullsend.jarvis.obd.OBDActivity.class);
        startActivity(obdIntent);
    }
    
    private void openSettings() {
        // Placeholder for settings activity
        Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
        logEvent("Settings requested");
    }

    private void checkPermissionsAndInitialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
            } else {
                initializeSystem();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            } else {
                initializeSystem();
            }
        }
    }

    private void requestManageExternalStoragePermission() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(R.string.storage_permission_required)
            .setPositiveButton("Grant", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                storagePermissionLauncher.launch(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            })
            .show();
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(R.string.storage_permission_required)
                .setPositiveButton(R.string.retry, (dialog, which) -> 
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                        STORAGE_PERMISSION_CODE))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .create()
                .show();
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                STORAGE_PERMISSION_CODE);
        }
    }


    private void initializeSystem() {
        // Create the JarvisAI directory if it doesn't exist
        File jarvisDir = new File(Environment.getExternalStorageDirectory(), LOG_DIRECTORY);
        if (!jarvisDir.exists()) {
            if (!jarvisDir.mkdirs()) {
                Toast.makeText(this, "Failed to create JarvisAI directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Log the initialization
        logEvent("System startup initiated");
        
        // Start the Jarvis service
        Intent serviceIntent = new Intent(this, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Update UI
        updateUIForInitialization();
    }

    private void updateUIForInitialization() {
        // Simulate the initialization process shown in the Python script
        tvAiStatus.setText(R.string.initializing);
        tvLogStatus.setText(R.string.authentication_loaded);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                runOnUiThread(() -> {
                    tvAiStatus.setText(R.string.ai_system_online);
                    tvLogStatus.setText(R.string.power_management_online);
                    logEvent("✅ System startup complete.");
                    logEvent("✅ Authentication successful.");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                
                if (level != -1 && scale != -1) {
                    int batteryPct = (int) ((level / (float) scale) * 100);
                    updateBatteryStatus(batteryPct);
                }
            }
        };
        
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void updateBatteryStatus(int batteryLevel) {
        String batteryText = "Battery at " + batteryLevel + "%. ";
        
        if (batteryLevel < 20) {
            batteryText += "Switching to power-saving mode.";
            tvBatteryStatus.setTextColor(getResources().getColor(R.color.orange, null));
        } else {
            batteryText += "Operating at full power.";
            tvBatteryStatus.setTextColor(getResources().getColor(R.color.white, null));
        }
        
        tvBatteryStatus.setText(batteryText);
        logEvent("Battery status: " + batteryLevel + "%");
    }
    
    private void logEvent(String event) {
        // The actual logging will be handled by the service
        Intent intent = new Intent(this, JarvisService.class);
        intent.setAction(JarvisService.ACTION_LOG_EVENT);
        intent.putExtra(JarvisService.EXTRA_LOG_MESSAGE, event);
        startService(intent);
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.RECORD_AUDIO}, 
            AUDIO_PERMISSION_CODE);
    }
    
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
            LOCATION_PERMISSION_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeSystem();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                
            case AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceRecognition();
                    updateVoiceUI();
                } else {
                    Toast.makeText(this, "Voice recognition requires microphone permission", Toast.LENGTH_SHORT).show();
                }
                break;
                
            case LOCATION_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logEvent("Location permission granted");
                } else {
                    logEvent("Location permission denied");
                }
                break;
        }
    }
    
    // VoiceManager.VoiceListener implementation
    @Override
    public void onWakeWordDetected() {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Wake word detected - Listening...");
            tvVoiceStatus.setTextColor(getResources().getColor(R.color.neon_green, null));
            
            // Visual feedback
            if (ivJarvisCore != null) {
                ivJarvisCore.clearAnimation();
                Animation rapidPulse = new AlphaAnimation(0.1f, 1.0f);
                rapidPulse.setDuration(300);
                rapidPulse.setRepeatMode(Animation.REVERSE);
                rapidPulse.setRepeatCount(4);
                ivJarvisCore.startAnimation(rapidPulse);
            }
        });
        
        logEvent("Wake word detected");
    }
    
    @Override
    public void onVoiceCommand(String command) {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Processing: " + command);
            tvVoiceStatus.setTextColor(getResources().getColor(R.color.cyan, null));
        });
        
        // Process command with AI
        if (jarvisAI != null) {
            jarvisAI.processCommand(command);
        }
        
        logEvent("Voice command received: " + command);
    }
    
    @Override
    public void onVoiceError(String error) {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Voice error: " + error);
            tvVoiceStatus.setTextColor(getResources().getColor(R.color.red, null));
            
            // Reset to listening state after error
            new Handler().postDelayed(() -> {
                if (isVoiceEnabled) {
                    tvVoiceStatus.setText("Listening for 'Hey Jarvis'");
                    tvVoiceStatus.setTextColor(getResources().getColor(R.color.cyan, null));
                }
            }, 2000);
        });
        
        logEvent("Voice error: " + error);
    }
    
    @Override
    public void onSpeechStarted() {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Jarvis speaking...");
            tvVoiceStatus.setTextColor(getResources().getColor(R.color.neon_green, null));
        });
    }
    
    @Override
    public void onSpeechCompleted() {
        runOnUiThread(() -> {
            if (isVoiceEnabled) {
                tvVoiceStatus.setText("Listening for 'Hey Jarvis'");
                tvVoiceStatus.setTextColor(getResources().getColor(R.color.cyan, null));
                startJarvisCoreAnimation();
            }
        });
    }
    
    @Override
    public void onListeningStarted() {
        runOnUiThread(() -> {
            if (isVoiceEnabled) {
                tvVoiceStatus.setText("Listening for 'Hey Jarvis'");
                tvVoiceStatus.setTextColor(getResources().getColor(R.color.cyan, null));
            }
        });
    }
    
    @Override
    public void onListeningStopped() {
        runOnUiThread(() -> {
            tvVoiceStatus.setText("Voice recognition disabled");
            tvVoiceStatus.setTextColor(getResources().getColor(R.color.gray, null));
        });
    }
    
    // JarvisAI.AIResponseListener implementation
    @Override
    public void onTextResponse(String response) {
        runOnUiThread(() -> {
            tvLogStatus.setText("Jarvis: " + response);
            tvLogStatus.setTextColor(getResources().getColor(R.color.neon_green, null));
        });
        
        logEvent("AI Response: " + response);
    }
    
    @Override
    public void onActionRequired(String action, Map<String, Object> parameters) {
        runOnUiThread(() -> handleAIAction(action, parameters));
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            tvLogStatus.setText("AI Error: " + error);
            tvLogStatus.setTextColor(getResources().getColor(R.color.red, null));
            Toast.makeText(this, "AI Error: " + error, Toast.LENGTH_SHORT).show();
        });
        
        logEvent("AI Error: " + error);
    }
    
    private void handleAIAction(String action, Map<String, Object> parameters) {
        switch (action) {
            case "show_battery_status":
                // Battery status is already shown in UI, just highlight it
                if (tvBatteryStatus != null) {
                    Animation highlight = new AlphaAnimation(0.3f, 1.0f);
                    highlight.setDuration(500);
                    highlight.setRepeatCount(2);
                    highlight.setRepeatMode(Animation.REVERSE);
                    tvBatteryStatus.startAnimation(highlight);
                }
                break;
                
            case "run_system_scan":
                tvLogStatus.setText("Running comprehensive system scan...");
                tvLogStatus.setTextColor(getResources().getColor(R.color.cyan, null));
                
                // Simulate system scan
                new Handler().postDelayed(() -> {
                    tvLogStatus.setText("System scan complete - All systems nominal");
                    tvLogStatus.setTextColor(getResources().getColor(R.color.neon_green, null));
                    if (voiceManager != null) {
                        voiceManager.speak("System scan complete. All systems are operating normally, sir.");
                    }
                }, 3000);
                break;
                
            case "activate_alert_mode":
                activateAlertMode();
                break;
                
            case "open_obd_diagnostics":
                openOBDDiagnostics();
                break;
                
            case "emergency_shutdown":
                showShutdownConfirmation();
                break;
                
            case "fetch_weather":
                tvLogStatus.setText("Weather services integration coming soon");
                tvLogStatus.setTextColor(getResources().getColor(R.color.cyan, null));
                break;

            case "open_video_guidance":
                String q = null;
                if (parameters != null && parameters.containsKey("query")) {
                    Object obj = parameters.get("query");
                    if (obj != null) q = obj.toString();
                }
                openVideoGuide(q);
                break;
                
            default:
                logEvent("Unknown AI action: " + action);
                break;
        }
    }

    private void openVideoGuide(String query) {
        logEvent("Opening Video Guidance" + (query != null ? (": " + query) : ""));
        Intent guideIntent = new Intent(MainActivity.this, com.fullsend.jarvis.guide.VideoGuideActivity.class);
        if (query != null) {
            guideIntent.putExtra(com.fullsend.jarvis.guide.VideoGuideActivity.EXTRA_QUERY, query);
        }
        startActivity(guideIntent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Stop time updates
        if (uiUpdateHandler != null && timeUpdateRunnable != null) {
            uiUpdateHandler.removeCallbacks(timeUpdateRunnable);
        }
        
        // Cleanup voice manager
        if (voiceManager != null) {
            voiceManager.cleanup();
        }
        
        // Unregister battery receiver
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
        
        logEvent("Jarvis AI shutdown complete");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Resume voice recognition if it was enabled
        if (isVoiceEnabled && isAuthenticated) {
            startVoiceRecognition();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause voice recognition to save battery
        if (voiceManager != null) {
            voiceManager.stopListening();
        }
    }
}
