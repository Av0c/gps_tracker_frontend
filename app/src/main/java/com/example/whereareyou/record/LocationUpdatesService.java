package com.example.whereareyou.record;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.example.whereareyou.MainActivity;
import com.example.whereareyou.R;
import com.example.whereareyou.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Response;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that service is removed.
 */
public class LocationUpdatesService extends Service {

  private static final String PACKAGE_NAME = "com.example.whereareyou.locationupdatesservice";
  private static final String TAG = "MyDebug";
  private static final String CHANNEL_ID = "channel_01";
  static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
  static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
  private static final int NOTIFICATION_ID = 12345678;

  private final IBinder localBinder = new LocalBinder();

  // Used to check whether the bound activity has really gone away and not unbound as part of an
  // orientation change (no-op).
  private boolean isChangingConfig = false;

  private boolean isForeground = false;

  private NotificationManager notificationManager;

  // Locations-related
  private FusedLocationProviderClient fusedLocationClient;
  private LocationCallback locationCallback;
  private Location lastLocation;

  public LocationUpdatesService() {
  }

  @Override
  public void onCreate() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        onNewLocation(locationResult.getLastLocation());
      }
    };

    // getLastLocation();

    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    // Android O requires a Notification Channel.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.app_name);
      // Create the channel for the notification
      NotificationChannel mChannel =
          new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

      // Set the Notification Channel for the Notification Manager.
      notificationManager.createNotificationChannel(mChannel);
    }

    // Init networking
    AndroidNetworking.initialize(getApplicationContext().getApplicationContext());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "Service started");

    // Tells the system to not try to recreate the service after it has been killed.
    return START_NOT_STICKY;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    isChangingConfig = true;
  }

  @Override
  public IBinder onBind(Intent intent) {
    // Called when a client (MainActivity) comes to the foreground
    // and binds with this service.
    // The service should cease to be a foreground service when that happens.
    Log.d(TAG, "in onBind()");
    stopForeground(true);
    isForeground = false;
    isChangingConfig = false;

    return localBinder;
  }

  @Override
  public void onRebind(Intent intent) {
    // Called when a client (MainActivity) returns to the foreground
    // and binds once again with this service.
    // The service should cease to be a foreground service when that happens.
    Log.d(TAG, "in onRebind()");
    stopForeground(true);
    isForeground = false;
    isChangingConfig = false;

    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(TAG, "Client unbound from service");

    // Called when the client (MainActivity) unbinds from this service.
    // Make this service foreground if applicable
    if (!isChangingConfig && Utils.requestingLocationUpdates(this)) {
      Log.d(TAG, "Starting foreground service");

      startForeground(NOTIFICATION_ID, getNotification());
      isForeground = true;
    }

    return true; // Ensures onRebind() is called when a client re-binds.
  }

  public void startLocationUpdates() {
    Log.d(TAG, "Requesting location updates");
    Utils.setRequestingLocationUpdates(this, true);
    startService(new Intent(getApplicationContext(), LocationUpdatesService.class));

    try {
      int interval = Utils.locationUpdateInterval(this);
      LocationRequest locationRequest = new LocationRequest();
      locationRequest.setInterval(interval * 1000);
      locationRequest.setFastestInterval(interval * 1000);
      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      fusedLocationClient.requestLocationUpdates(
          locationRequest,
          locationCallback,
          Looper.myLooper()
      );
    } catch (SecurityException unlikely) {
      Utils.setRequestingLocationUpdates(this, false);
      Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
    }
  }

  public void stopLocationUpdates() {
    Log.d(TAG, "Stop location updates");

    try {
      fusedLocationClient.removeLocationUpdates(locationCallback);
      Utils.setRequestingLocationUpdates(this, false);
      stopSelf();
    } catch (SecurityException unlikely) {
      Utils.setRequestingLocationUpdates(this, true);
      Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
    }
  }

  private Notification getNotification() {
    // The PendingIntent to launch activity.
    PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, MainActivity.class), 0);
    int interval = Utils.locationUpdateInterval(this);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("WhereAreYou is tracking your location..")
        .setContentText("Submitting coordinates every " + interval + " seconds.")
        .setSmallIcon(R.drawable.ic_gps)
        .setContentIntent(activityPendingIntent)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true);

    // Set the Channel ID for Android O.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      builder.setChannelId(CHANNEL_ID);
    }

    return builder.build();
  }

  private void getLastLocation() {
    try {
      fusedLocationClient.getLastLocation()
          .addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
              if (task.isSuccessful() && task.getResult() != null) {
                lastLocation = task.getResult();
              } else {
                Log.d(TAG, "Failed to get location.");
              }
            }
          });
    } catch (SecurityException unlikely) {
      Log.d(TAG, "Lost location permission." + unlikely);
    }
  }

  private void onNewLocation(final Location location) {
    lastLocation = location;

    // Notify anyone listening for broadcasts about the new location.
    // Intent intent = new Intent(ACTION_BROADCAST);
    // intent.putExtra(EXTRA_LOCATION, location);
    // LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    // Log.d("MyDebug", "long: " + location.getLongitude());
    // Log.d("MyDebug", "lat: " + location.getLatitude());
    // Log.d("MyDebug", "timestamp: " + location.getTime());
    // Log.d("MyDebug", "now      : " + System.currentTimeMillis());
    // SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
    // Log.d("MyDebug", "time     : " + format.format(new Date()));
    // Log.d("MyDebug", "diff (seconds): " + (location.getTime() - System.currentTimeMillis())/1000);

    // JSONObject postBody = new JSONObject();
    // try {
    //   postBody.put("longitude", location.getLongitude());
    //   postBody.put("latitude", location.getLatitude());
    // } catch (JSONException e) {
    //   e.printStackTrace();
    // }

    AndroidNetworking.post(getString(R.string.api_root)+"point")
        .addHeaders("Authorization", "Basic " + Utils.getAuthentication(getApplicationContext()))
        .addBodyParameter("longitude", String.valueOf(location.getLongitude()))
        .addBodyParameter("latitude", String.valueOf(location.getLatitude()))
        .setTag("postPoint")
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsOkHttpResponse(new OkHttpResponseListener() {
          @Override
          public void onResponse(Response response) {
            Log.d(TAG, "Location submitted: " + location.toString());
          }
          @Override
          public void onError(ANError anError) {
            Log.d(TAG, "Failed to submit location: " + anError.getMessage());
          }
        });

    // Update notification content if running as a foreground service.
    if (isForeground) {
      notificationManager.notify(NOTIFICATION_ID, getNotification());
    }
  }

  /**
   * Class used for the client Binder.  Since this service runs in the same process as its
   * clients, we don't need to deal with IPC.
   */
  class LocalBinder extends Binder {
    LocationUpdatesService getService() {
      return LocationUpdatesService.this;
    }
  }
}
