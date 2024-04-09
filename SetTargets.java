package com.example.testsample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetTargets extends AppCompatActivity {
    private String currentUserUid;
    private DatabaseReference petReference;
    private Pet selectedPet;
    private EditText weightGoalEditText;
    private EditText activityGoalEditText;
    private TextView recommendedStepCountTextView;
    private int recommendedStepCount;

    private Button saveButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_targets);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid();
        }

        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet");

        if (selectedPet == null) {
            Log.e("PetHealth", "Selected pet is null");
            finish();
            return;
        }

        weightGoalEditText = findViewById(R.id.weightGoalEditText);
        activityGoalEditText = findViewById(R.id.activityGoalEditText);
        recommendedStepCountTextView = findViewById(R.id.recommendedStepCountTextView);
        saveButton = findViewById(R.id.saveButton);
        Button saveCatWeightGoalButton = findViewById(R.id.saveCatWeightGoalButton);
Button saveOtherPetWeightGoalButton = findViewById(R.id.saveOtherPetWeightGoalButton);



        if ("Cat".equals(selectedPet.getType())) {

            activityGoalEditText.setEnabled(false);

            recommendedStepCountTextView.setText("Cats generally need around 30 minutes of exercise per day to stay healthy and happy.");

            saveButton.setVisibility(View.GONE);
           saveOtherPetWeightGoalButton.setVisibility(View.GONE);
            saveCatWeightGoalButton.setVisibility(View.VISIBLE);

            saveCatWeightGoalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveCatWeightGoal();
                }
            });
        }
       else  if ("Other".equals(selectedPet.getType())) {

            activityGoalEditText.setEnabled(true);

            recommendedStepCountTextView.setText("No recommendations");

            saveButton.setVisibility(View.GONE);

            saveCatWeightGoalButton.setVisibility(View.GONE);


            saveOtherPetWeightGoalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveOtherPetWeightGoal();
                }
            });
        }
       else if ("Dog".equals(selectedPet.getType())) {

            saveCatWeightGoalButton.setVisibility(View.GONE);
            saveOtherPetWeightGoalButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveButton.setVisibility(View.VISIBLE);
                    saveTargets();
                }
            });

            retrieveHealthDetailsAndCalculateStepCount();
        }
    }

    private void saveOtherPetWeightGoal() {
        String weightGoal = weightGoalEditText.getText().toString();
        String activityGoal = activityGoalEditText.getText().toString();

        DatabaseReference petHealthReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health")
                .child("petHealthTarget")
                .child("weightGoal");

        petHealthReference.setValue(Double.parseDouble(weightGoal));

        DatabaseReference activityGoalReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health")
                .child("petHealthTarget")
                .child("activityGoal");

        activityGoalReference.setValue(Integer.parseInt(activityGoal));
        Toast.makeText(this, "No recommended weight or activity goal for this pet on the system", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Pet weight goal and activity goal saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveCatWeightGoal() {

        String weightGoal = weightGoalEditText.getText().toString();


        DatabaseReference petHealthReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health")
                .child("catHealthTarget")
                .child("weightGoal");


        petHealthReference.setValue(Double.parseDouble(weightGoal));

        String activityGoal = "30";


        DatabaseReference activityGoalReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health")
                .child("catHealthTarget")
                .child("activityDurationGoal");

        activityGoalReference.setValue(Integer.parseInt(activityGoal));


        Toast.makeText(this, "Cat weight goal saved successfully and cat activity goal is set to 30 minutes", Toast.LENGTH_SHORT).show();
    }
    private void retrieveHealthDetailsAndCalculateStepCount() {
        DatabaseReference petHealthReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health");

        petHealthReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double petWeight = dataSnapshot.child("weight").getValue(Double.class);
                    double petIdealWeight = dataSnapshot.child("idealWeight").getValue(Double.class);
                    int ageInMonths = dataSnapshot.child("ageInMonths").getValue(Integer.class);
                    recommendedStepCount = calculateRecommendedStepCount(petWeight, ageInMonths);

                    recommendedStepCountTextView.setText("Pet Weight: " + petWeight + " lb\n"
                            + "Pet Ideal Weight: " + petIdealWeight + " lb\n"
                            + "Recommended Step Count: " + recommendedStepCount);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }
    private void saveTargets() {
        String weightGoal = weightGoalEditText.getText().toString();
        String activityGoal = activityGoalEditText.getText().toString();

        if (weightGoal.isEmpty() || activityGoal.isEmpty()) {
            Toast.makeText(this, "Please enter weight and activity goals", Toast.LENGTH_SHORT).show();
            return;
        }

        double weightGoalValue = Double.parseDouble(weightGoal);

        DatabaseReference petReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health");


        // For cats and dogs, check breed weight range
        petReference.child("breedWeightRange").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double minWeight = dataSnapshot.child("first").getValue(Double.class);
                    double maxWeight = dataSnapshot.child("second").getValue(Double.class);

                    if (weightGoalValue < minWeight || weightGoalValue > maxWeight) {
                        Toast.makeText(SetTargets.this, "Weight goal should be within the breed weight range", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println("Weight Goal: " + weightGoal);
                        System.out.println("Activity Goal: " + activityGoal);
                        compareTargetsWithRecommendation();
                        saveTargetsToFirebase(weightGoal, activityGoal);
                    }
                } else {
                    Toast.makeText(SetTargets.this, "Breed weight range data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }


    private void saveTargetsToFirebase(String weightGoal, String activityGoal) {
        DatabaseReference petHealthReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health");



        if ("Cat".equals(selectedPet.getType())) {
            petHealthReference = petHealthReference.child("catHealthTarget");
            activityGoal = "30";
        } else if ("Dog".equals(selectedPet.getType())) {
            petHealthReference = petHealthReference.child("dogHealthTarget");
        }

        petHealthReference.child("weightGoal").setValue(Double.parseDouble(weightGoal));
        petHealthReference.child("activityGoal").setValue(Integer.parseInt(activityGoal));

        compareTargetsWithRecommendation();
    }

    private void compareTargetsWithRecommendation() {
        int userActivityGoal = Integer.parseInt(activityGoalEditText.getText().toString());

        int lowerLimit = recommendedStepCount - 300;
        int upperLimit = recommendedStepCount + 300;

        if (userActivityGoal < lowerLimit) {
            Toast.makeText(this, "Warning: Activity goal is significantly below recommended level", Toast.LENGTH_SHORT).show();
        } else if (userActivityGoal > upperLimit) {
            Toast.makeText(this, "Warning: Activity goal is significantly above recommended level", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Targets saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateRecommendedStepCount(double petWeight, int petAgeInMonths) {
        return (int) (petWeight * 100);
    }
}
