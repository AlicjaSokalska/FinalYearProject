package com.example.testsample;

// StepEntry.java
public class StepEntry {
    private long timestamp;
    private int stepCount;

    public StepEntry(long timestamp, int stepCount) {
        this.timestamp = timestamp;
        this.stepCount = stepCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStepCount() {
        return stepCount;
    }
}
