package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;public class PastExercise extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ListView listView;
    private List<DailyTotal> dailyTotalsList;

    private String selectedPetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_exercise);

        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
        }


        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        listView = findViewById(R.id.listView);
        dailyTotalsList = new ArrayList<>();

        fetchAndDisplayExerciseData();
    }

    private void fetchAndDisplayExerciseData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && selectedPetName != null) {
            String currentUserId = currentUser.getUid();

            DatabaseReference userRef = databaseReference.child(currentUserId)
                    .child("pets").child(selectedPetName).child("daily_totals");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dailyTotalsList.clear();

                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String date = dateSnapshot.getKey(); // Assuming date is the key
                        double distance = dateSnapshot.child("distance").getValue(Double.class);
                        int stepCount = dateSnapshot.child("stepCount").getValue(Integer.class);

                        DailyTotal dailyTotal = new DailyTotal(formatDate(date), distance, stepCount);
                        dailyTotalsList.add(dailyTotal);
                    }


                    updateListView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ExerciseData", "Error fetching exercise data", error.toException());
                }
            });
        }
    }

    private void updateListView() {
        DailyTotalAdapter adapter = new DailyTotalAdapter(this, dailyTotalsList);
        listView.setAdapter(adapter);
    }


    private String formatDate(String date) {

        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
    }
}
