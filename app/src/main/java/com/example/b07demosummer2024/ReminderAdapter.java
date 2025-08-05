package com.example.b07demosummer2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnReminderEditListener {
        void onEdit(Reminder reminder);
    }

    public interface OnReminderDeleteListener {
        void onDelete(Reminder reminder);
    }

    private List<Reminder> reminderList;
    private OnReminderEditListener editListener;
    private OnReminderDeleteListener deleteListener;

    public ReminderAdapter(List<Reminder> reminderList, OnReminderEditListener editListener, OnReminderDeleteListener deleteListener) {
        this.reminderList = reminderList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.textViewTitle.setText(reminder.getTitle());
        holder.textViewFrequency.setText(reminder.getFrequency().substring(0, 1).toUpperCase() +
                                       reminder.getFrequency().substring(1));
        holder.textViewTime.setText(reminder.getTime());

        holder.buttonEdit.setOnClickListener(v -> editListener.onEdit(reminder));
        holder.buttonDelete.setOnClickListener(v -> deleteListener.onDelete(reminder));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewFrequency;
        TextView textViewTime;
        Button buttonEdit;
        Button buttonDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewReminderTitle);
            textViewFrequency = itemView.findViewById(R.id.textViewReminderFrequency);
            textViewTime = itemView.findViewById(R.id.textViewReminderTime);
            buttonEdit = itemView.findViewById(R.id.buttonEditReminder);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteReminder);
        }
    }
}
