package com.example.b07demosummer2024;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.AdapterView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AddItemFragment extends Fragment {

    //documents
    private EditText editTextTitle, editTextDate, editTextDescription;
    private EditText editTextGovId, editTextCourtOrders;
    private EditText editTextName, editTextRelationship, editTextPhone;
    private EditText editTextAddress, editTextNote;
    private EditText editTextMedName, editTextDosage;
    private ImageView imageViewAddImage, imageViewAddPdf;
    private ImageView imageViewAddImageDisplay;
    private TextView textViewPdfNameDisplay;
    private Spinner spinnerCategory;
    private Button buttonAdd;
    private FirebaseDatabase db;
    private DatabaseReference itemsRef;
    private StorageReference reference;
    private Uri imageUri, pdfUri;


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

        imageViewAddImage = view.findViewById(R.id.imageViewAddImage);
        imageViewAddPdf = view.findViewById(R.id.imageViewAddPdf);

        imageViewAddImageDisplay = view.findViewById(R.id.imageViewAddImageDisplay);
        textViewPdfNameDisplay = view.findViewById(R.id.textViewPdfNameDisplay);

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        buttonAdd = view.findViewById(R.id.buttonAdd);

        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");
        reference = FirebaseStorage.getInstance().getReference();

        // Set up the spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 2);
            }
        });

        imageViewAddPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileIntent = new Intent();
                fileIntent.setAction(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("application/pdf"); // Any file type
                startActivityForResult(fileIntent, 3);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getSelectedItem().toString().toLowerCase();
                onCategoryChanged(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });
        return view;
    }
    private void onCategoryChanged(String selectedCategory){
        switch (selectedCategory){
            case "document":
                editTextGovId.setVisibility(View.VISIBLE);
                editTextCourtOrders.setVisibility(View.VISIBLE);

                editTextName.setVisibility(View.GONE);
                editTextRelationship.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);

                editTextAddress.setVisibility(View.GONE);
                editTextNote.setVisibility(View.GONE);

                editTextMedName.setVisibility(View.GONE);
                editTextDosage.setVisibility(View.GONE);

                break;
            case "emergency contact":
                editTextGovId.setVisibility(View.GONE);
                editTextCourtOrders.setVisibility(View.GONE);

                editTextName.setVisibility(View.VISIBLE);
                editTextRelationship.setVisibility(View.VISIBLE);
                editTextPhone.setVisibility(View.VISIBLE);

                editTextAddress.setVisibility(View.GONE);
                editTextNote.setVisibility(View.GONE);

                editTextMedName.setVisibility(View.GONE);
                editTextDosage.setVisibility(View.GONE);

                break;
            case "safe location":
                editTextGovId.setVisibility(View.GONE);
                editTextCourtOrders.setVisibility(View.GONE);

                editTextName.setVisibility(View.GONE);
                editTextRelationship.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);

                editTextAddress.setVisibility(View.VISIBLE);
                editTextNote.setVisibility(View.VISIBLE);

                editTextMedName.setVisibility(View.GONE);
                editTextDosage.setVisibility(View.GONE);

                break;
            case "medication":
                editTextGovId.setVisibility(View.GONE);
                editTextCourtOrders.setVisibility(View.GONE);

                editTextName.setVisibility(View.GONE);
                editTextRelationship.setVisibility(View.GONE);
                editTextPhone.setVisibility(View.GONE);

                editTextAddress.setVisibility(View.GONE);
                editTextNote.setVisibility(View.GONE);

                editTextMedName.setVisibility(View.VISIBLE);
                editTextDosage.setVisibility(View.VISIBLE);
                break;
        }
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

        if (imageUri != null && pdfUri != null){
            addBoth(imageUri, pdfUri, item);
        } else if (imageUri != null) {
            addMedia(imageUri, item, true);
        } else if (pdfUri != null) {
            addMedia(pdfUri, item, false);
        } else {
            itemsRef.child(id).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Item added", Toast.LENGTH_SHORT).show();
                    setTextNull();
                } else {
                    Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageViewAddImageDisplay.setImageURI(imageUri); //this is what change the image in the app
            imageViewAddImageDisplay.setVisibility(View.VISIBLE);
        }

        else if (requestCode == 3 && resultCode == RESULT_OK && data != null){
//            File selection
            pdfUri = data.getData();

            // Get file name to display
            String fileName = getFileName(pdfUri);
            textViewPdfNameDisplay.setText(fileName); // Show selected file name
            textViewPdfNameDisplay.setVisibility(View.VISIBLE);
        }
    }

    private void addMedia(Uri uri, Item item, boolean isImage){
        final StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileRef.getDownloadUrl().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        String downloadUrl = task2.getResult().toString();

                        if (isImage) {
                            item.setImageUrl(downloadUrl);
                        } else {
                            item.setFileUrl(downloadUrl);
                        }

                        itemsRef.child(item.getId()).setValue(item).addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful()) {
                                String mediaType = isImage ? "image" : "file";
                                Toast.makeText(getContext(), "Item added with " + mediaType, Toast.LENGTH_SHORT).show();
                                setTextNull();
                            } else {
                                Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        Toast.makeText(getContext(), "Failed to add media link", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to upload media", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBoth(Uri imageUri, Uri pdfUri, Item item) {
        // First upload image
        final StorageReference imageRef = reference.child(System.currentTimeMillis() + "_image." + getFileExtension(imageUri));
        imageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageRef.getDownloadUrl().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        // Set image URL
                        item.setImageUrl(task2.getResult().toString());

                        // Now upload PDF
                        final StorageReference pdfRef = reference.child(System.currentTimeMillis() + "_pdf." + getFileExtension(pdfUri));
                        pdfRef.putFile(pdfUri).addOnCompleteListener(task3 -> {
                            if (task3.isSuccessful()) {
                                pdfRef.getDownloadUrl().addOnCompleteListener(task4 -> {
                                    if (task4.isSuccessful()) {
                                        // Set PDF URL
                                        item.setFileUrl(task4.getResult().toString());

                                        // Save item with both URLs
                                        itemsRef.child(item.getId()).setValue(item).addOnCompleteListener(task5 -> {
                                            if (task5.isSuccessful()) {
                                                Toast.makeText(getContext(), "Item added with image and PDF", Toast.LENGTH_SHORT).show();
                                                setTextNull();
                                            } else {
                                                Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getContext(), "Failed to get PDF download URL", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Failed to upload PDF", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to get image download URL", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){

        ContentResolver cr = requireActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {  // Check if column exists
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void setTextNull(){
        editTextTitle.setText(null);
        editTextDate.setText(null);
        editTextDescription.setText(null);

        editTextGovId.setText(null);
        editTextCourtOrders.setText(null);

        editTextName.setText(null);
        editTextRelationship.setText(null);
        editTextPhone.setText(null);

        editTextAddress.setText(null);
        editTextNote.setText(null);

        editTextMedName.setText(null);
        editTextDosage.setText(null);

        imageUri = null;
        imageViewAddImageDisplay.setVisibility(View.GONE); //need to change this now

        pdfUri = null;
        textViewPdfNameDisplay.setVisibility(View.GONE);
    }
}