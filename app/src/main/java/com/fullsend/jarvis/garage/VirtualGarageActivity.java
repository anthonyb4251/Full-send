package com.fullsend.jarvis.garage;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fullsend.jarvis.R;
import com.fullsend.jarvis.obd.OBDActivity;

import java.util.ArrayList;
import java.util.List;

public class VirtualGarageActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "action";

    private View carArea;
    private View toolboxArea;
    private View agentView;
    private View doorOverlay;
    private View hoodOverlay;
    private LinearLayout stepsContainer;
    private LinearLayout toolsContainer;
    private TextView statusText;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_garage);

        carArea = findViewById(R.id.garage_car_area);
        toolboxArea = findViewById(R.id.garage_toolbox_area);
        agentView = findViewById(R.id.garage_agent);
        doorOverlay = findViewById(R.id.garage_door_overlay);
        hoodOverlay = findViewById(R.id.garage_hood_overlay);
        stepsContainer = findViewById(R.id.garage_steps_container);
        toolsContainer = findViewById(R.id.garage_tools_container);
        statusText = findViewById(R.id.garage_status_text);

        Button btnOpenDoor = findViewById(R.id.btnGarageOpenDoor);
        Button btnOpenHood = findViewById(R.id.btnGarageOpenHood);
        Button btnShowEngine = findViewById(R.id.btnGarageShowEngine);
        Button btnShowInterior = findViewById(R.id.btnGarageShowInterior);
        Button btnShowExterior = findViewById(R.id.btnGarageShowExterior);
        Button btnUseScanner = findViewById(R.id.btnGarageUseScanner);

        btnOpenDoor.setOnClickListener(v -> performAction("open_door"));
        btnOpenHood.setOnClickListener(v -> performAction("open_hood"));
        btnShowEngine.setOnClickListener(v -> performAction("show_engine"));
        btnShowInterior.setOnClickListener(v -> performAction("show_interior"));
        btnShowExterior.setOnClickListener(v -> performAction("show_exterior"));
        btnUseScanner.setOnClickListener(v -> performAction("use_scanner"));

        // If started with an action, perform it after the initial layout pass so coordinates are available
        String initialAction = getIntent().getStringExtra(EXTRA_ACTION);
        if (initialAction != null) {
            carArea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    carArea.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    performAction(initialAction);
                }
            });
        }
    }

    private void performAction(String action) {
        resetVisuals();

        switch (action) {
            case "open_door":
                statusText.setText("Opening driver's door...");
                setTools(listOf("Trim tool", "Plastic pry tool", "Work gloves"));
                setSteps(listOf(
                        "Approach the toolbox",
                        "Pick up the trim tool",
                        "Walk to the driver's door",
                        "Insert tool at handle seam",
                        "Pull handle and open the door"
                ));
                animateAgentTo(toolboxArea, () -> animateAgentTo(carArea, () -> {
                    doorOverlay.setVisibility(View.VISIBLE);
                    pulse(doorOverlay);
                    statusText.setText("Door opened");
                }));
                break;

            case "open_hood":
                statusText.setText("Opening hood...");
                setTools(listOf("Hands", "Hood prop"));
                setSteps(listOf(
                        "Go to driver's footwell",
                        "Pull the hood release lever",
                        "Walk to front of the car",
                        "Release secondary safety latch",
                        "Lift hood and secure with prop"
                ));
                animateAgentTo(carArea, () -> {
                    hoodOverlay.setVisibility(View.VISIBLE);
                    pulse(hoodOverlay);
                    statusText.setText("Hood opened");
                });
                break;

            case "show_engine":
                statusText.setText("Showing engine components...");
                setTools(listOf("Flashlight"));
                setSteps(listOf(
                        "Open hood if not already open",
                        "Identify air intake, throttle body, MAF",
                        "Locate coils, spark plugs, and injectors",
                        "Check belts, pulleys, and coolant reservoir"
                ));
                animateAgentTo(carArea, () -> {
                    hoodOverlay.setVisibility(View.VISIBLE);
                    pulse(hoodOverlay);
                    statusText.setText("Engine view highlighted");
                });
                break;

            case "show_interior":
                statusText.setText("Showing interior...");
                setTools(listOf("Panel removal tool"));
                setSteps(listOf(
                        "Open driver's door",
                        "Point out dashboard, cluster, infotainment",
                        "Show OBD-II port under dash",
                        "Access cabin fuses if needed"
                ));
                animateAgentTo(carArea, () -> {
                    doorOverlay.setVisibility(View.VISIBLE);
                    pulse(doorOverlay);
                    statusText.setText("Interior view highlighted");
                });
                break;

            case "show_exterior":
                statusText.setText("Showing exterior panels...");
                setTools(listOf("Soft microfiber cloth"));
                setSteps(listOf(
                        "Walk around the vehicle",
                        "Identify panels: hood, fenders, doors, trunk",
                        "Inspect gaps and alignment"
                ));
                animateAgentTo(carArea, () -> pulse(carArea));
                break;

            case "use_scanner":
                statusText.setText("Using diagnostic scanner...");
                setTools(listOf("OBD-II scanner", "Laptop with software"));
                setSteps(listOf(
                        "Go to toolbox and pick scanner",
                        "Locate OBD-II port under dash",
                        "Plug in scanner and connect",
                        "Launch diagnostics"
                ));
                animateAgentTo(toolboxArea, () -> animateAgentTo(carArea, () -> {
                    statusText.setText("Launching OBD diagnostics...");
                    // Offer to open OBD activity
                    Button openObd = findViewById(R.id.btnOpenObdFromGarage);
                    openObd.setVisibility(View.VISIBLE);
                    openObd.setOnClickListener(v -> startActivity(new Intent(VirtualGarageActivity.this, OBDActivity.class)));
                    pulse(openObd);
                }));
                break;

            default:
                statusText.setText("Standing by in the virtual garage");
                setTools(new ArrayList<>());
                setSteps(new ArrayList<>());
                break;
        }
    }

    private void setSteps(List<String> steps) {
        stepsContainer.removeAllViews();
        int index = 1;
        for (String step : steps) {
            TextView tv = new TextView(this);
            tv.setTextColor(getColor(R.color.white));
            tv.setText(index + ". " + step);
            tv.setTextSize(14f);
            tv.setPadding(0, 8, 0, 8);
            stepsContainer.addView(tv);
            index++;
        }
    }

    private void setTools(List<String> tools) {
        toolsContainer.removeAllViews();
        if (tools.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setTextColor(getColor(R.color.gray));
            tv.setText("No tools required");
            toolsContainer.addView(tv);
            return;
        }
        for (String tool : tools) {
            TextView tv = new TextView(this);
            tv.setTextColor(getColor(R.color.cyan));
            tv.setText("• " + tool);
            tv.setTextSize(14f);
            tv.setPadding(0, 6, 0, 6);
            toolsContainer.addView(tv);
        }
    }

    private void resetVisuals() {
        doorOverlay.setVisibility(View.INVISIBLE);
        hoodOverlay.setVisibility(View.INVISIBLE);
        findViewById(R.id.btnOpenObdFromGarage).setVisibility(View.GONE);
    }

    private void pulse(View v) {
        Animation a = new AlphaAnimation(0.3f, 1f);
        a.setDuration(600);
        a.setRepeatMode(Animation.REVERSE);
        a.setRepeatCount(2);
        v.startAnimation(a);
    }

    private void animateAgentTo(View target, Runnable endAction) {
        // Compute center positions
        int[] agentLoc = new int[2];
        int[] targetLoc = new int[2];
        agentView.getLocationOnScreen(agentLoc);
        target.getLocationOnScreen(targetLoc);

        Rect targetRect = new Rect();
        target.getGlobalVisibleRect(targetRect);

        float fromX = 0f;
        float fromY = 0f;
        float toX = (targetRect.centerX() - (agentLoc[0] + agentView.getWidth() / 2f));
        float toY = (targetRect.centerY() - (agentLoc[1] + agentView.getHeight() / 2f));

        TranslateAnimation anim = new TranslateAnimation(0, toX, 0, toY);
        anim.setDuration(800);
        anim.setFillAfter(true);
        anim.setAnimationListener(new SimpleAnimationListener(() -> {
            agentView.clearAnimation();
            agentView.setTranslationX(agentView.getTranslationX() + toX);
            agentView.setTranslationY(agentView.getTranslationY() + toY);
            if (endAction != null) endAction.run();
        }));
        agentView.startAnimation(anim);
    }

    private List<String> listOf(String... items) {
        List<String> list = new ArrayList<>();
        for (String i : items) list.add(i);
        return list;
    }

    private static class SimpleAnimationListener implements Animation.AnimationListener {
        private final Runnable end;
        SimpleAnimationListener(Runnable end) { this.end = end; }
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationEnd(Animation animation) { if (end != null) end.run(); }
        @Override public void onAnimationRepeat(Animation animation) {}
    }
}
