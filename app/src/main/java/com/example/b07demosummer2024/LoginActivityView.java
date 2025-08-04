package com.example.b07demosummer2024;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivityView extends AppCompatActivity implements LoginActivityPresenter.LoginView {

    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, buttonGoogleSignIn, buttonForgotPassword;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private LoginActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
    public void startMainActivity() {
        Intent intent = new Intent(LoginActivityView.this, MainActivity.class);
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
        return this; // Or getApplicationContext() if appropriate
    }
}
