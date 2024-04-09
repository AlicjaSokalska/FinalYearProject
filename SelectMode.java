package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SelectMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        MaterialButtonToggleGroup modeToggleGroup = findViewById(R.id.modeToggleGroup);
        MaterialButton userModeButton = findViewById(R.id.userModeButton);
        MaterialButton petModeButton = findViewById(R.id.petModeButton);

        // Clear default checked state
        modeToggleGroup.clearChecked();

        modeToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.userModeButton) {
                        Intent intent = new Intent(SelectMode.this, StartUpPage.class);
                        startActivity(intent);
                        finish();
                    } else if (checkedId == R.id.petModeButton) {
                        Intent intent = new Intent(SelectMode.this, MainOption.class);
                        startActivity(intent);
                        finish();
                    }
                    // Add more cases for additional buttons if needed
                }
            }
        });
    }
}
