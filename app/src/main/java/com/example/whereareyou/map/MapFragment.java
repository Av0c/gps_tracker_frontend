package com.example.whereareyou.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.whereareyou.R;
import com.example.whereareyou.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MapFragment extends Fragment
    implements OnMapReadyCallback {
  private static final String TAG = "MyDebug";

  private GoogleMap googleMap;

  // Constants
  public static final int MAP_CONTROL_REQUEST_CODE = 999000;

  // Components
  Button buttonMapControl;
  TextView mapInfoTextView;

  // Variables
  private JSONArray data = new JSONArray();
  private String username = "";
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;

  private String mapInfoText = "";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Init map controls
    startDate = LocalDate.now().minusDays(7);
    endDate = LocalDate.now();
    startTime = LocalTime.MIN;
    endTime = LocalTime.parse("23:59");

    // Init networking
    AndroidNetworking.initialize(getActivity().getApplicationContext());
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.map_main, container, false);

    SupportMapFragment mapFragment = (SupportMapFragment) requireActivity()
        .getSupportFragmentManager()
        .findFragmentById(R.id.map);

    if (mapFragment == null) {
      FragmentManager fragmentManager = getChildFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      mapFragment = SupportMapFragment.newInstance();
      fragmentTransaction.replace(R.id.map, mapFragment).commit();
    }

    mapFragment.getMapAsync(this);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    mapInfoTextView = view.findViewById(R.id.mapInfo);
    buttonMapControl = view.findViewById(R.id.buttonMapControl);
    buttonMapControl.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogFragment newFragment = new MapControlFragment(
            username,
            startDate, endDate, startTime, endTime
        );
        newFragment.setTargetFragment(MapFragment.this, MAP_CONTROL_REQUEST_CODE);
        newFragment.show(MapFragment.this.getParentFragmentManager(), "MapControlFragment");
      }
    });
  }

  @Override
  public void onMapReady(@NonNull GoogleMap readyMap) {
    googleMap = readyMap;

    // Add a marker in Sydney and move the camera
    LatLng sydney = new LatLng(-34, 151);
    googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Bundle bundle = data.getExtras();
    switch (requestCode) {
      case MAP_CONTROL_REQUEST_CODE:
        startDate = LocalDate.parse(bundle.getString("startDate"));
        endDate = LocalDate.parse(bundle.getString("endDate"));
        startTime = LocalTime.parse(bundle.getString("startTime"));
        endTime = LocalTime.parse(bundle.getString("endTime"));

        if (!username.equals(bundle.getString("username"))) {
          // Re-fetch data if username changed
          username = bundle.getString("username");
          fetchData(bundle.getString("username"));
        } else {
          // Else just update the map with filtered data
          username = bundle.getString("username");
          updateMap();
        }

        break;
    }
  }

  private void fetchData(String newUsername) {
    // API call to fetch data of username
    AndroidNetworking.get(Utils.getApiRoot(getContext()) + "point/{username}")
        .addPathParameter("username", newUsername)
        .addHeaders("Authorization", "Basic " + Utils.getAuthentication(getActivity()))
        .setTag("getPoint")
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsJSONArray(new JSONArrayRequestListener() {
          @Override
          public void onResponse(JSONArray response) {
            // Log.d(TAG, "Get locations success: " + response.toString());
            data = response;
            updateMap();
          }
          @Override
          public void onError(ANError error) {
            // handle error
            error.printStackTrace();
          }
        });
    updateMap();
  }

  @SuppressLint("SetTextI18n")
  private void updateMap() {

    // Clear all previous markers
    googleMap.clear();
    LatLng lastLoc = new LatLng(0, 0);
    boolean hasData = false;

    int currentDataCount = 0;

    // Initiate line options
    PolylineOptions lineOptions = new PolylineOptions();
    lineOptions.width(8);
    lineOptions.color(Color.rgb(2, 174, 174));

    for (int i = 0; i < data.length(); i++) {
      double longitude = 0;
      double latitude = 0;
      LocalDateTime timestamp = null;
      try {
        JSONObject dataPoint = data.getJSONObject(i);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        timestamp = LocalDateTime.parse(
            dataPoint.getString("created_at"),
            formatter
        );
        latitude = dataPoint.getDouble("latitude");
        longitude = dataPoint.getDouble("longitude");
      } catch (JSONException e) {
        e.printStackTrace();
      }

      if (timestamp == null) {
        break;
      }

      if (
          (
              timestamp.toLocalDate().isAfter(startDate)
              && timestamp.toLocalDate().isBefore(endDate)
          )
          && (
              timestamp.toLocalTime().isAfter(startTime)
              && timestamp.toLocalTime().isBefore(endTime)
          )
      ) {
        hasData = true;
        currentDataCount++;
        // Data is recorded within selected time range
        LatLng loc = new LatLng(latitude, longitude);
        lineOptions.add(loc);
        googleMap.addMarker(
            new MarkerOptions()
                .position(loc)
                .anchor(0.5f, 0.5f)
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_dot_12dp))
                .title("Point")
                .snippet("Recorded at: " + timestamp.toString())
        );
        lastLoc = loc;
      }
    }
    if (data.length() > 0) {
      if (hasData) {
        // Draw path between points
        googleMap.addPolyline(lineOptions);
        // Move camera to last point of records
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(lastLoc));
      }
    }

    // Update map's information text box
    // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    // SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    mapInfoText = "Username: " + username +
        "\nDate-range: " + startDate.toString() + " - " + endDate.toString() +
        "\nTime-range: " + startTime.toString() + " - " + endTime.toString() +
        "\nRecords (Shown/Total): " + currentDataCount + "/" + data.length() +
        (data.length() <= 0 ? "\n(Possibly user doesn't exist)" : "");
    mapInfoTextView.setText(mapInfoText);
  }

  private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
    // Get Bitmap for drawing marker from vector image
    Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
    vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.draw(canvas);
    return BitmapDescriptorFactory.fromBitmap(bitmap);
  }
}
