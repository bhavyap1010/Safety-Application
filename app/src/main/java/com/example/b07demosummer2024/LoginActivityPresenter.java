package com.example.b07demosummer2024;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivityPresenter {

    private static final String TAG = "LoginActivity";

    public interface LoginView {
        void showToast(String message);
        void startMainActivity(boolean isNewAccount);
        void startPinLoginActivity();
        void startPinSetupActivity(boolean isNewAccount);
        String getEmailText();
        String getPasswordText();
        void showSuccessMessage(String message);
        void showErrorMessage(String message);

        Context getContext();
    }

    private PinManager pinManager;
    private LoginView view;
    private LoginActivityModel model;

    public LoginActivityPresenter(LoginView view, LoginActivityModel model, PinManager pinManager) {
        this.view = view;
        this.model = model;
        this.pinManager = pinManager;
    }

    public void checkIfUserLoggedIn() {
        FirebaseUser currentUser = model.getCurrentUser();
        if (currentUser != null) {

            if (pinManager.isPinEnabled(view.getContext(), model.getCurrentUser().getUid())) {
                // PIN is set up, go to PIN login screen
                view.startPinLoginActivity();
            } else {
                // PIN not set up, but user is logged in (e.g., from a previous session before PIN feature)
                // Decide the flow: go to main activity or prompt for PIN setup.
                // For now, let's assume if Firebase user exists and no PIN, go to main.
                // Or, you could force PIN setup here as well.
                view.startMainActivity(false);
            }
        }
    }

    public void loginWithEmail() {
        String email = view.getEmailText();
        String password = view.getPasswordText();

        model.loginWithEmail(email, password, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess(boolean isNew) {
                Log.d(TAG, "signInWithEmail:success");
                view.showSuccessMessage("Login successful");

                if (pinManager.isPinEnabled(view.getContext(), model.getCurrentUser().getUid())) {
                    // PIN is set up, go to PIN login screen
                    view.startMainActivity();
                } else {
                    // PIN not set up, but user is logged in (e.g., from a previous session before PIN feature)
                    // Decide the flow: go to main activity or prompt for PIN setup.
                    // For now, let's assume if Firebase user exists and no PIN, go to main.
                    // Or, you could force PIN setup here as well
                    view.startPinSetupActivity();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "signInWithEmail:failure");
                Log.e(TAG, "Full error: " + errorMessage);
                view.showErrorMessage(errorMessage);
            }
        });
    }

    public void registerWithEmail() {
        String email = view.getEmailText();
        String password = view.getPasswordText();

        if (!isValidEmail(email)) {
            view.showErrorMessage("Please enter a valid email address.");
            return;
        }
        if (password == null || password.isEmpty() || password.length() < 6) {
            view.showErrorMessage("Password must be at least 6 characters long.");
            return;
        }

        model.registerWithEmail(email, password, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess(boolean isNewAccount) {
                // Firebase automatically signs in the user upon successful registration.
                // The getCurrentUser() in the model will now return the new user.
                Log.d(TAG, "createUserWithEmail:success - User registered and signed in.");

                // Now, guide the new user to set up a PIN
                view.showSuccessMessage("Registration successful! Please set up your PIN.");
                view.startPinSetupActivity(true); // Navigate to PIN setup screen
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "createUserWithEmail:failure");
                Log.e(TAG, "Full error: " + errorMessage);
                // More user-friendly error messages based on common Firebase errors:
                if (errorMessage.contains("ERROR_EMAIL_ALREADY_IN_USE")) {
                    view.showErrorMessage("This email address is already in use. Please try logging in or use a different email.");
                } else if (errorMessage.contains("ERROR_WEAK_PASSWORD")) {
                    view.showErrorMessage("The password is too weak. Please choose a stronger password.");
                } else {
                    view.showErrorMessage("Registration failed: " + errorMessage);
                }
            }
        });
    }

    public void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + idToken);

        model.firebaseAuthWithGoogle(idToken, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess(boolean isNew) {
                Log.d(TAG, "signInWithCredential:success");
                view.showSuccessMessage("Google sign in successful");
                view.startMainActivity(isNew);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "signInWithCredential:failure");
                view.showErrorMessage(errorMessage);
            }
        });
    }

    public void resetPassword() {
        String email = view.getEmailText();

        model.resetPassword(email, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess(boolean ignored) {
                view.showSuccessMessage("Password reset email sent");
            }

            @Override
            public void onFailure(String errorMessage) {
                view.showErrorMessage(errorMessage);
            }
        });
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
}
