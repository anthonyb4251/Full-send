package com.fullsend.jarvis.obd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.fullsend.jarvis.JarvisService;
import com.fullsend.jarvis.R;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class OBDActivity extends AppCompatActivity implements 
    KKLCableManager.KKLConnectionListener {
    
    private static final String TAG = "OBDActivity";
    
    private KKLCableManager kklManager;
    private OBDProtocol obdProtocol;
    private Handler mainHandler;
    private Timer dataTimer;
    
    // UI Components
    private TextView tvConnectionStatus;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnReadDTCs;
    private Button btnClearDTCs;
    private Button btnLiveData;
    private Button btnAdvancedDiag;
    private ProgressBar progressBar;
    private LinearLayout layoutLiveData;
    private LinearLayout layoutDTCs;
    private LinearLayout layoutAdvanced;
    private ScrollView scrollViewMain;
    
    // Live data TextViews
    private TextView tvRPM;
    private TextView tvSpeed;
    private TextView tvCoolantTemp;
    private TextView tvEngineLoad;
    private TextView tvThrottlePos;
    private TextView tvMAF;
    private TextView tvFuelPressure;
    private TextView tvTimingAdvance;
    
    // Status flags
    private boolean isConnected = false;
    private boolean isLiveDataActive = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        initializeUI();
        initializeOBD();
        
        logEvent("OBD Diagnostic system initialized");
    }
    
    private void initializeUI() {
        // Connection controls
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        progressBar = findViewById(R.id.progressBar);
        
        // Main function buttons
        btnReadDTCs = findViewById(R.id.btnReadDTCs);
        btnClearDTCs = findViewById(R.id.btnClearDTCs);
        btnLiveData = findViewById(R.id.btnLiveData);
        btnAdvancedDiag = findViewById(R.id.btnAdvancedDiag);
        
        // Layout containers
        layoutLiveData = findViewById(R.id.layoutLiveData);
        layoutDTCs = findViewById(R.id.layoutDTCs);
        layoutAdvanced = findViewById(R.id.layoutAdvanced);
        scrollViewMain = findViewById(R.id.scrollViewMain);
        
        // Live data displays
        tvRPM = findViewById(R.id.tvRPM);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvCoolantTemp = findViewById(R.id.tvCoolantTemp);
        tvEngineLoad = findViewById(R.id.tvEngineLoad);
        tvThrottlePos = findViewById(R.id.tvThrottlePos);
        tvMAF = findViewById(R.id.tvMAF);
        tvFuelPressure = findViewById(R.id.tvFuelPressure);
        tvTimingAdvance = findViewById(R.id.tvTimingAdvance);
        
        // Set click listeners
        btnConnect.setOnClickListener(v -> connectToKKL());
        btnDisconnect.setOnClickListener(v -> disconnectFromKKL());
        btnReadDTCs.setOnClickListener(v -> readDTCs());
        btnClearDTCs.setOnClickListener(v -> clearDTCs());
        btnLiveData.setOnClickListener(v -> toggleLiveData());
        btnAdvancedDiag.setOnClickListener(v -> showAdvancedDiagnostics());
        
        // Initially disable buttons until connected
        updateConnectionUI(false);
    }
    
    private void initializeOBD() {
        kklManager = new KKLCableManager(this);
        kklManager.setConnectionListener(this);
        obdProtocol = new OBDProtocol(kklManager);
    }
    
    private void connectToKKL() {
        tvConnectionStatus.setText("Searching for KKL cable...");
        progressBar.setVisibility(View.VISIBLE);
        btnConnect.setEnabled(false);
        
        logEvent("Attempting to connect to KKL cable");
        
        new Thread(() -> {
            boolean found = kklManager.findAndConnectKKLCable();
            if (!found) {
                mainHandler.post(() -> {
                    tvConnectionStatus.setText("No KKL cable found");
                    progressBar.setVisibility(View.GONE);
                    btnConnect.setEnabled(true);
                    Toast.makeText(this, "No USB 409.1 KKL cable detected", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void disconnectFromKKL() {
        stopLiveData();
        kklManager.disconnect();
        logEvent("Disconnected from KKL cable");
    }
    
    private void readDTCs() {
        progressBar.setVisibility(View.VISIBLE);
        layoutDTCs.removeAllViews();
        
        logEvent("Reading stored DTCs");
        
        obdProtocol.getStoredDTCs(response -> mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            if (response.success) {
                displayDTCs(response, "Stored DTCs");
                
                // Also read pending DTCs
                obdProtocol.getPendingDTCs(pendingResponse -> mainHandler.post(() -> {
                    if (pendingResponse.success) {
                        displayDTCs(pendingResponse, "Pending DTCs");
                    }
                }));
            } else {
                showError("Failed to read DTCs: " + response.errorMessage);
            }
        }));
    }
    
    private void clearDTCs() {
        progressBar.setVisibility(View.VISIBLE);
        
        logEvent("Clearing DTCs");
        
        obdProtocol.clearDTCs(response -> mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            if (response.success) {
                Toast.makeText(this, "DTCs cleared successfully", Toast.LENGTH_SHORT).show();
                layoutDTCs.removeAllViews();
                logEvent("DTCs cleared successfully");
            } else {
                showError("Failed to clear DTCs: " + response.errorMessage);
            }
        }));
    }
    
    private void toggleLiveData() {
        if (isLiveDataActive) {
            stopLiveData();
        } else {
            startLiveData();
        }
    }
    
    private void startLiveData() {
        isLiveDataActive = true;
        btnLiveData.setText("Stop Live Data");
        layoutLiveData.setVisibility(View.VISIBLE);
        
        logEvent("Started live data monitoring");
        
        dataTimer = new Timer();
        dataTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isLiveDataActive && isConnected) {
                    updateLiveDataValues();
                }
            }
        }, 0, 1000); // Update every second
    }
    
    private void stopLiveData() {
        isLiveDataActive = false;
        btnLiveData.setText("Start Live Data");
        
        if (dataTimer != null) {
            dataTimer.cancel();
            dataTimer = null;
        }
        
        logEvent("Stopped live data monitoring");
    }
    
    private void updateLiveDataValues() {
        // Read RPM
        obdProtocol.getCurrentData(OBDProtocol.PID_ENGINE_RPM, response -> {
            if (response.success && response.parsedData.containsKey("rpm")) {
                int rpm = (Integer) response.parsedData.get("rpm");
                mainHandler.post(() -> tvRPM.setText(String.format(Locale.getDefault(), "RPM: %d", rpm)));
            }
        });
        
        // Read Vehicle Speed
        obdProtocol.getCurrentData(OBDProtocol.PID_VEHICLE_SPEED, response -> {
            if (response.success && response.parsedData.containsKey("speed")) {
                int speed = (Integer) response.parsedData.get("speed");
                mainHandler.post(() -> tvSpeed.setText(String.format(Locale.getDefault(), "Speed: %d km/h", speed)));
            }
        });
        
        // Read Coolant Temperature
        obdProtocol.getCurrentData(OBDProtocol.PID_COOLANT_TEMP, response -> {
            if (response.success && response.parsedData.containsKey("temperature")) {
                int temp = (Integer) response.parsedData.get("temperature");
                mainHandler.post(() -> tvCoolantTemp.setText(String.format(Locale.getDefault(), "Coolant: %d°C", temp)));
            }
        });
        
        // Read Engine Load
        obdProtocol.getCurrentData(OBDProtocol.PID_ENGINE_LOAD, response -> {
            if (response.success && response.parsedData.containsKey("load")) {
                double load = (Double) response.parsedData.get("load");
                mainHandler.post(() -> tvEngineLoad.setText(String.format(Locale.getDefault(), "Load: %.1f%%", load)));
            }
        });
        
        // Read Throttle Position
        obdProtocol.getCurrentData(OBDProtocol.PID_THROTTLE_POSITION, response -> {
            if (response.success && response.parsedData.containsKey("throttle")) {
                double throttle = (Double) response.parsedData.get("throttle");
                mainHandler.post(() -> tvThrottlePos.setText(String.format(Locale.getDefault(), "Throttle: %.1f%%", throttle)));
            }
        });
        
        // Read MAF
        obdProtocol.getCurrentData(OBDProtocol.PID_MAF_AIR_FLOW, response -> {
            if (response.success && response.parsedData.containsKey("maf")) {
                double maf = (Double) response.parsedData.get("maf");
                mainHandler.post(() -> tvMAF.setText(String.format(Locale.getDefault(), "MAF: %.2f g/s", maf)));
            }
        });
        
        // Read Fuel Pressure
        obdProtocol.getCurrentData(OBDProtocol.PID_FUEL_PRESSURE, response -> {
            if (response.success && response.parsedData.containsKey("fuel_pressure")) {
                int pressure = (Integer) response.parsedData.get("fuel_pressure");
                mainHandler.post(() -> tvFuelPressure.setText(String.format(Locale.getDefault(), "Fuel: %d kPa", pressure)));
            }
        });
        
        // Read Timing Advance
        obdProtocol.getCurrentData(OBDProtocol.PID_TIMING_ADVANCE, response -> {
            if (response.success && response.parsedData.containsKey("timing_advance")) {
                double timing = (Double) response.parsedData.get("timing_advance");
                mainHandler.post(() -> tvTimingAdvance.setText(String.format(Locale.getDefault(), "Timing: %.1f°", timing)));
            }
        });
    }
    
    private void showAdvancedDiagnostics() {
        layoutAdvanced.removeAllViews();
        layoutAdvanced.setVisibility(View.VISIBLE);
        
        // Create advanced diagnostic options
        addAdvancedOption("Enter Programming Session", () -> enterProgrammingSession());
        addAdvancedOption("Read ECU Information", () -> readECUInfo());
        addAdvancedOption("Security Access Test", () -> testSecurityAccess());
        addAdvancedOption("Actuator Tests", () -> showActuatorTests());
        addAdvancedOption("Memory Read", () -> showMemoryRead());
        addAdvancedOption("Custom Commands", () -> showCustomCommands());
        
        logEvent("Accessed advanced diagnostics menu");
    }
    
    private void addAdvancedOption(String title, Runnable action) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4);
        cardView.setRadius(8);
        
        Button button = new Button(this);
        button.setText(title);
        button.setTextColor(getResources().getColor(R.color.white, null));
        button.setBackground(getResources().getDrawable(R.color.primary, null));
        button.setOnClickListener(v -> action.run());
        
        cardView.addView(button);
        layoutAdvanced.addView(cardView);
    }
    
    private void enterProgrammingSession() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Enter extended diagnostic session
        obdProtocol.enterDiagnosticSession((byte) 0x03, response -> mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            if (response.success) {
                Toast.makeText(this, "Programming session activated", Toast.LENGTH_SHORT).show();
                logEvent("Entered programming diagnostic session");
            } else {
                showError("Failed to enter programming session: " + response.errorMessage);
            }
        }));
    }
    
    private void readECUInfo() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Read VIN
        obdProtocol.getVehicleInfo((byte) 0x02, response -> mainHandler.post(() -> {
            if (response.success && response.parsedData.containsKey("vin")) {
                String vin = (String) response.parsedData.get("vin");
                addInfoDisplay("VIN: " + vin);
                // Persist VIN for video guidance and vehicle profile features
                com.fullsend.jarvis.guide.VehicleProfileManager.saveVIN(this, vin);
            }
            
            // Read ECU name
            obdProtocol.getVehicleInfo((byte) 0x0A, ecuResponse -> mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                if (ecuResponse.success && ecuResponse.parsedData.containsKey("ecu_name")) {
                    String ecuName = (String) ecuResponse.parsedData.get("ecu_name");
                    addInfoDisplay("ECU: " + ecuName);
                }
            }));
        }));
    }
    
    private void testSecurityAccess() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Request seed for security level 1
        obdProtocol.requestSeed((byte) 0x01, response -> mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            if (response.success) {
                addInfoDisplay("Security seed received: " + bytesToHex(response.rawData));
                logEvent("Security access seed request successful");
            } else {
                showError("Security access failed: " + response.errorMessage);
            }
        }));
    }
    
    private void showActuatorTests() {
        // Implementation for actuator tests would go here
        addInfoDisplay("Actuator Tests - Feature in development");
        logEvent("Accessed actuator tests (development feature)");
    }
    
    private void showMemoryRead() {
        // Implementation for memory reading would go here
        addInfoDisplay("Memory Read - Feature in development");
        logEvent("Accessed memory read (development feature)");
    }
    
    private void showCustomCommands() {
        // Implementation for custom commands would go here
        addInfoDisplay("Custom Commands - Feature in development");
        logEvent("Accessed custom commands (development feature)");
    }
    
    private void addInfoDisplay(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.cyan, null));
        textView.setTextSize(14);
        textView.setPadding(16, 8, 16, 8);
        layoutAdvanced.addView(textView);
    }
    
    private void displayDTCs(OBDProtocol.OBDResponse response, String title) {
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(getResources().getColor(R.color.neon_green, null));
        titleView.setTextSize(18);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(16, 16, 16, 8);
        layoutDTCs.addView(titleView);
        
        if (response.parsedData.containsKey("dtcs")) {
            @SuppressWarnings("unchecked")
            List<String> dtcs = (List<String>) response.parsedData.get("dtcs");
            
            if (dtcs.isEmpty()) {
                TextView noDtcView = new TextView(this);
                noDtcView.setText("No DTCs found");
                noDtcView.setTextColor(getResources().getColor(R.color.white, null));
                noDtcView.setPadding(16, 8, 16, 8);
                layoutDTCs.addView(noDtcView);
            } else {
                for (String dtc : dtcs) {
                    TextView dtcView = new TextView(this);
                    dtcView.setText("• " + dtc);
                    dtcView.setTextColor(getResources().getColor(R.color.orange, null));
                    dtcView.setTextSize(16);
                    dtcView.setPadding(16, 4, 16, 4);
                    layoutDTCs.addView(dtcView);
                }
            }
        }
        
        if (response.parsedData.containsKey("dtc_count")) {
            int count = (Integer) response.parsedData.get("dtc_count");
            logEvent(title + ": " + count + " DTCs found");
        }
    }
    
    private void updateConnectionUI(boolean connected) {
        isConnected = connected;
        btnConnect.setEnabled(!connected);
        btnDisconnect.setEnabled(connected);
        btnReadDTCs.setEnabled(connected);
        btnClearDTCs.setEnabled(connected);
        btnLiveData.setEnabled(connected);
        btnAdvancedDiag.setEnabled(connected);
        
        if (!connected) {
            stopLiveData();
            layoutLiveData.setVisibility(View.GONE);
            layoutDTCs.setVisibility(View.GONE);
            layoutAdvanced.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }
    
    private void logEvent(String event) {
        Intent intent = new Intent(this, JarvisService.class);
        intent.setAction(JarvisService.ACTION_LOG_EVENT);
        intent.putExtra(JarvisService.EXTRA_LOG_MESSAGE, "OBD: " + event);
        startService(intent);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }
    
    // KKLConnectionListener implementation
    @Override
    public void onConnectionEstablished() {
        mainHandler.post(() -> {
            tvConnectionStatus.setText("KKL Cable Connected");
            progressBar.setVisibility(View.GONE);
            updateConnectionUI(true);
            Toast.makeText(this, "USB 409.1 KKL Cable Connected", Toast.LENGTH_SHORT).show();
            logEvent("KKL cable connection established");
        });
    }
    
    @Override
    public void onConnectionLost() {
        mainHandler.post(() -> {
            tvConnectionStatus.setText("Connection Lost");
            updateConnectionUI(false);
            Toast.makeText(this, "KKL Cable Disconnected", Toast.LENGTH_SHORT).show();
            logEvent("KKL cable connection lost");
        });
    }
    
    @Override
    public void onDataReceived(byte[] data) {
        // Handle raw data if needed
        Log.d(TAG, "Data received: " + bytesToHex(data));
    }
    
    @Override
    public void onError(String error) {
        mainHandler.post(() -> {
            tvConnectionStatus.setText("Error: " + error);
            progressBar.setVisibility(View.GONE);
            updateConnectionUI(false);
            showError(error);
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLiveData();
        if (kklManager != null) {
            kklManager.cleanup();
        }
        logEvent("OBD diagnostic system shutdown");
    }
}
