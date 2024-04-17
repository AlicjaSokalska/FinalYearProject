package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HealthData extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView currentWeightTextView;
    private TextView statusTextView;
    private TextView ageTextView;
    private TextView breedTextView;
    private TextView lifeStageTextView;
    private TextView activityTextView;
    private TextView idealWeightTextView;
    private TextView rangeTextView;
    private TextView recommendedActivityTextView;
    private TextView foodTextView;
    private TextView weightGoalWeightTextView;
    private TextView activityGoalTextView, weightRangeTextView, weeklyWeightTrendsTextView, predTextView, recommendedTextView;
    private Pair<Double, Double> weightRange;
    private double petWeight;
    private Pet selectedPet;
    private String petName;
    private String petBreed;
    private String petDateOfBirth;
    private String currentUserUid;
    private DatabaseReference petReference;
    private boolean detailsVisibleOne = false;
    private boolean detailsVisibleTwo = false;
    private boolean detailsVisibleThree = false;
    private boolean detailsVisibleFour = false;
    private boolean detailsVisibleFive = false;
    private boolean detailsVisibleSix = false;
    private TextView weeklyTrendsTextView;
    private TextView currentActivityTextView;
    private TextView activityComparisonTextView;
    private ImageButton addTargetsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_data);

        // Initialize TextViews
        currentWeightTextView = findViewById(R.id.currentWeightTextView);
        statusTextView = findViewById(R.id.statusTextView);
        ageTextView = findViewById(R.id.ageTextView);
        breedTextView = findViewById(R.id.breedTextView);
        lifeStageTextView = findViewById(R.id.lifeStageTextView);
        activityTextView = findViewById(R.id.activityTextView);
        idealWeightTextView = findViewById(R.id.idealWeightTextView);
        weightRangeTextView = findViewById(R.id.rangeTextView);
        recommendedActivityTextView = findViewById(R.id.recommendedActivityTextView);
        foodTextView = findViewById(R.id.foodTextView);
        weightGoalWeightTextView = findViewById(R.id.weightGoalWeightTextView);
        activityGoalTextView = findViewById(R.id.activityGoalTextView);
        weeklyTrendsTextView = findViewById(R.id.weeklyTrendsTextView);
        weeklyWeightTrendsTextView = findViewById(R.id.weeklyWeightTrendsTextView);
        recommendedTextView = findViewById(R.id.recommendedTextView);
        predTextView = findViewById(R.id.predTextView);
        addTargetsBtn = findViewById(R.id.addTargetsBtn);
        addTargetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSetTargetsDialog(selectedPet);

            }
        });

        final TextView healthDetailsTextView = findViewById(R.id.healthDetailsTextView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final TextView recommendedDetailsTextView = findViewById(R.id.recommendedDetailsTextView);
        recommendedDetailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibilityTwo();
            }
        });

        final TextView guidelinesTextView = findViewById(R.id.guidelinesTextView);
        guidelinesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibilityThree();
            }
        });
        final TextView targetDetailsTextView = findViewById(R.id.targetDetailsTextView);
        targetDetailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibilityFour();
            }
        });
        final TextView predictionsTextView = findViewById(R.id.predictionsTextView);
        predictionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fetchAndDisplayWeightTrends();
                toggleVisibilityFive();

            }
        });
        final TextView trendsTextView = findViewById(R.id.trendsTextView);
        trendsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVisibilitySix();
            }
        });


        // Fetch pet health data from Firebase or other sources
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid();
        }

        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet");
        if (selectedPet == null) {
            Log.e("HealthData", "Selected pet is null");
            finish();
            return;
        }

        petReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName());

        fetchCurrentWeight();

        String petAge = calculateAgeFromDOB(selectedPet.getDob());
        ageTextView.setText("Age: " + petAge);
        breedTextView.setText("Breed: " + selectedPet.getBreed());


        int ageInMonths = calculateAgeInMonths(selectedPet.getDob());
        String lifeStage = calculateLifeStage(selectedPet.getType(), ageInMonths);
        lifeStageTextView.setText("Life Stage: " + lifeStage);

        displayWeightRange(selectedPet.getType(), selectedPet.getBreed());
        displayIdealWeight(selectedPet.getType(), selectedPet.getBreed(), weightRange);
        DatabaseReference weightStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName()).child("health").child("weightStatus");

        displayWeightStatus(weightStatusRef);

        healthDetailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Cat".equalsIgnoreCase(selectedPet.getType())) {
                    toggleVisibilityPartTWO();
                } else {
                    toggleVisibility();
                }
            }
        });

        fetchCurrentWeight();
        fetchAndDisplayTrends();
        fetchAndDisplayWeightTrends();


        //    displayAdditionalTrendInfo(recommendedStepCount, averageStepCountPerDay);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_pet);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    startActivity(new Intent(HealthData.this, StartUpPage.class));
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_tracker) {
                    navigateToViewPetLocation();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_health) {
                    navigateToHealthActivity();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_profile) {
                    startActivity(new Intent(HealthData.this, DisplayPetProfile.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

    }

    private void showSetTargetsDialog(Pet pet) {
        Intent intent = new Intent(this, SetTargets.class);
        intent.putExtra("selectedPet", pet);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCurrentWeight();
        fetchAndDisplayTrends();
        fetchAndDisplayWeightTrends();

    }

    private void navigateToHealthActivity() {
        Intent intent = new Intent(this, HealthData.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }

    private void navigateToViewPetLocation() {
        Intent intent = new Intent(this, ViewPetLocation.class);
        intent.putExtra("selectedPet", selectedPet); // Assuming selectedPet is the object representing the selected pet
        startActivity(intent);
    }

    private void fetchAndDisplayTrends() {
        DatabaseReference petReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName());

        petReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot dailyTotalsRef = dataSnapshot.child("daily_totals");
                    Calendar calendar = Calendar.getInstance();
                    double totalSteps = 0;
                    int daysWithActivity = 0;

                    // Loop through the last 7 days
                    for (int i = 0; i < 7; i++) {
                        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.getTime());
                        DataSnapshot daySnapshot = dailyTotalsRef.child(date);

                        if (daySnapshot.exists()) {
                            int stepCount = daySnapshot.child("stepCount").getValue(Integer.class);
                            totalSteps += stepCount;
                            daysWithActivity++;
                        }

                        // Move to the previous day
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    // Calculate the weekly average step count
                    int averageStepCountPerDay = daysWithActivity > 0 ? (int) (totalSteps / daysWithActivity) : 0;
                    //double petWeight = selectedPet.getWeight(); // Assuming this method fetches pet weight
                    int ageInMonths = calculateAgeInMonths(selectedPet.getDob()); // Assuming this method calculates age
                    int recommendedStepCount = calculateRecommendedStepCount(petWeight, ageInMonths);


                    displayTrends(averageStepCountPerDay, recommendedStepCount);


                } else {
                    // Handle case where pet data does not exist
                    weeklyTrendsTextView.setText("No data available for trends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
                weeklyTrendsTextView.setText("Error fetching data for trends");
            }
        });
    }

    private void displayTrends(int averageStepCountPerDay, int recommendedStepCount) {
        // Call the original method to display the average step count per day
        weeklyTrendsTextView.setText("Average Step Count Per Day This Week: " + averageStepCountPerDay);

        // Call the new method to display additional trend information
        displayAdditionalTrendInfo(recommendedStepCount, averageStepCountPerDay);
    }

    private void displayAdditionalTrendInfo(int recommendedStepCount, int averageStepCountPerDay) {
        // Compare recommended steps with weekly average
        String comparisonResult = compareStepCounts(recommendedStepCount, averageStepCountPerDay);

        // Update the TextView to display the additional trend info
        String displayText = "Activity Level: " + comparisonResult;
        activityTextView.setText(displayText);
    }

    private String compareStepCounts(int recommendedStepCount, int averageStepCountPerDay) {
        if (averageStepCountPerDay < recommendedStepCount) {
            return "Underactive";
        } else if (averageStepCountPerDay > recommendedStepCount) {
            return "Overactive";
        } else {
            return "Ideal";
        }
    }


    private void fetchAndDisplayWeightTrends() {
        DatabaseReference petReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health")
                .child("weightData");

        petReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double totalWeightChange = 0;
                    int weightDataCount = 0;
                    double prevWeight = 0;
                    for (DataSnapshot weightSnapshot : dataSnapshot.getChildren()) {
                        double weight = weightSnapshot.child("weight").getValue(Double.class);

                        if (prevWeight != 0) {
                            double weightChange = weight - prevWeight;
                            totalWeightChange += weightChange;
                            weightDataCount++;
                        }
                        prevWeight = weight;
                    }
                    double averageWeightChange = weightDataCount > 0 ? totalWeightChange / weightDataCount : 0;

                    DecimalFormat decimalFormat = new DecimalFormat("#.00");
                    String formattedAverageWeightChange = decimalFormat.format(averageWeightChange);

                    // Determine if weight loss or gain
                    String trend;
                    if (averageWeightChange < 0) {
                        trend = "Weight Loss";
                    } else if (averageWeightChange > 0) {
                        trend = "Weight Gain";
                    } else {
                        trend = "No Change";
                    }

                    displayWeightTrends(Double.parseDouble(formattedAverageWeightChange), trend);

                    DatabaseReference targetWeightReference;
                    if (selectedPet.getType().equalsIgnoreCase("Dog")) {
                        targetWeightReference = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserUid)
                                .child("pets")
                                .child(selectedPet.getName())
                                .child("health")
                                .child("dogHealthTarget")
                                .child("weightGoal");
                    } else if (selectedPet.getType().equalsIgnoreCase("Cat")) {
                        targetWeightReference = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserUid)
                                .child("pets")
                                .child(selectedPet.getName())
                                .child("health")
                                .child("catHealthTarget")
                                .child("weightGoal");
                    } else {
                        // Handle other types of pets here
                        return; // Or implement appropriate handling according to your app logic
                    }
                    targetWeightReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                double targetWeight = dataSnapshot.getValue(Double.class);

                                // Call predictTimeToTargetWeight method here
                                predictTimeToTargetWeight(petWeight, targetWeight, averageWeightChange);
                            } else {
                                Toast.makeText(HealthData.this, "Target weight data not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle onCancelled event
                            // You may display an error message or handle this scenario according to your app logic
                        }
                    });

                } else {
                    // Handle case where weight data does not exist
                    weeklyWeightTrendsTextView.setText("No weight data available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
                weeklyWeightTrendsTextView.setText("Error fetching weight data");
            }
        });
    }


    private void displayWeightTrends(double averageWeightChange, String trend) {
        weeklyWeightTrendsTextView.setText("Average Weight Change: " + averageWeightChange + " (" + trend + ")");

    }

    private void predictTimeToTargetWeight(double currentWeight, double targetWeight, double averageWeightChangePerWeek) {
        double rateOfChangePerWeek = averageWeightChangePerWeek; // Assuming averageWeightChangePerWeek is in pounds/week

        // Ensure the rate of change is not zero to avoid division by zero
        if (rateOfChangePerWeek == 0) {
            // Provide a message indicating that weight is not changing
            predTextView.setText("Weight is not changing. Adjust calorie intake and activity.");
            return;
        }
        double maxAllowableLossPerWeek = 0.08 * currentWeight; // Assuming max loss is 1-2%
        double actualRateOfChangePerWeek;
        if (currentWeight < targetWeight) {
            // Pet needs to gain weight, so actual rate of change should be positive
            actualRateOfChangePerWeek = Math.max(rateOfChangePerWeek, maxAllowableLossPerWeek);
        } else {
            // Pet needs to lose weight, so actual rate of change should be negative
            actualRateOfChangePerWeek = Math.min(rateOfChangePerWeek, -maxAllowableLossPerWeek);
        }

        double weeksToTargetWeight = Math.abs((targetWeight - currentWeight) / actualRateOfChangePerWeek);

        double daysToTargetWeight;
        String timeUnit;
        if (weeksToTargetWeight < 1) {
            daysToTargetWeight = weeksToTargetWeight * 7;
            timeUnit = "days";
        } else {
            daysToTargetWeight = weeksToTargetWeight * 7;
            timeUnit = "weeks";
        }

        // Display the prediction and provide recommendations based on the rate of weight change
        String predictionMessage;
        if (actualRateOfChangePerWeek > 0) {
            // Pet is gaining weight
            if (currentWeight < targetWeight) {
                // Pet is underweight, calculate time to reach target weight gain
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight gain: %.1f %s", daysToTargetWeight, timeUnit);
            } else {
                // Pet is overweight, calculate time to lose weight
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight loss: %.1f %s", daysToTargetWeight, timeUnit);
            }
        } else if (actualRateOfChangePerWeek < 0) {
            // Pet is losing weight
            if (currentWeight < targetWeight) {
                // Pet is underweight, calculate time to reach target weight gain
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight gain: %.1f %s", daysToTargetWeight, timeUnit);
            } else {
                // Pet is overweight, calculate time to lose weight
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight loss: %.1f %s", daysToTargetWeight, timeUnit);
            }
        } else {
            // Pet is already at the target weight
            predictionMessage = "Pet has reached target weight. Maintain current calorie intake and activity levels to sustain weight.";
        }

        // Display the prediction message
        predTextView.setText(predictionMessage);

        // Provide recommendations based on the rate of weight change
        if (actualRateOfChangePerWeek < -0.10 * currentWeight) {
            // Weight loss is too rapid, recommend decreasing calorie intake and increasing activity
            recommendedTextView.setText("Recommendation: Pet should not lose more than 10% of body weight per week, as it is dangerous.");
        } else if (actualRateOfChangePerWeek > 0) {
            // Weight gain is too slow, recommend increasing calorie intake and decreasing activity
            recommendedTextView.setText("Recommendation: Consider increasing calorie intake and decreasing activity levels to promote weight gain.");
        } else {
            // If there's no change in weight, it's recommended to maintain the current calorie intake and ensure to achieve the daily step target for weight maintenance.
            recommendedTextView.setText("Recommendation: Maintain current calorie intake and ensure to meet the daily step target to maintain weight.");
        }
    }


        private void displayRecommendedActivity(double petWeight, int ageInMonths) {
        Log.d("DEBUG", "Displaying recommended activity...");

        // Calculate recommended step count here
        Log.d("DEBUG", "Pet Weight: " + petWeight);
        Log.d("DEBUG", "Age in Months: " + ageInMonths);

        // Check the inputs are correct
        int recommendedStepCount = calculateRecommendedStepCount(petWeight, ageInMonths);
        Log.d("DEBUG", "Recommended Step Count: " + recommendedStepCount);

        if (selectedPet == null) {
            Log.e("DEBUG", "Selected pet is null");
            return;
        }

        if (selectedPet.getType().equalsIgnoreCase("Dog")) {
            Log.d("DEBUG", "Setting recommended activity for Dog...");
            recommendedActivityTextView.setText("Recommended Step Count: " + recommendedStepCount);
        } else if (selectedPet.getType().equalsIgnoreCase("Cat")) {
            Log.d("DEBUG", "Setting recommended activity for Cat...");
            recommendedActivityTextView.setText("Recommended Activity : Cats generally need around 30 minutes of exercise per day to stay healthy and happy.");
        } else {
            // For other pet types
            Log.d("DEBUG", "No recommendations for other pet types...");
            recommendedActivityTextView.setText("No recommendations");
        }
    }


    private int calculateRecommendedStepCount(double petWeight, int petAgeInMonths) {
        Log.d("DEBUG", "Calculating recommended step count...");

        // Example formula, adjust as needed
        int recommendedStepsPerPound;
        if (calculateDogLifeStage(petAgeInMonths).equals("Puppy Stage") || calculateDogLifeStage(petAgeInMonths).equals("Senior")) {
            // Adjust recommended steps for puppies or senior dogs
            recommendedStepsPerPound = 100;
        } else {
            recommendedStepsPerPound = 500;
        }

        int recommendedSteps = recommendedStepsPerPound * (int) petWeight;
        Log.d("DEBUG", "Recommended Steps (initial): " + recommendedSteps);

        Log.d("DEBUG", "Final Recommended Steps: " + recommendedSteps);
        return recommendedSteps;
    }


    private void displayIdealWeight(String petType, String breed, Pair<Double, Double> weightRange) {
        String idealWeight = "N/A";
        if (petType != null && breed != null && weightRange != null) {
            Double calculatedIdealWeight = calculateIdealWeight(weightRange);
            if (calculatedIdealWeight != null) {
                idealWeight = String.format(Locale.getDefault(), "%.1f lbs", calculatedIdealWeight);
            }
        }
        idealWeightTextView.setText("Ideal Weight: " + idealWeight);
    }

    private Double calculateIdealWeight(Pair<Double, Double> weightRange) {
        if (weightRange != null) {
            return (weightRange.first + weightRange.second) / 2;
        } else {
            return null; // Return null if weight range is not available
        }
    }

    private void displayWeightStatus(DatabaseReference weightStatusRef) {
        weightStatusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String weightStatus = dataSnapshot.getValue(String.class);
                    if (weightStatus != null) {
                        statusTextView.setText("Weight Status: " + weightStatus);
                    } else {
                        // Handle missing weight status data
                        statusTextView.setText("Weight Status: N/A (Missing weight status)");
                    }
                } else {
                    // Handle missing weight status data
                    statusTextView.setText("Weight Status: N/A (Missing weight status)");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                statusTextView.setText("Weight Status: N/A (Error retrieving data)");
            }
        });
    }


    private void displayWeightRange(String petType, String breed) {
        String weightRangeText = "N/A";
        if (petType != null && breed != null) {
            if (petType.equalsIgnoreCase("dog")) {
                weightRange = getDogBreedWeightRange(breed); // Assign the retrieved range to the instance variable
            } else if (petType.equalsIgnoreCase("cat")) {
                weightRange = getCatBreedWeightRange(breed); // Assign the retrieved range to the instance variable
            }
            if (weightRange != null) {
                weightRangeText = String.format(Locale.getDefault(), "%.1f - %.1f lbs", weightRange.first, weightRange.second);
            }
        }
        weightRangeTextView.setText("Weight Range: " + weightRangeText);
    }


    private Pair<Double, Double> getCatBreedWeightRange(String breed) {

        Map<String, Pair<Double, Double>> breedWeightMap = new HashMap<>();
        breedWeightMap.put("Siamese", new Pair<>(7.0, 10.0));
        breedWeightMap.put("Persian", new Pair<>(7.0, 12.0));
        breedWeightMap.put("Maine Coon", new Pair<>(10.0, 15.0));
        breedWeightMap.put("Himalayan", new Pair<>(7.0, 12.0));


        return breedWeightMap.getOrDefault(breed, new Pair<>(8.0, 10.0));
    }//to do fix to add all breed

    private Pair<Double, Double> getDogBreedWeightRange(String breed) {
        Map<String, Pair<Double, Double>> breedWeightMap = new HashMap<>();
        breedWeightMap.put("Affenpinscher", new Pair<>(7.0, 10.0));
        breedWeightMap.put("Afghan Hound", new Pair<>(50.0, 60.0));
        breedWeightMap.put("Airedale Terrier", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Akitas", new Pair<>(70.0, 130.0));
        breedWeightMap.put("Alaskan Malamute", new Pair<>(71.0, 95.0));
        breedWeightMap.put("American English Coonhound", new Pair<>(40.0, 65.0));
        breedWeightMap.put("American Eskimo Toy", new Pair<>(6.0, 11.0));
        breedWeightMap.put("American Eskimo Miniature", new Pair<>(10.0, 21.0));
        breedWeightMap.put("American Eskimo Standard", new Pair<>(20.0, 40.0));
        breedWeightMap.put("American Foxhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("American Hairless Terrier", new Pair<>(12.0, 16.0));
        breedWeightMap.put("American Staffordshire Terrier", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Anatolian Shepherd", new Pair<>(80.0, 150.0));
        breedWeightMap.put("Australian Cattle Dog", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Australian Shepherd", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Australian Terrier", new Pair<>(15.0, 20.0));
        breedWeightMap.put("Basenji", new Pair<>(20.0, 26.0));
        breedWeightMap.put("Basset Hound", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Beagle Mini", new Pair<>(0.0, 20.0));
        breedWeightMap.put("Beagle", new Pair<>(20.0, 24.0));
        breedWeightMap.put("Bearded Collie", new Pair<>(45.0, 60.0));
        breedWeightMap.put("Beaucerons", new Pair<>(66.0, 110.0));
        breedWeightMap.put("Bedlington Terrier", new Pair<>(17.0, 23.0));
        breedWeightMap.put("Belgian Malinois", new Pair<>(40.0, 80.0));
        breedWeightMap.put("Belgian Sheepdog", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Belgian Tervuren", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Bergamasco", new Pair<>(57.0, 84.0));
        breedWeightMap.put("Berger Picard", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Bernese Mountain Dog", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Bichons Frise", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Black and Tan Coonhound", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Black Russian Terrier", new Pair<>(80.0, 130.0));
        breedWeightMap.put("Bloodhound", new Pair<>(80.0, 110.0));
        breedWeightMap.put("Bluetick Coonhounds", new Pair<>(45.0, 80.0));
        breedWeightMap.put("Boerboel", new Pair<>(150.0, 200.0));
        breedWeightMap.put("Border Collie", new Pair<>(30.0, 45.0));
        breedWeightMap.put("Border Terrier", new Pair<>(11.5, 15.5));
        breedWeightMap.put("Bouviers des Flandres", new Pair<>(60.0, 110.0));
        breedWeightMap.put("Boxer", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Briard", new Pair<>(50.0, 100.0));
        breedWeightMap.put("Brittany", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Brussels Griffons", new Pair<>(8.0, 10.0));
        breedWeightMap.put("Bulldog", new Pair<>(40.0, 50.0));
        breedWeightMap.put("Bullmastiff", new Pair<>(100.0, 130.0));
        breedWeightMap.put("Cairn Terrier", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Canaan Dog", new Pair<>(35.0, 55.0));
        breedWeightMap.put("Cane Corso", new Pair<>(88.0, 110.0));
        breedWeightMap.put("Cardigan Welsh Corgi", new Pair<>(25.0, 38.0));
        breedWeightMap.put("Catahoula Leopard Dog", new Pair<>(50.0, 95.0));
        breedWeightMap.put("Cavalier King Charles Spaniel", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Chesapeake Bay Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Chihuahua", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Chinese Crested", new Pair<>(5.0, 12.0));
        breedWeightMap.put("Chinese Shar-Pei", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Chinook", new Pair<>(55.0, 90.0));
        breedWeightMap.put("Chow Chow", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Clumber Spaniel", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Cockapoo", new Pair<>(6.0, 20.0));
        breedWeightMap.put("Collie", new Pair<>(50.0, 75.0));
        breedWeightMap.put("Coonhounds", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Cardigan Welsh Corgi", new Pair<>(25.0, 38.0));
        breedWeightMap.put("Catahoula Leopard Dog", new Pair<>(50.0, 95.0));
        breedWeightMap.put("Cavalier King Charles Spaniel", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Chesapeake Bay Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Chihuahua", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Chinese Crested", new Pair<>(5.0, 12.0));
        breedWeightMap.put("Chinese Shar-Pei", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Corgi Pembroke Welsh", new Pair<>(25.0, 30.0));
        breedWeightMap.put("Coton de Tulear", new Pair<>(8.0, 15.0));
        breedWeightMap.put("Curly-Coated Retriever", new Pair<>(60.0, 95.0));
        breedWeightMap.put("Dachshund Miniature", new Pair<>(8.0, 11.0));
        breedWeightMap.put("Dachshund Standard", new Pair<>(16.0, 32.0));
        breedWeightMap.put("Dachshund Toy", new Pair<>(6.0, 12.0));
        breedWeightMap.put("Dalmatian", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Dandie Dinmont Terrier", new Pair<>(18.0, 24.0));
        breedWeightMap.put("Doberman Pinscher", new Pair<>(60.0, 100.0));
        breedWeightMap.put("Dogo Argentino", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Dogue de Bordeaux", new Pair<>(99.0, 110.0));
        breedWeightMap.put("Dutch Shepherd", new Pair<>(50.0, 70.0));
        breedWeightMap.put("English Bulldog", new Pair<>(40.0, 50.0));
        breedWeightMap.put("English Cocker Spaniel", new Pair<>(26.0, 34.0));
        breedWeightMap.put("English Foxhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("English Setter", new Pair<>(45.0, 80.0));
        breedWeightMap.put("English Springer Spaniel", new Pair<>(40.0, 50.0));
        breedWeightMap.put("English Toy Spaniel", new Pair<>(8.0, 14.0));
        breedWeightMap.put("Entlebucher Mountain Dog", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Eskimo Toy", new Pair<>(6.0, 10.0));
        breedWeightMap.put("Eskimo Miniature", new Pair<>(10.0, 20.0));
        breedWeightMap.put("Eskimo Standard", new Pair<>(25.0, 35.0));
        breedWeightMap.put("Estrela Mountain Dog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Field Spaniel", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Finnish Lapphund", new Pair<>(33.0, 53.0));
        breedWeightMap.put("Finnish Spitz", new Pair<>(20.0, 33.0));
        breedWeightMap.put("Flat-Coated Retriever", new Pair<>(55.0, 75.0));
        breedWeightMap.put("French Bulldog", new Pair<>(16.0, 28.0));
        breedWeightMap.put("French Spaniel", new Pair<>(55.0, 61.0));
        breedWeightMap.put("Galgo Spanish Greyhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("German Pinscher", new Pair<>(25.0, 45.0));
        breedWeightMap.put("German Shepherd", new Pair<>(50.0, 90.0));
        breedWeightMap.put("German Shorthaired Pointer", new Pair<>(45.0, 70.0));
        breedWeightMap.put("German Wirehaired Pointer", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Giant Schnauzer", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Glen of Imaal Terrier", new Pair<>(32.0, 40.0));
        breedWeightMap.put("Golden Retriever", new Pair<>(55.0, 75.0));
        breedWeightMap.put("Gordon Setter", new Pair<>(45.0, 80.0));
        breedWeightMap.put("Great Dane", new Pair<>(140.0, 175.0));
        breedWeightMap.put("Great Pyrenees", new Pair<>(85.0, 160.0));
        breedWeightMap.put("Greater Swiss Mountain Dog", new Pair<>(85.0, 140.0));
        breedWeightMap.put("Greyhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Harrier", new Pair<>(45.0, 60.0));
        breedWeightMap.put("Havanese", new Pair<>(7.0, 13.0));
        breedWeightMap.put("Hokkaido", new Pair<>(44.0, 66.0));
        breedWeightMap.put("Hovawart", new Pair<>(55.0, 90.0));
        breedWeightMap.put("Ibizan Hound", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Icelandic Sheepdog", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Irish Red and White Setter", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Irish Setter", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Irish Terrier", new Pair<>(25.0, 27.0));
        breedWeightMap.put("Irish Water Spaniel", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Irish Wolfhound", new Pair<>(140.0, 180.0));
        breedWeightMap.put("Italian Greyhound", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Jack Russell Terrier", new Pair<>(13.0, 17.0));
        breedWeightMap.put("Japanese Chin", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Japanese Spitz", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Japanese Terrier", new Pair<>(5.0, 9.0));
        breedWeightMap.put("Kai Ken", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Karelian Bear Dog", new Pair<>(44.0, 50.0));
        breedWeightMap.put("Keeshond", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Kerry Blue Terrier", new Pair<>(33.0, 40.0));
        breedWeightMap.put("King Charles Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Kishu Ken", new Pair<>(30.0, 60.0));
        breedWeightMap.put("Komondor", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Kooikerhondje", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Korean Jindo Dog", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Kromfohrlander", new Pair<>(22.0, 28.0));
        breedWeightMap.put("Kuvasz", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Labrador Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Lagotto Romagnolo", new Pair<>(24.0, 35.0));
        breedWeightMap.put("Lakeland Terrier", new Pair<>(15.0, 17.0));
        breedWeightMap.put("Lancashire Heeler", new Pair<>(6.0, 13.0));
        breedWeightMap.put("Landseer", new Pair<>(100.0, 180.0));
        breedWeightMap.put("Leonberger", new Pair<>(90.0, 170.0));
        breedWeightMap.put("Lhasa Apso", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Lowchen", new Pair<>(8.0, 18.0));
        breedWeightMap.put("Maltese", new Pair<>(4.0, 7.0));
        breedWeightMap.put("Manchester Terrier", new Pair<>(12.0, 22.0));
        breedWeightMap.put("Maremma Sheepdog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Mastiff", new Pair<>(175.0, 190.0));
        breedWeightMap.put("Miniature Bull Terrier", new Pair<>(25.0, 33.0));
        breedWeightMap.put("Miniature Pinscher", new Pair<>(8.0, 12.0));
        breedWeightMap.put("Miniature Schnauzer", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Neapolitan Mastiff", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Newfoundland", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Italian Greyhound", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Jack Russell Terrier", new Pair<>(13.0, 17.0));
        breedWeightMap.put("Japanese Chin", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Japanese Spitz", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Japanese Terrier", new Pair<>(5.0, 9.0));
        breedWeightMap.put("Kai Ken", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Karelian Bear Dog", new Pair<>(44.0, 50.0));
        breedWeightMap.put("Keeshond", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Kerry Blue Terrier", new Pair<>(33.0, 40.0));
        breedWeightMap.put("King Charles Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Kishu Ken", new Pair<>(30.0, 60.0));
        breedWeightMap.put("Komondor", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Kooikerhondje", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Korean Jindo Dog", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Kromfohrlander", new Pair<>(22.0, 28.0));
        breedWeightMap.put("Kuvasz", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Labrador Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Lagotto Romagnolo", new Pair<>(24.0, 35.0));
        breedWeightMap.put("Lakeland Terrier", new Pair<>(15.0, 17.0));
        breedWeightMap.put("Lancashire Heeler", new Pair<>(6.0, 13.0));
        breedWeightMap.put("Landseer", new Pair<>(100.0, 180.0));
        breedWeightMap.put("Leonberger", new Pair<>(90.0, 170.0));
        breedWeightMap.put("Lhasa Apso", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Lowchen", new Pair<>(8.0, 18.0));
        breedWeightMap.put("Maltese", new Pair<>(4.0, 7.0));
        breedWeightMap.put("Manchester Terrier", new Pair<>(12.0, 22.0));
        breedWeightMap.put("Maremma Sheepdog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Mastiff", new Pair<>(175.0, 190.0));
        breedWeightMap.put("Miniature Bull Terrier", new Pair<>(25.0, 33.0));
        breedWeightMap.put("Miniature Pinscher", new Pair<>(8.0, 12.0));
        breedWeightMap.put("Miniature Schnauzer", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Neapolitan Mastiff", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Newfoundland", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Norfolk Terrier", new Pair<>(11.0, 12.0));
        breedWeightMap.put("Norwegian Buhund", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Norwegian Elkhound", new Pair<>(48.0, 55.0));
        breedWeightMap.put("Norwegian Lundehund", new Pair<>(13.0, 15.0));
        breedWeightMap.put("Norwich Terrier", new Pair<>(11.0, 12.0));
        breedWeightMap.put("Nova Scotia Duck Tolling Retriever", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Old English Sheepdog", new Pair<>(60.0, 100.0));
        breedWeightMap.put("Otterhound", new Pair<>(80.0, 115.0));
        breedWeightMap.put("Papillon", new Pair<>(4.0, 9.0));
        breedWeightMap.put("Pekingese", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Pembroke Welsh Corgi", new Pair<>(25.0, 30.0));
        breedWeightMap.put("Perro de Presa Canario", new Pair<>(88.0, 110.0));
        breedWeightMap.put("Peruvian Inca Orchid", new Pair<>(26.0, 55.0));
        breedWeightMap.put("Petit Basset Griffon Vendeen", new Pair<>(25.0, 40.0));
        breedWeightMap.put("Pharaoh Hound", new Pair<>(45.0, 55.0));
        breedWeightMap.put("Plott Hound", new Pair<>(50.0, 60.0));
        breedWeightMap.put("Pointer", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Polish Lowland Sheepdog", new Pair<>(30.0, 35.0));
        breedWeightMap.put("Pomeranian", new Pair<>(3.0, 7.0));
        breedWeightMap.put("Poodle", new Pair<>(4.0, 70.0));
        breedWeightMap.put("Portuguese Water Dog", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Preso Canario", new Pair<>(83.0, 110.0));
        breedWeightMap.put("Pug", new Pair<>(14.0, 18.0));
        breedWeightMap.put("Puli", new Pair<>(25.0, 35.0));
        breedWeightMap.put("Pumi", new Pair<>(18.0, 33.0));
        breedWeightMap.put("Pyrenean Mastiff", new Pair<>(130.0, 220.0));
        breedWeightMap.put("Pyrenean Shepherd", new Pair<>(15.0, 30.0));
        breedWeightMap.put("Rat Terrier", new Pair<>(10.0, 25.0));
        breedWeightMap.put("Redbone Coonhound", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Rhodesian Ridgeback", new Pair<>(70.0, 85.0));
        breedWeightMap.put("Rottweiler", new Pair<>(80.0, 135.0));
        breedWeightMap.put("Russian Toy", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Russian Tsvetnaya Bolonka", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Saint Bernard", new Pair<>(140.0, 180.0));
        breedWeightMap.put("Saluki", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Samoyed", new Pair<>(50.0, 65.0));
        breedWeightMap.put("Schapendoes", new Pair<>(26.0, 44.0));
        breedWeightMap.put("Schipperke", new Pair<>(10.0, 16.0));
        breedWeightMap.put("Scottish Deerhound", new Pair<>(85.0, 110.0));
        breedWeightMap.put("Scottish Terrier", new Pair<>(18.0, 22.0));
        breedWeightMap.put("Sealyham Terrier", new Pair<>(20.0, 24.0));
        breedWeightMap.put("Shetland Sheepdog", new Pair<>(14.0, 27.0));
        breedWeightMap.put("Shiba Inu", new Pair<>(17.0, 23.0));
        breedWeightMap.put("Shih Tzu", new Pair<>(9.0, 16.0));
        breedWeightMap.put("Siberian Husky", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Silky Terrier", new Pair<>(8.0, 11.0));
        breedWeightMap.put("Skye Terrier", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Sloughi", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Small Munsterlander Pointer", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Smooth Fox Terrier", new Pair<>(15.0, 18.0));
        breedWeightMap.put("Soft Coated Wheaten Terrier", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Spanish Water Dog", new Pair<>(30.0, 49.0));
        breedWeightMap.put("Spinone Italiano", new Pair<>(64.0, 85.0));
        breedWeightMap.put("Staffordshire Bull Terrier", new Pair<>(24.0, 38.0));
        breedWeightMap.put("Standard Schnauzer", new Pair<>(30.0, 50.0));
        breedWeightMap.put("Sussex Spaniel", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Swedish Lapphund", new Pair<>(16.0, 29.0));
        breedWeightMap.put("Swedish Vallhund", new Pair<>(20.0, 35.0));
        breedWeightMap.put("Tibetan Mastiff", new Pair<>(90.0, 150.0));
        breedWeightMap.put("Tibetan Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Tibetan Terrier", new Pair<>(18.0, 30.0));
        breedWeightMap.put("Toy Fox Terrier", new Pair<>(3.0, 7.0));
        breedWeightMap.put("Treeing Walker Coonhound", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Vizsla", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Weimaraner", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Welsh Springer Spaniel", new Pair<>(35.0, 55.0));
        breedWeightMap.put("Welsh Terrier", new Pair<>(20.0, 21.0));
        breedWeightMap.put("West Highland White Terrier", new Pair<>(13.0, 22.0));
        breedWeightMap.put("Whippet", new Pair<>(25.0, 40.0));
        breedWeightMap.put("Wire Fox Terrier", new Pair<>(15.0, 18.0));
        breedWeightMap.put("Wirehaired Pointing Griffon", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Wirehaired Vizsla", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Xoloitzcuintli", new Pair<>(10.0, 50.0));
        breedWeightMap.put("Yorkshire Terrier", new Pair<>(4.0, 7.0));

        return breedWeightMap.getOrDefault(breed, new Pair<>(0.0, 0.0));
    }


    private String calculateLifeStage(String species, int ageInMonths) {
        // Implement your logic to calculate life stage based on species and age
        // For example:
        if (species.equalsIgnoreCase("Dog")) {
            return calculateDogLifeStage(ageInMonths);
        } else if (species.equalsIgnoreCase("Cat")) {
            return calculateCatLifeStage(ageInMonths);
        } else {
            // Handle other species
            return "Unknown";
        }


    }

    private String calculateCatLifeStage(int petAgeInMonths) {
        if (petAgeInMonths < 12) {
            return "Kitten";
        } else if (petAgeInMonths < 60) {
            return "Adult";
        } else {
            return "Senior";
        }
    }

    private String calculateDogLifeStage(int petAgeInMonths) {


        if (petAgeInMonths <= 12) {
            return "Puppy Stage";
        } else if (petAgeInMonths <= 60) {
            return "Adulthood";
        } else {
            return "Senior";
        }
    }

    private int calculateAgeInMonths(String dateOfBirth) {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            Calendar currentDate = Calendar.getInstance();

            // Define the date formats to try
            SimpleDateFormat[] dateFormats = {
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            };

            // Try parsing the date with each format
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    Date petDOB = dateFormat.parse(dateOfBirth);
                    Calendar petDOBDate = Calendar.getInstance();
                    petDOBDate.setTime(petDOB);

                    int years = currentDate.get(Calendar.YEAR) - petDOBDate.get(Calendar.YEAR);
                    int months = currentDate.get(Calendar.MONTH) - petDOBDate.get(Calendar.MONTH);
                    int days = currentDate.get(Calendar.DAY_OF_MONTH) - petDOBDate.get(Calendar.DAY_OF_MONTH);

                    if (days < 0) {
                        months--;
                        days += currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    if (months < 0) {
                        years--;
                        months += 12;
                    }

                    int ageInMonths = years * 12 + months;

                    return ageInMonths;
                } catch (ParseException e) {
                    // Ignore parsing errors and try the next format
                }
            }
        }

        // Return -1 if date is invalid or empty
        return -1;
    }

    private String calculateAgeFromDOB(String dateOfBirth) {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            Calendar currentDate = Calendar.getInstance();

            // Define the date formats to try
            SimpleDateFormat[] dateFormats = {
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            };

            // Try parsing the date with each format
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    Date petDOB = dateFormat.parse(dateOfBirth);
                    Calendar petDOBDate = Calendar.getInstance();
                    petDOBDate.setTime(petDOB);

                    int years = currentDate.get(Calendar.YEAR) - petDOBDate.get(Calendar.YEAR);
                    int months = currentDate.get(Calendar.MONTH) - petDOBDate.get(Calendar.MONTH);
                    int days = currentDate.get(Calendar.DAY_OF_MONTH) - petDOBDate.get(Calendar.DAY_OF_MONTH);

                    if (days < 0) {
                        months--;
                        days += currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    if (months < 0) {
                        years--;
                        months += 12;
                    }

                    StringBuilder ageStringBuilder = new StringBuilder();
                    if (years > 0) {
                        ageStringBuilder.append(years).append(" year");
                        if (years > 1) {
                            ageStringBuilder.append("s");
                        }
                        ageStringBuilder.append(" ");
                    }
                    if (months > 0) {
                        ageStringBuilder.append(months).append(" month");
                        if (months > 1) {
                            ageStringBuilder.append("s");
                        }
                        ageStringBuilder.append(" ");
                    }
                    if (days > 0) {
                        ageStringBuilder.append(days).append(" day");
                        if (days > 1) {
                            ageStringBuilder.append("s");
                        }
                    }

                    return ageStringBuilder.toString();
                } catch (ParseException e) {
                    // Ignore parsing errors and try the next format
                }
            }
        }

        // Return "Date of Birth is not available" if date is invalid or empty
        return "Date of Birth is not available";
    }


    private void fetchCurrentWeight() {
        petReference.child("health").child("weight").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double currentWeight = dataSnapshot.getValue(Double.class);
                    Log.d("DEBUG", "Current Weight from Firebase: " + currentWeight);
                    if (currentWeight != null) {
                        petWeight = currentWeight;
                        currentWeightTextView.setText("Current Weight: " + currentWeight + " lb");
                        displayPetFoodRecommendation(petWeight);
                        displayRecommendedActivity(petWeight, calculateAgeInMonths(selectedPet.getDob()));
                        fetchTargetData();

                    } else {
                        // If weight is empty, set it to zero
                        petWeight = 0.0;
                        currentWeightTextView.setText("Current Weight: 0.0 lb");
                    }
                } else {
                    // If weight is not available, set it to zero
                    petWeight = 0.0;
                    currentWeightTextView.setText("Current Weight: 0.0 lb");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void fetchTargetData() {
        petReference.child("health").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch target weight and target activity based on pet type
                    DataSnapshot dogHealthTargetSnapshot = dataSnapshot.child("dogHealthTarget");
                    DataSnapshot catHealthTargetSnapshot = dataSnapshot.child("catHealthTarget");

                    if (selectedPet.getType().equalsIgnoreCase("Dog")) {
                        Double targetWeight = dogHealthTargetSnapshot.child("weightGoal").getValue(Double.class);
                        if (targetWeight != null) {
                            weightGoalWeightTextView.setText("Target Weight: " + targetWeight + " lb");
                        }
                        Integer targetActivity = dogHealthTargetSnapshot.child("activityGoal").getValue(Integer.class);
                        if (targetActivity != null) {
                            activityGoalTextView.setText("Target Activity: " + targetActivity + " steps");
                        }
                    } else if (selectedPet.getType().equalsIgnoreCase("Cat")) {
                        Double targetWeight = catHealthTargetSnapshot.child("weightGoal").getValue(Double.class);
                        if (targetWeight != null) {
                            weightGoalWeightTextView.setText("Target Weight: " + targetWeight + " lb");
                        }
                        Integer targetActivity = catHealthTargetSnapshot.child("activityDurationGoal").getValue(Integer.class);
                        if (targetActivity != null) {
                            activityGoalTextView.setText("Target Activity: " + targetActivity + " mins");
                        }

                    }
                } else {
                    Log.d("DEBUG", "Health data does not exist for the pet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    private void displayPetFoodRecommendation(double petWeight) {
        // Validate pet weight
        if (petWeight <= 0) {
            // Handle invalid weight
            foodTextView.setText("Invalid weight provided.");
            return;
        }

        // Calculate recommended calories per day based on pet weight
        int recommendedCaloriesPerDay;
        if (selectedPet.getType().equalsIgnoreCase("Dog")) {
            // Recommended calories per day for dogs
            if (petWeight < weightRange.first) {
                recommendedCaloriesPerDay = (int) (petWeight * 50);
            } else if (petWeight <= weightRange.second) {
                recommendedCaloriesPerDay = (int) (petWeight * 40);
            } else {
                recommendedCaloriesPerDay = (int) (petWeight * 30);
            }

            // Adjust recommendation based on weight status
            if (petWeight > weightRange.second) {
                recommendedCaloriesPerDay -= 100; // Decrease calories for overweight dogs
            } else if (petWeight < weightRange.first) {
                recommendedCaloriesPerDay += 800; // Increase calories for underweight dogs
            }
        } else if (selectedPet.getType().equalsIgnoreCase("Cat")) {
            // Recommended calories per day for cats
            recommendedCaloriesPerDay = (int) (petWeight * 30);

            // Adjust recommendation based on weight status
            if (petWeight > weightRange.second) {
                recommendedCaloriesPerDay -= 5; // Decrease calories for overweight cats
            } else if (petWeight < weightRange.first) {
                recommendedCaloriesPerDay += 5; // Increase calories for underweight cats
            }
        } else {
            // Handle other types of pets here
            recommendedCaloriesPerDay = 0; // No recommendation available
        }

        // Display the recommendation
        foodTextView.setText("Recommended Calories Per Day: " + recommendedCaloriesPerDay);
    }

    private void toggleVisibilitySix() {
        if (detailsVisibleSix) {
            findViewById(R.id.weeklyTrendsTextView).setVisibility(View.GONE);
            findViewById(R.id.weeklyWeightTrendsTextView).setVisibility(View.GONE);

            detailsVisibleSix = false;
        } else {
            findViewById(R.id.weeklyTrendsTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.weeklyWeightTrendsTextView).setVisibility(View.VISIBLE);
            detailsVisibleSix = true;
        }
    }

    private void toggleVisibilityFive() {
        if (detailsVisibleFive) {
            findViewById(R.id.predTextView).setVisibility(View.GONE);
            findViewById(R.id.recommendedTextView).setVisibility(View.GONE);
            detailsVisibleFive = false;
        } else {
            findViewById(R.id.predTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.recommendedTextView).setVisibility(View.VISIBLE);
            detailsVisibleFive = true;
        }
    }

    private void toggleVisibilityFour() {
        if (detailsVisibleFour) {
            findViewById(R.id.addTargetsBtn).setVisibility(View.GONE);
            findViewById(R.id.weightGoalWeightTextView).setVisibility(View.GONE);
            findViewById(R.id.activityGoalTextView).setVisibility(View.GONE);
            detailsVisibleFour = false;
        } else {
            findViewById(R.id.addTargetsBtn).setVisibility(View.VISIBLE);
            findViewById(R.id.weightGoalWeightTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.activityGoalTextView).setVisibility(View.VISIBLE);
            detailsVisibleFour = true;
        }
    }


    private void toggleVisibilityThree() {
        ImageView dogImage = findViewById(R.id.dogweightImage);
        ImageView catImage = findViewById(R.id.catweightImage);

        if (detailsVisibleThree) {
            // If the visibility is already true, hide both images
            dogImage.setVisibility(View.GONE);
            catImage.setVisibility(View.GONE);
            detailsVisibleThree = false;
        } else {
            // If the selected pet is a dog, show the dog image; otherwise, show the cat image
            if (selectedPet.getType().equals("Dog")) {
                dogImage.setVisibility(View.VISIBLE);
                catImage.setVisibility(View.GONE);
            } else if (selectedPet.getType().equals("Cat")) {
                dogImage.setVisibility(View.GONE);
                catImage.setVisibility(View.VISIBLE);
            }
            detailsVisibleThree = true;
        }
    }

    private void toggleVisibilityTwo() {
        if (detailsVisibleTwo) {
            findViewById(R.id.idealWeightTextView).setVisibility(View.GONE);
            findViewById(R.id.rangeTextView).setVisibility(View.GONE);
            findViewById(R.id.recommendedActivityTextView).setVisibility(View.GONE);
            findViewById(R.id.foodTextView).setVisibility(View.GONE);
            detailsVisibleTwo = false;
        } else {
            findViewById(R.id.idealWeightTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.rangeTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.recommendedActivityTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.foodTextView).setVisibility(View.VISIBLE);
            detailsVisibleTwo = true;
        }
    }

    private void toggleVisibility() {
        if (detailsVisibleOne) {
            findViewById(R.id.currentWeightTextView).setVisibility(View.GONE);
            findViewById(R.id.statusTextView).setVisibility(View.GONE);
            findViewById(R.id.ageTextView).setVisibility(View.GONE);
            findViewById(R.id.breedTextView).setVisibility(View.GONE);
            findViewById(R.id.lifeStageTextView).setVisibility(View.GONE);

            findViewById(R.id.activityTextView).setVisibility(View.GONE);
            detailsVisibleOne = false;
        } else {
            findViewById(R.id.currentWeightTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.statusTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.ageTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.breedTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.lifeStageTextView).setVisibility(View.VISIBLE);

            findViewById(R.id.activityTextView).setVisibility(View.VISIBLE);
            detailsVisibleOne = true;
        }
    }

    private void toggleVisibilityPartTWO() {
        if (detailsVisibleOne) {
            findViewById(R.id.currentWeightTextView).setVisibility(View.GONE);
            findViewById(R.id.statusTextView).setVisibility(View.GONE);
            findViewById(R.id.ageTextView).setVisibility(View.GONE);
            findViewById(R.id.breedTextView).setVisibility(View.GONE);
            findViewById(R.id.lifeStageTextView).setVisibility(View.GONE);

            findViewById(R.id.activityTextView).setVisibility(View.GONE);
            detailsVisibleOne = false;
        } else {
            findViewById(R.id.currentWeightTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.statusTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.ageTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.breedTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.lifeStageTextView).setVisibility(View.VISIBLE);

            findViewById(R.id.activityTextView).setVisibility(View.GONE);
            detailsVisibleOne = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mode_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuReturnToModeSelection) {

            Intent returnToModeIntent = new Intent(HealthData.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(HealthData.this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
