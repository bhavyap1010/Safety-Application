package com.example.b07demosummer2024;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

class Question {
    String qs;
    String id;
    String t;
    ArrayList<String> c;

    private Map<String, String> tips;

    // getters
    public String getId() {
        return id;
    }
    public String getQuestion() {
        return qs;
    }
    public Map<String, String> getTips() {
        return tips;
    }

    public Question(String q, String t, ArrayList<String> c, String id) {
        this.t=t;
        this.id=id;
        this.qs=q;
        this.c=c;
    }
    public Question(String q, String t, String id) {
        this.id=id;

        this.t=t;
        this.qs=q;
    }
}
public class QuestionnaireFragment extends Fragment {
    FirebaseDatabase rootNode;
    DatabaseReference ref;
    int Qnum=1;
    public TextView question;
    Button next;
    Button back;
    EditText input;
    String statchoice;
    TextView qNum;
    LinearLayout buttons;


    ArrayList<Question> allQuestions = new ArrayList<>();
    int clicked =0;

    int ind=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        View v = inflater.inflate(R.layout.fragment_question, container, false);
        question = v.findViewById(R.id.qText);
        next = v.findViewById(R.id.SubmitB);
        back = v.findViewById(R.id.BackB);
        input = v.findViewById(R.id.in);
        buttons = v.findViewById(R.id.ButtonStorage);
        qNum = v.findViewById(R.id.qNum);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userNow;
        if (user != null) {
            userNow = user.getUid();
        } else {
            userNow = "";
        }

        statusGetter();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.removeAllViews();
                rootNode = FirebaseDatabase.getInstance();
                ref = rootNode.getReference("users");
                Question x = allQuestions.get(Qnum);

                String response = input.getText().toString();
                if(x.c == null) {
                    if(Qnum==1) {
                        x = allQuestions.get(Qnum-1);
                        ref.child(userNow).child(x.id).setValue(response);
                    }
                    ref.child(userNow).child(x.id).setValue(response);


                }
                int c = allQuestions.size() - 1;
                Qnum++;
                qNum.setText(String.valueOf(Qnum));
                if(Qnum<=c) {


                     Question x2 = allQuestions.get(Qnum);


                    ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            input.setVisibility(View.VISIBLE);

                            showQ(Qnum);

                            if(task.isSuccessful() && task.getResult().exists() && x2.c == null) {
                                input.setText(String.valueOf(task.getResult().getValue()));
                            }else {
                                input.setText("Your Answer");
                            }




                        }


                    });

                }else {
                    question.setText("Thanks!!!");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttons.removeAllViews();


                if (Qnum>0){

                    Qnum--;
                    qNum.setText(String.valueOf(Qnum));
                    rootNode = FirebaseDatabase.getInstance();
                    ref = rootNode.getReference("users");
                    Question x;
                    x = allQuestions.get(Qnum);

                    if(Qnum==1) {
                        x = allQuestions.get(Qnum-1);
                    }
                    String user = "Hamed";

                    ref.child(user).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            input.setVisibility(View.VISIBLE);
                            Question x;
                            x = allQuestions.get(Qnum);

                            if(Qnum==1) {
                                x = allQuestions.get(Qnum-1);
                            }

                            if(task.isSuccessful() && task.getResult().exists() && x.c == null) {
                                input.setText(String.valueOf(task.getResult().getValue()));
                            }else {
                                input.setText("Your Answer");
                            }
                            showQ(Qnum);





                        }


                    });



                }

            }
        });

        return v;
    }
    private void showQ(int index) {
        buttons.removeAllViews();

        int c = allQuestions.size() - 1;

        if(index<=c) {

            Question x = allQuestions.get(index-1);
            question.setText(x.qs);

            if (x.c != null) {
                Button choiceButton;
                input.setVisibility(View.GONE);
                for (int i = 0; i < x.c.size(); i++) {
                    choiceButton = new Button(getContext());
                    choiceButton.setText(x.c.get(i));
                    buttons.addView(choiceButton);
                    int finalI = i;
                    choiceButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String current = x.c.get(finalI);
                            rootNode = FirebaseDatabase.getInstance();
                            ref = rootNode.getReference("users");
                            String user = "Hamed";
                            String response = current;
                            ref.child(user).child(x.id).setValue(response);



                        }
                    });


                }

            }

        }
    }

    private void statusGetter() {
        question.setText("Which best describes your situation?");
        String[] choicesS = {"Still in a Relationship", "Planning to Leave", "Post-Separation"};

        buttons.removeAllViews();
        qNum.setText(String.valueOf(Qnum));

        for(int i=0; i<3; i++) {

            Button choiceButton;
            String current = choicesS[i];


            choiceButton = new Button(getContext());
            choiceButton.setText(current);
            buttons.addView(choiceButton);
            choiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    statchoice = current;
                    buttons.removeAllViews();
                    readJson();



                }
            });




        }
    }
    private void readJson() {

        try{

            //load
            InputStream ins = requireContext().getAssets().open("questions.json");
            int s = ins.available();
            byte[] buffer = new byte[s];
            ins.read(buffer);
            ins.close();



            //read

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);

            JSONObject branches = root.getJSONObject("Branch");
            JSONArray arr = branches.getJSONArray("Common Warm-Up Questions");
            int max = arr.length();



            for(int i=1; i<max; i++) {
                JSONObject jsonObject = arr.getJSONObject(i);
                String questionContent = jsonObject.getString("Question");
                String typeofq = jsonObject.getString("Type");
                String qid = jsonObject.getString("id");




                ArrayList<String> cs= new ArrayList<>();

                if (jsonObject.has("Choices")) {
                    JSONArray choices = jsonObject.getJSONArray("Choices");
                    for(int j=0; j< choices.length(); j++) {
                        cs.add(choices.getString(j));
                    }
                    allQuestions.add(new Question(questionContent, typeofq, cs,qid));
                } else{
                    allQuestions.add(new Question(questionContent, typeofq,qid));
                }

            }
                arr = branches.getJSONArray(statchoice);

                max = arr.length();


                for (int i = 0; i < max; i++) {
                    JSONObject jsonObject = arr.getJSONObject(i);
                    String questionContent = jsonObject.getString("Question");
                    String qid = jsonObject.getString("id");
                    String typeofq = jsonObject.getString("Type");


                    ArrayList<String> cs = new ArrayList<>();

                    if (jsonObject.has("Choices")) {
                        JSONArray choices = jsonObject.getJSONArray("Choices");
                        for (int j = 0; j < choices.length(); j++) {
                            cs.add(choices.getString(j));
                        }
                        allQuestions.add(new Question(questionContent, typeofq, cs,qid));
                    } else {
                        allQuestions.add(new Question(questionContent, typeofq,qid));
                    }

                }


            question.setText(allQuestions.get(0).qs);
            showQ(0);

        }catch (Exception e) {

            Log.e("TAG", "error");
        }

    }


}
