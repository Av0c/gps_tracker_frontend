package com.example.whereareyou.map;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.whereareyou.R;
import com.google.android.material.textfield.TextInputEditText;

public class MapControlFragment extends DialogFragment {
  private static final String TAG = "MyDebug";

  // Fragments communication
  private static final int DATE_PICKER_REQUEST = 123000;
  private static final int START_TIME_PICKER_REQUEST = 123001;
  private static final int END_TIME_PICKER_REQUEST = 123002;

  private static final int DATE_PICKER_RESULT_CODE = 1;
  private static final int TIME_PICKER_RESULT_CODE = 2;

  // Inputs
  private Button buttonDatePicker;
  private Button buttonStartTimePicker;
  private Button buttonEndTimePicker;
  private TextInputEditText usernameTextInput;

  // Variables
  private AlertDialog mapControlDialog;
  private String username;
  private int year, month, day, startHour, startMinute, endHour, endMinute;

  MapControlFragment(
      String username,
      int year, int month, int day, int startHour, int startMinute, int endHour, int endMinute
  ) {
    this.username = username;
    this.year = year;
    this.month = month;
    this.day = day;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endHour = endHour;
    this.endMinute = endMinute;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    // Inflate the control view
    LayoutInflater inflater = requireActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.map_control, null);

    builder.setMessage("Show a user's locations")
        .setView(view)
        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // (Re)fetch data
            Intent intent = new Intent(getActivity(), MapFragment.class);
            String username = usernameTextInput.getText().toString();
            intent.putExtra("username", username);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            intent.putExtra("startHour", startHour);
            intent.putExtra("startMinute", startMinute);
            intent.putExtra("endHour", endHour);
            intent.putExtra("endMinute", endMinute);
            getTargetFragment().onActivityResult(getTargetRequestCode(), 1, intent);
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog
          }
        });
    mapControlDialog = builder.create();

    // Username input + validation
    usernameTextInput = view.findViewById(R.id.usernameInput);
    usernameTextInput.setText(username);
    usernameTextInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // Validate username
        String username = usernameTextInput.getText().toString();
        if (username.equals("")) {
          // Empty username
          usernameTextInput.setError("Username can not be blank");
          mapControlDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        } else {
          mapControlDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
      }
      @Override
      public void afterTextChanged(Editable editable) {
      }
    });

    // Set buttons' controller
    buttonDatePicker = view.findViewById(R.id.buttonDatePicker);
    buttonStartTimePicker = view.findViewById(R.id.buttonStartTimePicker);
    buttonEndTimePicker = view.findViewById(R.id.buttonEndTimePicker);
    final FragmentManager fm = getParentFragmentManager();
    buttonDatePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize DatePicker
        DialogFragment newFragment = new DatePickerFragment(year, month, day);
        newFragment.setTargetFragment(MapControlFragment.this, DATE_PICKER_REQUEST);
        newFragment.show(fm, "datePicker");
      }
    });
    buttonStartTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize TimePicker for Start
        DialogFragment newFragment = new TimePickerFragment(startHour, startMinute);
        newFragment.setTargetFragment(MapControlFragment.this, START_TIME_PICKER_REQUEST);
        newFragment.show(fm, "startTimePicker");
      }
    });
    buttonEndTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize TimePicker for End
        DialogFragment newFragment = new TimePickerFragment(endHour, endMinute);
        newFragment.setTargetFragment(MapControlFragment.this, END_TIME_PICKER_REQUEST);
        newFragment.show(fm, "endTimePicker");
      }
    });

    // Create the AlertDialog object and return it
    return mapControlDialog;
  }

  @Override
  public void onStart() {
    super.onStart();
    // Complain by default when username is blank
    if (usernameTextInput.getText().toString().equals("")) {
      mapControlDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
      usernameTextInput.setError("Please enter a username to track");
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Bundle bundle = data.getExtras();
    switch (requestCode) {
      case DATE_PICKER_REQUEST:
        // Set date
        year = bundle.getInt("year");
        month = bundle.getInt("month");
        day = bundle.getInt("day");
        break;

      case START_TIME_PICKER_REQUEST:
        // Set start time
        startHour = bundle.getInt("hour");
        startMinute = bundle.getInt("minute");
        if (startHour+startMinute/60 > endHour+endMinute/60) {
          startHour = endHour;
          startMinute = endMinute;
        }
        break;

      case END_TIME_PICKER_REQUEST:
        // Set end time
        endHour = bundle.getInt("hour");
        endMinute = bundle.getInt("minute");
        if (endHour+endMinute/60 < startHour+startMinute/60) {
          endHour = startHour;
          endMinute = startMinute;
        }
        break;
    }
  }

  // Date/Time picker fragment classes
  public static class TimePickerFragment extends DialogFragment
      implements TimePickerDialog.OnTimeSetListener {
    private int hour;
    private int minute;

    TimePickerFragment(int hour, int minute) {
      this.hour = hour;
      this.minute = minute;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Create a new instance of TimePickerDialog and return it
      return new TimePickerDialog(getActivity(), this, hour, minute,
          DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
      // Do something with the time chosen by the user
      Intent intent = new Intent(getActivity(), MapFragment.class);
      intent.putExtra("hour", hour);
      intent.putExtra("minute", minute);
      getTargetFragment().onActivityResult(getTargetRequestCode(), TIME_PICKER_RESULT_CODE, intent);
    }
  }

  public static class DatePickerFragment extends DialogFragment
      implements DatePickerDialog.OnDateSetListener {
    private int year;
    private int month;
    private int day;

    DatePickerFragment(int year, int month, int day) {
      this.year = year;
      this.month = month;
      this.day = day;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Create a new instance of DatePickerDialog and return it
      return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
      // Do something with the date chosen by the user
      Intent intent = new Intent(getActivity(), MapFragment.class);
      intent.putExtra("year", year);
      intent.putExtra("month", month);
      intent.putExtra("day", day);
      getTargetFragment().onActivityResult(getTargetRequestCode(), DATE_PICKER_RESULT_CODE, intent);
    }
  }
}
