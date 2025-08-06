package com.example.b07demosummer2024;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    private final Context context;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ReminderScheduler(Context context) {
        this.context = context;
    }

    public void scheduleReminder(Reminder reminder) {
        try {
            // Parse the time
            Date reminderTime = timeFormat.parse(reminder.getTime());
            if (reminderTime == null) return;

            // Calculate the initial delay
            Calendar now = Calendar.getInstance();
            Calendar scheduledTime = Calendar.getInstance();
            scheduledTime.setTime(reminderTime);

            // Set the scheduled time for today
            scheduledTime.set(Calendar.YEAR, now.get(Calendar.YEAR));
            scheduledTime.set(Calendar.MONTH, now.get(Calendar.MONTH));
            scheduledTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // If the time has already passed today, schedule for tomorrow
            if (now.after(scheduledTime)) {
                scheduledTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Calculate the delay in minutes
            long delayInMinutes = (scheduledTime.getTimeInMillis() - now.getTimeInMillis()) / (60 * 1000);

            // Create the work request with minimal data
            Data inputData = new Data.Builder()
                .putInt("notificationId", reminder.getId().hashCode())
                .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
                .setInputData(inputData)
                .build();

            // Enqueue the work
            WorkManager.getInstance(context).enqueue(workRequest);

            // For recurring reminders, schedule the next occurrence based on frequency
            if (!reminder.getFrequency().equals("once")) {
                scheduleNextRecurrence(reminder, scheduledTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void scheduleNextRecurrence(Reminder reminder, Calendar lastScheduled) {
        Calendar nextSchedule = (Calendar) lastScheduled.clone();

        switch (reminder.getFrequency().toLowerCase()) {
            case "daily":
                nextSchedule.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "weekly":
                nextSchedule.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                nextSchedule.add(Calendar.MONTH, 1);
                break;
            default:
                return;
        }

        long delayInMinutes = (nextSchedule.getTimeInMillis() - System.currentTimeMillis()) / (60 * 1000);

        Data inputData = new Data.Builder()
            .putInt("notificationId", reminder.getId().hashCode())
            .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
