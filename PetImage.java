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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/*
public class PetImage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText imageNameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private DatabaseReference petDetailsReference;

    private String currentUserId;

    private ImageView showImageView;
    private Button showImageButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_image);

        imageNameEditText = findViewById(R.id.imageNameEditText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        Button chooseImageButton = findViewById(R.id.chooseImageButton);
        Button uploadImageButton = findViewById(R.id.uploadImageButton);

        showImageView = findViewById(R.id.showImageView);
        showImageButton = findViewById(R.id.showImageButton);

        // Set a click listener for the "Show Image" button
        showImageButton.setOnClickListener(v -> showImage());


        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Realtime Database reference
        //  user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            petDetailsReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("pets");
        }
        chooseImageButton.setOnClickListener(v -> openFileChooser());

        uploadImageButton.setOnClickListener(v -> uploadImage());

        // Set a default image
        imageView.setImageResource(R.drawable.placeholder_image);
    }

    private void saveImageUrlToDatabase(String petName, String imageUrl) {
        // Update the pet details in the database with the image URL
        DatabaseReference petRef = petDetailsReference.child(petName);
        petRef.child("imageUrl").setValue(imageUrl);
    }



    private void showImage() {
        // Check if the imageUri is not null
        if (imageUri != null) {
            // Load and display the image using Picasso
            Picasso.get().load(imageUri).into(showImageView);
        } else {
            // Provide feedback to the user that there is no image to show
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
        String imageName = imageNameEditText.getText().toString().trim();

        if (imageName.isEmpty() || imageUri == null) {
            // Handle validation or user feedback
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        StorageReference imageRef = storageReference.child("users").child(currentUserId).child("pets").child(imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Handle successful upload, e.g., show a success message
                    progressBar.setVisibility(ProgressBar.GONE);

                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the image URL in the database under the current pet
                        String imageUrl = uri.toString();
                        saveImageUrlToDatabase(imageName, imageUrl);

                        // Clear the image name field
                        imageNameEditText.setText("");
                    }).addOnFailureListener(e -> {
                        // Handle failure to get download URL
                    });

                })
                .addOnFailureListener(e -> {
                    // Handle failed upload, e.g., show an error message
                    progressBar.setVisibility(ProgressBar.GONE);
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
}*/
public class PetImage extends AppCompatActivity {

    // ... (existing imports)

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

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Realtime Database reference
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

                        imagePetNameEditText.setText(""); // Clear the pet name field
                        imageView.setImageResource(0); // Clear the image view
                    }).addOnFailureListener(e -> {
                        // Handle failure to get download URL
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
            imageView.setImageURI(imageUri);
        }
    }
}
