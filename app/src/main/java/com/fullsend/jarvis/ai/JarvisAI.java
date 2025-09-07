package com.fullsend.jarvis.ai;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.fullsend.jarvis.JarvisService;
import com.fullsend.jarvis.obd.OBDActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarvisAI {
    private static final String TAG = "JarvisAI";
    
    private Context context;
    private AIResponseListener responseListener;
    private VoiceManager voiceManager;
    private Map<String, String> userPreferences;
    private Map<String, Object> contextData;
    
    // Command patterns for natural language processing
    private static final Map<String, List<String>> COMMAND_PATTERNS = new HashMap<>();
    
    static {
        // System commands
        COMMAND_PATTERNS.put("battery_status", Arrays.asList(
            "(?i).*battery.*status.*", "(?i).*power.*level.*", "(?i).*how.*much.*battery.*",
            "(?i).*battery.*percentage.*", "(?i).*charge.*level.*"
        ));
        
        COMMAND_PATTERNS.put("system_scan", Arrays.asList(
            "(?i).*system.*scan.*", "(?i).*run.*diagnostic.*", "(?i).*check.*system.*",
            "(?i).*analyze.*device.*", "(?i).*system.*health.*"
        ));
        
        COMMAND_PATTERNS.put("alert_mode", Arrays.asList(
            "(?i).*alert.*mode.*", "(?i).*activate.*alert.*", "(?i).*security.*mode.*",
            "(?i).*enable.*alerts.*", "(?i).*heightened.*security.*"
        ));
        
        COMMAND_PATTERNS.put("obd_diagnostics", Arrays.asList(
            "(?i).*car.*diagnostic.*", "(?i).*vehicle.*scan.*", "(?i).*obd.*diagnostic.*",
            "(?i).*check.*car.*", "(?i).*engine.*diagnostic.*", "(?i).*vehicle.*health.*"
        ));
        
        COMMAND_PATTERNS.put("time_query", Arrays.asList(
            "(?i).*what.*time.*", "(?i).*current.*time.*", "(?i).*tell.*time.*",
            "(?i).*time.*is.*it.*"
        ));
        
        COMMAND_PATTERNS.put("date_query", Arrays.asList(
            "(?i).*what.*date.*", "(?i).*today.*date.*", "(?i).*current.*date.*",
            "(?i).*what.*day.*"
        ));
        
        COMMAND_PATTERNS.put("weather_query", Arrays.asList(
            "(?i).*weather.*", "(?i).*temperature.*", "(?i).*forecast.*",
            "(?i).*how.*hot.*", "(?i).*rain.*today.*"
        ));
        
        COMMAND_PATTERNS.put("shutdown", Arrays.asList(
            "(?i).*shutdown.*", "(?i).*turn.*off.*", "(?i).*power.*down.*",
            "(?i).*emergency.*shutdown.*", "(?i).*stop.*jarvis.*"
        ));
        
        COMMAND_PATTERNS.put("greeting", Arrays.asList(
            "(?i).*hello.*", "(?i).*hi.*jarvis.*", "(?i).*good.*morning.*",
            "(?i).*good.*afternoon.*", "(?i).*good.*evening.*", "(?i).*how.*are.*you.*"
        ));
        
        COMMAND_PATTERNS.put("help", Arrays.asList(
            "(?i).*help.*", "(?i).*what.*can.*you.*do.*", "(?i).*commands.*",
            "(?i).*capabilities.*", "(?i).*functions.*"
        ));
        
        COMMAND_PATTERNS.put("status", Arrays.asList(
            "(?i).*status.*report.*", "(?i).*system.*status.*", "(?i).*how.*is.*everything.*",
            "(?i).*all.*systems.*", "(?i).*full.*status.*"
        ));

        // Interactive repair/video guidance
        COMMAND_PATTERNS.put("video_guidance", Arrays.asList(
            "(?i).*(how to|how do i|show me how to)\\s+.+",
            "(?i).*(fix|replace|repair|install|remove)\\s+.+",
            "(?i).*(video|guide|tutorial).*(engine|brake|battery|sensor|alternator|spark plug|oil).*"
        ));
    }
    
    // Response templates
    private static final Map<String, List<String>> RESPONSE_TEMPLATES = new HashMap<>();
    
    static {
        RESPONSE_TEMPLATES.put("greeting", Arrays.asList(
            "Good %s, sir. Jarvis at your service.",
            "Hello sir. All systems are operational.",
            "Greetings. How may I assist you today?",
            "Good %s. I'm ready to help.",
            "At your service, sir."
        ));
        
        RESPONSE_TEMPLATES.put("battery_acknowledgment", Arrays.asList(
            "Battery status retrieved, sir.",
            "Power levels analyzed.",
            "Current charge status confirmed.",
            "Battery diagnostics complete."
        ));
        
        RESPONSE_TEMPLATES.put("system_scan_started", Arrays.asList(
            "Initiating comprehensive system scan.",
            "Running full diagnostic sweep.",
            "Analyzing all systems, sir.",
            "Commencing system health check."
        ));
        
        RESPONSE_TEMPLATES.put("alert_mode_activated", Arrays.asList(
            "Alert mode activated. Enhanced monitoring engaged.",
            "Security protocols heightened, sir.",
            "All systems on high alert.",
            "Enhanced security mode is now active."
        ));
        
        RESPONSE_TEMPLATES.put("obd_opening", Arrays.asList(
            "Opening automotive diagnostic interface.",
            "Initiating vehicle diagnostic protocols.",
            "Accessing OBD systems, sir.",
            "Vehicle diagnostic suite ready."
        ));
        
        RESPONSE_TEMPLATES.put("help_response", Arrays.asList(
            "I can assist with system monitoring, automotive diagnostics, voice commands, repair video guides, and much more. Try asking: 'show me how to replace my alternator'.",
            "My capabilities include system analysis, OBD vehicle diagnostics, interactive repair guidance, security monitoring, and intelligent assistance. What would you like me to help with?",
            "I'm equipped with advanced diagnostic tools, voice interaction, automotive systems, and interactive step-by-step guides. How may I be of service?"
        ));

        RESPONSE_TEMPLATES.put("video_guidance_intro", Arrays.asList(
            "Generating an interactive repair guide.",
            "Preparing a step-by-step video tutorial for you.",
            "Creating a visual walkthrough now."
        ));
        
        RESPONSE_TEMPLATES.put("unknown_command", Arrays.asList(
            "I'm not sure I understand that command, sir. Could you rephrase?",
            "Command not recognized. Please try again.",
            "I didn't catch that, sir. Could you repeat the request?",
            "That's not within my current capabilities. Try asking for help to see what I can do."
        ));
        
        RESPONSE_TEMPLATES.put("error", Arrays.asList(
            "I'm experiencing a technical difficulty, sir.",
            "There seems to be an issue with that request.",
            "Unable to complete that operation at this time.",
            "Encountering system constraints with that command."
        ));
    }
    
    public interface AIResponseListener {
        void onTextResponse(String response);
        void onActionRequired(String action, Map<String, Object> parameters);
        void onError(String error);
    }
    
    public JarvisAI(Context context) {
        this.context = context;
        this.userPreferences = new HashMap<>();
        this.contextData = new HashMap<>();
        initializeContextData();
    }
    
    public void setResponseListener(AIResponseListener listener) {
        this.responseListener = listener;
    }
    
    public void setVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }
    
    private void initializeContextData() {
        contextData.put("device_model", Build.MODEL);
        contextData.put("device_manufacturer", Build.MANUFACTURER);
        contextData.put("android_version", Build.VERSION.RELEASE);
        contextData.put("last_interaction", System.currentTimeMillis());
    }
    
    public void processCommand(String command) {
        Log.d(TAG, "Processing command: " + command);
        
        // Update context
        contextData.put("last_interaction", System.currentTimeMillis());
        contextData.put("last_command", command);
        
        // Log the interaction
        logEvent("Voice command: " + command);
        
        // Analyze command and generate response
        AICommand recognizedCommand = analyzeCommand(command);
        
        if (recognizedCommand != null) {
            handleCommand(recognizedCommand);
        } else {
            handleUnknownCommand(command);
        }
    }
    
    private AICommand analyzeCommand(String input) {
        String normalizedInput = input.toLowerCase().trim();
        
        for (Map.Entry<String, List<String>> entry : COMMAND_PATTERNS.entrySet()) {
            String commandType = entry.getKey();
            List<String> patterns = entry.getValue();
            
            for (String pattern : patterns) {
                if (normalizedInput.matches(pattern.toLowerCase())) {
                    return new AICommand(commandType, normalizedInput, extractParameters(normalizedInput, pattern));
                }
            }
        }
        
        return null;
    }
    
    private Map<String, String> extractParameters(String input, String pattern) {
        Map<String, String> params = new HashMap<>();
        
        // Extract time-related parameters
        Pattern timePattern = Pattern.compile("\\b(\\d{1,2})\\s*(am|pm|hours?|minutes?)\\b", Pattern.CASE_INSENSITIVE);
        Matcher timeMatcher = timePattern.matcher(input);
        if (timeMatcher.find()) {
            params.put("time", timeMatcher.group());
        }
        
        // Extract location parameters
        Pattern locationPattern = Pattern.compile("\\b(in|at|near)\\s+([a-zA-Z\\s]{2,})\\b", Pattern.CASE_INSENSITIVE);
        Matcher locationMatcher = locationPattern.matcher(input);
        if (locationMatcher.find()) {
            params.put("location", locationMatcher.group(2).trim());
        }
        
        // Extract repair/query topic for video guidance
        String q = null;
        Matcher m1 = Pattern.compile("(?i).*?(?:how to|how do i|show me how to)\\s+(.+)").matcher(input);
        if (m1.matches()) {
            q = m1.group(1).trim();
        }
        if (q == null) {
            Matcher m2 = Pattern.compile("(?i).*(?:fix|replace|repair|install|remove)\\s+(.+)").matcher(input);
            if (m2.matches()) {
                q = m2.group(1).trim();
            }
        }
        if (q != null && !q.isEmpty()) {
            params.put("query", q);
        }
        
        return params;
    }
    
    private void handleCommand(AICommand command) {
        switch (command.getType()) {
            case "greeting":
                handleGreeting();
                break;
            case "battery_status":
                handleBatteryStatus();
                break;
            case "system_scan":
                handleSystemScan();
                break;
            case "alert_mode":
                handleAlertMode();
                break;
            case "obd_diagnostics":
                handleOBDDiagnostics();
                break;
            case "time_query":
                handleTimeQuery();
                break;
            case "date_query":
                handleDateQuery();
                break;
            case "weather_query":
                handleWeatherQuery();
                break;
            case "help":
                handleHelp();
                break;
            case "status":
                handleStatusReport();
                break;
            case "shutdown":
                handleShutdown();
                break;
            case "video_guidance":
                handleVideoGuidance(command);
                break;
            default:
                handleUnknownCommand(command.getInput());
                break;
        }
    }
    
    private void handleGreeting() {
        String timeOfDay = getTimeOfDay();
        String response = getRandomResponse("greeting");
        if (response.contains("%s")) {
            response = String.format(response, timeOfDay);
        }
        
        respond(response);
    }
    
    private void handleBatteryStatus() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        
        String statusText;
        if (batteryLevel >= 80) {
            statusText = "excellent";
        } else if (batteryLevel >= 60) {
            statusText = "good";
        } else if (batteryLevel >= 40) {
            statusText = "moderate";
        } else if (batteryLevel >= 20) {
            statusText = "low";
        } else {
            statusText = "critical";
        }
        
        String response = String.format("Battery level is at %d percent, sir. Status: %s.", batteryLevel, statusText);
        
        if (batteryLevel < 20) {
            response += " I recommend connecting to a power source.";
        }
        
        respond(response);
        
        // Trigger battery status action
        Map<String, Object> params = new HashMap<>();
        params.put("battery_level", batteryLevel);
        params.put("status", statusText);
        triggerAction("show_battery_status", params);
    }
    
    private void handleSystemScan() {
        respond(getRandomResponse("system_scan_started"));
        
        // Trigger system scan
        Map<String, Object> params = new HashMap<>();
        params.put("scan_type", "full");
        triggerAction("run_system_scan", params);
    }
    
    private void handleAlertMode() {
        respond(getRandomResponse("alert_mode_activated"));
        
        // Trigger alert mode
        Map<String, Object> params = new HashMap<>();
        params.put("mode", "alert");
        triggerAction("activate_alert_mode", params);
    }
    
    private void handleOBDDiagnostics() {
        respond(getRandomResponse("obd_opening"));
        
        // Trigger OBD diagnostics
        Map<String, Object> params = new HashMap<>();
        triggerAction("open_obd_diagnostics", params);
    }
    
    private void handleTimeQuery() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        
        respond(String.format("The current time is %s, sir.", currentTime));
    }
    
    private void handleDateQuery() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        
        respond(String.format("Today is %s, sir.", currentDate));
    }
    
    private void handleWeatherQuery() {
        // For now, provide a placeholder response
        respond("I'm working on accessing weather services, sir. This feature will be available soon.");
        
        // Future implementation would integrate with weather APIs
        Map<String, Object> params = new HashMap<>();
        triggerAction("fetch_weather", params);
    }
    
    private void handleHelp() {
        respond(getRandomResponse("help_response"));
    }
    
    private void handleStatusReport() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        
        String response = String.format(
            "All systems operational, sir. Current time: %s. Battery: %d percent. Device status: nominal. Ready for your commands.",
            currentTime, batteryLevel
        );
        
        respond(response);
    }
    
    private void handleShutdown() {
        respond("Initiating shutdown sequence. Goodbye, sir.");
        
        // Trigger shutdown
        Map<String, Object> params = new HashMap<>();
        triggerAction("emergency_shutdown", params);
    }
    
    private void handleVideoGuidance(AICommand command) {
        String q = null;
        if (command.getParameters() != null) {
            q = command.getParameters().get("query");
        }
        if (q == null || q.isEmpty()) {
            // Try to heuristically remove filler words
            String input = command.getInput();
            input = input.replaceAll("(?i)(show me|how to|how do i|please|jarvis|video|guide)", "").trim();
            q = input;
        }
        respond(getRandomResponse("video_guidance_intro") + (q != null && !q.isEmpty() ? (" Topic: " + q) : ""));
        Map<String, Object> params = new HashMap<>();
        if (q != null) params.put("query", q);
        triggerAction("open_video_guidance", params);
    }
    
    private void handleUnknownCommand(String command) {
        respond(getRandomResponse("unknown_command"));
        
        // Log unknown command for learning
        logEvent("Unknown command: " + command);
    }
    
    private String getTimeOfDay() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        
        if (hour < 12) {
            return "morning";
        } else if (hour < 17) {
            return "afternoon";
        } else {
            return "evening";
        }
    }
    
    private String getRandomResponse(String category) {
        List<String> responses = RESPONSE_TEMPLATES.get(category);
        if (responses != null && !responses.isEmpty()) {
            Random random = new Random();
            return responses.get(random.nextInt(responses.size()));
        }
        return "Response template not found.";
    }
    
    private void respond(String response) {
        Log.d(TAG, "AI Response: " + response);
        
        if (responseListener != null) {
            responseListener.onTextResponse(response);
        }
        
        // Speak the response
        if (voiceManager != null) {
            voiceManager.speak(response);
        }
        
        // Log the response
        logEvent("AI Response: " + response);
    }
    
    private void triggerAction(String action, Map<String, Object> parameters) {
        if (responseListener != null) {
            responseListener.onActionRequired(action, parameters);
        }
    }
    
    private void logEvent(String event) {
        Intent intent = new Intent(context, JarvisService.class);
        intent.setAction(JarvisService.ACTION_LOG_EVENT);
        intent.putExtra(JarvisService.EXTRA_LOG_MESSAGE, "AI: " + event);
        context.startService(intent);
    }
    
    public void updateContext(String key, Object value) {
        contextData.put(key, value);
    }
    
    public Object getContext(String key) {
        return contextData.get(key);
    }
    
    public void setUserPreference(String key, String value) {
        userPreferences.put(key, value);
    }
    
    public String getUserPreference(String key) {
        return userPreferences.get(key);
    }
    
    // Inner class for command structure
    private static class AICommand {
        private String type;
        private String input;
        private Map<String, String> parameters;
        
        public AICommand(String type, String input, Map<String, String> parameters) {
            this.type = type;
            this.input = input;
            this.parameters = parameters;
        }
        
        public String getType() { return type; }
        public String getInput() { return input; }
        public Map<String, String> getParameters() { return parameters; }
    }
}
