package com.example.b07demosummer2024;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, buttonGoogleSignIn, buttonForgotPassword;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private PinManager pinManager; // Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pinManager = new PinManager();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google sign in with account selection prompt
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1030651688579-q04kt057ofuhkiv5dij48083e4loj327.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();

        setupClickListeners();

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
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);
                            Toast.makeText(this, "Google sign in failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "Google sign in cancelled or failed");
                        Toast.makeText(this, "Google sign in cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (pinManager.isPinEnabled(this)) {
                // PIN is set up, go to PIN login screen
                launchPinLoginActivity();
            } else {
                // PIN not set up, but user is logged in (e.g., from a previous session before PIN feature)
                // Decide the flow: go to main activity or prompt for PIN setup.
                // For now, let's assume if Firebase user exists and no PIN, go to main.
                // Or, you could force PIN setup here as well.
                startMainActivity();
            }
        }
    }

    private void launchPinLoginActivity() {
        Intent intent = new Intent(LoginActivity.this, PinLoginActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to LoginActivity
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
        buttonLogin.setOnClickListener(v -> loginWithEmail());
        buttonRegister.setOnClickListener(v -> registerWithEmail());
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        buttonForgotPassword.setOnClickListener(v -> resetPassword());
    }
    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Exception exception = task.getException();
                            String errorMessage = "Login failed";
                            if (exception != null) {
                                String exceptionName = exception.getClass().getSimpleName();
                                if (exceptionName.equals("FirebaseAuthInvalidCredentialsException")) {
                                    errorMessage = "Invalid email/password.";
                                } else {
                                    errorMessage = "Login failed: " + exception.getMessage();
                                }
                                Log.e(TAG, "Full error: " + exception.toString());
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void registerWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        launchPinSetupActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Exception exception = task.getException();
                        String errorMessage = "Registration failed";
                        if (exception != null) {
                            errorMessage = "Registration failed: " + exception.getMessage();
                            Log.e(TAG, "Full error: " + exception.toString());
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void signInWithGoogle() {
        // Sign out first to force account selection
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Launch sign in intent after signing out
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Google sign in successful", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close login activity so user can't go back to it
    }

    private void launchPinSetupActivity() {
        Intent intent = new Intent(LoginActivity.this, PinSetupActivity.class);
        // You might want to pass the user's email or ID if needed in PinSetupActivity
        // intent.putExtra("USER_EMAIL", mAuth.getCurrentUser().getEmail());
        startActivity(intent);
        finish(); // Optional: finish LoginActivity if you don't want users to go back
    }
}

