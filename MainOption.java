package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainOption extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private Button trackPetLocationButton, btnRecordPetActivity;
    private Spinner petSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_option);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }
        trackPetLocationButton = findViewById(R.id.btnTrackPetLocation);
        btnRecordPetActivity = findViewById(R.id.btnRecordPetActivity);
        petSpinner = findViewById(R.id.spinnerPet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        usersRef.child("pets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> petNames = new ArrayList<>();
                for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                    String petName = petSnapshot.getKey();
                    petNames.add(petName);
                }


                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainOption.this,
                        android.R.layout.simple_spinner_item, petNames);


                petSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        trackPetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedPetName = petSpinner.getSelectedItem().toString();

                Intent intent = new Intent(MainOption.this, Tracking.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });



        btnRecordPetActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedPetName = petSpinner.getSelectedItem().toString();


                Intent intent = new Intent(MainOption.this, ExerciseActivity.class);
                intent.putExtra("selectedPetName", selectedPetName);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mode_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuReturnToModeSelection) {

            Intent returnToModeIntent = new Intent(MainOption.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(MainOption.this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}

