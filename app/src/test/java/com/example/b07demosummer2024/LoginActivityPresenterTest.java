package com.example.b07demosummer2024;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoginActivityPresenterTest {

    @Mock
    private LoginActivityPresenter.LoginView mockView;

    @Mock
    private LoginActivityModel mockModel;

    @Mock
    private PinManager mockPinManager;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private Context mockContext;

    private LoginActivityPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new LoginActivityPresenter(mockView, mockModel, mockPinManager);
        when(mockView.getContext()).thenReturn(mockContext);
    }

    @Test
    public void checkIfUserLoggedIn_UserWithPin_StartsPinLoginActivity() {
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(true);

        presenter.checkIfUserLoggedIn();

        verify(mockView).startPinLoginActivity();
        verify(mockView, never()).startMainActivity(anyBoolean());
    }

    @Test
    public void checkIfUserLoggedIn_UserWithoutPin_StartsMainActivity() {
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(false);

        presenter.checkIfUserLoggedIn();

        verify(mockView).startMainActivity(anyBoolean());
        verify(mockView, never()).startPinLoginActivity();
    }

    @Test
    public void checkIfUserLoggedIn_NoUser_DoesNothing() {
        when(mockModel.getCurrentUser()).thenReturn(null);

        presenter.checkIfUserLoggedIn();

        verifyNoInteractions(mockPinManager);
        verify(mockView, never()).startMainActivity(anyBoolean());
        verify(mockView, never()).startPinLoginActivity();
    }

    @Test
    public void loginWithEmail_Success_ShowsSuccessAndStartsMain() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(false);

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onSuccess(false);
            return null;
        }).when(mockModel).loginWithEmail(anyString(), anyString(), any());

        presenter.loginWithEmail();

        verify(mockView).showSuccessMessage("Login successful");
        verify(mockView).showToast("Please set up a PIN for additional security");
        verify(mockView).startPinSetupActivity();
    }

    @Test
    public void loginWithEmail_Success_NoPinEnabled_StartsMainActivity() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(true);

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onSuccess(false);
            return null;
        }).when(mockModel).loginWithEmail(anyString(), anyString(), any());

        presenter.loginWithEmail();

        verify(mockView).showSuccessMessage("Login successful");
        verify(mockView).startMainActivity(anyBoolean());
    }

    @Test
    public void loginWithEmail_Failure_ShowsError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onFailure("error");
            return null;
        }).when(mockModel).loginWithEmail(anyString(), anyString(), any());

        presenter.loginWithEmail();

        verify(mockView).showErrorMessage("error");
    }

    @Test
    public void registerWithEmail_NullEmail_ShowsError() {
        when(mockView.getEmailText()).thenReturn(null);
        when(mockView.getPasswordText()).thenReturn("password");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Please enter a valid email address.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_EmptyEmail_ShowsError() {
        when(mockView.getEmailText()).thenReturn("");
        when(mockView.getPasswordText()).thenReturn("password");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Please enter a valid email address.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_WhitespaceEmail_ShowsError() {
        when(mockView.getEmailText()).thenReturn("   ");
        when(mockView.getPasswordText()).thenReturn("password");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Please enter a valid email address.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_InvalidEmail_ShowsError() {
        when(mockView.getEmailText()).thenReturn("invalid");
        when(mockView.getPasswordText()).thenReturn("password");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Please enter a valid email address.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_NullPassword_ShowsError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn(null);

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Password must be at least 6 characters long.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_EmptyPassword_ShowsError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Password must be at least 6 characters long.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_ShortPassword_ShowsError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("123");

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Password must be at least 6 characters long.");
        verifyNoInteractions(mockModel);
    }

    @Test
    public void registerWithEmail_Success_StartsPinSetup() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onSuccess(true);
            return null;
        }).when(mockModel).registerWithEmail(anyString(), anyString(), any());

        presenter.registerWithEmail();

        verify(mockView).showSuccessMessage("Registration successful! Please set up your PIN.");
        verify(mockView).startPinSetupActivity();
    }

    @Test
    public void registerWithEmail_EmailAlreadyInUse_ShowsSpecificError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onFailure("ERROR_EMAIL_ALREADY_IN_USE");
            return null;
        }).when(mockModel).registerWithEmail(anyString(), anyString(), any());

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("This email address is already in use. Please try logging in or use a different email.");
    }

    @Test
    public void registerWithEmail_WeakPassword_ShowsSpecificError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onFailure("ERROR_WEAK_PASSWORD");
            return null;
        }).when(mockModel).registerWithEmail(anyString(), anyString(), any());

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("The password is too weak. Please choose a stronger password.");
    }

    @Test
    public void registerWithEmail_GenericFailure_ShowsGenericError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");
        when(mockView.getPasswordText()).thenReturn("password");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(2);
            cb.onFailure("Some other error");
            return null;
        }).when(mockModel).registerWithEmail(anyString(), anyString(), any());

        presenter.registerWithEmail();

        verify(mockView).showErrorMessage("Registration failed: Some other error");
    }

    @Test
    public void firebaseAuthWithGoogle_Success_ShowsSuccessAndStartsMain() {
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(false);
        
        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(1);
            cb.onSuccess(false);
            return null;
        }).when(mockModel).firebaseAuthWithGoogle(anyString(), any());

        presenter.firebaseAuthWithGoogle("token");

        verify(mockView).showSuccessMessage("Google sign in successful");
        verify(mockView).showToast("Please set up a PIN for additional security");
        verify(mockView).startPinSetupActivity();
    }

    @Test
    public void firebaseAuthWithGoogle_Success_NoPinEnabled_StartsMainActivity() {
        when(mockModel.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid123");
        when(mockPinManager.isPinEnabled(mockContext, "uid123")).thenReturn(true);
        
        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(1);
            cb.onSuccess(false);
            return null;
        }).when(mockModel).firebaseAuthWithGoogle(anyString(), any());

        presenter.firebaseAuthWithGoogle("token");

        verify(mockView).showSuccessMessage("Google sign in successful");
        verify(mockView).startMainActivity(anyBoolean());
    }

    @Test
    public void firebaseAuthWithGoogle_Failure_ShowsError() {
        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(1);
            cb.onFailure("error");
            return null;
        }).when(mockModel).firebaseAuthWithGoogle(anyString(), any());

        presenter.firebaseAuthWithGoogle("token");

        verify(mockView).showErrorMessage("error");
    }

    @Test
    public void resetPassword_Success_ShowsSuccess() {
        when(mockView.getEmailText()).thenReturn("test@example.com");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(1);
            cb.onSuccess(false);
            return null;
        }).when(mockModel).resetPassword(anyString(), any());

        presenter.resetPassword();

        verify(mockView).showSuccessMessage("Password reset email sent");
    }

    @Test
    public void resetPassword_Failure_ShowsError() {
        when(mockView.getEmailText()).thenReturn("test@example.com");

        doAnswer(invocation -> {
            LoginActivityModel.AuthCallback cb = invocation.getArgument(1);
            cb.onFailure("error");
            return null;
        }).when(mockModel).resetPassword(anyString(), any());

        presenter.resetPassword();

        verify(mockView).showErrorMessage("error");
    }
}