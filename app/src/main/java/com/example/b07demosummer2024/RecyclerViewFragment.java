package com.example.b07demosummer2024;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private Spinner spinnerCategory;
    private FirebaseDatabase db;
    private DatabaseReference itemsRef;
    private ValueEventListener currentListener;
    private FloatingActionButton buttonAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonAdd = view.findViewById(R.id.buttonAdd);

        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);

//         Set the edit click listener
        itemAdapter.setOnItemEditClickListener(new ItemAdapter.OnItemEditClickListener() {
            @Override
            public void onEditClick(Item item, int position) {
                navigateToEditFragment(item);
            }
        });

        // Set delete listener
        itemAdapter.setOnItemDeleteClickListener(new ItemAdapter.OnItemDeleteClickListener() {
            @Override
            public void onDeleteClick(Item item, int position) {
                deleteItemFromFirebase(item, position);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new AddItemFragment() );
            }
         });

        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString().toLowerCase();
                fetchItemsFromDatabase(category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return view;
    }
    private void fetchItemsFromDatabase(String category) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            return;
        }
        String userId = currentUser.getUid();

        if (itemAdapter != null) {
            itemAdapter.setCurrentCategory(category); // Ensure category is set before data binding with new items
        }

        itemsRef = db.getReference("users/" + userId + "/categories/" + category);
        currentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());
            }
        };
        itemsRef.addValueEventListener(currentListener);
    }
    // Simpler approach - replace your navigateToEditFragment method with this:
    private void navigateToEditFragment(Item item) {
        if (item == null) {
            Log.e("Navigation", "Cannot navigate: item is null");
            Toast.makeText(getContext(), "Error: Invalid item", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getParentFragmentManager() == null) {
            Log.e("Navigation", "Cannot navigate: FragmentManager is null");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("item_to_edit", item);
        bundle.putString("category", item.getCategory());

        EditItemFragment editFragment = new EditItemFragment();
        editFragment.setArguments(bundle);

        // Use the same pattern as your working code snippet
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment) // Make sure this ID matches your actual container
                .addToBackStack(null)
                .commit();
    }
    private void deleteItemFromFirebase(Item item, int position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in to delete items", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String category = item.getCategory().toLowerCase();

        DatabaseReference itemRef = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/")
                .getReference("users/" + userId + "/categories/" + category + "/" + item.getId());

        itemRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Use adapter method
                itemAdapter.removeItem(item.getId());
                Toast.makeText(getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
