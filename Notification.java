package com.example.testsample;

public class Notification {
    public String petName;
    public String description;
    public String tag;
    public String date;
    public String status;
    public String urgency;

    public Notification(String petName, String description, String tag, String date, String status, String urgency) {
        this.petName = petName;
        this.description = description;
        this.tag = tag;
        this.date = date;
        this.status = status;
        this.urgency = urgency;
    }

    public Notification() {

    }


}
