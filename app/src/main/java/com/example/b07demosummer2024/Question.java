package com.example.b07demosummer2024;

import java.util.Map;

/**
 * This classs represents a single object from the json file
 * Created so that it makes it easy to deal with questions and tips from JSON file
 */
public class Question {
    private String id;
    private String question;
    private Map<String, String> tips;

    // getters
    public String getId() {
        return id;
    }
    public String getQuestion() {
        return question;
    }
    public Map<String, String> getTips() {
        return tips;
    }
}