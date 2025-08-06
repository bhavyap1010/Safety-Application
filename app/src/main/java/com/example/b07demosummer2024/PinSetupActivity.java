package com.example.b07demosummer2024; // Use your actual package name

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PinSetupActivity extends AppCompatActivity {

    private boolean isNewAccount;
    private EditText editTextPin, editTextConfirmPin;
    private Button buttonSubmitPin, buttonCancelPinSetup;

    private static final String PREFERENCE_FILE_KEY = "com.example.b07demosummer2024.PIN_PREFS";
    private static final String PIN_KEY = "user_pin";

    private FirebaseAuth mAuth;

    private PinManager pinManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        isNewAccount = getIntent().getBooleanExtra("NEW_ACCOUNT_CREATED", false);

        pinManager = new PinManager();

        mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();

        editTextPin = findViewById(R.id.editTextPin);

        editTextConfirmPin = findViewById(R.id.editTextConfirmPin);
        buttonSubmitPin = findViewById(R.id.buttonSubmitPin);
        buttonCancelPinSetup = findViewById(R.id.buttonCancelPinSetup);

        buttonSubmitPin.setOnClickListener(v -> setupPin(userID));
        buttonCancelPinSetup.setOnClickListener(v -> {
            Intent intent = new Intent(PinSetupActivity.this, MainActivity.class);
            intent.putExtra("NEW_ACCOUNT_CREATED", isNewAccount);
            startActivity(intent);
            finish();
        });
    }

    private void setupPin(String userID) {
        String pin = editTextPin.getText().toString();
        String confirmPin = editTextConfirmPin.getText().toString();

        if (TextUtils.isEmpty(pin) || pin.length() != 4 || !TextUtils.isDigitsOnly(pin)) {
            editTextPin.setError("PIN must be 4 digits.");
            return;
        }

        if (TextUtils.isEmpty(confirmPin) || !confirmPin.equals(pin)) {
            editTextConfirmPin.setError("PINs do not match.");
            return;
        }

        if (pinManager.storePin(this, pin, userID)) { // Use PinManager
            Toast.makeText(this, "PIN setup successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("NEW_ACCOUNT_CREATED", isNewAccount);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Failed to save PIN. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isNewAccount) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("NEW_ACCOUNT_CREATED", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
