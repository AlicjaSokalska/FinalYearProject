package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DisplayPetProfile extends AppCompatActivity {
    private Pet selectedPet; // Updated to store the Pet object
    private Toolbar toolbar;
    private DatabaseReference userPetsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pet_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        TextView currentDateTextView = findViewById(R.id.currentDateTextView);
        TextView currentTimeTextView = findViewById(R.id.currentTimeTextView);

// Get current date and time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        String formattedDate = dateFormat.format(currentDate);
        String formattedTime = timeFormat.format(currentDate);

        currentDateTextView.setText(formattedDate);
        currentTimeTextView.setText(formattedTime);


        // Get pet details
        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet");

        if (selectedPet == null) {
            Log.e("DisplayPetProfile", "Selected pet is null");
            finish();
            return;
        }


        TextView petDetailsTextView = findViewById(R.id.petDetailsTextView);

        ImageView petImageView = findViewById(R.id.petImageView);
        TextView activityTextView2 = findViewById(R.id.activityTextView2);
        TextView weightTextView2 = findViewById(R.id.weightTextView2);
        RelativeLayout activityLayout = findViewById(R.id.layout2);
        RelativeLayout weightLayout = findViewById(R.id.layout3);

        StringBuilder petDetailsBuilder = new StringBuilder();
        petDetailsBuilder.append("Name: ").append(selectedPet.getName()).append("\n");
        petDetailsBuilder.append("Date of Birth: ").append(selectedPet.getDob()).append("\n");
        petDetailsBuilder.append("Breed: ").append(selectedPet.getBreed()).append("\n");
        petDetailsBuilder.append("Description: ").append(selectedPet.getDescription()).append("\n");


        PetLocation location = selectedPet.getLocation();
        if (location != null) {
            petDetailsBuilder.append("Address: ");
            String address = location.getAddress();
            if (address != null && !address.isEmpty()) {
                petDetailsBuilder.append(address);
            } else {
                petDetailsBuilder.append("Not available");
            }
        } else {
            petDetailsBuilder.append("Location: Not available");
        }

        activityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToViewPetExerciseActivity();
            }
        });
        weightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPetWeightDataActivity();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference exerciseDataRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("current_exercise_data");
            DatabaseReference weightRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health");
            DatabaseReference catHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("catHealthTarget");

            DatabaseReference dogHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("dogHealthTarget");
            exerciseDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("stepCount").exists()) { // Check if stepCount exists under health
                            int stepCount = snapshot.child("stepCount").getValue(Integer.class); // Retrieve step count
                            activityTextView2.setText("Step Count: " + stepCount);

                            ProgressBar progressBar = findViewById(R.id.stepsProgressBar);
                            TextView stepsTargetTextView = findViewById(R.id.stepsTargetTextView);

                            if (selectedPet.getType().equals("Cat")) {
                                // Hide stepsProgressBar if the pet is a cat

                                stepsTargetTextView.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                            } else {
                                // Show stepsProgressBar for dogs
                                progressBar.setVisibility(View.GONE);
                                progressBar.setProgress(stepCount);
                            }

                            dogHealthTargetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        int activityGoal = dataSnapshot.child("activityGoal").getValue(Integer.class);

                                        // Update ProgressBar
                                        progressBar.setMax(activityGoal);
                                        // Set the current progress to the step count
                                        TextView stepCountTextView = findViewById(R.id.stepTargetTextView);
                                        stepCountTextView.setText(stepCount + " / " + activityGoal);
                                    } else {
                                        Log.e("DisplayPetProfile", "No activity goal data available");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("DisplayPetProfile", "Firebase dog health target data error: " + error.getMessage());
                                }
                            });

                        } else {
                            activityTextView2.setText("No exercise data available");
                        }
                    } else {
                        activityTextView2.setText("No health data available");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DisplayPetProfile", "Firebase exercise data error: " + error.getMessage());
                }
            });

