package com.example.testsample;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdatePet extends AppCompatActivity {

    private EditText petNameEditText;
    private EditText updatedPetNameEditText;
    private EditText updatedPetAgeEditText;
    private EditText updatedPetBreedEditText;
    private EditText updatedPetDescriptionEditText;
    private DatabaseReference userPetsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet);

        petNameEditText = findViewById(R.id.petNameEditText);
        updatedPetNameEditText = findViewById(R.id.updatedPetNameEditText);
        updatedPetAgeEditText = findViewById(R.id.updatedPetAgeEditText);
        updatedPetBreedEditText = findViewById(R.id.updatedPetBreedEditText);
        updatedPetDescriptionEditText = findViewById(R.id.updatedPetDescriptionEditText);

        Button updateButton = findViewById(R.id.updateButton);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Update the userPetsReference with the actual user ID
            userPetsReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");


            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String petName = petNameEditText.getText().toString();
                    String updatedPetName = updatedPetNameEditText.getText().toString();
                    String updatedPetAge = updatedPetAgeEditText.getText().toString();
                    String updatedPetBreed = updatedPetBreedEditText.getText().toString();
                    String updatedPetDescription = updatedPetDescriptionEditText.getText().toString();

                    // Update the pet information in Firebase
                    updatePetInFirebase(petName, updatedPetName, updatedPetAge, updatedPetBreed, updatedPetDescription);
                }
            });
        }
    }
    private void updatePetInFirebase(String petName, String updatedPetName, String updatedPetAge, String updatedPetBreed, String updatedPetDescription) {
        // First, query the database to find the pet with the given name
        userPetsReference.orderByChild("name").equalTo(petName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if the pet exists
                if (task.getResult().exists()) {
                    for (DataSnapshot petSnapshot : task.getResult().getChildren()) {
                        // Update the pet's information
                        petSnapshot.child("name").getRef().setValue(updatedPetName);
                        petSnapshot.child("age").getRef().setValue(updatedPetAge);
                        petSnapshot.child("breed").getRef().setValue(updatedPetBreed);
                        petSnapshot.child("description").getRef().setValue(updatedPetDescription);

                        Toast.makeText(UpdatePet.this, "Pet updated.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdatePet.this, "Pet not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UpdatePet.this, "Error updating pet.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
