package com.fullsend.jarvis.guide;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class VideoGuideClient {
    private static final String TAG = "VideoGuideClient";
    private static final String PREFS_NAME = "video_guide_prefs";
    private static final String KEY_BASE_URL = "base_url";

    public interface GuideService {
        @POST("/guide/generate")
        Call<VideoGuideResponse> generateGuide(@Body VideoGuideRequest request);
    }

    public interface GuideCallback {
        void onSuccess(VideoGuideResponse response);
        void onError(String error);
    }

    public static class VideoGuideRequest {
        @SerializedName("query") public String query;
        @SerializedName("make") public String make;
        @SerializedName("model") public String model;
        @SerializedName("year") public String year;
        @SerializedName("vin") public String vin;
        @SerializedName("trim") public String trim;

        public VideoGuideRequest(String query, VehicleProfileManager.VehicleProfile profile) {
            this.query = query;
            if (profile != null) {
                this.make = profile.make;
                this.model = profile.model;
                this.year = profile.year;
                this.vin = profile.vin;
                this.trim = profile.trim;
            }
        }
    }

    public static class Step {
        @SerializedName("title") public String title;
        @SerializedName("description") public String description;
        @SerializedName("startSeconds") public int startSeconds;
        @SerializedName("endSeconds") public int endSeconds;
    }

    public static class VideoGuideResponse {
        @SerializedName("videoUrl") public String videoUrl;
        @SerializedName("thumbnailUrl") public String thumbnailUrl;
        @SerializedName("steps") public List<Step> steps;
        @SerializedName("notes") public String notes;
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient http = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(http)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static void generateGuide(Context context, String query, VehicleProfileManager.VehicleProfile profile, GuideCallback cb) {
        String baseUrl = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BASE_URL, "");

        if (!TextUtils.isEmpty(baseUrl)) {
            try {
                GuideService service = buildRetrofit(baseUrl).create(GuideService.class);
                VideoGuideRequest req = new VideoGuideRequest(query, profile);
                service.generateGuide(req).enqueue(new Callback<VideoGuideResponse>() {
                    @Override
                    public void onResponse(Call<VideoGuideResponse> call, Response<VideoGuideResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cb.onSuccess(response.body());
                        } else {
                            Log.w(TAG, "API returned error, falling back to local stub");
                            fallbackGenerate(query, profile, cb);
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoGuideResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        fallbackGenerate(query, profile, cb);
                    }
                });
                return;
            } catch (Exception e) {
                Log.e(TAG, "Retrofit error: " + e.getMessage());
            }
        }

        // No base URL configured - use fallback
        fallbackGenerate(query, profile, cb);
    }

    private static void fallbackGenerate(String query, VehicleProfileManager.VehicleProfile profile, GuideCallback cb) {
        // Simulate a generation delay and return a sample guide
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            VideoGuideResponse resp = new VideoGuideResponse();
            // Sample small MP4
            resp.videoUrl = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4";
            resp.thumbnailUrl = "";
            resp.steps = new ArrayList<>();

            Step s1 = new Step();
            s1.title = "Preparation";
            s1.description = "Park on level ground, engage parking brake, and open the hood. Gather required tools.";
            s1.startSeconds = 0; s1.endSeconds = 2;
            resp.steps.add(s1);

            Step s2 = new Step();
            s2.title = "Locate Component";
            s2.description = "Identify the target component in your engine bay using the highlighted area in the video.";
            s2.startSeconds = 2; s2.endSeconds = 4;
            resp.steps.add(s2);

            Step s3 = new Step();
            s3.title = "Removal";
            s3.description = "Remove connectors and fasteners as shown. Keep bolts organized.";
            s3.startSeconds = 4; s3.endSeconds = 5;
            resp.steps.add(s3);

            Step s4 = new Step();
            s4.title = "Installation";
            s4.description = "Install the new part, torque fasteners to spec, and reconnect all connectors.";
            s4.startSeconds = 5; s4.endSeconds = 5;
            resp.steps.add(s4);

            resp.notes = (TextUtils.isEmpty(query) ? "Procedure" : ("Guide for: " + query))
                    + (profile != null && !TextUtils.isEmpty(profile.toDisplayString()) ?
                    "\nVehicle: " + profile.toDisplayString() : "");

            cb.onSuccess(resp);
        }, 1200);
    }

    public static void saveBaseUrl(Context context, String baseUrl) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    public static String getBaseUrl(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BASE_URL, "");
    }
}
