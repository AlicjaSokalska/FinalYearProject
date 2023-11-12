package com.example.testsample;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ViewPet extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference usersRef;
    private RecyclerView petsRecyclerView;
    private PetCardAdapter petAdapter;
    private List<Pet> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pet);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        usersRef = mDatabase.getReference("users");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference currentUserRef = usersRef.child(userId);

            petsRecyclerView = findViewById(R.id.petsRecyclerView);
            petsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            petAdapter = new PetCardAdapter(petList, this);
            petsRecyclerView.setAdapter(petAdapter);

            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("pets")) {
                        petList.clear();

                        for (DataSnapshot petSnapshot : dataSnapshot.child("pets").getChildren()) {
                            String petName = petSnapshot.getKey();
                            String petAge = petSnapshot.child("age").getValue(String.class);
                            String petBreed = petSnapshot.child("breed").getValue(String.class);
                            String petDescription = petSnapshot.child("description").getValue(String.class);
                            String imageUrl = petSnapshot.child("imageUrl").getValue(String.class);

                            Pet pet = new Pet(petName, petAge, petBreed, petDescription, imageUrl);

                            petList.add(pet);
                        }
                        petAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that may occur while fetching data
                }
            });

            petAdapter.setOnItemClickListener(new PetCardAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    // Handle opening the pet profile activity for the selected pet
                    Pet selectedPet = petAdapter.getPet(position);
                    if (selectedPet != null) {
                        // Start the PetProfileActivity with the selected pet's data
                        Intent intent = new Intent(ViewPet.this, PetProfile.class);

                        StringBuilder petDetails = new StringBuilder();
                        petDetails.append("Pets: \n");
                        petDetails.append("Name: ").append(selectedPet.getName()).append("\n\n");
                        petDetails.append("Age: ").append(selectedPet.getAge()).append("\n\n");
                        petDetails.append("Breed: ").append(selectedPet.getBreed()).append("\n\n");
                        petDetails.append("Description: ").append(selectedPet.getDescription()).append("\n\n");

                        intent.putExtra("petDetails", petDetails.toString());
                        intent.putExtra("imageUrl", selectedPet.getImageUrl());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
