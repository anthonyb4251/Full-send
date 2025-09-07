package com.fullsend.jarvis.guide;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsend.jarvis.JarvisService;
import com.fullsend.jarvis.R;

import java.util.List;

public class VideoGuideActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_query";

    private TextView tvVehicle;
    private EditText etQuery;
    private Button btnGenerate;
    private ProgressBar progressBar;
    private VideoView videoView;
    private LinearLayout stepsContainer;
    private Button btnPrev;
    private Button btnNext;
    private EditText etBaseUrl;
    private Button btnSaveBaseUrl;

    private List<VideoGuideClient.Step> currentSteps;
    private int currentStepIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_guide);

        tvVehicle = findViewById(R.id.tvVehicle);
        etQuery = findViewById(R.id.etQuery);
        btnGenerate = findViewById(R.id.btnGenerate);
        progressBar = findViewById(R.id.progressBar);
        videoView = findViewById(R.id.videoView);
        stepsContainer = findViewById(R.id.stepsContainer);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        etBaseUrl = findViewById(R.id.etBaseUrl);
        btnSaveBaseUrl = findViewById(R.id.btnSaveBaseUrl);

        VehicleProfileManager.VehicleProfile profile = VehicleProfileManager.getVehicleProfile(this);
        tvVehicle.setText(profile.toDisplayString());

        String initialQuery = getIntent().getStringExtra(EXTRA_QUERY);
        if (!TextUtils.isEmpty(initialQuery)) {
            etQuery.setText(initialQuery);
        }

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        btnGenerate.setOnClickListener(v -> generate());

        btnSaveBaseUrl.setOnClickListener(v -> {
            String baseUrl = etBaseUrl.getText().toString().trim();
            VideoGuideClient.saveBaseUrl(this, baseUrl);
            Toast.makeText(this, TextUtils.isEmpty(baseUrl) ? "Using built-in generator" : "API base URL saved", Toast.LENGTH_SHORT).show();
        });

        String savedBaseUrl = VideoGuideClient.getBaseUrl(this);
        if (!TextUtils.isEmpty(savedBaseUrl)) {
            etBaseUrl.setText(savedBaseUrl);
        }

        btnPrev.setOnClickListener(v -> seekToStep(currentStepIndex - 1));
        btnNext.setOnClickListener(v -> seekToStep(currentStepIndex + 1));
    }

    private void generate() {
        String query = etQuery.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Enter what you want to learn (e.g., replace alternator)", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        VehicleProfileManager.VehicleProfile profile = VehicleProfileManager.getVehicleProfile(this);
        VideoGuideClient.generateGuide(this, query, profile, new VideoGuideClient.GuideCallback() {
            @Override
            public void onSuccess(VideoGuideClient.VideoGuideResponse response) {
                setLoading(false);
                if (!TextUtils.isEmpty(response.videoUrl)) {
                    videoView.setVideoURI(Uri.parse(response.videoUrl));
                    videoView.requestFocus();
                    videoView.start();
                }
                populateSteps(response.steps);
                Toast.makeText(VideoGuideActivity.this, TextUtils.isEmpty(response.notes) ? "Guide ready" : response.notes, Toast.LENGTH_LONG).show();
                logEvent("Video guide generated: " + query);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(VideoGuideActivity.this, "Failed to generate guide: " + error, Toast.LENGTH_LONG).show();
                logEvent("Video guide error: " + error);
            }
        });
    }

    private void populateSteps(@Nullable List<VideoGuideClient.Step> steps) {
        stepsContainer.removeAllViews();
        currentSteps = steps;
        currentStepIndex = -1;

        if (steps == null || steps.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No steps returned.");
            tv.setTextColor(getResources().getColor(R.color.white, null));
            tv.setPadding(16, 8, 16, 8);
            stepsContainer.addView(tv);
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }

        for (int i = 0; i < steps.size(); i++) {
            VideoGuideClient.Step s = steps.get(i);
            TextView stepView = new TextView(this);
            stepView.setText((i + 1) + ". " + (TextUtils.isEmpty(s.title) ? "Step" : s.title) + "\n" +
                    (TextUtils.isEmpty(s.description) ? "" : s.description));
            stepView.setTextColor(getResources().getColor(R.color.cyan, null));
            stepView.setPadding(16, 12, 16, 12);
            stepsContainer.addView(stepView);
        }

        seekToStep(0);
    }

    private void seekToStep(int index) {
        if (currentSteps == null || currentSteps.isEmpty()) return;
        if (index < 0 || index >= currentSteps.size()) return;
        currentStepIndex = index;

        VideoGuideClient.Step step = currentSteps.get(index);
        if (step != null) {
            int ms = Math.max(0, step.startSeconds) * 1000;
            try {
                videoView.seekTo(ms);
            } catch (Exception ignored) {
            }
        }

        btnPrev.setEnabled(currentStepIndex > 0);
        btnNext.setEnabled(currentStepIndex < currentSteps.size() - 1);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
    }

    private void logEvent(String event) {
        android.content.Intent intent = new android.content.Intent(this, JarvisService.class);
        intent.setAction(JarvisService.ACTION_LOG_EVENT);
        intent.putExtra(JarvisService.EXTRA_LOG_MESSAGE, "GUIDE: " + event);
        startService(intent);
    }
}
