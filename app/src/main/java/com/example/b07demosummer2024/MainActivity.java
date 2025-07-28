package com.example.b07demosummer2024;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    FirebaseDatabase db;
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_PRIVACY_AGREED = "privacy_agreed";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //start of new
        Button exitBtn = findViewById(R.id.btn_exit);
        exitBtn.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(browser);
            finishAndRemoveTask();
        });
        //End of new

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

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


//        myRef.setValue("B07 Demo!");
//        myRef.child("movies").setValue("B07 Demo!");

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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