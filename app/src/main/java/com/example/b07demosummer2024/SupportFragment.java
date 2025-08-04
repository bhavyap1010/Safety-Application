package com.example.b07demosummer2024;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class SupportFragment extends Fragment {
    private TextView cityText; // main title

    private TextView victimTextTitle, hotlineTextTitle, shelterTextTitle, legalTextTitle, policeTextTitle; // headings/titles
    private TextView victimText, hotlineText, shelterText, legalText, policeText; // main info

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseDatabase db;
        DatabaseReference cityRef;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        cityText = view.findViewById(R.id.cityText);

        victimTextTitle = view.findViewById(R.id.victimTextViewTitle);
        hotlineTextTitle = view.findViewById(R.id.hotlineTextViewTitle);
        shelterTextTitle = view.findViewById(R.id.shelterTextViewTitle);
        legalTextTitle = view.findViewById(R.id.legalAidTextViewTitle);
        policeTextTitle = view.findViewById(R.id.policeTextViewTitle);

        victimText = view.findViewById(R.id.victimTextView);
        hotlineText = view.findViewById(R.id.hotlineTextView);
        shelterText = view.findViewById(R.id.shelterTextView);
        legalText = view.findViewById(R.id.legalAidTextView);
        policeText = view.findViewById(R.id.policeTextView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // if the user is not logged in, then redirect to login
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in. Redirecting to login page", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivityView.class));
            requireActivity().finish();
            return view;
        }

        String uid = currentUser.getUid();
        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");
        cityRef = db.getReference("users").child(uid).child("wu_02");

        cityRef.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    String city = snapshot.getValue(String.class);
                    if (city != null && !city.trim().isEmpty()) {
                        String finalDisplay = "Support Resources for " + city;
                        cityText.setText(finalDisplay);
                        displayCityResources(city);
                        return;
                    }
                }
                showFillOutMessage(); // implies the user has not filled out the questionnaire yet
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SupportFragment", "Firebase realtime DB error: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load city", Toast.LENGTH_SHORT).show();
            }
        } );

        return view;
    }

    public String loadJSON(Context context){
        try {
            InputStream is = context.getAssets().open("city_resources.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            if (bytesRead != size) {
                Log.w("SupportFragment", "Required:  " + size + " bytes, but succesfully read only " + bytesRead + " bytes");
            }
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            // e.printStackTrace();
            // instead of e.printStackTrace(), print out a more detailed log
            Log.e("SupportFragment", "Error reading JSON file", e);
            return null;
        }
    }

    public void displayCityResources(String city){
        try{
            String data = loadJSON(requireContext());
            JSONObject JSONData = new JSONObject(data);
            JSONArray cities = JSONData.getJSONArray("cities");

            // loop through the cities to find the required one
            for (int i = 0; i < cities.length(); i++){
                JSONObject curr = cities.getJSONObject(i);

                if (curr.getString("name").equals(city)){
                    // first get all the JSON objects (resources) related to that city
                    JSONObject victim = curr.getJSONObject("victim_services");
                    JSONObject hotline = curr.getJSONObject("hotlines");
                    JSONObject shelter = curr.getJSONObject("shelters");
                    JSONObject legal = curr.getJSONObject("legal_aid");
                    JSONObject police = curr.getJSONObject("police");

                    // create full strings that contain all the resources related to that city
                    String victimFinalText = "Name: " + victim.getString("name") + "\n"
                                            + "Phone: " + victim.getString("phone") + "\n"
                                            + "Address: " + victim.getString("address") + "\n"
                                            + "Website: " + victim.getString("website") + "\n";

                    String hotlineFinalText = "Name: " + hotline.getString("name") + "\n"
                            + "Phone: " + hotline.getString("phone") + "\n"
                            + "Address: " + hotline.getString("address") + "\n"
                            + "Website: " + hotline.getString("website") + "\n";

                    String shelterFinalText = "Name: " + shelter.getString("name") + "\n"
                            + "Phone: " + shelter.getString("phone") + "\n"
                            + "Address: " + shelter.getString("address") + "\n"
                            + "Website: " + shelter.getString("website") + "\n";

                    String legalFinalText = "Name: " + legal.getString("name") + "\n"
                            + "Phone: " + legal.getString("phone") + "\n"
                            + "Address: " + legal.getString("address") + "\n"
                            + "Website: " + legal.getString("website") + "\n";

                    String policeFinalText = "Name: " + police.getString("name") + "\n"
                            + "Phone: " + police.getString("phone") + "\n"
                            + "Address: " + police.getString("address") + "\n"
                            + "Website: " + police.getString("website") + "\n";

                    // update the text in fragment_support.xml
                    victimText.setText(victimFinalText);
                    hotlineText.setText(hotlineFinalText);
                    shelterText.setText(shelterFinalText);
                    legalText.setText(legalFinalText);
                    policeText.setText(policeFinalText);

                    return; // safe to return because we already found the city, and then set the appropriate texts
                }
            }

            // if we reach at this point, it means that city was not found
            Toast.makeText(getContext(), "No data found for city: " + city, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.e("SupportFragment", "Load info error: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to load resources info", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFillOutMessage() {
        String message = "Please fill out the questionnaire to get city specific resources";
        cityText.setText(message);

        // Clear all the title and resource text views
        victimTextTitle.setText("");
        hotlineTextTitle.setText("");
        shelterTextTitle.setText("");
        legalTextTitle.setText("");
        policeTextTitle.setText("");

        victimText.setText("");
        hotlineText.setText("");
        shelterText.setText("");
        legalText.setText("");
        policeText.setText("");
    }

}