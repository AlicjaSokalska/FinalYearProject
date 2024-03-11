package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class PetWeightData extends AppCompatActivity {
    private Pet selectedPet;
    ArrayList<String> entryDates = new ArrayList<>();
    private ListView weightLogsListView;
    private ArrayAdapter<String> weightLogsAdapter;



    private TextView currentWeightTextView;
    private TextView weightStatusTextView;
    private TextView idealWeightTextView;
    private TextView weightRangeTextView, startWeightTextView, endWeightTextView;
    private TextView weightTargetTextView;
    private TextView petNameTextView;
    private WebView chartWebView;
    private ArrayList<Double> weightDataList = new ArrayList<>();
    private DatabaseReference userWeightsReference;
    private ValueEventListener weightEventListener;
    private ProgressBar weightProgressBar;

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


        // Initialize ListView and adapter
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
            petNameTextView.setText("Pet Name: " + selectedPet.getName());
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
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_weight);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_add_weight) {
                showAddWeightDialog();
                return true;
            } else if (item.getItemId() == R.id.navigation_pet_profile) {
                startActivity(new Intent(this, DisplayPetProfile.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, StartUpPage.class));
                return true;
            }
            return false;
        });

    }


    private void showUpdateOrDeleteDialog(String entryDate, double weight) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update or Delete Weight Data");
        builder.setMessage("Choose an action:");

        // Add buttons for update and delete actions
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Show dialog for updating weight
            showUpdateWeightDialog(entryDate, weight);
        });
        builder.setNegativeButton("Delete", (dialog, which) -> {
            // Delete the weight data
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
        // Convert weight data list to JSON array for WebView
        JSONArray jsonArray = new JSONArray(weightDataList);
        String weightDataJson = jsonArray.toString();

        // Load JavaScript function to update graph with new weight data
        String jsFunction = "updateGraph('" + weightDataJson + "')";
        chartWebView.evaluateJavascript(jsFunction, null);
    }

    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Weight");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
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
            // Get the current date as a string
            String currentDate = getCurrentDateAsString();

            // Push new weight and entry date to weightData node
            DatabaseReference weightDataRef = userWeightsReference.child(currentDate);
            weightDataRef.child("weight").setValue(weight);
            weightDataRef.child("entryDate").setValue(currentDate);

            // Update current weight in the health node
            DatabaseReference currentWeightRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("pets").child(selectedPet.getName()).child("health").child("weight");
            currentWeightRef.setValue(weight); // Update current weight in Firebase
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

                        // Retrieve current weight as Long
                        Long currentWeightLong = healthSnapshot.child("weight").getValue(Long.class);

                        // Retrieve ideal weight as Long
                        Long idealWeightLong = healthSnapshot.child("idealWeight").getValue(Long.class);
                        String idealWeight = String.valueOf(idealWeightLong);

                        // Retrieve other weight-related fields as Long if necessary
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

                        // Convert weight-related fields to String
                        String weightTargetStr = String.valueOf(weightTarget);
                        String weightRange = firstBreedWeightRange + " - " + secondBreedWeightRange;
                        String weightStatus = healthSnapshot.child("weightStatus").getValue(String.class);

                        // Display pet health data
                        displayPetWeightData(currentWeightLong, idealWeight, weightRange, weightTargetStr, weightStatus);

                        // Fetch and populate weight data
                        DataSnapshot weightDataSnapshot = snapshot.child("health").child("weightData");
                        for (DataSnapshot weightSnapshot : weightDataSnapshot.getChildren()) {
                            String entryDate = weightSnapshot.child("entryDate").getValue(String.class);
                            double weight = weightSnapshot.child("weight").getValue(Double.class);
                            String log = "Date: " + entryDate + ", Weight: " + weight;
                            weightLogsAdapter.add(log);
                        }
                        weightLogsAdapter.notifyDataSetChanged(); // Notify adapter data set changed


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

        // Convert weight range string to long values
        String[] weightRangeValues = weightRange.split(" - ");
        long weightRangeStart = Long.parseLong(weightRangeValues[0]);
        long weightRangeEnd = Long.parseLong(weightRangeValues[1]);

        // Update the current weight in Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && selectedPet != null) {
            DatabaseReference currentWeightRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUser.getUid())
                    .child("pets").child(selectedPet.getName())
                    .child("health").child("weight");
            currentWeightRef.setValue(currentWeightLong); // Update current weight in Firebase
            updatePetWeightStatus(currentWeightLong, weightRangeStart, weightRangeEnd);
        }

        // Determine start weight
        // Determine start weight
        double startWeight;

// overweight: start weight is above target

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



        // Set start weight text
       // startWeightTextView.setText("Start Weight: " + startWeight);

        // Calculate progress value
        double progress = ((currentWeightLong - startWeight) / (Double.parseDouble(weightTarget) - startWeight)) * 100;

        // Set progress to the ProgressBar
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
}
