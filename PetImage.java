package com.example.testsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PetImage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText imagePetNameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private DatabaseReference petDetailsReference;

    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_image);

        imagePetNameEditText = findViewById(R.id.imagePetNameEditText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        Button chooseImageButton = findViewById(R.id.chooseImageButton);
        Button uploadImageButton = findViewById(R.id.uploadImageButton);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            petDetailsReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("pets");
        }

        chooseImageButton.setOnClickListener(v -> openFileChooser());
        uploadImageButton.setOnClickListener(v -> uploadImage());
    }

    private void saveImageUrlToDatabase(String petName, String imageUrl) {
        DatabaseReference petRef = petDetailsReference.child(petName);
        petRef.child("imageUrl").setValue(imageUrl);
    }

    private void showImage() {
        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
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

    private void uploadImage() {
        String petName = imagePetNameEditText.getText().toString().trim();
        if (petName.isEmpty() || imageUri == null) {
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

                        imagePetNameEditText.setText("");
                        imageView.setImageResource(0);
                    }).addOnFailureListener(e -> {

                    });

                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            showImage();
        }
    }
}
