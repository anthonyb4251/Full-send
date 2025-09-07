package com.fullsend.jarvis.guide;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class VehicleProfileManager {
    private static final String PREFS_NAME = "vehicle_profile_prefs";
    private static final String KEY_MAKE = "make";
    private static final String KEY_MODEL = "model";
    private static final String KEY_YEAR = "year";
    private static final String KEY_VIN = "vin";
    private static final String KEY_TRIM = "trim";

    public static class VehicleProfile {
        public final String make;
        public final String model;
        public final String year;
        public final String vin;
        public final String trim;

        public VehicleProfile(String make, String model, String year, String vin, String trim) {
            this.make = make;
            this.model = model;
            this.year = year;
            this.vin = vin;
            this.trim = trim;
        }

        public String toDisplayString() {
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(year)) sb.append(year).append(" ");
            if (!TextUtils.isEmpty(make)) sb.append(make).append(" ");
            if (!TextUtils.isEmpty(model)) sb.append(model);
            String base = sb.toString().trim();
            if (!TextUtils.isEmpty(trim)) {
                base = base + " (" + trim + ")";
            }
            if (!TextUtils.isEmpty(vin)) {
                base = base + "\nVIN: " + vin;
            }
            return base.isEmpty() ? "Unknown Vehicle" : base;
        }
    }

    public static void saveVehicleProfile(Context context, VehicleProfile profile) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_MAKE, profile.make)
                .putString(KEY_MODEL, profile.model)
                .putString(KEY_YEAR, profile.year)
                .putString(KEY_VIN, profile.vin)
                .putString(KEY_TRIM, profile.trim)
                .apply();
    }

    public static VehicleProfile getVehicleProfile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String make = prefs.getString(KEY_MAKE, "");
        String model = prefs.getString(KEY_MODEL, "");
        String year = prefs.getString(KEY_YEAR, "");
        String vin = prefs.getString(KEY_VIN, "");
        String trim = prefs.getString(KEY_TRIM, "");
        return new VehicleProfile(make, model, year, vin, trim);
    }

    public static void saveVIN(Context context, String vin) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_VIN, vin).apply();
    }
}
