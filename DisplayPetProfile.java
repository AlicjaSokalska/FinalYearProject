package com.example.testsample;

import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DisplayPetProfile extends AppCompatActivity {
    private Pet selectedPet; // Updated to store the Pet object
    private Toolbar toolbar;
    private DatabaseReference userPetsReference;
    private FirebaseAuth mAuth;
    private boolean targetVisited = false;
    private boolean weightVisited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pet_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView currentDateTextView = findViewById(R.id.currentDateTextView);
        TextView currentTimeTextView = findViewById(R.id.currentTimeTextView);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String formattedDate = dateFormat.format(currentDate);
        String formattedTime = timeFormat.format(currentDate);
        currentDateTextView.setText(formattedDate);
        currentTimeTextView.setText(formattedTime);
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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            activityLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!selectedPet.getType().equals("Cat") && !selectedPet.getType().equals("Dog")) {
                        Toast.makeText(DisplayPetProfile.this, "Function only available for cats or dogs", Toast.LENGTH_SHORT).show();
                        navigateToOtherPetExerciseActivity();
                    } else {
                        DatabaseReference exerciseDataRef = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(userId)
                                .child("pets")
                                .child(selectedPet.getName())
                                .child("current_exercise_data")
                                .child("stepCount");

                        exerciseDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    navigateToViewPetExerciseActivity();
                                } else {
                                    Toast.makeText(DisplayPetProfile.this, "Step count data not available", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("DisplayPetProfile", "Firebase step count data error: " + error.getMessage());
                            }
                        });
                    }
                }
            });

           weightLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference weightRef = FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(userId)
                            .child("pets")
                            .child(selectedPet.getName())
                            .child("health")
                            .child("weight");

                    weightRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                navigateToPetWeightDataActivity();
                            } else {
                                showActivityDialog();
                                Toast.makeText(DisplayPetProfile.this, "Weight data not available", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DisplayPetProfile", "Firebase weight data error: " + error.getMessage());
                        }
                    });
                }
            });

        }
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference exerciseDataRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("current_exercise_data");
            DatabaseReference weightRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health");
            DatabaseReference catHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("catHealthTarget");

            DatabaseReference dogHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("dogHealthTarget");
            exerciseDataRef.addValueEventListener(new ValueEventListener() {
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

            weightRef.addValueEventListener(new ValueEventListener() {
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
                } else if (item.getItemId() == R.id.navigation_pet_tracker) {
                    navigateToViewPetLocation();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_health) {
                    navigateToHealthActivity();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_profile) {

                    startActivity(new Intent(DisplayPetProfile.this, DisplayPetProfile.class));
                    return true;
                } else {
                    return false;
                }
            }
        });
        displayReminderDate();
    }



    private void showActivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No weight data available");
        builder.setMessage("Do you want to set weight?");
        builder.setPositiveButton("Set Weight", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                navigateToRECActivity();

            }
        });
        builder.show();
    }

    private void displayReminderDate() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId) // Use userId variable
                    .child("pets")
                    .child(selectedPet.getName())
                    .child("reminders");

            remindersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        long currentDateMillis = System.currentTimeMillis();
                        long closestReminderMillis = Long.MAX_VALUE;
                        String closestReminderDate = "";
                        String closestReminderTag = "";

                        // Iterate over each reminder to find the closest one
                        for (DataSnapshot reminderSnapshot : dataSnapshot.getChildren()) {
                            String reminderDate = reminderSnapshot.child("date").getValue(String.class);
                            long reminderMillis = convertDateStringToMillis(reminderDate);

                            // Calculate the difference between the reminder date and current date
                            long differenceMillis = Math.abs(reminderMillis - currentDateMillis);

                            // Update closest reminder if the current reminder is closer to the current date
                            if (differenceMillis < closestReminderMillis) {
                                closestReminderMillis = differenceMillis;
                                closestReminderDate = reminderDate;
                                closestReminderTag = reminderSnapshot.child("tag").getValue(String.class);
                            }
                        }

                        // Display the closest reminder date and tag
                        TextView notificationTextView2 = findViewById(R.id.notificationTextView2);
                        if (!closestReminderDate.isEmpty()) {
                            notificationTextView2.setText(" Date: " + closestReminderDate + "\n Tag: " + closestReminderTag);
                        } else {
                            notificationTextView2.setText("No reminders available");
                        }
                    } else {
                        TextView notificationTextView2 = findViewById(R.id.notificationTextView2);
                        notificationTextView2.setText("No reminders available");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("DisplayPetProfile", "Failed to fetch reminders: " + databaseError.getMessage());
                }
            });
        }
    }
 private long convertDateStringToMillis(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void navigateToViewPetLocation() {
        Intent intent = new Intent(this, ViewPetLocation.class);
        intent.putExtra("selectedPet", selectedPet); // Assuming selectedPet is the object representing the selected pet
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mode_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuReturnToModeSelection) {

            Intent returnToModeIntent = new Intent(DisplayPetProfile.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(DisplayPetProfile.this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void navigateToOtherPetExerciseActivity() {
        Toast.makeText(DisplayPetProfile.this, "Function Not Available", Toast.LENGTH_SHORT).show();

        /*
        Intent intent = new Intent(this, OtherPetExercise.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);*/
    }
    private void navigateToViewPetExerciseActivity() {
        Intent intent = new Intent(this, ViewPetExercise.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    private void navigateToTargetActivity() {
        Intent intent = new Intent(this, SetTargets.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    private void navigateToRECActivity() {
        Intent intent = new Intent(this, PetHealth.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    private void navigateToUpdatePetActivity() {
        Intent intent = new Intent(this, UpdatePet.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }  private void navigateToPetWeightDataActivity() {
        Intent intent = new Intent(this, PetWeightData.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    } private void navigateToOtherPetWeightDataActivity() {
        Intent intent = new Intent(this, OtherPetWeightData.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }
    private void navigateToHealthActivity() {
        if (selectedPet.getType().equals("Cat") || selectedPet.getType().equals("Dog")) {
            Intent intent = new Intent(this, HealthData.class);
            intent.putExtra("selectedPet", selectedPet);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Function is unavailable for this type of pet", Toast.LENGTH_SHORT).show();
        }
    }



}





