package com.example.testsample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;

public class PetProfile extends AppCompatActivity {
    private String petName;
  private Pet selectedPet;
    private Toolbar toolbar;
    private DatabaseReference userPetsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);


        String petDetails = getIntent().getStringExtra("petDetails");



        String[] detailsArray = petDetails.split("\n");
        petName = detailsArray[1];


        TextView petNameTextView = findViewById(R.id.petNameTextView);
        TextView petDobTextView = findViewById(R.id.petDobTextView);
        TextView petBreedTextView = findViewById(R.id.petBreedTextView);
        TextView petDescriptionTextView = findViewById(R.id.petDescriptionTextView);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //
        petNameTextView.setText(detailsArray[1]);
        petDobTextView.setText(detailsArray[3]);
        petBreedTextView.setText(detailsArray[5]);
        petDescriptionTextView.setText(detailsArray[7]);


//move naviagtion to top
        ///add pet medical records
        // add album
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}