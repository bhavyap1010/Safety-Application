package com.example.b07demosummer2024; // Use your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PinLoginActivity extends AppCompatActivity {

    private EditText editTextLoginPin;
    private Button buttonUnlock, buttonSwitchToEmailLogin;
    private PinManager pinManager;
    private FirebaseAuth mAuth;
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login); // Ensure this matches your layout file name

        pinManager = new PinManager();
        mAuth = FirebaseAuth.getInstance();

        editTextLoginPin = findViewById(R.id.editTextLoginPin);
        buttonUnlock = findViewById(R.id.buttonUnlock);
        buttonSwitchToEmailLogin = findViewById(R.id.buttonSwitchToEmailLogin);

        buttonUnlock.setOnClickListener(v -> verifyAndLogin());
        buttonSwitchToEmailLogin.setOnClickListener(v -> switchToEmailLogin());
    }

    private void verifyAndLogin() {
        String enteredPin = editTextLoginPin.getText().toString();

        if (TextUtils.isEmpty(enteredPin) || enteredPin.length() != 4 || !TextUtils.isDigitsOnly(enteredPin)) {
            editTextLoginPin.setError("Enter a valid 4-digit PIN.");
            return;
        }

        if (pinManager.verifyPin(this, enteredPin)) {
            Toast.makeText(this, "PIN correct. Logging in...", Toast.LENGTH_SHORT).show();
            loginAttempts = 0; // Reset attempts on success
            // Navigate to your app's main screen
            Intent intent = new Intent(PinLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                Toast.makeText(this, "Too many incorrect attempts. Switching to email login.", Toast.LENGTH_LONG).show();
                // Optionally clear the PIN here or require email login for recovery
                // pinManager.clearPin(this); // Be careful with this, user might just be forgetful
                switchToEmailLogin();
            } else {
                editTextLoginPin.setError("Incorrect PIN. " + (MAX_LOGIN_ATTEMPTS - loginAttempts) + " attempts remaining.");
                Toast.makeText(this, "Incorrect PIN.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void switchToEmailLogin() {
        // Clear any stored PIN preference if you want to force email login next time
        // or just for this session.
        // For now, just sign out Firebase user to ensure LoginActivity shows email fields.
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut(); // Sign out to ensure LoginActivity doesn't auto-redirect via Firebase
        }
        // Also clear the PIN enabled flag so onStart() in LoginActivity doesn't redirect here.
        // This means they'll have to set up PIN again after email login if they wish.
        pinManager.clearPin(this); // This makes them go through PIN setup again after email login

        Intent intent = new Intent(PinLoginActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
