package com.example.b07demosummer2024;

import android.content.Context;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

/**
 * This class loads the questions_and_tips.json from assets.
 */
public class JSONUtility {
    public static List<Question> loadQuestionTips(Context context) {
        try {
            InputStream is = context.getAssets().open("questions_and_tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonData = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Question[] array = gson.fromJson(jsonData, Question[].class);
            return Arrays.asList(array);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}