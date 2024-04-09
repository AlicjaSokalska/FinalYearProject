package com.example.testsample;


public class ExerciseData {
    private String date;
    private double distance;
    private int stepCount;


    public ExerciseData(String date, double distance, int stepCount) {
        this.date = date;
        this.distance = distance;
        this.stepCount = stepCount;

    }

    public String getDate() {
        return date;
    }

    public double getDistance() {
        return distance;
    }

    public int getStepCount() {
        return stepCount;
    }


}
