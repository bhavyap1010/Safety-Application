package com.example.b07demosummer2024;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.app.AlarmManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivityView extends BaseActivity implements LoginActivityPresenter.LoginView {

    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, buttonGoogleSignIn, buttonForgotPassword;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private LoginActivityPresenter presenter;

    private static final String PREFS_NAME = "AlarmPermissionPrefs";
    private static final String KEY_DONT_ASK_AGAIN = "dontAskAgain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkAlarmPermission();

        LoginActivityModel model = new LoginActivityModel();
        PinManager pinManager = new PinManager();
        presenter = new LoginActivityPresenter(this, model, pinManager);

        // Configure Google sign in with account selection prompt
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1030651688579-q04kt057ofuhkiv5dij48083e4loj327.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();
        setupClickListeners();
        setupGoogleSignInLauncher();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly
        presenter.checkIfUserLoggedIn();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> presenter.loginWithEmail());
        buttonRegister.setOnClickListener(v -> presenter.registerWithEmail());
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        buttonForgotPassword.setOnClickListener(v -> presenter.resetPassword());
    }

    private void setupGoogleSignInLauncher() {
        // Initialize Google sign in launcher
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Google Sign-In result code: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        presenter.firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);
                        showErrorMessage("Google sign in failed: " + e.getLocalizedMessage());
                    }
                } else {
                    Log.d(TAG, "Google sign in cancelled or failed");
                    showToast("Google sign in cancelled");
                }
            }
        );
    }

    private void signInWithGoogle() {
        // Sign out first to force account selection
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Launch sign in intent after signing out
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    // Implementation of LoginView interface methods
    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startMainActivity(boolean isNewAccount) {
        Intent intent = new Intent(LoginActivityView.this, MainActivity.class);
        intent.putExtra("NEW_ACCOUNT_CREATED", isNewAccount);
        startActivity(intent);
        finish(); // Close login activity so user can't go back to it
    }

    @Override
    public void startPinLoginActivity() {
        Intent intent = new Intent(LoginActivityView.this, PinLoginActivity.class);
        startActivity(intent);
        finish(); // Close login activity so user can't go back to it
    }

    @Override
    public void startPinSetupActivity() {
        Intent intent = new Intent(LoginActivityView.this, PinSetupActivity.class); // Or your actual PIN setup activity name
        startActivity(intent);
        finish(); // Close login activity
    }

    @Override
    public String getEmailText() {
        return editTextEmail.getText().toString().trim();
    }

    @Override
    public String getPasswordText() {
        return editTextPassword.getText().toString().trim();
    }

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context getContext() {
        return this;
    }

    private void checkAlarmPermission() {
        Log.d(TAG, "Checking alarm permission");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean dontAskAgain = settings.getBoolean(KEY_DONT_ASK_AGAIN, false);
        Log.d(TAG, "Don't ask again preference: " + dontAskAgain);

        if (!dontAskAgain) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Only check canScheduleExactAlarms on Android S (API 31) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                boolean canSchedule = alarmManager.canScheduleExactAlarms();
                Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
                if (!canSchedule) {
                    Log.d(TAG, "Showing alarm permission dialog");
                    showAlarmPermissionDialog();
                }
            } else {
                // On older Android versions, the permission is granted by default
                Log.d(TAG, "Android version below S, permission granted by default");
            }
        }
    }

    private void showAlarmPermissionDialog() {
        View checkBoxView = getLayoutInflater().inflate(R.layout.dialog_checkbox, null);
        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setText("Don't ask again");

        new AlertDialog.Builder(this)
                .setTitle("Exact Alarm Permission Required")
                .setMessage("This app needs permission to schedule exact alarms for better functionality. Would you like to grant this permission?")
                .setView(checkBoxView)
                .setPositiveButton("Settings", (dialog, which) -> {
                    if (checkBox.isChecked()) {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        settings.edit().putBoolean(KEY_DONT_ASK_AGAIN, true).apply();
                    }
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (checkBox.isChecked()) {
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        settings.edit().putBoolean(KEY_DONT_ASK_AGAIN, true).apply();
                    }
                })
                .show();
    }
}
