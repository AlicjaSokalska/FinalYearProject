package com.example.testsample;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RemovePet extends AppCompatActivity {

    private EditText petNameEditText;
    private DatabaseReference userPetsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_pet);

        petNameEditText = findViewById(R.id.petNameEditText);
        Button removeButton = findViewById(R.id.removeButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Remove Pet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            userPetsReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pets");

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String petName = petNameEditText.getText().toString();


                    removePetFromFirebase(petName);
                }
            });
        }}
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remove_pet, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void removePetFromFirebase(final String petName) {
        userPetsReference.orderByChild("name").equalTo(petName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {

                        for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {

                            petSnapshot.getRef().removeValue();
                        }

                        Toast.makeText(RemovePet.this, "Pet removed.", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(RemovePet.this, "Pet not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(RemovePet.this, "Error removing pet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
