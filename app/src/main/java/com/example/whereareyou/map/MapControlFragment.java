package com.example.whereareyou.map;

import android.app.Activity;
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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class MapControlFragment extends DialogFragment {
  private static final String TAG = "MyDebug";

  // Fragments communication
  private static final int START_DATE_PICKER_REQUEST = 321001;
  private static final int END_DATE_PICKER_REQUEST = 321002;
  private static final int START_TIME_PICKER_REQUEST = 123001;
  private static final int END_TIME_PICKER_REQUEST = 123002;

  private static final int DATE_PICKER_RESULT_CODE = 1;
  private static final int TIME_PICKER_RESULT_CODE = 2;

  // Inputs
  private Button buttonStartDatePicker;
  private Button buttonEndDatePicker;
  private Button buttonStartTimePicker;
  private Button buttonEndTimePicker;
  private TextInputEditText usernameTextInput;

  // Variables
  private AlertDialog mapControlDialog;
  private final String username;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalTime startTime;
  private LocalTime endTime;

  MapControlFragment(
      String username,
      LocalDate startDate,
      LocalDate endDate,
      LocalTime startTime,
      LocalTime endTime
  ) {
    this.username = username;
    this.startDate = startDate;
    this.endDate = endDate;
    this.startTime = startTime;
    this.endTime = endTime;
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
            intent.putExtra("startDate", startDate.toString());
            intent.putExtra("endDate", endDate.toString());
            intent.putExtra("startTime", startTime.toString());
            intent.putExtra("endTime", endTime.toString());
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
    buttonStartDatePicker = view.findViewById(R.id.buttonStartDatePicker);
    buttonEndDatePicker = view.findViewById(R.id.buttonEndDatePicker);

    buttonStartTimePicker = view.findViewById(R.id.buttonStartTimePicker);
    buttonEndTimePicker = view.findViewById(R.id.buttonEndTimePicker);

    final FragmentManager fm = getParentFragmentManager();

    buttonStartDatePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize DatePicker
        DialogFragment newFragment = new DatePickerFragment(startDate);
        newFragment.setTargetFragment(MapControlFragment.this, START_DATE_PICKER_REQUEST);
        newFragment.show(fm, "startDatePicker");
      }
    });

    buttonEndDatePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize DatePicker
        DialogFragment newFragment = new DatePickerFragment(endDate);
        newFragment.setTargetFragment(MapControlFragment.this, END_DATE_PICKER_REQUEST);
        newFragment.show(fm, "endDatePicker");
      }
    });

    buttonStartTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize TimePicker for Start
        DialogFragment newFragment = new TimePickerFragment(startTime);
        newFragment.setTargetFragment(MapControlFragment.this, START_TIME_PICKER_REQUEST);
        newFragment.show(fm, "startTimePicker");
      }
    });

    buttonEndTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Initialize TimePicker for End
        DialogFragment newFragment = new TimePickerFragment(endTime);
        newFragment.setTargetFragment(MapControlFragment.this, END_TIME_PICKER_REQUEST);
        newFragment.show(fm, "endTimePicker");
      }
    });

    updateDateTimeButtonText();

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
      case START_DATE_PICKER_REQUEST:
        // Set date
        startDate = LocalDate.parse(bundle.getString("dateString"));
        break;

      case END_DATE_PICKER_REQUEST:
        // Set date
        endDate = LocalDate.parse(bundle.getString("dateString"));
        break;

      case START_TIME_PICKER_REQUEST:
        // Set start time
        startTime = LocalTime.parse(bundle.getString("timeString"));
        break;

      case END_TIME_PICKER_REQUEST:
        // Set end time
        endTime = LocalTime.parse(bundle.getString("timeString"));
        break;
    }

    updateDateTimeButtonText();
  }

  private void updateDateTimeButtonText() {
    buttonStartDatePicker.setText(startDate.toString());
    buttonEndDatePicker.setText(endDate.toString());
    buttonStartTimePicker.setText(startTime.toString());
    buttonEndTimePicker.setText(endTime.toString());
  }

  // Date/Time picker fragment classes
  public static class TimePickerFragment extends DialogFragment
      implements TimePickerDialog.OnTimeSetListener {
    private final LocalTime time;

    TimePickerFragment(LocalTime time) {
      this.time = time;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Create a new instance of TimePickerDialog and return it
      return new TimePickerDialog(
          getActivity(),
          this,
          time.getHour(),
          time.getMinute(),
          true
      );
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
      // Do something with the time chosen by the user
      LocalTime newTime = LocalTime.of(hour, minute);

      Intent intent = new Intent(getActivity(), MapFragment.class);
      intent.putExtra("timeString", newTime.toString());
      getTargetFragment().onActivityResult(getTargetRequestCode(), TIME_PICKER_RESULT_CODE, intent);
    }
  }

  public static class DatePickerFragment extends DialogFragment
      implements DatePickerDialog.OnDateSetListener {
    private final LocalDate date;

    DatePickerFragment(LocalDate date) {
      this.date = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Create a new instance of DatePickerDialog and return it
      return new DatePickerDialog(
          getActivity(),
          this,
          date.getYear(),
          date.getMonthValue() - 1,
          date.getDayOfMonth()
      );
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
      // Do something with the date chosen by the user
      LocalDate newDate = LocalDate.of(year, month + 1, day);

      Intent intent = new Intent(getActivity(), MapFragment.class);
      intent.putExtra("dateString", newDate.toString());
      getTargetFragment().onActivityResult(getTargetRequestCode(), DATE_PICKER_RESULT_CODE, intent);
    }
  }
}
