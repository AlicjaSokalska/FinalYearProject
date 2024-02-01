package com.example.testsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class StartUpPage extends AppCompatActivity {

    private List<Pet> petList;
    private PetAdapter petListAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_add_pet) {

                    startActivity(new Intent(StartUpPage.this, AddFullPetDetails.class));
                    return true;
                } else if (item.getItemId() == R.id.action_user_profile) {

                    startActivity(new Intent(StartUpPage.this, UserProfile.class));
                    return true;
                } else if (item.getItemId() == R.id.action_map) {

                    startActivity(new Intent(StartUpPage.this, SelectOption.class));
                    return true;
                }
                else if (item.getItemId() == R.id.action_exercise) {

                    startActivity(new Intent(StartUpPage.this, ChooseOption.class));
                    return true;
                }
                return false;
            }
        });




        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        } else {

            return;
        }

        ListView petListView = findViewById(R.id.petListView);

        petList = new ArrayList<>();
        petListAdapter = new PetAdapter(this, R.layout.pet_list_item, petList);
        petListView.setAdapter(petListAdapter);


        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pet selectedPet = petList.get(position);
                Toast.makeText(StartUpPage.this, "Clicked: " + selectedPet.getName(), Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(StartUpPage.this, DisplayPetProfile.class);
                intent.putExtra("selectedPet", selectedPet);
                startActivity(intent);
            }
        });
        petListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showRemovePetDialog(position);
                return true;
            }
        });



        loadPetDataFromFirebase();


    }

    private void showRemovePetDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Pet");
        builder.setMessage("Are you sure you want to remove this pet?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                removePet(position);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void removePet(int position) {
        final Pet removedPet = petList.remove(position);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                petListAdapter.notifyDataSetChanged();
                Toast.makeText(StartUpPage.this, "Removed: " + removedPet.getName(), Toast.LENGTH_SHORT).show();
            }
        });


        String petName = removedPet.getName();
        if (petName != null) {
            usersRef.child("pets").orderByChild("name").equalTo(petName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                                petSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Log.e("StartUpPage", "Failed to remove pet from Firebase", databaseError.toException());
                        }
                    });
        }
    }
    private void loadPetDataFromFirebase() {
        usersRef.child("pets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear();

                for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                    Pet pet = petSnapshot.getValue(Pet.class);


                    if (pet != null) {
                        petList.add(pet);
                    }
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        petListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e("StartUpPage", "Failed to load pet data", databaseError.toException());
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mode_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menuReturnToModeSelection) {

            Intent returnToModeIntent = new Intent(StartUpPage.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(StartUpPage.this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}



