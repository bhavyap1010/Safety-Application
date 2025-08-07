package com.example.b07demosummer2024;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.net.Uri;

public class MainActivity extends BaseActivity {

//    private FirebaseDatabase db;
    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "com.example.b07demosummer2024.PREFS";
    private static final String KEY_DISCLAIMER_SEEN_PREFIX = "disclaimer_seen_";


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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login
            startActivity(new Intent(this, LoginActivityView.class));
            finish();
            return;
        }

        if (!hasSeenDisclaimer()) {
            showDisclaimer();
        }

        DatabaseReference r= FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("done");
        r.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Integer done = task.getResult().getValue(Integer.class);
                if (done != null && done == 1) {
                    loadFragment(new PlanFragment());
                } else {
                    loadFragment(new QuestionnaireFragment());
                }
            }
            });

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
        } else if (item.getItemId() == R.id.action_change_pin) {

             Intent intent = new Intent(this, PinSetupActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_home) {
        // findViewById(R.id.main).setVisibility(View.GONE);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    public void showPlanFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PlanFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showDisclaimer() {
        View noticeView = getLayoutInflater()
                .inflate(R.layout.disclaimers, null, false);

        new AlertDialog.Builder(this)
                .setView(noticeView)
                .setCancelable(false)
                .setPositiveButton(R.string.i_understand, (d, w) -> setDisclaimerSeen())
                .show();
    }

    private boolean hasSeenDisclaimer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return false;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_DISCLAIMER_SEEN_PREFIX + user.getUid(), false);
    }

    private void setDisclaimerSeen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_DISCLAIMER_SEEN_PREFIX + user.getUid(), true)
                        .apply();
    }
}