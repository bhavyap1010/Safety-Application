package com.example.b07demosummer2024;

import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivityModel {

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private FirebaseAuth mAuth;

    public LoginActivityModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void loginWithEmail(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onFailure("Please enter email and password");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            Exception exception = task.getException();
                            String errorMessage = "Login failed";
                            if (exception != null) {
                                String exceptionName = exception.getClass().getSimpleName();
                                if (exceptionName.equals("FirebaseAuthInvalidCredentialsException")) {
                                    errorMessage = "Invalid email/password.";
                                } else {
                                    errorMessage = "Login failed: " + exception.getMessage();
                                }
                            }
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void registerWithEmail(String email, String password, AuthCallback callback) {
        if (email.isEmpty() || password.isEmpty()) {
            callback.onFailure("Please enter email and password");
            return;
        }

        if (password.length() < 6) {
            callback.onFailure("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            Exception exception = task.getException();
                            String errorMessage = "Registration failed";
                            if (exception != null) {
                                errorMessage = "Registration failed: " + exception.getMessage();
                            }
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            Exception exception = task.getException();
                            String errorMessage = "Authentication failed";
                            if (exception != null) {
                                errorMessage = "Authentication failed: " + exception.getMessage();
                            }
                            callback.onFailure(errorMessage);
                        }
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        if (email.isEmpty()) {
            callback.onFailure("Please enter your email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        Exception exception = task.getException();
                        String errorMessage = "Failed to send reset email";
                        if (exception != null) {
                            errorMessage = "Failed to send reset email: " + exception.getMessage();
                        }
                        callback.onFailure(errorMessage);
                    }
                });
    }
}
