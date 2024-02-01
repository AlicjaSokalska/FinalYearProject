package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ViewPetExercise extends AppCompatActivity {

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String selectedPetName;

    private TextView tvSelectedPet;
    private TextView tvCurrentExercise;
    private TextView tvDailyExercise;
    private TextView tvDateTime;

    private Button btnViewWeeklyExercise,btnViewPastExercise;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet_exercise);

        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            tvSelectedPet = findViewById(R.id.tv_selectedPet);
            tvSelectedPet.setText("Selected Pet: " + selectedPetName);
            tvSelectedPet.setVisibility(View.VISIBLE);
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        tvCurrentExercise = findViewById(R.id.tv_currentExercise);
       // tvDailyExercise = findViewById(R.id.tv_dailyExercise);
        tvDateTime = findViewById(R.id.tvDateTime);

        displayCurrentDateTime();


        fetchAndDisplayExerciseData();
        // fetchAndDisplayDailyTotal();


        btnViewWeeklyExercise = findViewById(R.id.btnViewWeeklyExercise);
        btnViewWeeklyExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ViewPetExercise.this, WeekExercise.class);
                startActivity(intent);
            }
        });


        btnViewPastExercise = findViewById(R.id.btnViewPastExercise);
        btnViewPastExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPetExercise.this, PastExercise.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });

}

    private void fetchAndDisplayDailyTotal() {
        if (selectedPetName != null) {
            String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            DatabaseReference dailyTotalRef = usersRef.child("pets").child(selectedPetName).child("daily_totals").child(currentDate);

            dailyTotalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int stepCount = snapshot.child("stepCount").getValue(Integer.class);
                        double distance = snapshot.child("distance").getValue(Double.class);

                        String dailyTotalInfo = "Daily Total for Yesterday \nStep Count: " + stepCount + "\nDistance: " + distance + " meters";
                        tvDailyExercise.setText(dailyTotalInfo);
                    } else {
                        tvDailyExercise.setText("No data available for yesterday.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void fetchAndDisplayExerciseData() {
        if (selectedPetName != null) {
            DatabaseReference exerciseRef = usersRef.child("pets").child(selectedPetName).child("current_exercise_data");

            exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int stepCount = snapshot.child("stepCount").getValue(Integer.class);
                        double distance = snapshot.child("distance").getValue(Double.class);


                        String exerciseInfo = "Step Count: " + stepCount + "\nDistance: " + distance + " meters";
                        tvCurrentExercise.setText(exerciseInfo);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void displayCurrentDateTime() {
        //
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());


        tvDateTime.setText("Current Date and Time: " + currentDateTime);
    }

}