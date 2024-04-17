package com.example.testsample;
public class Reminder {
    private String petName;
    private String tag;
    private String description;
    private String date;
    private boolean status;
    private String reminderId;
    private String repeatOption;

    // Default constructor (required by Firebase)
    public Reminder() {
    }

    public Reminder(String petName, String tag, String description, String date, boolean status, String reminderId, String repeatOption) {
        this.petName = petName;
        this.tag = tag;
        this.description = description;
        this.date = date;
        this.status = status;
        this.reminderId = reminderId;
        this.repeatOption = repeatOption;
    }

    // Getters and setters for Firebase to properly map data
    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getRepeatOption() {
        return repeatOption;
    }

    public void setRepeatOption(String repeatOption) {
        this.repeatOption = repeatOption;
    }
}
