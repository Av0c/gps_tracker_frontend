package com.example.whereareyou.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.example.whereareyou.MainActivity;
import com.example.whereareyou.R;
import com.example.whereareyou.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity
  implements RegisterDialog.OnRegisterListener {
  // Components
  private TextInputEditText usernameLoginInput;
  private TextInputLayout usernameLoginLayout;
  private TextInputEditText passwordLoginInput;
  private TextInputLayout passwordLoginLayout;

  private final OkHttpClient okHttpClient = new OkHttpClient();

  Call post(String url, RequestBody body, Callback callback) {
    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();

    Call call = okHttpClient.newCall(request);
    call.enqueue(callback);
    return call;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Bind inputs
    usernameLoginInput = findViewById(R.id.usernameLoginInput);
    usernameLoginLayout = findViewById(R.id.usernameLoginLayout);
    passwordLoginInput = findViewById(R.id.passwordLoginInput);
    passwordLoginLayout = findViewById(R.id.passwordLoginLayout);

    Button buttonLogin = findViewById(R.id.buttonLogin);
    Button buttonRegister = findViewById(R.id.buttonRegister);
    FloatingActionButton apiConfigButton = findViewById(R.id.api_config_button);

    apiConfigButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DialogFragment newFragment = new ApiConfigDialog();

        newFragment.show(getSupportFragmentManager(), "ApiConfigDialog");
      }
    });

    buttonLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // Validate inputs
        String tryUsername = usernameLoginInput.getText().toString();
        String tryPassword = passwordLoginInput.getText().toString();
        boolean validInputs = true;

        if (tryUsername.equals("")) {
          validInputs = false;
          usernameLoginLayout.setError("Username can not be blank");
        }
        if (tryPassword.equals("")) {
          validInputs = false;
          passwordLoginLayout.setError("Password can not be blank");
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
          usernameLoginLayout.setError("Username can not be blank");
        }
        if (tryPassword.equals("")) {
          validInputs = false;
          passwordLoginLayout.setError("Password can not be blank");
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
    RequestBody body = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("username", username)
        .addFormDataPart("password", password)
        .build();

    post(Utils.getApiRoot(getApplicationContext()) + "user", body, new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        usernameLoginLayout.setError("Failed to register username");
        passwordLoginLayout.setError("Failed to register password");
      }

      @Override
      public void onResponse(Call call, Response response) {
        if (response.isSuccessful()) {
          Utils.setAuthentication(LoginActivity.this, username, password);
          login();
        } else {
          usernameLoginLayout.setError("Failed to register username");
          passwordLoginLayout.setError("Failed to register password");
        }
        response.body().close();
      }
    });
  }

  private void authorizeUser(final String username, final String password) {
    JSONObject postBody = new JSONObject();
    try {
      postBody.put("username", username);
      postBody.put("password", password);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    AndroidNetworking.post(Utils.getApiRoot(getApplicationContext()) + "user/login")
        .addBodyParameter("username", username)
        .addBodyParameter("password", password)
        .setTag("postUserLogin")
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsOkHttpResponse(new OkHttpResponseListener() {
          @Override
          public void onResponse(Response response) {
            if (response.code() == 200) {
              Utils.setAuthentication(LoginActivity.this, username, password);
              login();
            }
            else {
              usernameLoginLayout.setError("Failed to authorize username");
              passwordLoginLayout.setError("Failed to authorize password");
            }
          }
          @Override
          public void onError(ANError anError) {
            usernameLoginLayout.setError("Failed to authorize username");
            passwordLoginLayout.setError("Failed to authorize password");
          }
        });
  }

  private void login() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

}
