package com.example.b07demosummer2024;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private RecyclerView recyclerView;
    private PlanItemAdapter adapter;
    private List<PlanItem> items;
    private TextView messageText; // for displaying if questionnaire is incomplete

    // we simply store the follow up question id in pairs so we can simply access it later
    // it is static because we want them to be available by class name and not only using object
    private static final Map<String, String> followUPMap = new HashMap<>();
    static {
        followUPMap.put("f_01", "code_word");
        followUPMap.put("f_02", "temp_shelter");
        followUPMap.put("f_03", "legal_order");
        followUPMap.put("f_04", "equipment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // intitial setup
        View view = inflater.inflate(R.layout.fragment_plan, container, false);
        recyclerView = view.findViewById(R.id.planItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        items = new ArrayList<>();
        adapter = new PlanItemAdapter(items);
        recyclerView.setAdapter(adapter);

        messageText = view.findViewById(R.id.messageText);
        messageText.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        loadPlan();

        return view;
    }

    private void loadPlan() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in. Redirecting to login page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivityView.class));
            requireActivity().finish();
            return;
        }

        // firebase setuup
        db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference("users").child(currentUser.getUid());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // new check: if questionnaire not answered, show message & hide plan list
                if (!snapshot.hasChild("wu_01")) {
                    recyclerView.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);
                    return;
                }

                items.clear();
                List<Question> templates = JSONUtility.loadQuestionTips(getContext());

                // initialize key-answer pair hashmap
                Map<String, String> answerMap = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    // skip categories used by VAULT epic
                    String key = child.getKey();
                    if (key != null && key.equals("categories")) {
                        continue;
                    }

                    // all the actual answer id-value pair logic:
                    Object val = child.getValue();
                    if (val instanceof List<?>) { // this is useful for sr_01 as user can select multiple choice
                        // Convert list to comma-separated string for better display in the screen
                        List<?> actualValue = (List<?>) val;
                        String joined = TextUtils.join(", ", actualValue);
                        answerMap.put(child.getKey(), joined);
                    } else {
                        answerMap.put(child.getKey(), String.valueOf(val));
                    }
                }


                // filter out the objects based on their prefixes
                // so only use the ones which are related to user's branch, such as sr = Still in a Relationship
                String initStatus = answerMap.get("wu_01");
                String prefix = null;

                if ("Still in a Relationship".equalsIgnoreCase(initStatus)) {
                    prefix = "sr_";
                } else if ("Planning to leave".equalsIgnoreCase(initStatus)) {
                    prefix = "pl_";
                } else if ("Post-separation".equalsIgnoreCase(initStatus)) {
                    prefix = "ps_";
                }

                for (Map.Entry<String, String> entry : answerMap.entrySet()) {
                    String questionID = entry.getKey();
                    String answer = entry.getValue();

                    // if there is a prefix, but not the one we want, so skip (i.e. continue)
                    if (prefix != null && !questionID.startsWith(prefix) && !questionID.startsWith("wu_") && !questionID.startsWith("f_") && !questionID.startsWith("fu_")) {
                        continue;
                    }

                    for (Question q : templates) {
                        if (q.getId().equals(questionID)) {
                            String tip = generateTipWithFollowUps(q, answer, answerMap);
                            items.add(new PlanItem(q.getQuestion(), Collections.singletonList(tip)));
                            break; // already found the required key so does not make sense to keep looping
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PlanFragment", "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load plan.", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("done")
                .setValue(1);
    }

    private String generateTipWithFollowUps(Question q, String answer, Map<String, String> answerMap) {
        Map<String, String> tips = q.getTips();

        String tipTemplate = null;
        if (tips.containsKey(answer)) {
            tipTemplate = tips.get(answer);
        } else if (tips.containsKey("default")) {
            tipTemplate = tips.get("default");
        }
        if (tipTemplate == null) {
            return "No tip available for this answer.";
        }

        // {user_answer} logic
        tipTemplate = tipTemplate.replace("{user_answer}", answer);

        // follow up logic (e.g. {code_word})
        String fid = q.getFid();
        if (fid != null && followUPMap.containsKey(fid)) {
            String placeholder = followUPMap.get(fid); // e.g. f_01 returns code_word
            String followUpValue = answerMap.get(fid);

            // replace with follow up value if present, and in case we cannot load it due to a bug, replace with [not provided]
            if (followUpValue != null && !followUpValue.trim().isEmpty()) {
                tipTemplate = tipTemplate.replace("{" + placeholder + "}", followUpValue);
            }
            else {
                tipTemplate = tipTemplate.replace("{" + placeholder + "}", "[not provided]");
            }
        }

        return tipTemplate;
    }
}
