package com.fullsend.jarvis.obd;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KKLCableManager {
    private static final String TAG = "KKLCableManager";
    private static final String ACTION_USB_PERMISSION = "com.fullsend.jarvis.USB_PERMISSION";
    
    // KKL Protocol parameters
    private static final int BAUD_RATE = 10400; // Standard KKL baud rate
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = UsbSerialPort.STOPBITS_1;
    private static final int PARITY = UsbSerialPort.PARITY_NONE;
    
    private Context context;
    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private UsbDeviceConnection connection;
    private ExecutorService executor;
    private KKLConnectionListener connectionListener;
    
    // K-Line timing parameters (ISO 14230-2)
    private static final int T_INIT = 25;      // Initial delay
    private static final int T_WUP = 50;       // Wake-up pattern duration
    private static final int T_P1_MIN = 0;     // Min time between end of wake-up and start address
    private static final int T_P1_MAX = 20;    // Max time between end of wake-up and start address
    private static final int T_P2_MIN = 25;    // Min time between start address and sync pattern
    private static final int T_P2_MAX = 50;    // Max time between start address and sync pattern
    
    public interface KKLConnectionListener {
        void onConnectionEstablished();
        void onConnectionLost();
        void onDataReceived(byte[] data);
        void onError(String error);
    }
    
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            connectToDevice(device);
                        }
                    } else {
                        Log.d(TAG, "Permission denied for device " + device);
                        if (connectionListener != null) {
                            connectionListener.onError("USB permission denied");
                        }
                    }
                }
            }
        }
    };
    
    public KKLCableManager(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.executor = Executors.newSingleThreadExecutor();
        
        // Register USB receiver
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }
    
    public void setConnectionListener(KKLConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public boolean findAndConnectKKLCable() {
        Log.d(TAG, "Searching for KKL cable...");
        
        // Find all available USB serial devices
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "No USB serial devices found");
            if (connectionListener != null) {
                connectionListener.onError("No USB devices found");
            }
            return false;
        }
        
        // Try to connect to the first available device
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDevice device = driver.getDevice();
        
        Log.d(TAG, "Found USB device: " + device.getDeviceName() + 
              " VID: " + String.format("0x%04X", device.getVendorId()) +
              " PID: " + String.format("0x%04X", device.getProductId()));
        
        // Check if we have permission
        if (usbManager.hasPermission(device)) {
            connectToDevice(device);
        } else {
            // Request permission
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, 
                new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(device, permissionIntent);
        }
        
        return true;
    }
    
    private void connectToDevice(UsbDevice device) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Connecting to USB device...");
                
                List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
                if (drivers.isEmpty()) {
                    throw new IOException("No USB serial drivers found");
                }
                
                UsbSerialDriver driver = drivers.get(0);
                connection = usbManager.openDevice(driver.getDevice());
                if (connection == null) {
                    throw new IOException("Failed to open USB connection");
                }
                
                serialPort = driver.getPorts().get(0);
                serialPort.open(connection);
                serialPort.setParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
                
                Log.d(TAG, "USB serial port opened successfully");
                
                // Initialize K-Line communication
                if (initializeKLine()) {
                    Log.d(TAG, "K-Line initialization successful");
                    if (connectionListener != null) {
                        connectionListener.onConnectionEstablished();
                    }
                } else {
                    throw new IOException("K-Line initialization failed");
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to device", e);
                if (connectionListener != null) {
                    connectionListener.onError("Connection failed: " + e.getMessage());
                }
                disconnect();
            }
        });
    }
    
    private boolean initializeKLine() {
        try {
            Log.d(TAG, "Initializing K-Line communication...");
            
            // Step 1: Initial delay
            Thread.sleep(T_INIT);
            
            // Step 2: Wake-up pattern (0x55 at 5 baud for 200ms, then switch to 10400 baud)
            serialPort.setParameters(5, DATA_BITS, STOP_BITS, PARITY);
            serialPort.write(new byte[]{0x55}, 1000);
            Thread.sleep(T_WUP);
            
            // Step 3: Switch to normal baud rate
            serialPort.setParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
            Thread.sleep(T_P1_MIN);
            
            // Step 4: Send start communication command (ISO 14230-2)
            byte[] startComm = {(byte) 0x81, 0x12, (byte) 0xF1, (byte) 0x81, (byte) 0x05}; // Start communication
            serialPort.write(startComm, 1000);
            
            // Step 5: Wait for ECU response
            Thread.sleep(T_P2_MIN);
            byte[] response = new byte[256];
            int bytesRead = serialPort.read(response, 1000);
            
            if (bytesRead > 0) {
                Log.d(TAG, "Received ECU response: " + bytesToHex(response, bytesRead));
                return true;
            } else {
                Log.w(TAG, "No response from ECU during initialization");
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "K-Line initialization failed", e);
            return false;
        }
    }
    
    public void sendCommand(byte[] command, CommandResponseListener listener) {
        if (serialPort == null || !serialPort.isOpen()) {
            if (listener != null) {
                listener.onError("KKL cable not connected");
            }
            return;
        }
        
        executor.execute(() -> {
            try {
                Log.d(TAG, "Sending command: " + bytesToHex(command, command.length));
                
                // Clear any pending data
                byte[] buffer = new byte[256];
                serialPort.read(buffer, 100); // Quick read to clear buffer
                
                // Send command
                serialPort.write(command, 1000);
                
                // Wait for response
                Thread.sleep(50); // Give ECU time to process
                
                byte[] response = new byte[256];
                int bytesRead = serialPort.read(response, 2000);
                
                if (bytesRead > 0) {
                    byte[] actualResponse = new byte[bytesRead];
                    System.arraycopy(response, 0, actualResponse, 0, bytesRead);
                    Log.d(TAG, "Received response: " + bytesToHex(actualResponse, bytesRead));
                    
                    if (listener != null) {
                        listener.onResponse(actualResponse);
                    }
                } else {
                    Log.w(TAG, "No response received for command");
                    if (listener != null) {
                        listener.onError("No response from ECU");
                    }
                }
                
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Failed to send command", e);
                if (listener != null) {
                    listener.onError("Command failed: " + e.getMessage());
                }
            }
        });
    }
    
    public interface CommandResponseListener {
        void onResponse(byte[] response);
        void onError(String error);
    }
    
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
    
    public void disconnect() {
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing serial port", e);
            }
            serialPort = null;
        }
        
        if (connection != null) {
            connection.close();
            connection = null;
        }
        
        if (connectionListener != null) {
            connectionListener.onConnectionLost();
        }
        
        Log.d(TAG, "KKL cable disconnected");
    }
    
    public void cleanup() {
        disconnect();
        context.unregisterReceiver(usbReceiver);
        executor.shutdown();
    }
    
    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(String.format("%02X ", bytes[i]));
        }
        return result.toString().trim();
    }
}
