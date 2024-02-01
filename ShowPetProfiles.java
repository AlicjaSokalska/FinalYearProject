package com.example.testsample;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowPetProfiles extends AppCompatActivity {
    private ImageView petImageView;
    private Spinner petSpinner;
    private TextView detailsTextView;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private FirebaseAuth firebaseAuth;

    private List<String> petNames = new ArrayList<>();
    private List<Pet> pets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pet_profile);

        petSpinner = findViewById(R.id.petSpinner);
        detailsTextView = findViewById(R.id.detailsTextView);
        petImageView = findViewById(R.id.petImageView);


        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users"); // Assuming "users" is the root node in your database
        firebaseAuth = FirebaseAuth.getInstance();



        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {

            usersReference.child(currentUser.getUid()).child("pets").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    petNames.clear();
                    pets.clear();

                    //
                    for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                        Pet pet = petSnapshot.getValue(Pet.class);
                        pets.add(pet);
                        petNames.add(pet.getName());
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ShowPetProfiles.this,
                            android.R.layout.simple_spinner_dropdown_item, petNames);
                    petSpinner.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });


            petSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // Display details of the selected pet


                    displayPetDetails(pets.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing here
                }
            });
        }
    }

    private void displayPetDetails(Pet pet) {
   
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Name: ").append(pet.getName()).append("\n");
        detailsBuilder.append("Date of Birth: ").append(pet.getDob()).append("\n");
        detailsBuilder.append("Breed: ").append(pet.getBreed()).append("\n");
        detailsBuilder.append("Description: ").append(pet.getDescription()).append("\n");

        if (pet.hasLocation()) {

            detailsBuilder.append("Location Address: ").append(pet.getLocation().getAddress());
        }

        detailsTextView.setText(detailsBuilder.toString());
        if (pet != null && pet.getImageUrl() != null) {
            Glide.with(this)
                    .load(pet.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(petImageView);}
    }
}