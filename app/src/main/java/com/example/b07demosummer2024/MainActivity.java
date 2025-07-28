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

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase db;
    private FirebaseAuth mAuth;
    RecyclerView recyclerView;
    PlanItemAdapter adapter;
    QuestionnaireFragment questionnaire;
    List<PlanItem> items;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

//         if (savedInstanceState == null) {
//            loadFragment(new HomeFragment());
//         }

        DatabaseReference r= FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("done");
        r.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
if(task.getResult().getValue(Integer.class) !=null) {
                if (task.getResult().getValue(Integer.class) == 1) {
                    findViewById(R.id.main).setVisibility(View.VISIBLE);
                    nowPlan();
                }
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.questionaire_fragment, new QuestionnaireFragment())
                            .commit();
                }

            }
        });



    }

    // this function simply generates a tip given a question and a user answer
    public String generateTip(Question q, String answer) {
        String tipTemplate = null;

        if (q.getTips().containsKey(answer)) {
            tipTemplate = q.getTips().get(answer);
        }
        else if (q.getTips().containsKey("default")) {
            tipTemplate = q.getTips().get("default");
        }

        if (tipTemplate == null) {
            tipTemplate = "Error generating tip for this question";
        }


        tipTemplate = tipTemplate.replace("{user_answer}", answer);

        return tipTemplate;
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
            // Handle the "Change pin" action here
            // For example, you might start a new Activity or show a Dialog:
             Intent intent = new Intent(this, PinSetupActivity.class);
            startActivity(intent);
            finish();
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

//    private void loadFragment(Fragment fragment) {
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, fragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void nowPlan() {

        Fragment q  = getSupportFragmentManager().findFragmentById(R.id.questionaire_fragment);
        if(q!=null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(q)
                    .commit();
        }

        findViewById(R.id.main).setVisibility(View.VISIBLE);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // connect to firebase database using a reference to the current user
        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = db.getReference("users").child(currentUser.getUid());
        // initial RecyclerView setup
        recyclerView = findViewById(R.id.planItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        items = new ArrayList<>();
        adapter = new PlanItemAdapter(items);
        recyclerView.setAdapter(adapter);



        // load the JSON file to templates list
        List<Question> templates = JSONUtility.loadQuestionTips(this);



        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();

                // for every question-answer pair in the db, search if the current question matches the one in the templates
                // if it matches then generate the tip that is to be displayed to the user accordingly
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String questionID = child.getKey();
                    String answer = String.valueOf(child.getValue());


                    for (Question q : templates){
                        if (q.getId().equals(questionID)){
                            String tip = generateTip(q, answer);
                            items.add(new PlanItem(q.getQuestion(), Collections.singletonList(tip)));
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Failed to load data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        };
        myRef.addListenerForSingleValueEvent(postListener);
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("done").setValue(1);
        }

    }
}