package com.example.b07demosummer2024;

import java.util.List;

/**
 * This class represents individual items in the recycler view
 */
public class PlanItem {
    private String question;
    private List<String> answers;

    public PlanItem(String question, List<String> answers){
        this.question = question;
        this.answers = answers;
    }

    // getters
    public String getQuestion(){
        return question;
    }

    public List<String> getAnswers(){
        return answers;
    }
}