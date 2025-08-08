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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class EditItemFragment extends Fragment {
    private EditText editTextTitle, editTextDate, editTextDescription;
    private EditText editTextGovId, editTextCourtOrders;
    private EditText editTextName, editTextRelationship, editTextPhone;
    private EditText editTextAddress, editTextNote;
    private EditText editTextMedName, editTextDosage;
    private ImageView imageViewAddImage, imageViewAddPdf;
    private ImageView imageViewAddImageDisplay;
    private TextView textViewPdfNameDisplay;
    private Button buttonSave;
    private FirebaseDatabase db;
    private DatabaseReference itemsRef;
    private StorageReference reference;
    private Uri imageUri, pdfUri;
    private Item currentItem;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_item, container, false);

        initializeViews(view);
        setupFirebase();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("item_to_edit")) {
            currentItem = (Item) bundle.getSerializable("item_to_edit");
            isEditMode = true;
            populateFieldsForEditing(currentItem);
        } else {
            isEditMode = false;
        }

        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
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

        buttonSave = view.findViewById(R.id.buttonSave);

    }
    private void setupFirebase() {
        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");
        reference = FirebaseStorage.getInstance().getReference();
    }
    private void populateFieldsForEditing(Item item) {
        editTextTitle.setText(item.getTitle());
        editTextDescription.setText(item.getDescription());
        editTextDate.setText(item.getDate());

        // Populate category-specific fields
        String category = item.getCategory();
        if (category != null) {
            switch (category.toLowerCase()) {
                case "document":
                    editTextGovId.setText(item.getGovId());
                    editTextCourtOrders.setText(item.getCourtOrder());
                    break;
                case "emergency contact":
                    editTextName.setText(item.getName());
                    editTextRelationship.setText(item.getRelationship());
                    editTextPhone.setText(item.getPhone());
                    break;
                case "safe location":
                    editTextAddress.setText(item.getAddress());
                    editTextNote.setText(item.getNotes());
                    break;
                case "medication":
                    editTextMedName.setText(item.getMedName());
                    editTextDosage.setText(item.getDosage());
                    break;
            }
        }

        displaySavedImage(item);

        String fileName = item.getPdfName();
        textViewPdfNameDisplay.setText(fileName); // Show selected file name
        textViewPdfNameDisplay.setVisibility(View.VISIBLE);

        onCategoryChanged(category);

        // Update button text for edit mode
        buttonSave.setText("Update Item");
    }


    private void setupClickListeners() {
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
                fileIntent.setType("application/pdf");
                startActivityForResult(fileIntent, 3);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateItem();
                } else {
                    saveItem();
                }
            }
        });
    }
    private void saveItem() {
        // Get text from EditText fields
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

        // Validation
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(getActivity(), LoginActivityView.class));
            if (getActivity() != null) getActivity().finish();
            return;
        }

        String userId = currentUser.getUid();

        Item item;
        String category;
        String itemId;

        if (isEditMode && currentItem != null) {
            item = currentItem;
            category = item.getCategory();
            itemId = item.getId();

            item.setTitle(title);
            item.setDescription(description);
            item.setDate(date);
        } else {
            Bundle bundle = getArguments();
            if (bundle != null && bundle.containsKey("category")) {
                category = bundle.getString("category");
            } else {
                Toast.makeText(getContext(), "Category not specified", Toast.LENGTH_SHORT).show();
                return;
            }

            itemsRef = db.getReference("users/" + userId + "/categories/" + category);
            itemId = itemsRef.push().getKey(); // Generate new ID only for new items
            item = new Item(itemId, title, description, date, category);
        }

        // Set the database reference for the category
        itemsRef = db.getReference("users/" + userId + "/categories/" + category);

        // Set category-specific fields based on category
        switch (category.toLowerCase()) {
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
    }

    private void updateItem() {
        if (currentItem == null) return;

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

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the current item
        currentItem.setTitle(title);
        currentItem.setDescription(description);
        currentItem.setDate(date);

        String category = currentItem.getCategory();
        setCategorySpecificFields(currentItem, category, govId, courtOrder, name, relationship, phone, address, note, medName, dosage);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(getActivity(), LoginActivityView.class));
            if (getActivity() != null) getActivity().finish();
            return;
        }

        String userId = currentUser.getUid();
        itemsRef = db.getReference("users/" + userId + "/categories/" + category);

        // Handle media uploads for update
        handleMediaUploads(currentItem, userId);
    }

    private void setCategorySpecificFields(Item item, String category, String govId, String courtOrder,
                                           String name, String relationship, String phone, String address,
                                           String note, String medName, String dosage) {
        switch (category.toLowerCase()) {
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
    }

    private void handleMediaUploads(Item item, String userId) {
        if (imageUri != null && pdfUri != null) {
            addBoth(imageUri, pdfUri, item, userId);
        } else if (imageUri != null) {
            addMedia(imageUri, item, true, userId);
        } else if (pdfUri != null) {
            addMedia(pdfUri, item, false, userId);
        } else {
            // No new media, just save the item
            itemsRef.child(item.getId()).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String message = isEditMode ? "Item updated" : "Item added";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    clearFields();

                    // Navigate back after successful save/update
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } else {
                    String message = isEditMode ? "Failed to update item" : "Failed to add item";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageViewAddImageDisplay.setImageURI(imageUri); //this is what changes the image in the app
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

    private void addMedia(Uri uri, Item item, boolean isImage, String userId){
        final StorageReference fileRef = reference.child(userId + "/" + System.currentTimeMillis() + "." + getFileExtension(uri));
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
                                clearFields();

                                // Navigate back after successful save/update
                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
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

    private void addBoth(Uri imageUri, Uri pdfUri, Item item, String userId) {
        // First upload image
        final StorageReference imageRef = reference.child(userId + "/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
        imageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageRef.getDownloadUrl().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        // Set image URL
                        item.setImageUrl(task2.getResult().toString());
                        // Now upload PDF
                        final StorageReference pdfRef = reference.child(userId + "/" + System.currentTimeMillis() + "." + getFileExtension(pdfUri));
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
                                                clearFields();
                                                if (getActivity() != null) {
                                                    getActivity().onBackPressed();
                                                }
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

    private void clearFields() {
        editTextTitle.setText("");
        editTextDescription.setText("");
        editTextDate.setText("");
        editTextGovId.setText("");
        editTextCourtOrders.setText("");
        editTextName.setText("");
        editTextRelationship.setText("");
        editTextPhone.setText("");
        editTextAddress.setText("");
        editTextNote.setText("");
        editTextMedName.setText("");
        editTextDosage.setText("");

        imageViewAddImageDisplay.setVisibility(View.GONE);
        textViewPdfNameDisplay.setVisibility(View.GONE);

        imageUri = null;
        pdfUri = null;
    }

    private void displaySavedImage(Item item) {
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache for better performance
                    .into(imageViewAddImageDisplay);

            imageViewAddImageDisplay.setVisibility(View.VISIBLE);
        } else {
            imageViewAddImageDisplay.setVisibility(View.GONE);
        }
    }

}
