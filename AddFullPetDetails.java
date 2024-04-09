package com.example.testsample;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFullPetDetails extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Toolbar toolbar;
    private EditText petNameEditText,  petDescriptionEditText;
    private EditText petDobEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference usersRef;

    private Uri imageUri;
    private String currentUserId;



    private Spinner spinnerPetType;
    private Spinner spinnerBreed;
    private AlertDialog breedInputDialog;
    private String enteredBreed;

    private static final String DOG_API_KEY = "live_CI7JERAnW7rx4sEk7l4kgfY5lOtiWkYL7un0N3YPSZXLm8uzNoFDHy4ZNFenGd32";

    private static final String CAT_API_KEY = "live_SnBu2FtUrm24c0t7riq0nVt8vtyYkNPfZ4LT8O1CZtm1DO1AieZlgDW4nkjpzY1R";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_pet_details);

        petNameEditText = findViewById(R.id.petNameEditText);
        petDobEditText = findViewById(R.id.petDobEditText);
        petDescriptionEditText = findViewById(R.id.petDescriptionEditText);

        spinnerPetType = findViewById(R.id.spinnerPetType);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        populatePetTypeSpinner();


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

        spinnerPetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedPetType = parentView.getItemAtPosition(position).toString();
                if (selectedPetType.equals("Dog")) {

                    spinnerBreed.setVisibility(View.VISIBLE);
                    new AddFullPetDetails.FetchDogBreedsTask().execute();

                } else if (selectedPetType.equals("Cat")) {

                    spinnerBreed.setVisibility(View.VISIBLE);
                    new AddFullPetDetails.FetchCatBreedsTask().execute();
                }  else if (selectedPetType.equals("Other")) {

                        showBreedInputDialog();
                } else {

                    spinnerBreed.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

    }
    private void showBreedInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Pet Breed");


        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        builder.setPositiveButton("OK", (dialog, which) -> {
            enteredBreed = input.getText().toString();

            if (!TextUtils.isEmpty(enteredBreed)) {

                Toast.makeText(this, "Entered Breed: " + enteredBreed, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            enteredBreed = null;
        });

        breedInputDialog = builder.create();

        spinnerBreed.setVisibility(View.GONE);
        breedInputDialog.show();
    }

    private void populatePetTypeSpinner() {
        // Dummy data for pet types
        List<String> petTypes = new ArrayList<>();
        petTypes.add("Cat");
        petTypes.add("Dog");
        petTypes.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPetType.setAdapter(adapter);
    }

    private class FetchDogBreedsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> dogBreeds = new ArrayList<>();

            try {
                URL url = new URL("https://api.thedogapi.com/v1/breeds");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("x-api-key", DOG_API_KEY);  // Include the API key in the request header

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(response.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject breedObject = jsonArray.getJSONObject(i);
                        String breedName = breedObject.getString("name");
                        dogBreeds.add(breedName);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return dogBreeds;
        }

        @Override
        protected void onPostExecute(List<String> dogBreeds) {
            super.onPostExecute(dogBreeds);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddFullPetDetails.this, android.R.layout.simple_spinner_item, dogBreeds);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBreed.setAdapter(adapter);
        }
    }
    private class FetchCatBreedsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> catBreeds = new ArrayList<>();

            try {
                URL url = new URL("https://api.thecatapi.com/v1/breeds");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("x-api-key", CAT_API_KEY);

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(response.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject breedObject = jsonArray.getJSONObject(i);
                        String breedName = breedObject.getString("name");
                        catBreeds.add(breedName);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return catBreeds;
        }

        @Override
        protected void onPostExecute(List<String> catBreeds) {
            super.onPostExecute(catBreeds);

            if (catBreeds.isEmpty()) {
                Toast.makeText(AddFullPetDetails.this, "Failed to fetch cat breeds", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddFullPetDetails.this, android.R.layout.simple_spinner_item, catBreeds);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBreed.setAdapter(adapter);
            }
        }
    }

    private void saveImageUrlToDatabase(String petName, String imageUrl) {
        DatabaseReference petRef = usersRef.child(currentUserId).child("pets").child(petName);
        Map<String, Object> petUpdates = new HashMap<>();
        petUpdates.put("imageUrl", imageUrl);
        petRef.updateChildren(petUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d("AddFullPetDetails", "Image URL saved to database: " + imageUrl);
                Toast.makeText(this, "Image URL saved to database", Toast.LENGTH_SHORT).show();
            } else {

                Log.e("AddFullPetDetails", "Failed to save image URL to database", task.getException());
                Toast.makeText(this, "Failed to save image URL to database", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void uploadImage(String petName) {
        if (imageUri != null) {
            final StorageReference imageRef = storageReference.child("users").child(currentUserId).child("pets").child(petName + ".jpg");
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                progressBar.setVisibility(ProgressBar.GONE);
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                    saveImageUrlToDatabase(petName, imageUrl);
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Image upload failed: " + task.getException().getMessage();
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("AddFullPetDetails", errorMessage);
                }
            });
        } else {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
        }
    }



    private void savePetDetails() {
        String petName = petNameEditText.getText().toString().trim();
        if (petName.isEmpty()) {
            Toast.makeText(this, "Please enter a pet name", Toast.LENGTH_SHORT).show();
            return;
        }

        String petDob = petDobEditText.getText().toString().trim();
        String petDescription = petDescriptionEditText.getText().toString().trim();
        //String petBreed = spinnerBreed.isShown() ? spinnerBreed.getSelectedItem().toString() : "Unknown";

        String petBreed;
        if (spinnerBreed.isShown()) {
            petBreed = spinnerBreed.getSelectedItem().toString();
        } else {

            petBreed = (enteredBreed != null) ? enteredBreed : "Unknown";
        }
        DatabaseReference petRef = usersRef.child(currentUserId).child("pets").child(petName);

        petRef.child("dob").setValue(petDob);
        petRef.child("description").setValue(petDescription);
        petRef.child("breed").setValue(petBreed);
        String petType = spinnerPetType.getSelectedItem().toString();

        Pet newPet = new Pet(petName, petDob,petType, petBreed, petDescription, "", null);

        petRef = usersRef.child(currentUserId).child("pets").child(petName);
        petRef.setValue(newPet).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                uploadImage(petName);
                Toast.makeText(this, "Pet details saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save pet details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            showImage();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


