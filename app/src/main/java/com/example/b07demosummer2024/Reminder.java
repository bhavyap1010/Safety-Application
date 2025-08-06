package com.example.b07demosummer2024;

public class Reminder {
    private String id;
    private String title;
    private String frequency;
    private String time;
    private boolean isActive;
    private long createdAt;

    public Reminder() {
    }

    public Reminder(String id, String title, String frequency, String time) {
        this.id = id;
        this.title = title;
        this.frequency = frequency;
        this.time = time;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
