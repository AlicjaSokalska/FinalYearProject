package com.example.testsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ApplyGeoFencing extends AppCompatActivity {

    private EditText edtGeofenceRadius;
    private Button btnApplyGeofencing;
    private String selectedPetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_geo_fencing);

        // Retrieve the selected pet name from the intent
        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            setTitle("Apply Geofencing for " + selectedPetName);
        }

        edtGeofenceRadius = findViewById(R.id.edtGeofenceRadius);
        btnApplyGeofencing = findViewById(R.id.btnApplyGeofencing);

        btnApplyGeofencing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyGeofencing();
            }
        });
    }

    private void applyGeofencing() {
        // Get the geofence radius entered by the user
        String radiusText = edtGeofenceRadius.getText().toString().trim();

        if (TextUtils.isEmpty(radiusText)) {
            // Handle case where radius is not entered
            Toast.makeText(this, "Please enter a geofence radius", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the radius as a float
        float geofenceRadius = Float.parseFloat(radiusText);

        // Create an intent to launch the map with the selected pet's geofence
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("selectedPetName", selectedPetName);
        mapIntent.putExtra("geofenceRadius", geofenceRadius);
        startActivity(mapIntent);
    }
}
