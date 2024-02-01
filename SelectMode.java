package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SelectMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        Button userModeButton = findViewById(R.id.userModeButton);
        Button petModeButton = findViewById(R.id.petModeButton);

        userModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectMode.this, StartUpPage.class);
                startActivity(intent);
                finish();
            }
        });

        petModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectMode.this, MainOption.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
