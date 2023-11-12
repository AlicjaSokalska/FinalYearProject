package com.example.testsample;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AddFullPetDetails extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Toolbar toolbar;
    private EditText petNameEditText, petBreedEditText, petAgeEditText, petDescriptionEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference usersRef;

    private Uri imageUri;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_pet_details);

        petNameEditText = findViewById(R.id.petNameEditText);
        petBreedEditText = findViewById(R.id.petBreedEditText);
        petAgeEditText = findViewById(R.id.petAgeEditText);
        petDescriptionEditText = findViewById(R.id.petDescriptionEditText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        Button chooseImageButton = findViewById(R.id.chooseImageButton);
        Button savePetDetailsButton = findViewById(R.id.savePetDetailsButton);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        currentUserId = mAuth.getUid();

        chooseImageButton.setOnClickListener(v -> openFileChooser());
        savePetDetailsButton.setOnClickListener(v -> savePetDetails());


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
    private void saveImageUrlToDatabase(String petName, String imageUrl) {
        DatabaseReference petRef = usersRef.child(currentUserId).child("pets").child(petName);
        petRef.child("imageUrl").setValue(imageUrl);
    }


    private void showImage() {
        if (imageUri != null) {
            Picasso.get().load(imageUri).into(imageView);
        } else {
            Toast.makeText(this, "No image available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(String petName) {
        if (imageUri == null) {
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        StorageReference imageRef = storageReference.child("users").child(currentUserId).child("pets").child(petName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressBar.setVisibility(ProgressBar.GONE);

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToDatabase(petName, imageUrl);

                        imageView.setImageResource(0);
                    }).addOnFailureListener(e -> {
                        // Handle failure to get download URL
                    });

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                });
    }

    private void savePetDetails() {
        String petName = petNameEditText.getText().toString().trim();
        if (petName.isEmpty()) {
            Toast.makeText(this, "Please enter a pet name", Toast.LENGTH_SHORT).show();
            return;
        }

        String petBreed = petBreedEditText.getText().toString().trim();
        String petAge = petAgeEditText.getText().toString().trim();
        String petDescription = petDescriptionEditText.getText().toString().trim();

        DatabaseReference petRef = usersRef.child(currentUserId).child("pets").child(petName);
        petRef.child("breed").setValue(petBreed);
        petRef.child("age").setValue(petAge);
        petRef.child("description").setValue(petDescription);


        // Create a new Pet object with the provided details
        Pet newPet = new Pet(petName, petAge, petBreed, petDescription, "");


        petRef = usersRef.child(currentUserId).child("pets").child(petName);
        petRef.setValue(newPet).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Upload the image with the pet name
                uploadImage(petName);
                Toast.makeText(this, "Pet details saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save pet details", Toast.LENGTH_SHORT).show();
            }
        });

    }
        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


