package com.nss.oneiitp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    EditText emailInput;
    Button loginBtn;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        // Check if already logged in
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if(isLoggedIn){
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if(email.isEmpty()){
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Standard email validation using Android Patterns
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate against JSON data
            if (isEmailInStudentData(email)) {
                // Save login state only after validation passes
                saveLoginState(email);
                navigateToMain();
            } else {
                Toast.makeText(this, "Invalid email id. Access denied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isEmailInStudentData(String email) {
        try {
            InputStream is = getResources().openRawResource(R.raw.student_data);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String studentEmail = obj.optString("INSTITUTE_EMAIL_ID");
                if (email.equalsIgnoreCase(studentEmail)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading student data JSON", e);
        }
        return false;
    }

    private void saveLoginState(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("email", email);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
