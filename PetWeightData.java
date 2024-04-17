package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class PetWeightData extends AppCompatActivity {
    private Pet selectedPet;
    ArrayList<String> entryDates = new ArrayList<>();
    private ListView weightLogsListView;
    private ArrayAdapter<String> weightLogsAdapter;

    private double averageWeightChangePerWeek;

    private FirebaseAuth mAuth;
    private TextView currentWeightTextView;
    private TextView weightStatusTextView;
    private TextView idealWeightTextView;
    private TextView weightRangeTextView, startWeightTextView, endWeightTextView,trend1TextView, trend2TextView,trend3TextView,predictionTextView1,predictionTextView2,predictionTextView3;
    private TextView weightTargetTextView;
    private TextView petNameTextView;
    private WebView chartWebView;
    private ArrayList<Double> weightDataList = new ArrayList<>();
    private DatabaseReference userWeightsReference;
    private ValueEventListener weightEventListener;
    private ProgressBar weightProgressBar;
    private ImageButton addWeightBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_weight_data);

        currentWeightTextView = findViewById(R.id.currentWeightTextView);
        weightStatusTextView = findViewById(R.id.weightStatusTextView);
        idealWeightTextView = findViewById(R.id.idealWeightTextView);
        weightRangeTextView = findViewById(R.id.weightRangeTextView);
        weightTargetTextView = findViewById(R.id.weightTargetTextView);
        petNameTextView = findViewById(R.id.petNameTextView);
        startWeightTextView = findViewById(R.id.startWeightTextView);
        endWeightTextView = findViewById(R.id.endWeightTextView);
        chartWebView = findViewById(R.id.chartWebView);
        addWeightBtn = findViewById(R.id.addWeightBtn);
      trend1TextView = findViewById(R.id.trend1);
        trend2TextView = findViewById(R.id.trend2);
        trend3TextView= findViewById(R.id.trend3);

        predictionTextView1= findViewById(R.id.predictionTextView1);
        predictionTextView2= findViewById(R.id.predictionTextView2);
        predictionTextView3= findViewById(R.id.predictionTextView3);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weightLogsListView = findViewById(R.id.weightLogsListView);
        weightLogsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        weightLogsListView.setAdapter(weightLogsAdapter);
        // Set click listener on weightLogsListView
        weightLogsListView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected weight log
            String weightLog = weightLogsAdapter.getItem(position);
            if (weightLog != null) {
                // Extract the date and weight from the log
                String[] parts = weightLog.split(", Weight: ");
                String entryDate = parts[0].substring(6); // Extract date from log
                double weight = Double.parseDouble(parts[1]); // Extract weight from log

                // Show dialog for options: update or delete weight data
                showUpdateOrDeleteDialog(entryDate, weight);
            }
        });
 weightProgressBar = findViewById(R.id.weightProgressBar);


        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet"); // Retrieve selected pet from intent

        if (selectedPet != null) {
            petNameTextView.setText("Pet: " + selectedPet.getName());
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userWeightsReference = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(currentUser.getUid()).child("pets").child(selectedPet.getName()).child("health").child("weightData");
                setupWeightEventListener();
                fetchPetData(selectedPet);
            } else {
                Toast.makeText(this, "No user signed in!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No pet selected!", Toast.LENGTH_SHORT).show();
            finish();
        }

        addWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddWeightDialog();
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_pet);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    startActivity(new Intent(PetWeightData.this, StartUpPage.class));
                    return true;
                } else if (item.getItemId() ==  R.id.navigation_pet_tracker) {
                    navigateToViewPetLocation();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_health) {
                    navigateToHealthActivity();
                    return true;}
                else if (item.getItemId() == R.id.navigation_pet_profile) {
                    startActivity(new Intent(PetWeightData.this, DisplayPetProfile.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

    }

    private void displayWeightTrends(Long currentWeightLong, String weightTarget) {
        if (!weightDataList.isEmpty()) {
            // Calculate total weight change since the first weight data entry
            double weightChange = currentWeightLong - weightDataList.get(0);
            String trend1 = "Total weight change since first entry: " + String.format("%.2f", weightChange);

            // Calculate average weight change per entry
            double totalWeightChange = 0;
            for (double weight : weightDataList) {
                totalWeightChange += weight;
            }
            double averageWeightChangePerEntry = totalWeightChange / weightDataList.size();
            String trend2 = "Average weight change per entry: " + String.format("%.2f", averageWeightChangePerEntry);

            // Calculate average weight change per week
            long daysBetweenFirstAndLastEntry = ChronoUnit.DAYS.between(
                    LocalDate.parse(entryDates.get(0), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalDate.parse(entryDates.get(entryDates.size() - 1), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );
            double weeks = daysBetweenFirstAndLastEntry / 7.0;
            double averageWeightChangePerWeek = weightChange / weeks;
            String trend3 = "Average weight change per week: " + String.format("%.2f", averageWeightChangePerWeek);

            // Display trends
            trend1TextView.setText(trend1);
            trend2TextView.setText(trend2);
            trend3TextView.setText(trend3);



        }
    }

    private void predictions(long currentWeightLong, long targetWeight, long firstBreedWeightRange, long secondBreedWeightRange, double averageWeightChangePerWeek) {
        double currentWeight = (double) currentWeightLong;
        double targetWeightDouble = (double) targetWeight;
        double averageWeightChangePerWeekDouble = averageWeightChangePerWeek;

        // Calculate the maximum allowable weight loss per week for pets (8% of total body weight)
        double maxAllowableLossPerWeek = 0.08 * currentWeight;

        // Ensure that the maximum allowable loss per week is not more than 10% of the body weight
        maxAllowableLossPerWeek = Math.min(maxAllowableLossPerWeek, 0.1 * currentWeight);

        double actualRateOfChangePerWeek = Math.min(averageWeightChangePerWeekDouble, -maxAllowableLossPerWeek);
        double weeksToTargetWeight = Math.abs((targetWeightDouble - currentWeight) / actualRateOfChangePerWeek);
        double daysToTargetWeight;
        String timeUnit;
        if (weeksToTargetWeight < 1) {
            daysToTargetWeight = weeksToTargetWeight * 7;
            timeUnit = "days";
        } else {
            daysToTargetWeight = weeksToTargetWeight * 7;
            timeUnit = "weeks";
        }
        String predictionMessage;
        if (actualRateOfChangePerWeek > 0) {
            if (currentWeight < targetWeightDouble) {
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight gain: %.1f %s", daysToTargetWeight, timeUnit);
            } else {
                // Pet is overweight, calculate time to lose weight
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight loss: %.1f %s", daysToTargetWeight, timeUnit);
            }
        } else if (actualRateOfChangePerWeek < 0) {
            // Pet is losing weight
            if (currentWeight < targetWeightDouble) {
                // Pet is underweight, calculate time to reach target weight gain
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight gain: %.1f %s", daysToTargetWeight, timeUnit);
            } else {
                // Pet is overweight, calculate time to lose weight
                predictionMessage = String.format(Locale.getDefault(), "Estimated time to reach target weight loss: %.1f %s", daysToTargetWeight, timeUnit);
            }
        } else {
            predictionMessage = "The pet is already at the target weight.";
        }

        // Check if the rate of change is too high and add a warning message
        if (actualRateOfChangePerWeek < -0.10 * currentWeight) {
            String warningMessage = "Warning: Pet should not lose more than 10% of body weight per week, as it is dangerous";
            // Display the warning message
            predictionTextView2.setText(warningMessage);
        }

        // Display the prediction message
        predictionTextView1.setText(predictionMessage);
    }

    private void showUpdateOrDeleteDialog(String entryDate, double weight) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update or Delete Weight Data");
        builder.setMessage("Choose an action:");
        builder.setPositiveButton("Update", (dialog, which) -> {

            showUpdateWeightDialog(entryDate, weight);
        });
        builder.setNegativeButton("Delete", (dialog, which) -> {

            deleteWeightData(entryDate);
        });

        builder.show();
    }

    private void showUpdateWeightDialog(String entryDate, double weight) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Weight");
        builder.setMessage("Enter the new weight:");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String weightInput = input.getText().toString();
            if (!TextUtils.isEmpty(weightInput)) {
                try {
                    double newWeight = Double.parseDouble(weightInput);
                    // Update the weight data
                    updateWeightData(entryDate, newWeight);
                } catch (NumberFormatException e) {
                    // Invalid input, show error message
                    Toast.makeText(this, "Invalid weight input!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Empty input, show error message
                Toast.makeText(this, "Weight input cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteWeightData(String entryDate) {
        if (userWeightsReference != null) {
            userWeightsReference.child(entryDate).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Weight data deleted successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete weight data!", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateWeightData(String entryDate, double weight) {
        if (userWeightsReference != null) {
            userWeightsReference.child(entryDate).child("weight").setValue(weight)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Weight data updated successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update weight data!", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupWeightEventListener() {
        weightEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    weightDataList.clear();
                    entryDates.clear(); // Clear entry dates list before updating
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Double weight = dataSnapshot.child("weight").getValue(Double.class);
                        if (weight != null) {
                            weightDataList.add(weight);
                            String entryDate = dataSnapshot.child("entryDate").getValue(String.class);
                            if (entryDate != null) {
                                entryDates.add(entryDate);
                            }
                        }
                    }
                    updateWeightGraph();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        userWeightsReference.addValueEventListener(weightEventListener);
    }

    private void updateWeightGraph() {
        JSONArray jsonArray = new JSONArray(weightDataList);
        String weightDataJson = jsonArray.toString();
 String jsFunction = "updateGraph('" + weightDataJson + "')";
        chartWebView.evaluateJavascript(jsFunction, null);
    }

    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Weight");
 final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);
 builder.setPositiveButton("OK", (dialog, which) -> {
            String weightInput = input.getText().toString();
            if (!TextUtils.isEmpty(weightInput)) {
                try {
                    double weight = Double.parseDouble(weightInput);
                    // Save weight to Firebase
                    if (selectedPet != null) {
                        saveWeightToFirebase(weight, selectedPet);
                    } else {
                        Toast.makeText(this, "No pet selected!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    // Invalid input, show error message
                    Toast.makeText(this, "Invalid weight input!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Empty input, show error message
                Toast.makeText(this, "Weight input cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveWeightToFirebase(double weight, Pet selectedPet) {
        if (userWeightsReference != null && selectedPet != null) {
            String currentDate = getCurrentDateAsString();
 DatabaseReference weightDataRef = userWeightsReference.child(currentDate);
            weightDataRef.child("weight").setValue(weight);
            weightDataRef.child("entryDate").setValue(currentDate);
 DatabaseReference currentWeightRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("pets").child(selectedPet.getName()).child("health").child("weight");
            currentWeightRef.setValue(weight);
           // predictions(weight, Long.parseLong(weightTargetTextView.getText().toString()), recommendedWeight);
        }
    }
  private String getCurrentDateAsString() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
    private void fetchPetData(Pet selectedPet) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userPetsReference = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUserId).child("pets").child(selectedPet.getName());

            userPetsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        DataSnapshot healthSnapshot = snapshot.child("health");
 Long currentWeightLong = healthSnapshot.child("weight").getValue(Long.class);
 Long idealWeightLong = healthSnapshot.child("idealWeight").getValue(Long.class);
                        String idealWeight = String.valueOf(idealWeightLong);
 long firstBreedWeightRange = healthSnapshot.child("breedWeightRange").child("first").getValue(Long.class);
                        long secondBreedWeightRange = healthSnapshot.child("breedWeightRange").child("second").getValue(Long.class);

                        // Retrieve health target based on pet type
                        long weightTarget;
                        if (selectedPet.getType().equals("Dog")) {
                            weightTarget = healthSnapshot.child("dogHealthTarget").child("weightGoal").getValue(Long.class);
                        } else if (selectedPet.getType().equals("Cat")) {
                            weightTarget = healthSnapshot.child("catHealthTarget").child("weightGoal").getValue(Long.class);
                        } else {
                            // Default value or handle other types
                            weightTarget = 0;
                        }
                        String weightTargetStr = String.valueOf(weightTarget);
                        String weightRange = firstBreedWeightRange + " - " + secondBreedWeightRange;
                        String weightStatus = healthSnapshot.child("weightStatus").getValue(String.class);

                        displayPetWeightData(currentWeightLong, idealWeight, weightRange, weightTargetStr, weightStatus);
                        predictions(currentWeightLong, Long.parseLong(weightTargetStr), firstBreedWeightRange, secondBreedWeightRange, averageWeightChangePerWeek);

                        DataSnapshot weightDataSnapshot = snapshot.child("health").child("weightData");
                        for (DataSnapshot weightSnapshot : weightDataSnapshot.getChildren()) {
                            String entryDate = weightSnapshot.child("entryDate").getValue(String.class);
                            double weight = weightSnapshot.child("weight").getValue(Double.class);
                            String log = "Date: " + entryDate + ", Weight: " + weight;
                            weightLogsAdapter.add(log);
                        }
                        weightLogsAdapter.notifyDataSetChanged(); // Notify adapter data set changed
                        // Display weight trends
                        displayWeightTrends(currentWeightLong, weightTargetStr);


                        // Load weight chart
                        loadWeightChart(weightDataList, firstBreedWeightRange, secondBreedWeightRange, Long.parseLong(weightTargetStr), currentWeightLong, entryDates);
                    } else {

                        Toast.makeText(PetWeightData.this, "Pet not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }
private void updatePetWeightStatus(long currentWeight, long weightRangeStart, long weightRangeEnd) {
        String weightStatus;
        if (currentWeight < weightRangeStart) {
            weightStatus = "Underweight";
        } else if (currentWeight > weightRangeEnd) {
            weightStatus = "Overweight";
        } else {
            weightStatus = "Normal Weight";
        }
        weightStatusTextView.setText("Weight Status: " + weightStatus);

        // Update weight status in Firebase if needed
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && selectedPet != null) {
            DatabaseReference weightStatusRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUser.getUid())
                    .child("pets").child(selectedPet.getName())
                    .child("health").child("weightStatus");
            weightStatusRef.setValue(weightStatus);
        }
    }
    private void displayPetWeightData(Long currentWeightLong, String idealWeight, String weightRange, String weightTarget, String weightStatus) {
        // Update the UI
        currentWeightTextView.setText("Current Weight: " + currentWeightLong);
        weightStatusTextView.setText("Weight Status: " + weightStatus);
        idealWeightTextView.setText("Ideal Weight: " + idealWeight);
        weightRangeTextView.setText("Weight Range: " + weightRange);
        weightTargetTextView.setText("Weight Target: " + weightTarget);
 endWeightTextView.setText("Target Weight: " + weightTarget);
  String[] weightRangeValues = weightRange.split(" - ");
        long weightRangeStart = Long.parseLong(weightRangeValues[0]);
        long weightRangeEnd = Long.parseLong(weightRangeValues[1]);
 FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && selectedPet != null) {
            DatabaseReference currentWeightRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUser.getUid())
                    .child("pets").child(selectedPet.getName())
                    .child("health").child("weight");
            currentWeightRef.setValue(currentWeightLong); // Update current weight in Firebase
            updatePetWeightStatus(currentWeightLong, weightRangeStart, weightRangeEnd);
        }
  double startWeight;
  if (!weightDataList.isEmpty() && currentWeightLong > weightDataList.get(0) && currentWeightLong >= Long.parseLong(weightTarget)) {
            // current weight is above start and target weight
             startWeight = Collections.max(weightDataList);
             startWeightTextView.setText("Highest Weight: " + startWeight);

        } else  if (!weightDataList.isEmpty() && currentWeightLong >= Long.parseLong(weightTarget)) {
            startWeight = weightDataList.get(0);
            startWeightTextView.setText("Start Weight: " + startWeight);
        } else {
            // underweight
            startWeight = 0;
            startWeightTextView.setText("Current Weight: " + currentWeightLong); // Set start weight as current weight if it's less than target weight
        }

        double progress = ((currentWeightLong - startWeight) / (Double.parseDouble(weightTarget) - startWeight)) * 100;

        ProgressBar weightProgressBar = findViewById(R.id.weightProgressBar);
        weightProgressBar.setProgress((int) progress);
    }

    private void loadWeightChart(ArrayList<Double> weightDataList, double weightRangeStart, double weightRangeEnd, double weightTarget, long currentWeight, ArrayList<String> entryDates) {
        chartWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script></head><body>");
        html.append("<canvas id=\"weightChart\" height=\"280\"></canvas>"); // Decreased height by 5 pixels
        html.append("<script>");
        html.append("var ctxWeight = document.getElementById('weightChart').getContext('2d');");
        html.append("var weightChart = new Chart(ctxWeight, {");
        html.append("type: 'line',");
        html.append("data: {");
        html.append("labels: [");

        // Append entry dates as labels
        for (int i = entryDates.size() - 1; i >= 0; i--) {
            html.append("'").append(entryDates.get(i)).append("'");
            if (i > 0) {
                html.append(", ");
            }
        }
        html.append("],");
        html.append("datasets: [{");
        html.append("label: 'Weight',");
        html.append("data: [");

        // Append weight data
        for (int i = 0; i < weightDataList.size(); i++) {
            html.append(weightDataList.get(i));
            if (i < weightDataList.size() - 1) {
                html.append(", ");
            }
        }
        html.append("],");
        html.append("fill: false,");
        html.append("borderColor: 'rgba(255, 99, 132, 1)',");
        html.append("borderWidth: 1");
        html.append("}, {");
        html.append("label: 'Weight Range Start',");
        html.append("data: Array(").append(weightDataList.size()).append(").fill(").append(weightRangeStart).append("),");
        html.append("borderColor: 'rgba(54, 162, 235, 1)',");
        html.append("borderWidth: 1");
        html.append("}, {");
        html.append("label: 'Weight Range End',");
        html.append("data: Array(").append(weightDataList.size()).append(").fill(").append(weightRangeEnd).append("),");
        html.append("borderColor: 'rgba(54, 162, 235, 1)',");
        html.append("borderWidth: 1");
        html.append("}, {");
        html.append("label: 'Weight Target',");
        html.append("data: Array(").append(weightDataList.size()).append(").fill(").append(weightTarget).append("),");
        html.append("fill: false,");
        html.append("borderColor: 'rgba(75, 192, 192, 1)',");
        html.append("borderWidth: 1");
        html.append("}]");
        html.append("},");
        html.append("options: {");
        html.append("plugins: {");
        html.append("title: {");
        html.append("display: true,");
        html.append("text: 'Weight Chart'");
        html.append("},");
        html.append("subtitle: {");
        html.append("display: true,");
        html.append("text: 'Weight Range: " + weightRangeStart + " - " + weightRangeEnd + ", Target Weight: " + weightTarget + ", Current Weight: " + currentWeight + "'");
        html.append("}");
        html.append("},");
        html.append("scales: {");
        html.append("y: {");
        html.append("beginAtZero: false");
        html.append("}");
        html.append("}");
        html.append("}");
        html.append("});");
        html.append("</script>");
        html.append("</body></html>");

        chartWebView.loadDataWithBaseURL(null, html.toString(), "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userWeightsReference != null && weightEventListener != null) {
            userWeightsReference.removeEventListener(weightEventListener);
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

            Intent returnToModeIntent = new Intent(PetWeightData.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(PetWeightData.this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
}
