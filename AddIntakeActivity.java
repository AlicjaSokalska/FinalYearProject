package com.example.testsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddIntakeActivity extends AppCompatActivity {
    private String selectedPetName;
    private Pet selectedPet;
    private TextView petNameTextView, intakeDetailsTextView, dateTextView;
    private Button addFoodButton, addWaterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_intake);

        petNameTextView = findViewById(R.id.petNameTextView);
        intakeDetailsTextView = findViewById(R.id.intakeDetailsTextView);
        dateTextView = findViewById(R.id.dateTextView);
        addFoodButton = findViewById(R.id.addFoodButton);
        addWaterButton = findViewById(R.id.addWaterButton);

        // Retrieve pet details from the intent
        Intent intent = getIntent();
        if (intent.hasExtra("selectedPet")) {
            selectedPet = (Pet) intent.getSerializableExtra("selectedPet");
            selectedPetName = selectedPet.getName(); // Assuming there's a 'getName' method in your Pet class
            petNameTextView.setText("Pet Name: " + selectedPetName);
        }


        // Display current date and day
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy (EEE)", Locale.getDefault());
        String currentDateAndDay = sdf.format(Calendar.getInstance().getTime());
        dateTextView.setText(currentDateAndDay);

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFoodDialog();
            }
        });

        addWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddWaterDialog();
            }
        });
    }

    private void showAddFoodDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Food Entry");

        // Set up the layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_add_food, null);
        builder.setView(view);

        // Get references to the EditText fields in the dialog layout
        EditText servingEditText = view.findViewById(R.id.servingEditText);
        EditText timeEditText = view.findViewById(R.id.timeEditText);
        EditText brandEditText = view.findViewById(R.id.brandEditText);

        // Set up the positive button click listener
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get user input from the EditText fields
                String serving = servingEditText.getText().toString();
                String time = timeEditText.getText().toString();
                String brand = brandEditText.getText().toString();

                // Update intakeDetailsTextView with the entered details
                String details = "Serving: " + serving + "\nTime: " + time;
                if (!brand.isEmpty()) {
                    details += "\nBrand: " + brand;
                }

                intakeDetailsTextView.setText(details);

            }
        });

        // Set up the negative button click listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
                dialog.cancel();
            }
        });

        // Show the AlertDialog
        builder.create().show();
    }

    private void showAddWaterDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Water Entry");

        // Set up the layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_add_water, null);
        builder.setView(view);

        // Get references to the EditText fields in the dialog layout
        EditText servingEditText = view.findViewById(R.id.waterServingEditText);
        EditText timeEditText = view.findViewById(R.id.waterTimeEditText);

        // Set up the positive button click listener
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get user input from the EditText fields
                String serving = servingEditText.getText().toString();
                String time = timeEditText.getText().toString();

                // Create a new IntakeEntry for water and add it to the list

            }
        });

        // Set up the negative button click listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
                dialog.cancel();
            }
        });

        // Show the AlertDialog
        builder.create().show();
    }

}