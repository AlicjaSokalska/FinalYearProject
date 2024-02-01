package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;


public class DisplayPetProfile extends AppCompatActivity {
    private Pet selectedPet; // Updated to store the Pet object
    private Toolbar toolbar;
    private DatabaseReference userPetsReference;
    private Button intakeBtn, healthBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pet_profile);



        intakeBtn = findViewById(R.id.intakeBtn);

        healthBtn = findViewById(R.id.healthBtn);

        // Get pet details
        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet");

        if (selectedPet == null) {
            Log.e("DisplayPetProfile", "Selected pet is null");
            finish();
            return;
        }


        TextView petDetailsTextView = findViewById(R.id.petDetailsTextView);
        ImageView petImageView = findViewById(R.id.petImageView);

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


        petDetailsTextView.setText(petDetailsBuilder.toString());
        petDetailsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUpdatePetActivity();
            }
        });


        selectedPet.loadPetImage(petImageView, this);



        intakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToIntakeActivity();
            }
        });


     healthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHealthActivity();
            }
        });
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




    private void navigateToHealthActivity() {
        Intent intent = new Intent(this, PetHealth.class);
        intent.putExtra("selectedPet", selectedPet);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}





