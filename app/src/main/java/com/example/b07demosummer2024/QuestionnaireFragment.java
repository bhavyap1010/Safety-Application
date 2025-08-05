package com.example.b07demosummer2024;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
    String f;
    String fid;
    ArrayList<String> c;

    boolean answered = false;

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

    public Question(String q, String t, ArrayList<String> c, String id, String f, String fid) {
        this.t=t;
        this.id=id;
        this.qs=q;
        this.c=c;
        this.f=f;
        this.fid=fid;
    }
    public Question(String q, String t, String id) {
        this.id=id;

        this.t=t;
        this.qs=q;
    }

    public Question(String q, String t, String id, String f, String fid) {
        this.id=id;
        this.fid=fid;
        this.t=t;
        this.qs=q;
        this.f=f;
    }

    public String getFid() {
        return fid;
    }
}
public class QuestionnaireFragment extends Fragment {
    FirebaseDatabase rootNode;
    View v;
    DatabaseReference ref;
    int Qnum=0;
    public TextView question;
    Button next;
    Button back;
    EditText input;
    boolean clicked=false;
    String statchoice;
    TextView qNum;
    TextView followUpQ;
    EditText followUpA;
    LinearLayout buttons;


    ArrayList<Question> allQuestions = new ArrayList<>();

    int ind=0;
    public String userNow;
    CheckBox physical;
    CheckBox other;
    CheckBox emotional;
    CheckBox financial;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        v = inflater.inflate(R.layout.fragment_question, container, false);
        question = v.findViewById(R.id.qText);
        next = v.findViewById(R.id.SubmitB);
        back = v.findViewById(R.id.BackB);
        input = v.findViewById(R.id.in);
        buttons = v.findViewById(R.id.ButtonStorage);
        qNum = v.findViewById(R.id.qNum);
        followUpQ= v.findViewById(R.id.followUpQ);
        followUpA= v.findViewById(R.id.followUpA);
        physical = v.findViewById(R.id.Physical);
        financial= v.findViewById(R.id.Financial);
        emotional = v.findViewById(R.id.Emotional);
        other = v.findViewById(R.id.Other);

