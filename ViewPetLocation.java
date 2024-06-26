package com.example.testsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewPetLocation extends AppCompatActivity {

    private TextView tvPetName, tvLat, tvLon, tvAltitude, tvAccuracy, tvSpeed, tvAddress;
    private Button btnShowMap;
    private String selectedPetName;
    private Pet selectedPet;
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private Handler handler;
    private final int UPDATE_INTERVAL = 10 * 60 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet_location);



        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver, new IntentFilter("LOCATION_UPDATED"));



        tvPetName = findViewById(R.id.tv_petName);
        tvLat = findViewById(R.id.tv_lat);
        tvLon = findViewById(R.id.tv_lon);
        tvAltitude = findViewById(R.id.tv_altitude);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvSpeed = findViewById(R.id.tv_speed);
        tvAddress = findViewById(R.id.tv_address);
        btnShowMap = findViewById(R.id.btn_showMap);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }
/*
        Intent intent = getIntent();
        selectedPetName = intent.getStringExtra("selectedPetName");
        selectedPet = (Pet) intent.getSerializableExtra("selectedPet");

        // Display the selected pet information based on which one is available
        if (selectedPet != null) {
            selectedPetName = selectedPet.getName();
            tvPetName.setText("Pet Name: " + selectedPet.getName());
            displayPetLocation(selectedPet.getName());
        }
*/
        Intent intent = getIntent();
        selectedPet = (Pet) intent.getSerializableExtra("selectedPet");

        // Display the selected pet information if it's available
        if (selectedPet != null) {
            selectedPetName = selectedPet.getName();
            tvPetName.setText("Pet Name: " + selectedPet.getName());
            displayPetLocation(selectedPet.getName());
        } else {
            // Handle the case where selectedPet is null
            // For example, display a message to the user or handle it according to your app's logic
            Toast.makeText(this, "No pet selected", Toast.LENGTH_SHORT).show();
        }
        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPetLocationOnMap();
            }
        });

        handler = new Handler();
        // Start the update task
        startUpdateTask();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_pet);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    startActivity(new Intent(ViewPetLocation.this, StartUpPage.class));
                    return true;
                } else if (item.getItemId() ==  R.id.navigation_pet_tracker) {
                    navigateToViewPetLocation();
                    return true;
                } else if (item.getItemId() == R.id.navigation_pet_health) {
                    navigateToHealthActivity();
                    return true;}
                else if (item.getItemId() == R.id.navigation_pet_profile) {
                    startActivity(new Intent(ViewPetLocation.this, DisplayPetProfile.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

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

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("petLocation")) {
                PetLocation petLocation = (PetLocation) intent.getSerializableExtra("petLocation");

                updateUIWithNewLocation(petLocation);
            }
        }
    };

    private void updateUIWithNewLocation(PetLocation petLocation) {

        tvLat.setText(String.valueOf(petLocation.getLatitude()));
        tvLon.setText(String.valueOf(petLocation.getLongitude()));
        tvAltitude.setText(String.valueOf(petLocation.getAltitude()));
        tvAccuracy.setText(String.valueOf(petLocation.getAccuracy()));
        tvSpeed.setText(String.valueOf(petLocation.getSpeed()));
        tvAddress.setText(petLocation.getAddress());

    }


    @Override
    protected void onDestroy() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        super.onDestroy();
    }


    private void startUpdateTask() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Assuming selectedPetName is properly set before calling displayPetLocation()
                displayPetLocation(selectedPetName);


                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

   /* private void showPetLocationOnMap() {
        String petNameToUse = selectedPetName != null ? selectedPetName : (selectedPet != null ? selectedPet.getName() : null);
        if (petNameToUse != null) {
            DatabaseReference petLocationRef = databaseReference.child("pets").child(petNameToUse).child("location");
            petLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        PetLocation petLocation = dataSnapshot.getValue(PetLocation.class);
                        if (petLocation != null) {
                            Intent mapIntent = new Intent(ViewPetLocation.this, MapsActivity.class);
                            mapIntent.putExtra("latitude", petLocation.getLatitude());
                            mapIntent.putExtra("longitude", petLocation.getLongitude());
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(ViewPetLocation.this, "Pet location not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ViewPetLocation.this, "Pet not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ViewPetLocation.this, "Error retrieving pet location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ViewPetLocation.this, "No pet selected", Toast.LENGTH_SHORT).show();
        }
    }*/private void showPetLocationOnMap() {
       if (selectedPet != null) {
           DatabaseReference petLocationRef = databaseReference.child("pets").child(selectedPet.getName()).child("location");
           petLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists()) {
                       PetLocation petLocation = dataSnapshot.getValue(PetLocation.class);
                       if (petLocation != null) {
                           Intent mapIntent = new Intent(ViewPetLocation.this, MapsActivity.class);
                           mapIntent.putExtra("selectedPet", selectedPet);
                           startActivity(mapIntent);
                       } else {
                           Toast.makeText(ViewPetLocation.this, "Pet location not available", Toast.LENGTH_SHORT).show();
                       }
                   } else {
                       Toast.makeText(ViewPetLocation.this, "Pet not found", Toast.LENGTH_SHORT).show();
                   }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {
                   Toast.makeText(ViewPetLocation.this, "Error retrieving pet location", Toast.LENGTH_SHORT).show();
               }
           });
       } else {
           Toast.makeText(ViewPetLocation.this, "No pet selected", Toast.LENGTH_SHORT).show();
       }
   }




    private void displayPetLocation(String petName) {
        databaseReference.child("pets").child(petName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Pet pet = dataSnapshot.getValue(Pet.class);
                    if (pet != null && pet.hasLocation()) {
                        updateUIWithNewLocation(pet.getLocation());
                    } else {
                        Toast.makeText(ViewPetLocation.this, "Pet location not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ViewPetLocation.this, "Pet not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewPetLocation.this, "Error retrieving pet location", Toast.LENGTH_SHORT).show();
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

            Intent returnToModeIntent = new Intent(ViewPetLocation.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(ViewPetLocation .this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


}