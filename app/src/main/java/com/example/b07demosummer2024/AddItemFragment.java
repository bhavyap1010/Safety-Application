package com.example.b07demosummer2024;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddItemFragment extends Fragment {

    //documents
    private EditText editTextTitle, editTextDate, editTextDescription;
    private EditText editTextGovId, editTextCourtOrders;
    private EditText editTextName, editTextRelationship, editTextPhone;
    private EditText editTextAddress, editTextNote;
    private EditText editTextMedName, editTextDosage;
    private Spinner spinnerCategory;
    private Button buttonAdd;
    private FirebaseDatabase db;
    private DatabaseReference itemsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextDescription = view.findViewById(R.id.editTextDescription);

        editTextGovId = view.findViewById(R.id.editTextGovId);
        editTextCourtOrders = view.findViewById(R.id.editTextCourtOrders);

        editTextName = view.findViewById(R.id.editTextName);
        editTextRelationship = view.findViewById(R.id.editTextRelationship);
        editTextPhone = view.findViewById(R.id.editTextPhone);

        editTextAddress = view.findViewById(R.id.editTextAddress);
        editTextNote = view.findViewById(R.id.editTextNote);

        editTextMedName = view.findViewById(R.id.editTextMedName);
        editTextDosage = view.findViewById(R.id.editTextDosage);

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        buttonAdd = view.findViewById(R.id.buttonAdd);

        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");

        // Set up the spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        return view;
    }

    private void addItem() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();

        String govId = editTextGovId.getText().toString().trim();
        String courtOrder = editTextCourtOrders.getText().toString().trim();

        String name = editTextName.getText().toString().trim();
        String relationship = editTextRelationship.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        String address = editTextAddress.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();

        String medName = editTextMedName.getText().toString().trim();
        String dosage = editTextDosage.getText().toString().trim();

        String category = spinnerCategory.getSelectedItem().toString().toLowerCase();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        itemsRef = db.getReference("categories/" + category);
        String id = itemsRef.push().getKey();
        Item item = new Item(id, title, description, date);

        switch (category){
            case "document":
                item.setGovId(govId);
                item.setCourtOrder(courtOrder);
                break;
            case "emergency contact":
                item.setName(name);
                item.setRelationship(relationship);
                item.setPhone(phone);
                break;
            case "safe location":
                item.setAddress(address);
                item.setNotes(note);
                break;
            case "medication":
                item.setMedName(medName);
                item.setDosage(dosage);
                break;
        }

        itemsRef.child(id).setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