        physical.setVisibility(View.GONE);
        financial.setVisibility(View.GONE);
        other.setVisibility(View.GONE);
        emotional.setVisibility(View.GONE);



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userNow = user.getUid();
        } else {
            userNow = "";
        }

        statusGetter();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rootNode = FirebaseDatabase.getInstance();
                ref = rootNode.getReference("users");
                Question x = allQuestions.get(Qnum);

                String response = input.getText().toString();

                ArrayList<String> abuse = new ArrayList<>();

                if(x.id.equals("sr_01")) {

                    if(physical.isChecked()) {
                        abuse.add(physical.getText().toString());
                    }
                    if(emotional.isChecked()) {
                        abuse.add(emotional.getText().toString());
                    }
                    if(financial.isChecked()) {
                        abuse.add(financial.getText().toString());
                    }
                    if(other.isChecked()) {
                        abuse.add(other.getText().toString());
                    }



                    ref.child(userNow).child(x.id).setValue(abuse);
                    if(abuse.isEmpty()) {
                        x.answered = false;

                    }else {
                        x.answered = true;

                    }
                    /*if(other.isChecked()) {
                        ref.child(userNow).child(x.id).child("sr_01fu").setValue(followUpA.getText().toString());
                    }*/


                }


                if(x.c == null && !x.id.equals("wu_02")) {

                    ref.child(userNow).child(x.id).setValue(response);
                    if(response.equals("")) {
                        x.answered = false;
                    }else {
                        x.answered = true;
                    }

                }
                if(x.f!=null) {
                    String followResponse = followUpA.getText().toString();
                    ref.child(userNow).child(x.fid).setValue(followResponse);
                    final String[] y = new String[1];
                    ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {

                            if (task.isSuccessful() && task.getResult().exists()) {
                                y[0] = String.valueOf(task.getResult().getValue());
                            } else {
                                y[0] ="em";
                            }

                            if(followResponse.equals("") && y[0].equals("Yes")) {
                                x.answered = false;
                            }else {
                                x.answered = true;
                            }
                        }
                    });


                }

                final boolean[] check = {false};
                ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {

                        if (task.isSuccessful() && task.getResult().exists()) {
                            if (!(String.valueOf(task.getResult().getValue()).equals(""))) {
                                check[0] = true;
                            }
                        }


                        if(check[0]==true || x.id.equals("wu_02")) {
                            buttons.removeAllViews();

                            physical.setVisibility(View.GONE);
                            financial.setVisibility(View.GONE);
                            other.setVisibility(View.GONE);
                            emotional.setVisibility(View.GONE);
                            int c = allQuestions.size() - 1;
                            Qnum++;
                            qNum.setText(String.valueOf(Qnum+1));

                            if (Qnum <= c) {


                                Question x2 = allQuestions.get(Qnum);


                                ref.child(userNow).child(x2.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        input.setVisibility(View.VISIBLE);
                                        showQ(Qnum);
                                        if (task.isSuccessful() && task.getResult().exists() && x2.c == null) {
                                            input.setText(String.valueOf(task.getResult().getValue()));
                                        } else {
                                            input.setText("");
                                        }
                                    }
                                });

                                if (x2.fid != null) {
                                    ref.child(userNow).child(x2.fid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (task.isSuccessful() && task.getResult().exists()) {
                                                followUpA.setText(String.valueOf(task.getResult().getValue()));
                                            } else {
                                                followUpA.setText("");
                                            }
                                        }
                                    });
                                }

                            } else {
                                question.setText("Thanks!!!");
                                ((MainActivity) requireActivity()).showPlanFragment();

                            }
                        }else {
                            //do nothing
                        }
                    }
                });
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttons.removeAllViews();
                physical.setVisibility(View.GONE);
                financial.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
                emotional.setVisibility(View.GONE);

                rootNode = FirebaseDatabase.getInstance();
                ref = rootNode.getReference("users");
                Question x = allQuestions.get(Qnum);

                String response = input.getText().toString();

                ArrayList<String> abuse = new ArrayList<>();

                if(x.id.equals("sr_01")) {

                    if(physical.isChecked()) {
                        abuse.add(physical.getText().toString());
                    }
                    if(emotional.isChecked()) {
                        abuse.add(emotional.getText().toString());
                    }
                    if(financial.isChecked()) {
                        abuse.add(financial.getText().toString());
                    }
                    if(other.isChecked()) {
                        abuse.add(other.getText().toString());
                    }



                    ref.child(userNow).child(x.id).setValue(abuse);
                    /*if(other.isChecked()) {
                        ref.child(userNow).child(x.id).child("sr_01fu").setValue(followUpA.getText().toString());
                    }*/


                }
                physical.setVisibility(View.GONE);
                financial.setVisibility(View.GONE);
                other.setVisibility(View.GONE);
                emotional.setVisibility(View.GONE);

                if(x.c == null && !x.id.equals("wu_02")) {


                    ref.child(userNow).child(x.id).setValue(response);


                }
                if(x.f!=null) {
                    String followResponse = followUpA.getText().toString();
                    ref.child(userNow).child(x.fid).setValue(followResponse);

                }


                if (Qnum>0){

                    Qnum--;
                    qNum.setText(String.valueOf(Qnum+1));
                    rootNode = FirebaseDatabase.getInstance();
                    ref = rootNode.getReference("users");
                    x = allQuestions.get(Qnum);



                    ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            input.setVisibility(View.VISIBLE);
                            Question x;
                            x = allQuestions.get(Qnum);
                            if(task.isSuccessful() && task.getResult().exists() && x.c == null) {
                                input.setText(String.valueOf(task.getResult().getValue()));
                            }else {
                                input.setText("");
                            }
                            showQ(Qnum);
                        }
                    });

                    if(x.fid!=null) {
                        ref.child(userNow).child(x.fid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful() && task.getResult().exists()) {
                                    followUpA.setText(String.valueOf(task.getResult().getValue()));
                                } else {
                                    followUpA.setText("");
                                }
                            }
                        });
                    }



                }

                if(Qnum==0) {
                    statusGetter();
                }

            }
        });

        return v;
    }
    private void showQ(int index) {
        Spinner spinner = v.findViewById(R.id.cityChoices);

        buttons.removeAllViews();
        followUpA.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        qNum.setVisibility(View.VISIBLE);


        followUpQ.setVisibility(View.GONE);
        int c = allQuestions.size() - 1;

        if(index<=c) {

            Question x = allQuestions.get(index);
            question.setText(x.qs);

            if (x.c != null && !x.id.equals("sr_01")) {
                Button choiceButton;
                input.setVisibility(View.GONE);
                for (int i = 0; i < x.c.size(); i++) {
                    choiceButton = new Button(getContext());
                    choiceButton.setText(x.c.get(i));
                    buttons.addView(choiceButton);
                    int finalI = i;
                    String current = x.c.get(finalI);
                    ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String yorn;
                            if(task.isSuccessful() && task.getResult().exists()) {
                                yorn = String.valueOf(task.getResult().getValue());
                            }else {
                                yorn="eempty";
                            }

                            if(x.t.equals("Select One + free form") && yorn.equals("Yes")) {

                                ref.child(userNow).child(x.fid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful() && task.getResult().exists()) {

                                            followUpA.setText(String.valueOf(task.getResult().getValue()));
                                        } else {
                                            followUpA.setText("");
                                        }
                                    }
                                });
                                followUpQ.setText(x.f);
                                followUpA.setVisibility(View.VISIBLE);
                                followUpQ.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    choiceButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            x.answered = true;

                            if(x.t.equals("Select One + free form") && current.equals("Yes")) {

                                ref.child(userNow).child(x.fid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful() && task.getResult().exists()) {

                                            followUpA.setText(String.valueOf(task.getResult().getValue()));
                                        } else {
                                            followUpA.setText("");
                                        }
                                    }
                                });
                                followUpQ.setText(x.f);
                                followUpA.setVisibility(View.VISIBLE);
                                followUpQ.setVisibility(View.VISIBLE);
                            }else {
                                followUpQ.setVisibility(View.GONE);
                                followUpA.setVisibility(View.GONE);


                            }



                            String current = x.c.get(finalI);
                            rootNode = FirebaseDatabase.getInstance();
                            ref = rootNode.getReference("users");
                            String response = current;
                            ref.child(userNow).child(x.id).setValue(response);
                            x.answered = true;

                            if(x.t.equals("Select One + free form") && response.equals("Yes")) {
                                followUpQ.setText(x.f);
                                followUpA.setVisibility(View.VISIBLE);
                                followUpQ.setVisibility(View.VISIBLE);



                            }

                            if(response.equals("No")) {
                                followUpQ.setVisibility(View.GONE);
                                followUpA.setVisibility(View.GONE);


                            }



                        }
                    });


                }

            }

            if(x.id.equals("sr_01")) {
                physical.setVisibility(View.VISIBLE);
                financial.setVisibility(View.VISIBLE);
                other.setVisibility(View.VISIBLE);
                emotional.setVisibility(View.VISIBLE);
                input.setVisibility(View.GONE);


                ref.child(userNow).child(x.id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        ArrayList<String> ress=null;
                        if(task.isSuccessful() && task.getResult().exists()) {
                            ress= (ArrayList<String>) task.getResult().getValue();
                            if(!ress.isEmpty()) {

                                ArrayList<CheckBox> boxes = new ArrayList<>();
                                boxes.add(physical);
                                boxes.add(emotional);
                                boxes.add(financial);
                                boxes.add(other);

                                for(String i: ress) {

                                    for(CheckBox j: boxes) {

                                        if (j.getText().toString().equals(i)) {
                                            j.setChecked(true);
                                        }

                                    }

                                }

                            }
                        }else {
                            input.setText("");
                        }



                    }
                });



                /*
                if(other.isChecked()) {
                    followUpQ.setVisibility(View.VISIBLE);
                    followUpA.setVisibility(View.VISIBLE);
                    ref.child(userNow).child(x.id).child("sr_01fu").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists()) {

                                followUpA.setText(String.valueOf(task.getResult().getValue()));
                            } else {
                                input.setText("");
                            }
                        }
                    });
                }*/

                /*
                other.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(other.isChecked()) {
                            followUpQ.setText("");
                            followUpQ.setVisibility(View.VISIBLE);

                            ref.child(userNow).child(x.id).child("sr_01fu").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful() && task.getResult().exists()) {

                                        followUpA.setText(String.valueOf(task.getResult().getValue()));
                                    } else {
                                        followUpA.setText("");
                                    }
                                }
                            });
                            followUpA.setVisibility(View.VISIBLE);



                        }else {
                            followUpQ.setVisibility(View.GONE);
                            followUpA.setVisibility(View.GONE);

                        }
                    }
                });*/


            }


            if(x.id.equals("wu_02")) {
                input.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);

