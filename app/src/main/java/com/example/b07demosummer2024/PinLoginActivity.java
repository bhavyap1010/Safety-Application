package com.example.b07demosummer2024; // Use your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PinLoginActivity extends BaseActivity {

    private EditText editTextLoginPin;
    private Button buttonUnlock, buttonSwitchToEmailLogin;
    private PinManager pinManager;
    private FirebaseAuth mAuth;
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);

        pinManager = new PinManager();
        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();

        editTextLoginPin = findViewById(R.id.editTextLoginPin);
        buttonUnlock = findViewById(R.id.buttonUnlock);
        buttonSwitchToEmailLogin = findViewById(R.id.buttonSwitchToEmailLogin);

        buttonUnlock.setOnClickListener(v -> verifyAndLogin(userID));
        buttonSwitchToEmailLogin.setOnClickListener(v -> switchToEmailLogin());
    }

    private void verifyAndLogin(String userID) {
        String enteredPin = editTextLoginPin.getText().toString();

        if (TextUtils.isEmpty(enteredPin) || enteredPin.length() != 4 || !TextUtils.isDigitsOnly(enteredPin)) {
            editTextLoginPin.setError("Enter a valid 4-digit PIN.");
            return;
        }

        if (pinManager.verifyPin(this, enteredPin, userID)) {
            Toast.makeText(this, "PIN correct. Logging in...", Toast.LENGTH_SHORT).show();
            loginAttempts = 0;
            MainApplication.setPinAuthRequired(false);
            Intent intent = new Intent(PinLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                Toast.makeText(this, "Too many incorrect attempts. Switching to email login.", Toast.LENGTH_LONG).show();
                switchToEmailLogin();
            } else {
                editTextLoginPin.setError("Incorrect PIN. " + (MAX_LOGIN_ATTEMPTS - loginAttempts) + " attempts remaining.");
                Toast.makeText(this, "Incorrect PIN.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void switchToEmailLogin() {
        if (mAuth.getCurrentUser() != null) {
            pinManager.clearPin(this, mAuth.getCurrentUser().getUid());
            mAuth.signOut();
        }

        Intent intent = new Intent(PinLoginActivity.this, LoginActivityView.class);
        startActivity(intent);
        finish();
    }
}
