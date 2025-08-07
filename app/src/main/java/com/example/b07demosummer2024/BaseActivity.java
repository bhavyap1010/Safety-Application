package com.example.b07demosummer2024;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {
    private PinManager pinManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pinManager = new PinManager();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.onForeground();
        checkPinAuthRequired();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.onBackground();
    }

    private void checkPinAuthRequired() {
        String currentActivityName = getClass().getSimpleName();
        boolean isAuthRelatedActivity =
            "PinLoginActivity".equals(currentActivityName) ||
            "LoginActivityView".equals(currentActivityName) ||
            "PinSetupActivity".equals(currentActivityName);

        if (!isAuthRelatedActivity) {
            if (mAuth.getCurrentUser() != null &&
                pinManager.isPinEnabled(this, mAuth.getCurrentUser().getUid()) &&
                MainApplication.isRequirePinAuth()) {

                Intent intent = new Intent(this, PinLoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
