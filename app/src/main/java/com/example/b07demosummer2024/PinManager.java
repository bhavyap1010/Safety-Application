// PinManager.java
package com.example.b07demosummer2024; // Use your actual package name

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class PinManager {

    private static final String PREFERENCE_FILE_KEY = "com.example.b07demosummer2024.PIN_PREFS";
    private static final String PIN_KEY = "user_pin"; // Store HASHED PIN here
    private static final String PIN_ENABLED_KEY = "pin_enabled";


    private SharedPreferences getEncryptedSharedPreferences(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        return EncryptedSharedPreferences.create(
                masterKeyAlias,
                PREFERENCE_FILE_KEY,
                context, // Use application context or activity context
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    // Call this after successful registration and PIN setup
    public boolean storePin(Context context, String pin) {
        // IMPORTANT: Hash the PIN before storing it!
        // String hashedPin = hashFunction(pin); // Implement your hashing function
        // For this example, storing directly (NOT RECOMMENDED FOR PRODUCTION)
        String hashedPin = pin; // Replace with actual hashing

        try {
            SharedPreferences.Editor editor = getEncryptedSharedPreferences(context).edit();
            editor.putString(PIN_KEY, hashedPin);
            editor.putBoolean(PIN_ENABLED_KEY, true);
            editor.apply();
            return true;
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Call this during PIN login to verify
    public boolean verifyPin(Context context, String enteredPin) {
        // IMPORTANT: Hash the enteredPin using the SAME hashing function and parameters
        // String hashedEnteredPin = hashFunction(enteredPin);
        // For this example, direct comparison (NOT RECOMMENDED FOR PRODUCTION)
        String hashedEnteredPin = enteredPin; // Replace with actual hashing

        try {
            String storedHashedPin = getEncryptedSharedPreferences(context).getString(PIN_KEY, null);
            return storedHashedPin != null && storedHashedPin.equals(hashedEnteredPin);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPinEnabled(Context context) {
        try {
            return getEncryptedSharedPreferences(context).getBoolean(PIN_ENABLED_KEY, false);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clearPin(Context context) {
        try {
            SharedPreferences.Editor editor = getEncryptedSharedPreferences(context).edit();
            editor.remove(PIN_KEY);
            editor.putBoolean(PIN_ENABLED_KEY, false);
            editor.apply();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // You'll need to implement a robust hashing function
    // Example (conceptual, use a proper library like Argon2 or SCrypt):
    /*
    private String hashFunction(String pin) {
        // Use a strong hashing algorithm with a salt.
        // For example, using SCrypt or Argon2 via a library.
        // This is a placeholder and NOT secure for production.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    */
}
