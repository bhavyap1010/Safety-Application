package com.example.b07demosummer2024;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.Button;
import android.view.View;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_PRIVACY_AGREED = "privacy_agreed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*To always have the privacy notice, uncomment the code below.
        * Otherwise, the privacy notice only pops up the first time someone
        * runs the app.*/
        /*
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(KEY_PRIVACY_AGREED)
                .apply();
        */
        showPrivacyDialogIfNeeded();
        db = FirebaseDatabase.getInstance("https://b07-demo-summer-2024-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = db.getReference("testDemo");

//        myRef.setValue("B07 Demo!");
        myRef.child("movies").setValue("B07 Demo!");

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void showPrivacyDialogIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean agreed = prefs.getBoolean(KEY_PRIVACY_AGREED, false);

        if (!agreed) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.privacy_notice_title))
                    .setMessage(getString(R.string.privacy_notice_msg))
                    .setCancelable(false)
                    .setPositiveButton(R.string.agree, (d, w) -> {
                        prefs.edit().putBoolean(KEY_PRIVACY_AGREED, true).apply();
                        // user continues into the app
                    })
                    .setNegativeButton(R.string.disagree, (d, w) -> {
                        // app exits
                        finishAffinity();
                    })
                    .show();
        }
    }
}