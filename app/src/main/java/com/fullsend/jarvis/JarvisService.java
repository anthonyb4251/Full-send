package com.fullsend.jarvis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JarvisService extends Service {

    private static final String TAG = "JarvisService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "JarvisServiceChannel";
    
    public static final String ACTION_LOG_EVENT = "com.fullsend.jarvis.ACTION_LOG_EVENT";
    public static final String EXTRA_LOG_MESSAGE = "com.fullsend.jarvis.EXTRA_LOG_MESSAGE";

    private static final String LOG_DIRECTORY = "JarvisAI";
    private static final String LOG_FILENAME = "jarvis_logs.txt";
    private File logFile;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLogFile();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        logEvent("JarvisService started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_LOG_EVENT.equals(intent.getAction())) {
            String message = intent.getStringExtra(EXTRA_LOG_MESSAGE);
            if (message != null) {
                logEvent(message);
            }
        }
        
        // We want this service to continue running until it is explicitly stopped
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logEvent("JarvisService destroyed - AI system shutdown");
    }

    private void initializeLogFile() {
        File directory = new File(Environment.getExternalStorageDirectory(), LOG_DIRECTORY);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create log directory");
                return;
            }
        }
        
        logFile = new File(directory, LOG_FILENAME);
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    Log.e(TAG, "Failed to create log file");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating log file: " + e.getMessage());
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Jarvis AI Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    public void logEvent(String event) {
        if (logFile == null || !logFile.exists()) {
            initializeLogFile();
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] %s\n", timestamp, event);
        
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.append(logEntry);
            Log.d(TAG, "Logged: " + logEntry);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file: " + e.getMessage());
        }
    }
}
