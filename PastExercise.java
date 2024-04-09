package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
public class PastExercise extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ListView listView;
    private List<DailyTotal> dailyTotalsList;

    private String selectedPetName;
    private Pet selectedPet;
    private TextView totalStepCountTextView;
    private TextView averageStepCountTextView;
    private TextView totalDistanceTextView;
    private TextView averageDistanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_exercise);

        Intent intent = getIntent();
        selectedPetName = intent.getStringExtra("selectedPetName");
        selectedPet = (Pet) intent.getSerializableExtra("selectedPet");

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        listView = findViewById(R.id.listView);
        dailyTotalsList = new ArrayList<>();

        totalStepCountTextView = findViewById(R.id.totalStepCountTextView);
        averageStepCountTextView = findViewById(R.id.averageStepCountTextView);
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        averageDistanceTextView = findViewById(R.id.averageDistanceTextView);

        fetchAndDisplayExerciseData();
    }

    private void fetchAndDisplayExerciseData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = databaseReference.child(currentUserId).child("pets");

            if (selectedPetName != null) {
                DatabaseReference petRef = userRef.child(selectedPetName).child("daily_totals");

                petRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dailyTotalsList.clear();
                        int totalStepCount = 0;
                        double totalDistance = 0.0;

                        for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            double distance = dateSnapshot.child("distance").getValue(Double.class);
                            int stepCount = dateSnapshot.child("stepCount").getValue(Integer.class);
                            totalStepCount += stepCount;
                            totalDistance += distance;

                            DailyTotal dailyTotal = new DailyTotal(formatDate(date), distance, stepCount);
                            dailyTotalsList.add(dailyTotal);
                        }

                        updateListView();
                        generateBarChartForStepCount();
                        generateBarChartForDistance();

                        totalStepCountTextView.setText("Total Step Count: " + totalStepCount);
                        if (dailyTotalsList.size() > 0) {
                            int averageStepCount = totalStepCount / dailyTotalsList.size();
                            averageStepCountTextView.setText("Average Step Count: " + averageStepCount);
                            double averageDistance = totalDistance / dailyTotalsList.size();
                            averageDistanceTextView.setText("Average Distance: " + averageDistance);
                        } else {
                            averageStepCountTextView.setText("Average Step Count: 0");
                            averageDistanceTextView.setText("Average Distance: 0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ExerciseData", "Error fetching exercise data", error.toException());
                    }
                });
            } else if (selectedPet != null) {
                DatabaseReference petRef = userRef.child(selectedPet.getName()).child("daily_totals");

                petRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dailyTotalsList.clear();
                        int totalStepCount = 0;
                        double totalDistance = 0.0;

                        for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            double distance = dateSnapshot.child("distance").getValue(Double.class);
                            int stepCount = dateSnapshot.child("stepCount").getValue(Integer.class);
                            totalStepCount += stepCount;
                            totalDistance += distance;

                            DailyTotal dailyTotal = new DailyTotal(formatDate(date), distance, stepCount);
                            dailyTotalsList.add(dailyTotal);
                        }

                        updateListView();
                        generateBarChartForStepCount();
                        generateBarChartForDistance();

                        totalStepCountTextView.setText("Total Step Count: " + totalStepCount);
                        if (dailyTotalsList.size() > 0) {
                            int averageStepCount = totalStepCount / dailyTotalsList.size();
                            averageStepCountTextView.setText("Average Step Count: " + averageStepCount);
                            double averageDistance = totalDistance / dailyTotalsList.size();
                            averageDistanceTextView.setText("Average Distance: " + averageDistance);
                        } else {
                            averageStepCountTextView.setText("Average Step Count: 0");
                            averageDistanceTextView.setText("Average Distance: 0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ExerciseData", "Error fetching exercise data", error.toException());
                    }
                });
            }
        }
    }

    private void updateListView() {
        DailyTotalAdapter adapter = new DailyTotalAdapter(this, dailyTotalsList);
        listView.setAdapter(adapter);
    }

    private void generateBarChartForStepCount() {
        List<String> labels = new ArrayList<>();
        List<Integer> stepCounts = new ArrayList<>();

        for (DailyTotal dailyTotal : dailyTotalsList) {
            labels.add(dailyTotal.getDate());
            stepCounts.add(dailyTotal.getStepCount());
        }

        String chartData = "{\"type\":\"bar\",\"data\":{\"labels\":" + new Gson().toJson(labels) +
                ",\"datasets\":[{\"label\":\"Step Count\",\"data\":" + new Gson().toJson(stepCounts) + "}]}}";

        WebView chartWebView = findViewById(R.id.chartWebView);
        setupWebView(chartWebView, chartData);
    }

    private void generateBarChartForDistance() {
        List<String> labels = new ArrayList<>();
        List<Double> distances = new ArrayList<>();

        for (DailyTotal dailyTotal : dailyTotalsList) {
            labels.add(dailyTotal.getDate());
            distances.add(dailyTotal.getDistance());
        }

        String chartData = "{\"type\":\"bar\",\"data\":{\"labels\":" + new Gson().toJson(labels) +
                ",\"datasets\":[{\"label\":\"Distance\",\"data\":" + new Gson().toJson(distances) + "}]}}";

        WebView chartWebView2 = findViewById(R.id.chartWebView2);
        setupWebView(chartWebView2, chartData);
    }

    private void setupWebView(WebView webView, String chartData) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String quickChartUrl = "https://quickchart.io/chart?c=" + chartData;
        webView.loadUrl(quickChartUrl);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setInitialScale(1);
                view.getSettings().setLoadWithOverviewMode(true);
                view.getSettings().setUseWideViewPort(true);
            }
        });
    }

    private String formatDate(String date) {
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
    }
}