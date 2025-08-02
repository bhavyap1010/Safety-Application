package com.example.b07demosummer2024;

import android.util.Log;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivityPresenter {

    private static final String TAG = "LoginActivity";

    public interface LoginView {
        void showToast(String message);
        void startMainActivity();
        String getEmailText();
        String getPasswordText();
        void showSuccessMessage(String message);
        void showErrorMessage(String message);
    }

    private LoginView view;
    private LoginActivityModel model;

    public LoginActivityPresenter(LoginView view) {
        this.view = view;
        this.model = new LoginActivityModel();
    }

    public void checkIfUserLoggedIn() {
        FirebaseUser currentUser = model.getCurrentUser();
        if (currentUser != null) {
            view.startMainActivity();
        }
    }

    public void loginWithEmail() {
        String email = view.getEmailText();
        String password = view.getPasswordText();

        model.loginWithEmail(email, password, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "signInWithEmail:success");
                view.showSuccessMessage("Login successful");
                view.startMainActivity();
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

        model.registerWithEmail(email, password, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "createUserWithEmail:success");
                view.showSuccessMessage("Registration successful");
                view.startMainActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.w(TAG, "createUserWithEmail:failure");
                Log.e(TAG, "Full error: " + errorMessage);
                view.showErrorMessage(errorMessage);
            }
        });
    }

    public void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + idToken);

        model.firebaseAuthWithGoogle(idToken, new LoginActivityModel.AuthCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "signInWithCredential:success");
                view.showSuccessMessage("Google sign in successful");
                view.startMainActivity();
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
            public void onSuccess() {
                view.showSuccessMessage("Password reset email sent");
            }

            @Override
            public void onFailure(String errorMessage) {
                view.showErrorMessage(errorMessage);
            }
        });
    }
}
