package com.example.whereareyou.record;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.whereareyou.R;
import com.example.whereareyou.Utils;
import com.example.whereareyou.login.LoginActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class RecordFragment extends Fragment {
  private static final String TAG = "MyDebug";
  private static final int REQUEST_LOCATION_SETTINGS = 7090;
  private static final int REQUEST_LOCATION_PERMISSIONS = 62475;

  // Inputs
  private TextInputEditText intervalInput;
  private Button buttonStart;
  private Button buttonStop;
  private Button buttonLogout;

  private LocationUpdatesService locationUpdatesService = null;
  private boolean serviceIsBound = false;

  private final ServiceConnection locationUpdatesServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
      locationUpdatesService = binder.getService();
      serviceIsBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      locationUpdatesService = null;
      serviceIsBound = false;
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Run this check if locations are tracking and app is started again
    // Check that the user hasn't revoked permissions by going to Settings.
    if (Utils.requestingLocationUpdates(getActivity())) {
      if (!checkLocationPermission()) {
        requestLocationPermission();
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.record_main, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    intervalInput = view.findViewById(R.id.intervalInput);
    buttonStart = view.findViewById(R.id.buttonStart);
    buttonStop = view.findViewById(R.id.buttonStop);
    buttonLogout = view.findViewById(R.id.buttonLogout);

    // Bind actions to buttons
    buttonStart.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            startService();
          }
        }
    );
    buttonStop.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            stopService();
          }
        }
    );
    buttonLogout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
      }
    });

    // Set button/input states based on if locations are tracking
    setInputsState(Utils.requestingLocationUpdates(getActivity()));

    // Restored interval value when app is started
    int interval = Utils.locationUpdateInterval(getActivity());
    intervalInput.setText(String.valueOf(interval));
  }

  @Override
  public void onStart() {
    super.onStart();
    // Bind to the service. If the service is in foreground mode, this signals to the service
    // that since this activity is in the foreground, the service can exit foreground mode.
    if (getActivity() == null) {
      throw new NullPointerException("Main activity is not found. This is unlikely!");
    }
    getActivity().bindService(
        new Intent(getActivity(), LocationUpdatesService.class),
        locationUpdatesServiceConnection,
        Context.BIND_AUTO_CREATE
    );
  }

  @Override
  public void onStop() {
    if (serviceIsBound) {
      // Unbind from the service. This signals to the service that this activity is no longer
      // in the foreground, and the service can respond by promoting itself to a foreground
      // service.
      if (getActivity() == null) {
        throw new NullPointerException("Main activity is not found. This is unlikely!");
      }
      getActivity().unbindService(locationUpdatesServiceConnection);
      serviceIsBound = false;
    }
    super.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    // Location permissions are only needed to be checked at onStart

    // Log.d(TAG, "onResume");
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    // Handle permission request result
    switch (requestCode) {
      case REQUEST_LOCATION_PERMISSIONS:
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // Permissions granted
          // Restart location updates if app is already set to run but somehow stopped
          // (Location permission revoked,...)
          // Log.d(TAG, "onRequestPermissionsResult: ");
          startService();
        } else {
          // Permissions denied, boo!
        }
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Called when settings request is completed
    // Log.d(TAG, "onActivityResult: " + resultCode);
    // Log.d(TAG, "onActivityResult: " + Activity.RESULT_OK);
    switch (requestCode) {
      case REQUEST_LOCATION_SETTINGS:
        if (resultCode == Activity.RESULT_OK) {
          // Settings are successfully configured

          // Since settings are only requested when the user has or trying to start the tracker
          // So the service is always called again
          startService();
        } else {
          // User denied to change settings
          // Do nothing
        }
    }
  }

  private void startService() {
    if (intervalInput.getText() == null
        || intervalInput.getText().toString().equalsIgnoreCase("")) {
      // Check empty values
      intervalInput.setError("Interval is required.", null);
    } else {
      // Check valid values
      int interval = Integer.parseInt(intervalInput.getText().toString());
      if (interval <= 0) {
        intervalInput.setError("Interval must be a valid integer, larger than 0.", null);
      } else {
        // Everything is good to go
        final Activity activity = getActivity();
        if (activity == null) {
          throw new NullPointerException("Main activity is not found. This is unlikely!");
        }

        // Save interval to sharedPreferences
        Utils.setLocationUpdateInterval(getActivity(), interval);

        // Setup temporary LocationRequest object to check location settings
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);

        // Check if location settings are satisfied
        final SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
          @Override
          public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            // Location settings are satisfied
            // Check location permissions next
            if (checkLocationPermission()) {
              // Permissions are ok
              // Log.d(TAG, "onSuccess");
              locationUpdatesService.startLocationUpdates();
              setInputsState(true); // Toggle input states
            } else {
              // Request permissions
              // Log.d(TAG, "onFailure permissions");
              requestLocationPermission();
            }
          }
        });
        final Fragment self = this;
        task.addOnFailureListener(getActivity(), new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            // Log.d(TAG, "onFailure settings");
            if (e instanceof ResolvableApiException) {
              // Location settings are wrong but can be fixed (turned on)
              try {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                self.startIntentSenderForResult(
                    resolvable.getResolution().getIntentSender(),
                    REQUEST_LOCATION_SETTINGS,
                    null, 0, 0, 0, null
                );
              } catch (IntentSender.SendIntentException sendEx) {
                // Ignore the error
              }
            }
          }
        });
      }
    }
  }

  private void stopService() {
    locationUpdatesService.stopLocationUpdates();
    setInputsState(false);
  }

  private boolean checkLocationPermission() {
    // Check if the required location settings are satisfied
    // Request them from user if not
    return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
        Objects.requireNonNull(getActivity()),
        Manifest.permission.ACCESS_FINE_LOCATION
    );
  }

  private void requestLocationPermission() {
    requestPermissions(
        new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},
        REQUEST_LOCATION_PERMISSIONS
    );
  }

  private void setInputsState(boolean requestingLocationUpdates) {
    if (requestingLocationUpdates) {
      buttonStart.setEnabled(false);
      buttonStart.setBackgroundColor(Color.GRAY);

      buttonStop.setEnabled(true);
      buttonStop.setBackgroundColor(
          ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorStop));

      intervalInput.setEnabled(false);
    } else {
      buttonStart.setEnabled(true);
      buttonStart.setBackgroundColor(
          ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorStart));

      buttonStop.setEnabled(false);
      buttonStop.setBackgroundColor(Color.GRAY);

      intervalInput.setEnabled(true);
    }
  }
}
