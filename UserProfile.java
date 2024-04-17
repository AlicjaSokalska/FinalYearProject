package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile extends AppCompatActivity {

    private TextView textViewEmail;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ImageView profileImageView;
    private TextView signOutText;
    private TextView deleteProfileText;
    private Switch darkModeSwitch;
    private Switch silentModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = mAuth.getCurrentUser().getUid();

        textViewEmail = findViewById(R.id.textViewEmail);
        profileImageView = findViewById(R.id.profileImageView);
        signOutText = findViewById(R.id.signOutText);
        deleteProfileText = findViewById(R.id.deleteProfileText);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        retrieveUserDetails();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfile.this, UploadProfilePic.class));
            }
        });

        signOutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        deleteProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfile();
            }
        });

        // Dark Mode Switch listener
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Apply Dark Mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    // Apply Light Mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.mainBottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("BottomNavigation", "Item selected: " + item.getTitle());
                if (item.getItemId() == R.id.action_add_pet) {

                    startActivity(new Intent(UserProfile.this, AddFullPetDetails.class));
                    return true;
                } else if (item.getItemId() == R.id.action_user_profile) {
                    getSupportActionBar().setTitle("User Profile");
                    startActivity(new Intent(UserProfile.this,UserProfile.class));
                    return true;
                } else if (item.getItemId() == R.id.navigate_home) {
                    getSupportActionBar().setTitle("Home");
                    startActivity(new Intent(UserProfile.this, StartUpPage.class));
                    return true;
                }
                else if (item.getItemId() == R.id.navigate_reminder) {
                    getSupportActionBar().setTitle("Reminders");
                    startActivity(new Intent(UserProfile.this, Reminders.class));
                    return true;
                }
                return false;
            }
        });




    }

    private void retrieveUserDetails() {
        DatabaseReference currentUserRef = usersRef.child(currentUserId);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            textViewEmail.setText("Email: " + email);

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(UserProfile.this)
                                    .load(profileImageUrl)
                                    .into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
       if (id == R.id.mode_selection_option) {
            startActivity(new Intent(UserProfile.this, SelectMode.class));
            return true;
        } else if (id == R.id.sign_out_option) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void signOut() {
        mAuth.signOut();
        startActivity(new Intent(UserProfile.this, Login.class));
        finish(); // Close current activity
    }

    private void deleteProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Delete profile data from database
                                usersRef.child(currentUserId).removeValue();
                                Toast.makeText(UserProfile.this, "Profile deleted successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UserProfile.this, Login.class));
                                finish(); // Close current activity
                            } else {
                                Toast.makeText(UserProfile.this, "Failed to delete profile.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
