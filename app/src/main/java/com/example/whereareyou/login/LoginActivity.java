package com.example.whereareyou.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.androidnetworking.AndroidNetworking;
;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.whereareyou.MainActivity;
import com.example.whereareyou.R;
import com.example.whereareyou.Utils;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
  implements RegisterDialog.OnRegisterListener {
  private static final String TAG = "MyDebug";
  // Components
  private TextInputEditText usernameLoginInput;
  private TextInputEditText passwordLoginInput;
  private Button buttonLogin;
  private Button buttonRegister;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Bind inputs
    usernameLoginInput = findViewById(R.id.usernameLoginInput);
    passwordLoginInput = findViewById(R.id.passwordLoginInput);
    buttonLogin = findViewById(R.id.buttonLogin);
    buttonRegister = findViewById(R.id.buttonRegister);

    buttonLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Validate inputs
        String tryUsername = usernameLoginInput.getText().toString();
        String tryPassword = passwordLoginInput.getText().toString();
        boolean validInputs = true;

        if (tryUsername.equals("")) {
          validInputs = false;
          usernameLoginInput.setError("Username can not be blank");
        }
        if (tryPassword.equals("")) {
          validInputs = false;
          passwordLoginInput.setError("Password can not be blank");
        }

        if (validInputs) {
          authorizeUser(tryUsername, tryPassword);
        }
      }
    });

    buttonRegister.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Validate inputs
        String tryUsername = usernameLoginInput.getText().toString();
        String tryPassword = passwordLoginInput.getText().toString();
        boolean validInputs = true;

        if (tryUsername.equals("")) {
          validInputs = false;
          usernameLoginInput.setError("Username can not be blank");
        }
        if (tryPassword.equals("")) {
          validInputs = false;
          passwordLoginInput.setError("Password can not be blank");
        }

        if (validInputs) {
          DialogFragment newFragment = new RegisterDialog(
              tryUsername,
              tryPassword
          );

          newFragment.show(getSupportFragmentManager(), "RegisterDialog");
        }
      }
    });

    // Init networking
    AndroidNetworking.initialize(getApplicationContext());
  }

  @Override
  public void onRegister(final String username, final String password) {
    Log.d(TAG, "onRegister: " + username + ":" + password);
    JSONObject postBody = new JSONObject();
    try {
      postBody.put("username", username);
      postBody.put("password", password);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    // AndroidNetworking.post(getString(R.string.api_root)+"user")
    //     .addJSONObjectBody(postBody)
    //     .setTag("postUserLogin")
    //     .setPriority(Priority.MEDIUM)
    //     .build()
    //     .getAsJSONObject(new JSONObjectRequestListener() {
    //       @Override
    //       public void onResponse(JSONObject response) {
    //         // Register ok (200)
    //         Utils.setAuthentication(LoginActivity.this, username, password);
    //         login();
    //       }
    //       @Override
    //       public void onError(ANError error) {
    //         usernameLoginInput.setError("Failed to register username");
    //         passwordLoginInput.setError("Failed to register password");
    //       }
    //     });
    Utils.setAuthentication(LoginActivity.this, username, password);
    login();
  }

  private void authorizeUser(final String username, final String password) {
    JSONObject postBody = new JSONObject();
    try {
      postBody.put("username", username);
      postBody.put("password", password);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Utils.setAuthentication(LoginActivity.this, username, password);
    login();
    // AndroidNetworking.post(getString(R.string.api_root)+"user/login")
    //     .addJSONObjectBody(postBody)
    //     .setTag("postUserLogin")
    //     .setPriority(Priority.MEDIUM)
    //     .build()
    //     .getAsJSONObject(new JSONObjectRequestListener() {
    //       @Override
    //       public void onResponse(JSONObject response) {
    //         // Login ok (200)
    //         Utils.setAuthentication(LoginActivity.this, username, password);
    //         login();
    //       }
    //       @Override
    //       public void onError(ANError error) {
    //         usernameLoginInput.setError("Failed to authorize username");
    //         passwordLoginInput.setError("Failed to authorize password");
    //       }
    //     });
  }

  private void login() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

}
