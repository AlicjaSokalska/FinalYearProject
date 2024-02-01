package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChooseOption extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private Button  recordPetActivityButton, viewPetActivityButton, recordDogTrainingButton, recordCatTrainingButton;
    private Spinner petSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_option);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }
        recordPetActivityButton = findViewById(R.id.btnRecordPetActivity);
        viewPetActivityButton = findViewById(R.id.btnViewPetActivity);
        recordDogTrainingButton = findViewById(R.id.btnRecordDogTraining);
        recordCatTrainingButton = findViewById(R.id.btnRecordCatTraining);
        petSpinner = findViewById(R.id.spinnerPet);


        usersRef.child("pets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> petNames = new ArrayList<>();
                for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                    String petName = petSnapshot.getKey();
                    petNames.add(petName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ChooseOption.this,
                        android.R.layout.simple_spinner_item, petNames);


                petSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        recordPetActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedPetName = petSpinner.getSelectedItem().toString();


                Intent intent = new Intent(ChooseOption.this, ExerciseActivity.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });



        viewPetActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPetName = petSpinner.getSelectedItem().toString();


                Intent intent = new Intent(ChooseOption.this, ViewPetExercise.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });
        recordDogTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedPetName = petSpinner.getSelectedItem().toString();


                Intent intent = new Intent(ChooseOption.this, DogTraining.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });

    recordCatTrainingButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String selectedPetName = petSpinner.getSelectedItem().toString();


            Intent intent = new Intent(ChooseOption.this, CatTraining.class);
            intent.putExtra("selectedPetName", selectedPetName);
            startActivity(intent);
        }
    });

}}