package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class OtherPetExercise extends AppCompatActivity {
    private int activityGoal;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String selectedPetName;

    private TextView tvSelectedPet;
    private Pet selectedPet;
    private TextView tvCurrentExercise;
    private TextView tvDailyExercise;
    private TextView tvDateTime;

    private Button btnViewWeeklyExercise, btnViewPastExercise;
    private TextView weekStepCountTextView, weekDistanceTextView;
    private TextView averageWeekStepCountTextView, averageWeekDistanceTextView;
    private WebView chartWebView, chartWebView2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet_exercise);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Activity Summary");


        chartWebView = findViewById(R.id.chartWebView);
        chartWebView2 = findViewById(R.id.chartWebView2);

        Intent intent = getIntent();
        selectedPetName = intent.getStringExtra("selectedPetName");
        selectedPet = (Pet) intent.getSerializableExtra("selectedPet");

        // Display the selected pet information based on which one is available
        if (selectedPetName != null) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            tvSelectedPet = findViewById(R.id.tv_selectedPet);
            tvSelectedPet.setText("Selected Pet: " + selectedPetName);
            tvSelectedPet.setVisibility(View.VISIBLE);
        } else if (selectedPet != null) {
            tvSelectedPet = findViewById(R.id.tv_selectedPet);
            tvSelectedPet.setText("Selected Pet: " + selectedPet.getName());
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
        weekStepCountTextView = findViewById(R.id.weekStepCountTextView);
        averageWeekStepCountTextView = findViewById(R.id.averageWeekStepCountTextView);

        weekDistanceTextView = findViewById(R.id.weekDistanceTextView);
        averageWeekDistanceTextView = findViewById(R.id.averageWeekDistanceTextView);
        displayCurrentDateTime();


        fetchAndDisplayExerciseData();
        fetchAndDisplayWeekExerciseData();
        fetchAndDisplayWeeklyExerciseData();
        // fetchAndDisplayDailyTotal();


        CardView buttonCardView = findViewById(R.id.buttonCardView);
        buttonCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherPetExercise.this, PastExercise.class);
                if (selectedPetName != null) {
                    intent.putExtra("selectedPetName", selectedPetName);
                } else if (selectedPet != null) {
                    intent.putExtra("selectedPet", selectedPet);
                }
                startActivity(intent);
            }
        });





        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference exerciseDataRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("current_exercise_data");
           // DatabaseReference catHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("catHealthTarget");

            DatabaseReference petHealthTargetRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("pets").child(selectedPet.getName()).child("health").child("petHealthTarget");
            exerciseDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.child("stepCount").exists()) { // Check if stepCount exists under health
                            int stepCount = snapshot.child("stepCount").getValue(Integer.class); // Retrieve step count

                            ProgressBar progressBar = findViewById(R.id.stepsProgressBar);
                            TextView stepsTargetTextView = findViewById(R.id.stepsTargetTextView);

                                // Show stepsProgressBar for dogs
                                progressBar.setVisibility(View.VISIBLE);
                                progressBar.setProgress(stepCount);


                           petHealthTargetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        int activityGoal = dataSnapshot.child("activityGoal").getValue(Integer.class);

                                        // Update ProgressBar
                                        progressBar.setMax(activityGoal);
                                        // Set the current progress to the step count
                                        progressBar.setProgress(stepCount);
                                        // Set the current step count and activity goal
                                        TextView stepCountTextView = findViewById(R.id.stepTargetTextView);
                                        stepCountTextView.setText(stepCount + " / " + activityGoal);
                                    } else {
                                        Log.e("DisplayPetProfile", "No activity goal data available");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("DisplayPetProfile", "Firebase pet health target data error: " + error.getMessage());
                                }
                            });

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DisplayPetProfile", "Firebase exercise data error: " + error.getMessage());
                }
            });
        }}

    private void fetchAndDisplayWeekExerciseData() {
        DatabaseReference petRef;
        if (selectedPetName != null) {
            petRef = usersRef.child("pets").child(selectedPetName);
        } else if (selectedPet != null) {
            petRef = usersRef.child("pets").child(selectedPet.getName());
        } else {
            // Handle if both selectedPetName and selectedPet are null
            return;
        }

        petRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot dailyTotalsRef = snapshot.child("daily_totals");
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

                    double averageSteps = daysWithActivity > 0 ? totalSteps / daysWithActivity : 0;

                    // Update UI with the weekly total and average step counts
                    weekStepCountTextView.setText("Total Step Count This Week: " + totalSteps);
                    averageWeekStepCountTextView.setText("Average Step Count This Week: " + averageSteps);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    private void fetchAndDisplayExerciseData() {
        if (selectedPetName != null) {
            DatabaseReference petRef = usersRef.child("pets").child(selectedPetName);

            petRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot healthSnapshot = snapshot.child("health");
                        int activityGoal;


                        if (healthSnapshot.hasChild("petHealthTarget")) {
                            activityGoal = healthSnapshot.child("petHealthTarget").child("activityGoal").getValue(Integer.class);

                        } else {
                            // Handle the case if neither dogHealthTarget nor catHealthTarget is present
                            return;
                        }


                        DatabaseReference exerciseRef = petRef.child("current_exercise_data");

                        exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    int stepCount = snapshot.child("stepCount").getValue(Integer.class);
                                    double distance = snapshot.child("distance").getValue(Double.class);

                                    String exerciseInfo = "Step Count: " + stepCount + "\nDistance: " + distance + " meters";
                                    exerciseInfo += "\nActivity Goal: " + activityGoal;

                                    tvCurrentExercise.setText(exerciseInfo);

                                    if ("Other".equals(snapshot.child("type").getValue(String.class))) {
                                        // Add the recommended step count as blue line on the step count chart
                                        ArrayList<Integer> stepCounts = new ArrayList<>();
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            // Assuming data is structured in a way where we can iterate over step counts
                                            int step = childSnapshot.child("stepCount").getValue(Integer.class);
                                            stepCounts.add(step);
                                        }

                                        loadStepChart(stepCounts, activityGoal);
                                    }


                                    if (stepCount >= activityGoal) {
                                        Toast.makeText(OtherPetExercise.this, "Congratulations! Your pet has reached its activity goal.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Get the current time
                                        Calendar calendar = Calendar.getInstance();
                                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                                        // Check if it's past 9 PM
                                        if (currentHour >= 21) {
                                            Toast.makeText(OtherPetExercise.this, "Alert! Your pet didn't reach its activity goal by 9 PM.", Toast.LENGTH_SHORT).show();
                                        } else if (currentHour >= 18 && stepCount == 0) {
                                            // Display alert message
                                            AlertDialog.Builder builder = new AlertDialog.Builder(OtherPetExercise.this);
                                            builder.setTitle("Alert")
                                                    .setMessage("Check Up on your Pet .Your pet's step count is zero after 6 PM.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        }
                                    }
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        } else if (selectedPet != null) {
            DatabaseReference petRef = usersRef.child("pets").child(selectedPet.getName());

            petRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot healthSnapshot = snapshot.child("health");
                        int activityGoal;

                        if (healthSnapshot.hasChild("petHealthTarget")) {
                            activityGoal = healthSnapshot.child("petHealthTarget").child("activityGoal").getValue(Integer.class);

                        }else {
                            // Handle the case if neither dogHealthTarget nor catHealthTarget is present
                            return;
                        }

                        DatabaseReference exerciseRef = petRef.child("current_exercise_data");

                        exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    int stepCount = snapshot.child("stepCount").getValue(Integer.class);
                                    double distance = snapshot.child("distance").getValue(Double.class);

                                    String exerciseInfo = "Step Count: " + stepCount + "\nDistance: " + distance + " meters";
                                    exerciseInfo += "\nActivity Goal: " + activityGoal;

                                    exerciseInfo += " steps";

                                    tvCurrentExercise.setText(exerciseInfo);
                                    if ("Other".equals(snapshot.child("type").getValue(String.class))) {
                                        // Add the recommended step count as blue line on the step count chart
                                        ArrayList<Integer> stepCounts = new ArrayList<>();
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            // Assuming data is structured in a way where we can iterate over step counts
                                            int step = childSnapshot.child("stepCount").getValue(Integer.class);
                                            stepCounts.add(step);
                                        }

                                        loadStepChart(stepCounts, activityGoal);
                                    }
                                    if (stepCount >= activityGoal) {
                                        Toast.makeText(OtherPetExercise.this, "Congratulations! Your pet has reached its activity goal.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Calendar calendar = Calendar.getInstance();
                                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);


                                        if (currentHour >= 21) {
                                            Toast.makeText(OtherPetExercise.this, "Alert! Your pet didn't reach its activity goal by 9 PM.", Toast.LENGTH_SHORT).show();
                                        } else if (currentHour >= 18 && stepCount == 0) {
                                            // Display alert message
                                            AlertDialog.Builder builder = new AlertDialog.Builder(OtherPetExercise.this);
                                            builder.setTitle("Alert")
                                                    .setMessage("Check Up on your Pet .Your pet's step count is zero after 6 PM.")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error
                            }
                        });
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

    private void fetchAndDisplayWeeklyExerciseData() {
        DatabaseReference petRef;
        if (selectedPetName != null) {
            petRef = usersRef.child("pets").child(selectedPetName);
        } else if (selectedPet != null) {
            petRef = usersRef.child("pets").child(selectedPet.getName());
        } else {
            // Handle if both selectedPetName and selectedPet are null
            return;
        }

        petRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot healthSnapshot = snapshot.child("health");

                    int activityGoal = 0; // Define the variable here

                    if (healthSnapshot.hasChild("petHealthTarget")) {
                        activityGoal = healthSnapshot.child("petHealthTarget").child("activityGoal").getValue(Integer.class);
                    } else {
                        // Handle the case if neither dogHealthTarget nor catHealthTarget is present
                        return;
                    }

                    DataSnapshot dailyTotalsRef = snapshot.child("daily_totals");
                    Calendar calendar = Calendar.getInstance();
                    ArrayList<Integer> stepCounts = new ArrayList<>();
                    ArrayList<Double> distances = new ArrayList<>();
                    int daysWithActivity = 0;

                    // Loop through the last 7 days
                    for (int i = 0; i < 7; i++) {
                        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.getTime());
                        DataSnapshot daySnapshot = dailyTotalsRef.child(date);

                        if (daySnapshot.exists()) {
                            int stepCount = daySnapshot.child("stepCount").getValue(Integer.class);
                            double distance = daySnapshot.child("distance").getValue(Double.class);

                            stepCounts.add(stepCount);
                            distances.add(distance);

                            daysWithActivity++;
                        } else {
                            // If no data for the day, add default values
                            stepCounts.add(0);
                            distances.add(0.0);
                        }

                        // Move to the previous day
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                    }

                    // Calculate total and average values
                    double totalSteps = calculateTotal(stepCounts);
                    double averageSteps = calculateAverage(totalSteps, daysWithActivity);
                    double totalDistance = calculateTotal(distances);
                    double averageDistance = calculateAverage(totalDistance, daysWithActivity);

                    // Update UI with the weekly total and average step counts
                    weekStepCountTextView.setText("Total Step Count This Week: " + totalSteps);
                    averageWeekStepCountTextView.setText("Average Step Count This Week: " + averageSteps);

                    // Update UI with the weekly total and average distance
                    weekDistanceTextView.setText("Total Distance This Week: " + totalDistance + " meters");
                    averageWeekDistanceTextView.setText("Average Distance This Week: " + averageDistance + " meters");

                    // Call loadChart method to display data on charts
                    loadChart(stepCounts, distances, activityGoal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    // Helper method to calculate total
    private double calculateTotal(ArrayList<? extends Number> list) {
        double total = 0;
        for (Number number : list) {
            total += number.doubleValue();
        }
        return total;
    }

    // Helper method to calculate average
    private double calculateAverage(double total, int count) {
        return count > 0 ? total / count : 0;
    }

    private void loadChart(ArrayList<Integer> stepCounts, ArrayList<Double> distances, int activityGoal) {
        loadStepChart(stepCounts, activityGoal);
        loadDistanceChart(distances);
    }

    private void loadStepChart(ArrayList<Integer> stepCounts, int activityGoal) {
        chartWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script></head><body>");
        html.append("<canvas id=\"stepChart\"></canvas>");
        html.append("<script>");
        html.append("var ctxStep = document.getElementById('stepChart').getContext('2d');");
        html.append("var stepChart = new Chart(ctxStep, {");
        html.append("type: 'bar',");
        html.append("data: {");
        html.append("labels: ['Day 7', 'Day 6', 'Day 5', 'Day 4', 'Day 3', 'Day 2', 'Day 1'],");
        html.append("datasets: [{");
        html.append("label: 'Step Count',");
        html.append("data: [");

        // Append step counts data
        for (int i = 0; i < stepCounts.size(); i++) {
            html.append(stepCounts.get(i));
            if (i < stepCounts.size() - 1) {
                html.append(", ");
            }
        }
        html.append("],");
        html.append("backgroundColor: 'rgba(255, 99, 132, 0.2)',");
        html.append("borderColor: 'rgba(255, 99, 132, 1)',");
        html.append("borderWidth: 1");
        html.append("},");

        // Add reference line for activity goal if the pet is a dog
        if (selectedPet != null && "Dog".equals(selectedPet.getType())) {
            html.append("{");
            html.append("label: 'Activity Goal',");
            html.append("data: [");
            // Repeat the activity goal value for each day
            for (int i = 0; i < stepCounts.size(); i++) {
                html.append(activityGoal);
                if (i < stepCounts.size() - 1) {
                    html.append(", ");
                }
            }
            html.append("],");
            html.append("backgroundColor: 'rgba(54, 162, 235, 0.2)',");
            html.append("borderColor: 'rgba(54, 162, 235, 1)',");
            html.append("borderWidth: 1,");
            html.append("borderDash: [10,5],"); // Dashed line
            html.append("fill: false,");
            html.append("hidden: false"); // Show legend for the goal line
            html.append("}");
        }

        html.append("]");
        html.append("},");
        html.append("options: {");
        html.append("scales: {");
        html.append("y: {");
        html.append("beginAtZero: true");
        html.append("}");
        html.append("}");
        html.append("}");
        html.append("});");
        html.append("</script>");
        html.append("</body></html>");

        chartWebView.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
    }

    private void loadDistanceChart(ArrayList<Double> distances) {
        chartWebView2.setWebViewClient(new WebViewClient());
        WebSettings webSettings = chartWebView2.getSettings();
        webSettings.setJavaScriptEnabled(true);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script></head><body>");
        html.append("<canvas id=\"distanceChart\"></canvas>"); // Update canvas ID here
        html.append("<script>");
        html.append("var ctxDistance = document.getElementById('distanceChart').getContext('2d');"); // Update chart ID here
        html.append("var distanceChart = new Chart(ctxDistance, {"); // Update chart ID here
        html.append("type: 'bar',");
        html.append("data: {");
        html.append("labels: ['Day 7', 'Day 6', 'Day 5', 'Day 4', 'Day 3', 'Day 2', 'Day 1'],");
        html.append("datasets: [{");
        html.append("label: 'Distance',");
        html.append("data: [");

        // Append distances data
        for (int i = 0; i < distances.size(); i++) {
            html.append(distances.get(i));
            if (i < distances.size() - 1) {
                html.append(", ");
            }
        }

        html.append("],");
        html.append("backgroundColor: 'rgba(255, 99, 132, 0.2)',");
        html.append("borderColor: 'rgba(255, 99, 132, 1)',");
        html.append("borderWidth: 1");
        html.append("}]");
        html.append("},");
        html.append("options: {");
        html.append("scales: {");
        html.append("y: {");
        html.append("beginAtZero: true");
        html.append("}");
        html.append("}");
        html.append("}");
        html.append("});");
        html.append("</script>");
        html.append("</body></html>");

        chartWebView2.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
    }}
