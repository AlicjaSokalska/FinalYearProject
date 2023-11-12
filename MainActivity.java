package com.example.testsample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference usersRef;
    private TextView userEmailTextView;
    private TextView petsTextView;

    private Button viewPetButton, removePetButton,updatePetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference("users");

        //textview
        userEmailTextView = findViewById(R.id.userEmailTextView);
        petsTextView = findViewById(R.id.petsTextView);

        //buttons
        viewPetButton = findViewById(R.id.viewPetButton);
        removePetButton = findViewById(R.id.removePetButton);

        updatePetButton = findViewById(R.id.updatePetButton);
        //toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference currentUserRef = usersRef.child(userId);

            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        userEmailTextView.setText("User Email: " + email);

                        // Display the user's pets
                        if (dataSnapshot.hasChild("pets")) {
                            StringBuilder pets = new StringBuilder("Pets: ");
                            for (DataSnapshot petSnapshot : dataSnapshot.child("pets").getChildren()) {
                                String petName = petSnapshot.child("name").getValue(String.class);
                                pets.append(petName).append(", ");
                            }
                            petsTextView.setText(pets.toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
//will add later
                }
            });
        }


        Button addPetDetailsButton = findViewById(R.id.addPetDetailsButton);
        addPetDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddFullPetDetails activity
                Intent intent = new Intent(MainActivity.this, AddFullPetDetails.class);
                startActivity(intent);
            }
        });


        // Move to remove pet
        removePetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, RemovePet.class);
                startActivity(intent);
            }
        });


    // Move to remove pet
        viewPetButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(MainActivity.this, ViewPet.class);
            startActivity(intent);
        }
    });


    // Add click listener for the "Update Pet" button
            updatePetButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Start the "Update Pet" activity
            startActivity(new Intent(MainActivity.this, UpdatePet.class));
        }
    });
}



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}


