package com.example.whereareyou;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.androidnetworking.interfaces.JSONObjectRequestListener;

import java.text.DateFormat;
import java.util.Base64;
import java.util.Date;

// Helper class used to save/restore application state
public class Utils {
  static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
  static final String KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval";
  static final String KEY_AUTHENTICATION = "authentication";

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

  public static void setAuthentication(Context context, String username, String password) {
    String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    Log.d("MyDebug", "setAuthentication: " + encoded);
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(KEY_AUTHENTICATION, encoded)
        .apply();
  }

  public static String getAuthentication(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(KEY_AUTHENTICATION, "");
  }
}
