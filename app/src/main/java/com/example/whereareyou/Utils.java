package com.example.whereareyou;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

// Helper class used to save/restore application state
public class Utils {
  static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
  static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";

  public static boolean requestingLocationUpdates(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
  }

  public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
        .apply();
  }

  public static int locationUpdateInterval(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getInt(KEY_LOCATION_UPDATE_INTERVAL, 60);
  }

  public static void setLocationUpdateInterval(Context context, int interval) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putInt(KEY_LOCATION_UPDATE_INTERVAL, interval)
        .apply();
  }
}
