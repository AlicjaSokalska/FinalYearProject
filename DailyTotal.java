package com.example.testsample;

public class DailyTotal {
    private String date;
    private double distance;
    private int stepCount;


    public DailyTotal() {
    }

    public DailyTotal(String date, double distance, int stepCount) {
        this.date = date;
        this.distance = distance;
        this.stepCount = stepCount;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
