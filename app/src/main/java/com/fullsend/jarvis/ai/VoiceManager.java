package com.fullsend.jarvis.ai;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VoiceManager implements RecognitionListener, TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceManager";
    private static final String WAKE_PHRASE = "hey jarvis";
    private static final String WAKE_PHRASE_ALT = "jarvis";
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private VoiceListener voiceListener;
    private boolean isListening = false;
    private boolean isSpeaking = false;
    private boolean isTTSReady = false;
    private boolean isWakeWordMode = true;
    
    public interface VoiceListener {
        void onWakeWordDetected();
        void onVoiceCommand(String command);
        void onVoiceError(String error);
        void onSpeechStarted();
        void onSpeechCompleted();
        void onListeningStarted();
        void onListeningStopped();
    }
    
    public VoiceManager(Context context) {
        this.context = context;
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }
    
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
            Log.d(TAG, "Speech recognizer initialized");
        } else {
            Log.e(TAG, "Speech recognition not available");
        }
    }
    
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, this);
    }
    
    public void setVoiceListener(VoiceListener listener) {
        this.voiceListener = listener;
    }
    
    public void startListening() {
        if (speechRecognizer != null && !isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for commands...");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            if (isWakeWordMode) {
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
            } else {
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            }
            
            speechRecognizer.startListening(intent);
            isListening = true;
            
            if (voiceListener != null) {
                voiceListener.onListeningStarted();
            }
            
            Log.d(TAG, "Started listening - Wake word mode: " + isWakeWordMode);
        }
    }
    
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            
            if (voiceListener != null) {
                voiceListener.onListeningStopped();
            }
            
            Log.d(TAG, "Stopped listening");
        }
    }
    
    public void speak(String text) {
        speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    
    public void speak(String text, int queueMode, String utteranceId) {
        if (textToSpeech != null && isTTSReady) {
            Map<String, String> params = new HashMap<>();
            if (utteranceId != null) {
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            }
            
            // Set voice characteristics for Jarvis-like speech
            textToSpeech.setSpeechRate(0.9f);
            textToSpeech.setPitch(0.8f);
            
            isSpeaking = true;
            textToSpeech.speak(text, queueMode, params);
            
            if (voiceListener != null) {
                voiceListener.onSpeechStarted();
            }
            
            Log.d(TAG, "Speaking: " + text);
        }
    }
    
    public void speakWithCallback(String text, final Runnable callback) {
        String utteranceId = "callback_" + System.currentTimeMillis();
        
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speech started
            }
            
            @Override
            public void onDone(String utteranceId) {
                isSpeaking = false;
                if (voiceListener != null) {
                    voiceListener.onSpeechCompleted();
                }
                if (callback != null) {
                    callback.run();
                }
            }
            
            @Override
            public void onError(String utteranceId) {
                isSpeaking = false;
                if (voiceListener != null) {
                    voiceListener.onSpeechCompleted();
                }
            }
        });
        
        speak(text, TextToSpeech.QUEUE_FLUSH, utteranceId);
    }
    
    public boolean isSpeaking() {
        return isSpeaking && textToSpeech != null && textToSpeech.isSpeaking();
    }
    
    public boolean isListening() {
        return isListening;
    }
    
    public void setWakeWordMode(boolean enabled) {
        this.isWakeWordMode = enabled;
        Log.d(TAG, "Wake word mode: " + enabled);
    }
    
    // RecognitionListener implementation
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "Ready for speech");
    }
    
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech");
    }
    
    @Override
    public void onRmsChanged(float rmsdB) {
        // RMS changed - can be used for voice level visualization
    }
    
    @Override
    public void onBufferReceived(byte[] buffer) {
        // Audio buffer received
    }
    
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "End of speech");
        isListening = false;
    }
    
    @Override
    public void onError(int error) {
        String errorMessage = getErrorMessage(error);
        Log.e(TAG, "Speech recognition error: " + errorMessage);
        
        isListening = false;
        
        if (voiceListener != null) {
            voiceListener.onVoiceError(errorMessage);
        }
        
        // Restart listening in wake word mode after error
        if (isWakeWordMode && error != SpeechRecognizer.ERROR_CLIENT) {
            new android.os.Handler().postDelayed(this::startListening, 1000);
        }
    }
    
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        
        if (matches != null && !matches.isEmpty()) {
            String bestMatch = matches.get(0).toLowerCase();
            Log.d(TAG, "Speech result: " + bestMatch);
            
            if (isWakeWordMode) {
                if (bestMatch.contains(WAKE_PHRASE) || bestMatch.contains(WAKE_PHRASE_ALT)) {
                    Log.d(TAG, "Wake word detected: " + bestMatch);
                    isWakeWordMode = false;
                    
                    if (voiceListener != null) {
                        voiceListener.onWakeWordDetected();
                    }
                    
                    // Start listening for actual command
                    new android.os.Handler().postDelayed(this::startListening, 500);
                } else {
                    // Continue listening for wake word
                    new android.os.Handler().postDelayed(this::startListening, 100);
                }
            } else {
                // Process voice command
                if (voiceListener != null) {
                    voiceListener.onVoiceCommand(bestMatch);
                }
                
                // Return to wake word mode
                isWakeWordMode = true;
                new android.os.Handler().postDelayed(this::startListening, 1000);
            }
        }
        
        isListening = false;
    }
    
    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String partialText = matches.get(0).toLowerCase();
            
            if (isWakeWordMode && (partialText.contains(WAKE_PHRASE) || partialText.contains(WAKE_PHRASE_ALT))) {
                Log.d(TAG, "Wake word detected in partial results: " + partialText);
            }
        }
    }
    
    @Override
    public void onEvent(int eventType, Bundle params) {
        // Recognition event
    }
    
    // TextToSpeech.OnInitListener implementation
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int langResult = textToSpeech.setLanguage(Locale.US);
            
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported for TTS");
            } else {
                isTTSReady = true;
                Log.d(TAG, "Text-to-speech initialized successfully");
            }
        } else {
            Log.e(TAG, "Text-to-speech initialization failed");
        }
    }
    
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
    
    public void cleanup() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        isListening = false;
        isSpeaking = false;
        isTTSReady = false;
        
        Log.d(TAG, "Voice manager cleaned up");
    }
}
