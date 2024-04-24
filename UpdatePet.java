package com.example.testsample;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class UpdatePet extends AppCompatActivity {

    private TextView petNameTextView;
    private EditText updatedPetDobEditText;
    private EditText updatedPetBreedEditText;
    private EditText updatedPetDescriptionEditText;
    private DatabaseReference userPetsReference;
    private Pet selectedPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pet);

        petNameTextView = findViewById(R.id.petNameTextView);
        updatedPetDobEditText = findViewById(R.id.updatedPetDobEditText);
        updatedPetDobEditText.setOnClickListener(v -> showDatePickerDialog()); // Updated field for Date of Birth
        updatedPetBreedEditText = findViewById(R.id.updatedPetBreedEditText);
        updatedPetDescriptionEditText = findViewById(R.id.updatedPetDescriptionEditText);

        // Disable editing for the updatedPetNameEditText
        //updatedPetNameEditText = findViewById(R.id.updatedPetNameEditText);
      //  updatedPetNameEditText.setEnabled(false);
       // updatedPetNameEditText.setFocusable(false);

        Button updateButton = findViewById(R.id.updateButton);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            userPetsReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");


            Intent intent = getIntent();
            if (intent != null) {
                selectedPet = (Pet) intent.getSerializableExtra("selectedPet");
                if (selectedPet != null) {
                    // Autofill EditText fields with pet details
                    petNameTextView.setText(selectedPet.getName());
                  //  updatedPetNameEditText.setText(selectedPet.getName());
                    updatedPetDobEditText.setText(selectedPet.getDob());
                    updatedPetBreedEditText.setText(selectedPet.getBreed());
                    updatedPetDescriptionEditText.setText(selectedPet.getDescription());
                }
            }
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String petName = petNameTextView.getText().toString();
                    String updatedPetDob = updatedPetDobEditText.getText().toString();
                    String updatedPetBreed = updatedPetBreedEditText.getText().toString();
                    String updatedPetDescription = updatedPetDescriptionEditText.getText().toString();


                    Log.d("UpdatePet", "Updating pet: " + petName);

                    updatePetInFirebase(petName, updatedPetDob, updatedPetBreed, updatedPetDescription);
                }
            });
        }
    }
    private void showDatePickerDialog() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Set the selected date to the EditText
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    updatedPetDobEditText.setText(dateFormat.format(calendar.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updatePetInFirebase(String petName, String updatedPetDob, String updatedPetBreed, String updatedPetDescription) {
        userPetsReference.child(petName).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot petSnapshot = task.getResult();
                if (petSnapshot.exists()) {
                    // Update the pet's information
                    petSnapshot.child("dob").getRef().setValue(updatedPetDob);
                    petSnapshot.child("breed").getRef().setValue(updatedPetBreed);
                    petSnapshot.child("description").getRef().setValue(updatedPetDescription);


                    Toast.makeText(UpdatePet.this, "Pet information updated.", Toast.LENGTH_SHORT).show();
                } else {

                    Log.d("UpdatePet", "Pet not found.");

                    Toast.makeText(UpdatePet.this, "Pet not found.", Toast.LENGTH_SHORT).show();
                }
            } else {

                Log.e("UpdatePet", "Error updating pet: " + task.getException());

                Toast.makeText(UpdatePet.this, "Error updating pet.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}