// Fetch and display pet weight
            weightRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot weightSnapshot = snapshot.child("weight");
                        if (weightSnapshot.exists()) {
                            Long petWeight = weightSnapshot.getValue(Long.class);
                            if (petWeight != null) {
                                long weightValue = petWeight.longValue();
                                Log.d("PetWeight", "Weight value retrieved: " + weightValue);
                                weightTextView2.setText("Pet Weight: " + weightValue + " lbs");
                                Query startWeightQuery = FirebaseDatabase.getInstance().getReference()
                                        .child("users")
                                        .child(userId)
                                        .child("pets")
                                        .child(selectedPet.getName())
                                        .child("health")
                                        .child("weightData")
                                        .orderByChild("timeStamp")
                                        .limitToFirst(1);

                                // Convert Query to DatabaseReference
                                DatabaseReference startWeightRef = startWeightQuery.getRef();


                                // Log the reference being used for start weight
                                Log.d("StartWeightRef", "Start weight reference: " + startWeightRef.toString());

                                startWeightRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot weightDataSnapshot : dataSnapshot.getChildren()) {
                                                long startWeight = weightDataSnapshot.child("weight").getValue(Long.class);
                                                Log.d("StartWeight", "Start weight retrieved: " + startWeight);

                                                // Calculate the difference between current weight and target weight
                                                long weightDifference = weightValue - startWeight;

                                                // Update ProgressBar for weight progress
                                                ProgressBar weightProgressBar = findViewById(R.id.weightProgressBar);
                                                weightProgressBar.setMax((int) Math.abs(weightDifference));

                                                if (weightDifference > 0) { // Pet is underweight
                                                    weightProgressBar.setProgress(0); // Start of the progress bar is the start weight
                                                } else if (weightDifference < 0) { // Pet is overweight
                                                    weightProgressBar.setProgress((int) Math.abs(weightDifference)); // End of the progress bar is the current weight
                                                } else { // Pet is at the target weight
                                                    weightProgressBar.setProgress(0);
                                                }

                                                // Display weight value / start weight
                                                TextView weightTextView = findViewById(R.id.weightTargetTextView2);
                                                weightTextView.setText(weightValue + " / " + startWeight + " lbs");
                                                break; // Assuming we only need the earliest weight entry
                                            }
                                        } else {
                                            Log.e("DisplayPetProfile", "No start weight data available");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("DisplayPetProfile", "Firebase start weight data error: " + error.getMessage());
                                    }
                                });

                            } else {
                                Log.d("PetWeight", "Weight data is null");
                                weightTextView2.setText("Pet Weight: Not available");
                            }
                        } else {
                            Log.d("PetWeight", "Weight snapshot does not exist");
                            weightTextView2.setText("Pet Weight: Not available");
                        }
                    } else {
                        weightTextView2.setText("No weight data available");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DisplayPetProfile", "Firebase weight data error: " + error.getMessage());
                }
            });
        }


            TextView weightTextView = findViewById(R.id.weightTextView);
        weightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPetWeightDataActivity();
            }
        });

        petDetailsTextView.setText(petDetailsBuilder.toString());
        petDetailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUpdatePetActivity();
            }
        });


        selectedPet.loadPetImage(petImageView, this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_pet);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    startActivity(new Intent(DisplayPetProfile.this, StartUpPage.class));
                    return true;
                } else if (item.getItemId() == R.id.navigation_viewLocation) {
                    navigateToViewPetLocation();
                    return true;
                } else if (item.getItemId() == R.id.navigation_viewActivity) {
                    navigateToHealthActivity();
                    return true;
                } else {
                    return false;
                }
            }
        });

}

    private void navigateToViewPetLocation() {
        Intent intent = new Intent(this, ViewPetLocation.class);
        intent.putExtra("selectedPet", selectedPet); // Assuming selectedPet is the object representing the selected pet
        startActivity(intent);
    }


    private void navigateToViewPetExerciseActivity() {
        Intent intent = new Intent(this, ViewPetExercise.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pet_profile_menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_set_target) {
            navigateToTargetActivity();
            return true;
        } else if (itemId == R.id.action_add_intake) {
            navigateToIntakeActivity();
            return true;



        }


        return super.onOptionsItemSelected(item);
    }






    private void navigateToTargetActivity() {
        Intent intent = new Intent(this, SetTargets.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }


    private void navigateToUpdatePetActivity() {
        Intent intent = new Intent(this, UpdatePet.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    private void navigateToIntakeActivity() {
        Intent intent = new Intent(this, AddIntakeActivity.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }

    private void navigateToPetWeightDataActivity() {
        Intent intent = new Intent(this, PetWeightData.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }


    private void navigateToHealthActivity() {
        Intent intent = new Intent(this, PetHealth.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }


}





