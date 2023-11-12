package com.example.testsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PetProfile extends AppCompatActivity {
    private String petName;
    private Toolbar toolbar;
    private DatabaseReference userPetsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);

        // Get pet details
        String petDetails = getIntent().getStringExtra("petDetails");


        String[] detailsArray = petDetails.split("\n");
        petName = detailsArray[1];

        // Display pet details in TextViews
        TextView petNameTextView = findViewById(R.id.petNameTextView);
        TextView petAgeTextView = findViewById(R.id.petAgeTextView);
        TextView petBreedTextView = findViewById(R.id.petBreedTextView);
        TextView petDescriptionTextView = findViewById(R.id.petDescriptionTextView);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //
        petNameTextView.setText(detailsArray[1]);
        petAgeTextView.setText(detailsArray[3]);
        petBreedTextView.setText(detailsArray[5]);
        petDescriptionTextView.setText(detailsArray[7]);



        ///add pet medical records
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}




