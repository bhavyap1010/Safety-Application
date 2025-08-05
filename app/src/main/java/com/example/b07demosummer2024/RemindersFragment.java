package com.example.b07demosummer2024;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RemindersFragment extends Fragment {
    private RecyclerView recyclerViewReminders;
    private Button buttonAddReminder;
    private ReminderAdapter reminderAdapter;
    private List<Reminder> reminderList;

    private FirebaseDatabase db;
    private DatabaseReference remindersRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://b07finalproject-23dae-default-rtdb.firebaseio.com/");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        remindersRef = db.getReference("users/" + currentUser.getUid() + "/reminders");

        recyclerViewReminders = view.findViewById(R.id.recyclerViewReminders);
        buttonAddReminder = view.findViewById(R.id.buttonAddReminder);

        reminderList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(reminderList, this::editReminder, this::deleteReminder);
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReminders.setAdapter(reminderAdapter);

        buttonAddReminder.setOnClickListener(v -> showAddReminderDialog());

        loadReminders();

        return view;
    }

    private void loadReminders() {
        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reminderList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reminder reminder = snapshot.getValue(Reminder.class);
                    if (reminder != null) {
                        reminderList.add(reminder);
                    }
                }
                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load reminders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddReminderDialog() {
        showReminderDialog(null, false);
    }

    private void editReminder(Reminder reminder) {
        showReminderDialog(reminder, true);
    }

    private void showReminderDialog(Reminder existingReminder, boolean isEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_reminder, null);

        EditText editTextTitle = dialogView.findViewById(R.id.editTextReminderTitle);
        Spinner spinnerFrequency = dialogView.findViewById(R.id.spinnerFrequency);
        Button buttonSetTime = dialogView.findViewById(R.id.buttonSetTime);
        Button buttonSave = dialogView.findViewById(R.id.buttonSaveReminder);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancelReminder);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        final String[] selectedTime = {""};

        if (isEdit && existingReminder != null) {
            editTextTitle.setText(existingReminder.getTitle());
            selectedTime[0] = existingReminder.getTime();
            buttonSetTime.setText("Time: " + existingReminder.getTime());

            String[] frequencies = getResources().getStringArray(R.array.frequency_array);
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i].equals(existingReminder.getFrequency())) {
                    spinnerFrequency.setSelection(i);
                    break;
                }
            }
        }

        buttonSetTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    buttonSetTime.setText("Time: " + selectedTime[0]);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTime[0].isEmpty()) {
                Toast.makeText(getContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit && existingReminder != null) {
                updateReminder(existingReminder.getId(), title, frequency, selectedTime[0]);
            } else {
                addReminder(title, frequency, selectedTime[0]);
            }

            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addReminder(String title, String frequency, String time) {
        String id = remindersRef.push().getKey();
        if (id != null) {
            Reminder reminder = new Reminder(id, title, frequency, time);
            remindersRef.child(id).setValue(reminder).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Reminder added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to add reminder", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateReminder(String id, String title, String frequency, String time) {
        Reminder reminder = new Reminder(id, title, frequency, time);
        remindersRef.child(id).setValue(reminder).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Reminder updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReminder(Reminder reminder) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete", (dialog, which) -> {
                remindersRef.child(reminder.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Reminder deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete reminder", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
