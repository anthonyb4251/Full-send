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
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String LOG_DIRECTORY = "JarvisAI";

    private TextView tvAiStatus;
    private TextView tvSystemStatus;
    private TextView tvBatteryStatus;
    private TextView tvLogStatus;
    private Button btnAlertMode;
    private Button btnEmergencyShutdown;
    
    private BroadcastReceiver batteryReceiver;

    private final ActivityResultLauncher<Intent> storagePermissionLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), 
            result -> checkPermissionsAndInitialize());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI elements
        initializeUI();
        
        // Check and request permissions
        checkPermissionsAndInitialize();
        
        // Register battery receiver
        registerBatteryReceiver();
    }

    private void initializeUI() {
        tvAiStatus = findViewById(R.id.tvAiStatus);
        tvSystemStatus = findViewById(R.id.tvSystemStatus);
        tvBatteryStatus = findViewById(R.id.tvBatteryStatus);
        tvLogStatus = findViewById(R.id.tvLogStatus);
        btnAlertMode = findViewById(R.id.btnAlertMode);
        btnEmergencyShutdown = findViewById(R.id.btnEmergencyShutdown);
        
        // Set button click listeners
        btnAlertMode.setOnClickListener(v -> {
            tvSystemStatus.setText(R.string.system_status_alert);
            tvSystemStatus.setTextColor(getResources().getColor(R.color.orange, null));
            logEvent("Alert mode activated");
        });
        
        btnEmergencyShutdown.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Emergency Shutdown")
                .setMessage("Are you sure you want to shut down the AI system?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logEvent("Emergency shutdown initiated");
                    stopService(new Intent(MainActivity.this, JarvisService.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
        });
        
        // Add OBD Diagnostics button
        Button btnOBDDiagnostics = findViewById(R.id.btnOBDDiagnostics);
        btnOBDDiagnostics.setOnClickListener(v -> {
            logEvent("Opening OBD diagnostics");
            Intent obdIntent = new Intent(MainActivity.this, com.fullsend.jarvis.obd.OBDActivity.class);
            startActivity(obdIntent);
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSystem();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
