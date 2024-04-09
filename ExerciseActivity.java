package com.example.testsample;


import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class ExerciseActivity extends AppCompatActivity implements SensorEventListener {

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String selectedPetName;

    private SensorManager sensorManager;
    private Sensor accelerometer;


    private int stepCount = 0;
    private double distance = 0.0;
    private double speed = 0.0;
    private long lastUpdateTime = 0;

    private TextView distanceTextView;
    private TextView speedTextView;
    private TextView stepCountTextView;
    private DatabaseReference dailyTotalRef;
    private TextView heartRateTextView;

    private Switch trackingSwitch;
    private boolean isTracking = false;





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            TextView tv_selectedPet = findViewById(R.id.tv_selectedPet);
            tv_selectedPet.setText("Selected Pet: " + selectedPetName);
            tv_selectedPet.setVisibility(View.VISIBLE);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        dailyTotalRef = usersRef.child("pets").child(selectedPetName).child("daily_totals");

        distanceTextView = findViewById(R.id.distanceTextView);
        speedTextView = findViewById(R.id.speedTextView);
        stepCountTextView = findViewById(R.id.stepCountTextView);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);




        trackingSwitch = findViewById(R.id.trackingSwitch);
        trackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTracking = isChecked;
            if (isChecked) {
                startTracking();
            } else {
                stopTracking();
            }
        });

        isTracking = trackingSwitch.isChecked();
        updateTrackingUI();
        fetchAndDisplayExerciseData();
    }





    private void startTracking() {

        Log.d("ExerciseActivity", "Pet activity is being tracked");

    }

    private void stopTracking() {
        Log.d("ExerciseActivity", "Daily total saved");
        saveDailyTotal(stepCount, distance);

        stepCount = 0;
        distance = 0.0;

        saveExerciseData(selectedPetName, stepCount, distance);

        Log.d("ExerciseActivity", "Data resetted");
    }

    private void updateTrackingUI() {
        if (isTracking) {
            Log.d("ExerciseActivity", "Pet activity is being tracked");
        } else {
            Log.d("ExerciseActivity", "Activity is not tracked");
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        fetchAndDisplayExerciseData();


    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastUpdateTime > 500) { // Update every 500 milliseconds
                lastUpdateTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                if (acceleration > 2.0) {
                    stepCount++;
                    updateStepCount();

                    distance += 0.76;
                    updateDistance();

                    // Speed = Distance / Time
                    speed = distance / ((currentTime - lastUpdateTime) / 1000.0);
                    updateSpeed();

                    saveExerciseData(selectedPetName, stepCount, distance);


                }}


            Calendar currentCalendar = Calendar.getInstance();
            if (currentCalendar.get(Calendar.HOUR_OF_DAY) == 11 && currentCalendar.get(Calendar.MINUTE) == 59) {
                saveDailyTotal(stepCount, distance);
            }


            Calendar midnightCalendar = Calendar.getInstance();
            if (midnightCalendar.get(Calendar.HOUR_OF_DAY) == 0 && midnightCalendar.get(Calendar.MINUTE) == 0) {

                stepCount = 0;
                distance = 0.0;
                saveExerciseData(selectedPetName, stepCount, distance);
            }
        }
    }









    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }

    private void fetchAndDisplayExerciseData() {
        if (selectedPetName != null) {
            DatabaseReference exerciseRef = usersRef.child("pets").child(selectedPetName).child("current_exercise_data");

            exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Get exercise data
                        stepCount = snapshot.child("stepCount").getValue(Integer.class);
                        distance = snapshot.child("distance").getValue(Double.class);

                        // Update UI with fetched data
                        updateStepCount();
                        updateDistance();
                        updateSpeed();
                    } else {
                        // If data doesn't exist, set counts to zero
                        stepCount = 0;
                        distance = 0.0;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void saveDailyTotal(int stepCount, double distance) {
        Log.d("ExerciseActivity", "Saving daily total exercise data");

        // Format the date as "yyyyMMdd"
        String formattedDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        DatabaseReference dailyTotalDataRef = dailyTotalRef.child(formattedDate);

        dailyTotalDataRef.child("stepCount").setValue(stepCount);
        dailyTotalDataRef.child("distance").setValue(distance);


        // Reset counts for the next day
        stepCount = 0;
        distance = 0.0;


        Log.d("ExerciseActivity", "Daily total exercise data saved successfully.");
    }

    private void saveExerciseData(String petName, int stepCount, double distance) {
        Log.d("ExerciseActivity", "Saving exercise data for pet: " + petName);

        if (petName != null) {
            DatabaseReference exerciseRef = usersRef.child("pets").child(petName).child("current_exercise_data");

            exerciseRef.child("stepCount").setValue(stepCount);
            exerciseRef.child("distance").setValue(distance);


            Log.d("ExerciseActivity", "Exercise data saved successfully.");
        } else {
            Log.e("ExerciseActivity", "Pet name is null. Unable to save exercise data.");
        }
    }

    private void updateStepCount() {
        runOnUiThread(() -> stepCountTextView.setText("Step Count: " + stepCount));
    }

    private void updateDistance() {
        runOnUiThread(() -> distanceTextView.setText("Distance: " + String.format("%.2f meters", distance)));
    }

    private void updateSpeed() {
        runOnUiThread(() -> speedTextView.setText("Speed: " + String.format("%.2f m/s", speed)));
    }




}