// Create an ArrayAdapter using the string array and a default spinner layout.
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        getContext(),
                        R.array.cities_array,
                        android.R.layout.simple_spinner_item
                );
// Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String city =adapterView.getItemAtPosition(i).toString();
                        rootNode = FirebaseDatabase.getInstance();
                        ref = rootNode.getReference("users");
                        ref.child(userNow).child(x.id).setValue(city);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });




            }


        }
    }

    private void statusGetter() {
        Spinner spinner = v.findViewById(R.id.cityChoices);

        qNum.setVisibility(View.GONE);
        input.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        followUpQ.setVisibility(View.GONE);
        followUpA.setVisibility(View.GONE);
        buttons.removeAllViews();
        spinner.setVisibility(View.GONE);





        question.setText("Which best describes your situation?");
        String[] choicesS = {"Still in a Relationship", "Planning to Leave", "Post-Separation"};

        buttons.removeAllViews();
        qNum.setText(String.valueOf(Qnum+1));

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
                    qNum.setVisibility(View.VISIBLE);
                    input.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    back.setVisibility(View.VISIBLE);
                    rootNode = FirebaseDatabase.getInstance();
                    ref = rootNode.getReference("users");
                    String response = current;
                    ref.child(userNow).child("wu_01").setValue(response);
                    readJson();



                }
            });




        }
    }
    private void readJson() {

        try{
            allQuestions.clear();

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
                    if(jsonObject.has("follow")) {
                        String f = jsonObject.getString("follow");
                        String fid = jsonObject.getString("fid");
                        allQuestions.add(new Question(questionContent, typeofq, cs, qid, f, fid));
                    }else {
                        allQuestions.add(new Question(questionContent, typeofq, cs, qid));
                    }
                } else {

                    if(jsonObject.has("follow")) {
                        String f = jsonObject.getString("follow");
                        String fid = jsonObject.getString("fid");
                        allQuestions.add(new Question(questionContent, typeofq, qid, f, fid));
                    }else {
                        allQuestions.add(new Question(questionContent, typeofq,qid));
                    }

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
                    if(jsonObject.has("follow")) {
                        String f = jsonObject.getString("follow");
                        String fid = jsonObject.getString("fid");
                        allQuestions.add(new Question(questionContent, typeofq, cs, qid, f, fid));
                    }else {
                        allQuestions.add(new Question(questionContent, typeofq, cs, qid));
                    }
                } else {

                    if(jsonObject.has("follow")) {
                        String f = jsonObject.getString("follow");
                        String fid = jsonObject.getString("fid");
                        allQuestions.add(new Question(questionContent, typeofq, qid, f, fid));
                    }else {
                        allQuestions.add(new Question(questionContent, typeofq,qid));
                    }

                }

            }


            question.setText(allQuestions.get(0).qs);
            showQ(0);

        }catch (Exception e) {

            Log.e("TAG", "error");
        }

    }


}