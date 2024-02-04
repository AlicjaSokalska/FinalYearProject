package com.example.testsample;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PetHealth extends AppCompatActivity {
    private double petWeight;
    private Pet selectedPet;
    private String petName;
    private String petBreed;
    private String petDateOfBirth;
    private String currentUserUid;

    private TextView petNameTextView;
    private TextView petBreedTextView;
    private TextView petAgeTextView;
    private EditText petWeightEditText;
    private DatabaseReference petReference;


    private Pair<Double, Double> weightRange;

    private Double idealWeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_health);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid();
        }

        selectedPet = (Pet) getIntent().getSerializableExtra("selectedPet");

        if (selectedPet == null) {
            Log.e("PetHealth", "Selected pet is null");
            finish();
            return;
        }


        petNameTextView = findViewById(R.id.petNameTextView);
        petBreedTextView = findViewById(R.id.petBreedTextView);
        petAgeTextView = findViewById(R.id.petAgeTextView);
        petWeightEditText = findViewById(R.id.petWeightEditText);


        petReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName());


        petNameTextView.setText("Pet Name: " + selectedPet.getName());

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });

        petReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    petBreed = dataSnapshot.child("breed").getValue(String.class);
                    petDateOfBirth = dataSnapshot.child("dob").getValue(String.class);


                    petBreedTextView.setText("Pet Breed: " + petBreed);
                    if (petBreed != null) {

                        weightRange = getBreedWeightRange(petBreed);
                        if (weightRange == null) {

                            Toast.makeText(PetHealth.this, "Weight range not available for the breed", Toast.LENGTH_SHORT).show();
                        } else {
                            idealWeight = calculateIdealWeight(weightRange);
                        }
                    }

                    String petAge = calculateAgeFromDOB(petDateOfBirth);
                    petAgeTextView.setText("Pet Age: " + petAge);
                    Double savedWeight = dataSnapshot.child("health").child("weight").getValue(Double.class);
                    if (savedWeight != null) {
                        petWeightEditText.setText(String.valueOf(savedWeight));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private String calculateAgeFromDOB(String dateOfBirth) {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {

            Calendar currentDate = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
            Date petDOB;
            try {
                petDOB = dateFormat.parse(dateOfBirth);
            } catch (ParseException e) {
                e.printStackTrace();
                return "Error";
            }
            Calendar petDOBDate = Calendar.getInstance();
            petDOBDate.setTime(petDOB);

            int years = currentDate.get(Calendar.YEAR) - petDOBDate.get(Calendar.YEAR);
            int months = currentDate.get(Calendar.MONTH) - petDOBDate.get(Calendar.MONTH);
            int days = currentDate.get(Calendar.DAY_OF_MONTH) - petDOBDate.get(Calendar.DAY_OF_MONTH);

            if (days < 0) {
                months--;
                days += currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            if (months < 0) {
                years--;
                months += 12;
            }
            StringBuilder ageStringBuilder = new StringBuilder();
            if (years > 0) {
                ageStringBuilder.append(years).append(" year");
                if (years > 1) {
                    ageStringBuilder.append("s");
                }
                ageStringBuilder.append(" ");
            }
            if (months > 0) {
                ageStringBuilder.append(months).append(" month");
                if (months > 1) {
                    ageStringBuilder.append("s");
                }
                ageStringBuilder.append(" ");
            }
            if (days > 0) {
                ageStringBuilder.append(days).append(" day");
                if (days > 1) {
                    ageStringBuilder.append("s");
                }
            }

            return ageStringBuilder.toString();
        } else {
            return "Date of Birth is not available";
        }

    }

    private int calculateAgeInMonths(String dateOfBirth) {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            Calendar currentDate = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
            Date petDOB;
            try {
                petDOB = dateFormat.parse(dateOfBirth);
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }

            Calendar petDOBDate = Calendar.getInstance();
            petDOBDate.setTime(petDOB);

            int years = currentDate.get(Calendar.YEAR) - petDOBDate.get(Calendar.YEAR);
            int months = currentDate.get(Calendar.MONTH) - petDOBDate.get(Calendar.MONTH);
            int days = currentDate.get(Calendar.DAY_OF_MONTH) - petDOBDate.get(Calendar.DAY_OF_MONTH);

            if (days < 0) {
                months--;
                days += currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            if (months < 0) {
                years--;
                months += 12;
            }


            int ageInMonths = years * 12 + months;

            return ageInMonths;
        } else {
            return -1;
        }
    }
    private void savePetHealthInformation(int petAgeInMonths, double petWeight) {
        DatabaseReference petHealthReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserUid)
                .child("pets")
                .child(selectedPet.getName())
                .child("health");


        petHealthReference.child("ageInMonths").setValue(petAgeInMonths);
        petHealthReference.child("weight").setValue(petWeight);
        petHealthReference.child("breedWeightRange").setValue(weightRange);
        petHealthReference.child("idealWeight").setValue(idealWeight);

        if (petWeight < weightRange.first) {
            petHealthReference.child("weightStatus").setValue("Underweight");
            Toast.makeText(this, "Warning: Pet is underweight", Toast.LENGTH_SHORT).show();
        } else if (petWeight > weightRange.second) {
            petHealthReference.child("weightStatus").setValue("Overweight");
            Toast.makeText(this, "Warning: Pet is overweight", Toast.LENGTH_SHORT).show();
        } else {
            petHealthReference.child("weightStatus").setValue("Ideal");
        }

        String lifeStage = calculateLifeStage(petAgeInMonths);
        petHealthReference.child("lifeStage").setValue(lifeStage);

        Toast.makeText(this, "Health information saved successfully", Toast.LENGTH_SHORT).show();

        displayHealthInformation(petAgeInMonths, petWeight);

    }

    private void displayHealthInformation(int petAgeInMonths, double petWeight) {
        TextView healthInfoTextView = findViewById(R.id.healthInfoTextView);

        StringBuilder healthInfo = new StringBuilder();


        healthInfo.append("Pet Weight: ").append(petWeight).append(" lb\n");
        healthInfo.append("Ideal Weight: ").append(idealWeight).append(" lb\n");

        if (petWeight < weightRange.first) {
            healthInfo.append("Weight Status: Underweight\n");
        } else if (petWeight > weightRange.second) {
            healthInfo.append("Weight Status: Overweight\n");
        } else {
            healthInfo.append("Weight Status: Ideal\n");
        }
        String lifeStage = calculateLifeStage(petAgeInMonths);
        healthInfo.append("Life Stage: ").append(lifeStage).append("\n");

        healthInfoTextView.setText(healthInfo.toString());
    }

    private String calculateLifeStage(int petAgeInMonths) {


        if (petAgeInMonths <= 12) {
            return "Puppy Stage";
        } else if (petAgeInMonths <= 60) {
            return "Adulthood";
        } else {
            return "Senior";
        }
    }



    private void saveButtonClicked() {
        String weightString = petWeightEditText.getText().toString();
        if (!TextUtils.isEmpty(weightString)) {
            petWeight = Double.parseDouble(weightString);
        }

        int petAgeInMonths = calculateAgeInMonths(petDateOfBirth);

        if (petAgeInMonths != -1) {
            savePetHealthInformation(petAgeInMonths, petWeight);
        } else {
            Toast.makeText(this, "Error calculating pet age", Toast.LENGTH_SHORT).show();
        }
    }

    private Pair<Double, Double> getBreedWeightRange(String breed) {

        Map<String, Pair<Double, Double>> breedWeightMap = new HashMap<>();
        breedWeightMap.put("Affenpinscher", new Pair<>(7.0, 10.0));
        breedWeightMap.put("Afghan Hound", new Pair<>(50.0, 60.0));
        breedWeightMap.put("Airedale Terrier", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Akitas", new Pair<>(70.0, 130.0));
        breedWeightMap.put("Alaskan Malamute", new Pair<>(71.0, 95.0));
        breedWeightMap.put("American English Coonhound", new Pair<>(40.0, 65.0));
        breedWeightMap.put("American Eskimo Toy", new Pair<>(6.0, 11.0));
        breedWeightMap.put("American Eskimo Miniature", new Pair<>(10.0, 21.0));
        breedWeightMap.put("American Eskimo Standard", new Pair<>(20.0, 40.0));
        breedWeightMap.put("American Foxhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("American Hairless Terrier", new Pair<>(12.0, 16.0));
        breedWeightMap.put("American Staffordshire Terrier", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Anatolian Shepherd", new Pair<>(80.0, 150.0));
        breedWeightMap.put("Australian Cattle Dog", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Australian Shepherd", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Australian Terrier", new Pair<>(15.0, 20.0));
        breedWeightMap.put("Basenji", new Pair<>(20.0, 26.0));
        breedWeightMap.put("Basset Hound", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Beagle Mini", new Pair<>(0.0, 20.0));
        breedWeightMap.put("Beagle", new Pair<>(20.0, 24.0));
        breedWeightMap.put("Bearded Collie", new Pair<>(45.0, 60.0));
        breedWeightMap.put("Beaucerons", new Pair<>(66.0, 110.0));
        breedWeightMap.put("Bedlington Terrier", new Pair<>(17.0, 23.0));
        breedWeightMap.put("Belgian Malinois", new Pair<>(40.0, 80.0));
        breedWeightMap.put("Belgian Sheepdog", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Belgian Tervuren", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Bergamasco", new Pair<>(57.0, 84.0));
        breedWeightMap.put("Berger Picard", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Bernese Mountain Dog", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Bichons Frise", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Black and Tan Coonhound", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Black Russian Terrier", new Pair<>(80.0, 130.0));
        breedWeightMap.put("Bloodhound", new Pair<>(80.0, 110.0));
        breedWeightMap.put("Bluetick Coonhounds", new Pair<>(45.0, 80.0));
        breedWeightMap.put("Boerboel", new Pair<>(150.0, 200.0));
        breedWeightMap.put("Border Collie", new Pair<>(30.0, 45.0));
        breedWeightMap.put("Border Terrier", new Pair<>(11.5, 15.5));
        breedWeightMap.put("Bouviers des Flandres", new Pair<>(60.0, 110.0));
        breedWeightMap.put("Boxer", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Briard", new Pair<>(50.0, 100.0));
        breedWeightMap.put("Brittany", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Brussels Griffons", new Pair<>(8.0, 10.0));
        breedWeightMap.put("Bulldog", new Pair<>(40.0, 50.0));
        breedWeightMap.put("Bullmastiff", new Pair<>(100.0, 130.0));
        breedWeightMap.put("Cairn Terrier", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Canaan Dog", new Pair<>(35.0, 55.0));
        breedWeightMap.put("Cane Corso", new Pair<>(88.0, 110.0));
        breedWeightMap.put("Cardigan Welsh Corgi", new Pair<>(25.0, 38.0));
        breedWeightMap.put("Catahoula Leopard Dog", new Pair<>(50.0, 95.0));
        breedWeightMap.put("Cavalier King Charles Spaniel", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Chesapeake Bay Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Chihuahua", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Chinese Crested", new Pair<>(5.0, 12.0));
        breedWeightMap.put("Chinese Shar-Pei", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Chinook", new Pair<>(55.0, 90.0));
        breedWeightMap.put("Chow Chow", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Clumber Spaniel", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Cockapoo", new Pair<>(6.0, 20.0));
        breedWeightMap.put("Collie", new Pair<>(50.0, 75.0));
        breedWeightMap.put("Coonhounds", new Pair<>(40.0, 75.0));
        breedWeightMap.put("Cardigan Welsh Corgi", new Pair<>(25.0, 38.0));
        breedWeightMap.put("Catahoula Leopard Dog", new Pair<>(50.0, 95.0));
        breedWeightMap.put("Cavalier King Charles Spaniel", new Pair<>(13.0, 18.0));
        breedWeightMap.put("Chesapeake Bay Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Chihuahua", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Chinese Crested", new Pair<>(5.0, 12.0));
        breedWeightMap.put("Chinese Shar-Pei", new Pair<>(40.0, 65.0));
        breedWeightMap.put("Corgi Pembroke Welsh", new Pair<>(25.0, 30.0));
        breedWeightMap.put("Coton de Tulear", new Pair<>(8.0, 15.0));
        breedWeightMap.put("Curly-Coated Retriever", new Pair<>(60.0, 95.0));
        breedWeightMap.put("Dachshund Miniature", new Pair<>(8.0, 11.0));
        breedWeightMap.put("Dachshund Standard", new Pair<>(16.0, 32.0));
        breedWeightMap.put("Dachshund Toy", new Pair<>(6.0, 12.0));
        breedWeightMap.put("Dalmatian", new Pair<>(40.0, 70.0));
        breedWeightMap.put("Dandie Dinmont Terrier", new Pair<>(18.0, 24.0));
        breedWeightMap.put("Doberman Pinscher", new Pair<>(60.0, 100.0));
        breedWeightMap.put("Dogo Argentino", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Dogue de Bordeaux", new Pair<>(99.0, 110.0));
        breedWeightMap.put("Dutch Shepherd", new Pair<>(50.0, 70.0));
        breedWeightMap.put("English Bulldog", new Pair<>(40.0, 50.0));
        breedWeightMap.put("English Cocker Spaniel", new Pair<>(26.0, 34.0));
        breedWeightMap.put("English Foxhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("English Setter", new Pair<>(45.0, 80.0));
        breedWeightMap.put("English Springer Spaniel", new Pair<>(40.0, 50.0));
        breedWeightMap.put("English Toy Spaniel", new Pair<>(8.0, 14.0));
        breedWeightMap.put("Entlebucher Mountain Dog", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Eskimo Toy", new Pair<>(6.0, 10.0));
        breedWeightMap.put("Eskimo Miniature", new Pair<>(10.0, 20.0));
        breedWeightMap.put("Eskimo Standard", new Pair<>(25.0, 35.0));
        breedWeightMap.put("Estrela Mountain Dog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Field Spaniel", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Finnish Lapphund", new Pair<>(33.0, 53.0));
        breedWeightMap.put("Finnish Spitz", new Pair<>(20.0, 33.0));
        breedWeightMap.put("Flat-Coated Retriever", new Pair<>(55.0, 75.0));
        breedWeightMap.put("French Bulldog", new Pair<>(16.0, 28.0));
        breedWeightMap.put("French Spaniel", new Pair<>(55.0, 61.0));
        breedWeightMap.put("Galgo Spanish Greyhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("German Pinscher", new Pair<>(25.0, 45.0));
        breedWeightMap.put("German Shepherd", new Pair<>(50.0, 90.0));
        breedWeightMap.put("German Shorthaired Pointer", new Pair<>(45.0, 70.0));
        breedWeightMap.put("German Wirehaired Pointer", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Giant Schnauzer", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Glen of Imaal Terrier", new Pair<>(32.0, 40.0));
        breedWeightMap.put("Golden Retriever", new Pair<>(55.0, 75.0));
        breedWeightMap.put("Gordon Setter", new Pair<>(45.0, 80.0));
        breedWeightMap.put("Great Dane", new Pair<>(140.0, 175.0));
        breedWeightMap.put("Great Pyrenees", new Pair<>(85.0, 160.0));
        breedWeightMap.put("Greater Swiss Mountain Dog", new Pair<>(85.0, 140.0));
        breedWeightMap.put("Greyhound", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Harrier", new Pair<>(45.0, 60.0));
        breedWeightMap.put("Havanese", new Pair<>(7.0, 13.0));
        breedWeightMap.put("Hokkaido", new Pair<>(44.0, 66.0));
        breedWeightMap.put("Hovawart", new Pair<>(55.0, 90.0));
        breedWeightMap.put("Ibizan Hound", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Icelandic Sheepdog", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Irish Red and White Setter", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Irish Setter", new Pair<>(60.0, 70.0));
        breedWeightMap.put("Irish Terrier", new Pair<>(25.0, 27.0));
        breedWeightMap.put("Irish Water Spaniel", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Irish Wolfhound", new Pair<>(140.0, 180.0));
        breedWeightMap.put("Italian Greyhound", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Jack Russell Terrier", new Pair<>(13.0, 17.0));
        breedWeightMap.put("Japanese Chin", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Japanese Spitz", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Japanese Terrier", new Pair<>(5.0, 9.0));
        breedWeightMap.put("Kai Ken", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Karelian Bear Dog", new Pair<>(44.0, 50.0));
        breedWeightMap.put("Keeshond", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Kerry Blue Terrier", new Pair<>(33.0, 40.0));
        breedWeightMap.put("King Charles Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Kishu Ken", new Pair<>(30.0, 60.0));
        breedWeightMap.put("Komondor", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Kooikerhondje", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Korean Jindo Dog", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Kromfohrlander", new Pair<>(22.0, 28.0));
        breedWeightMap.put("Kuvasz", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Labrador Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Lagotto Romagnolo", new Pair<>(24.0, 35.0));
        breedWeightMap.put("Lakeland Terrier", new Pair<>(15.0, 17.0));
        breedWeightMap.put("Lancashire Heeler", new Pair<>(6.0, 13.0));
        breedWeightMap.put("Landseer", new Pair<>(100.0, 180.0));
        breedWeightMap.put("Leonberger", new Pair<>(90.0, 170.0));
        breedWeightMap.put("Lhasa Apso", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Lowchen", new Pair<>(8.0, 18.0));
        breedWeightMap.put("Maltese", new Pair<>(4.0, 7.0));
        breedWeightMap.put("Manchester Terrier", new Pair<>(12.0, 22.0));
        breedWeightMap.put("Maremma Sheepdog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Mastiff", new Pair<>(175.0, 190.0));
        breedWeightMap.put("Miniature Bull Terrier", new Pair<>(25.0, 33.0));
        breedWeightMap.put("Miniature Pinscher", new Pair<>(8.0, 12.0));
        breedWeightMap.put("Miniature Schnauzer", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Neapolitan Mastiff", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Newfoundland", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Italian Greyhound", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Jack Russell Terrier", new Pair<>(13.0, 17.0));
        breedWeightMap.put("Japanese Chin", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Japanese Spitz", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Japanese Terrier", new Pair<>(5.0, 9.0));
        breedWeightMap.put("Kai Ken", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Karelian Bear Dog", new Pair<>(44.0, 50.0));
        breedWeightMap.put("Keeshond", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Kerry Blue Terrier", new Pair<>(33.0, 40.0));
        breedWeightMap.put("King Charles Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Kishu Ken", new Pair<>(30.0, 60.0));
        breedWeightMap.put("Komondor", new Pair<>(80.0, 100.0));
        breedWeightMap.put("Kooikerhondje", new Pair<>(20.0, 30.0));
        breedWeightMap.put("Korean Jindo Dog", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Kromfohrlander", new Pair<>(22.0, 28.0));
        breedWeightMap.put("Kuvasz", new Pair<>(70.0, 115.0));
        breedWeightMap.put("Labrador Retriever", new Pair<>(55.0, 80.0));
        breedWeightMap.put("Lagotto Romagnolo", new Pair<>(24.0, 35.0));
        breedWeightMap.put("Lakeland Terrier", new Pair<>(15.0, 17.0));
        breedWeightMap.put("Lancashire Heeler", new Pair<>(6.0, 13.0));
        breedWeightMap.put("Landseer", new Pair<>(100.0, 180.0));
        breedWeightMap.put("Leonberger", new Pair<>(90.0, 170.0));
        breedWeightMap.put("Lhasa Apso", new Pair<>(12.0, 18.0));
        breedWeightMap.put("Lowchen", new Pair<>(8.0, 18.0));
        breedWeightMap.put("Maltese", new Pair<>(4.0, 7.0));
        breedWeightMap.put("Manchester Terrier", new Pair<>(12.0, 22.0));
        breedWeightMap.put("Maremma Sheepdog", new Pair<>(77.0, 99.0));
        breedWeightMap.put("Mastiff", new Pair<>(175.0, 190.0));
        breedWeightMap.put("Miniature Bull Terrier", new Pair<>(25.0, 33.0));
        breedWeightMap.put("Miniature Pinscher", new Pair<>(8.0, 12.0));
        breedWeightMap.put("Miniature Schnauzer", new Pair<>(11.0, 20.0));
        breedWeightMap.put("Neapolitan Mastiff", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Newfoundland", new Pair<>(100.0, 150.0));
        breedWeightMap.put("Norfolk Terrier", new Pair<>(11.0, 12.0));
        breedWeightMap.put("Norwegian Buhund", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Norwegian Elkhound", new Pair<>(48.0, 55.0));
        breedWeightMap.put("Norwegian Lundehund", new Pair<>(13.0, 15.0));
        breedWeightMap.put("Norwich Terrier", new Pair<>(11.0, 12.0));
        breedWeightMap.put("Nova Scotia Duck Tolling Retriever", new Pair<>(35.0, 50.0));
        breedWeightMap.put("Old English Sheepdog", new Pair<>(60.0, 100.0));
        breedWeightMap.put("Otterhound", new Pair<>(80.0, 115.0));
        breedWeightMap.put("Papillon", new Pair<>(4.0, 9.0));
        breedWeightMap.put("Pekingese", new Pair<>(7.0, 14.0));
        breedWeightMap.put("Pembroke Welsh Corgi", new Pair<>(25.0, 30.0));
        breedWeightMap.put("Perro de Presa Canario", new Pair<>(88.0, 110.0));
        breedWeightMap.put("Peruvian Inca Orchid", new Pair<>(26.0, 55.0));
        breedWeightMap.put("Petit Basset Griffon Vendeen", new Pair<>(25.0, 40.0));
        breedWeightMap.put("Pharaoh Hound", new Pair<>(45.0, 55.0));
        breedWeightMap.put("Plott Hound", new Pair<>(50.0, 60.0));
        breedWeightMap.put("Pointer", new Pair<>(45.0, 75.0));
        breedWeightMap.put("Polish Lowland Sheepdog", new Pair<>(30.0, 35.0));
        breedWeightMap.put("Pomeranian", new Pair<>(3.0, 7.0));
        breedWeightMap.put("Poodle", new Pair<>(4.0, 70.0));
        breedWeightMap.put("Portuguese Water Dog", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Preso Canario", new Pair<>(83.0, 110.0));
        breedWeightMap.put("Pug", new Pair<>(14.0, 18.0));
        breedWeightMap.put("Puli", new Pair<>(25.0, 35.0));
        breedWeightMap.put("Pumi", new Pair<>(18.0, 33.0));
        breedWeightMap.put("Pyrenean Mastiff", new Pair<>(130.0, 220.0));
        breedWeightMap.put("Pyrenean Shepherd", new Pair<>(15.0, 30.0));
        breedWeightMap.put("Rat Terrier", new Pair<>(10.0, 25.0));
        breedWeightMap.put("Redbone Coonhound", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Rhodesian Ridgeback", new Pair<>(70.0, 85.0));
        breedWeightMap.put("Rottweiler", new Pair<>(80.0, 135.0));
        breedWeightMap.put("Russian Toy", new Pair<>(2.0, 6.0));
        breedWeightMap.put("Russian Tsvetnaya Bolonka", new Pair<>(4.0, 11.0));
        breedWeightMap.put("Saint Bernard", new Pair<>(140.0, 180.0));
        breedWeightMap.put("Saluki", new Pair<>(40.0, 60.0));
        breedWeightMap.put("Samoyed", new Pair<>(50.0, 65.0));
        breedWeightMap.put("Schapendoes", new Pair<>(26.0, 44.0));
        breedWeightMap.put("Schipperke", new Pair<>(10.0, 16.0));
        breedWeightMap.put("Scottish Deerhound", new Pair<>(85.0, 110.0));
        breedWeightMap.put("Scottish Terrier", new Pair<>(18.0, 22.0));
        breedWeightMap.put("Sealyham Terrier", new Pair<>(20.0, 24.0));
        breedWeightMap.put("Shetland Sheepdog", new Pair<>(14.0, 27.0));
        breedWeightMap.put("Shiba Inu", new Pair<>(17.0, 23.0));
        breedWeightMap.put("Shih Tzu", new Pair<>(9.0, 16.0));
        breedWeightMap.put("Siberian Husky", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Silky Terrier", new Pair<>(8.0, 11.0));
        breedWeightMap.put("Skye Terrier", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Sloughi", new Pair<>(45.0, 70.0));
        breedWeightMap.put("Small Munsterlander Pointer", new Pair<>(35.0, 60.0));
        breedWeightMap.put("Smooth Fox Terrier", new Pair<>(15.0, 18.0));
        breedWeightMap.put("Soft Coated Wheaten Terrier", new Pair<>(30.0, 40.0));
        breedWeightMap.put("Spanish Water Dog", new Pair<>(30.0, 49.0));
        breedWeightMap.put("Spinone Italiano", new Pair<>(64.0, 85.0));
        breedWeightMap.put("Staffordshire Bull Terrier", new Pair<>(24.0, 38.0));
        breedWeightMap.put("Standard Schnauzer", new Pair<>(30.0, 50.0));
        breedWeightMap.put("Sussex Spaniel", new Pair<>(35.0, 45.0));
        breedWeightMap.put("Swedish Lapphund", new Pair<>(16.0, 29.0));
        breedWeightMap.put("Swedish Vallhund", new Pair<>(20.0, 35.0));
        breedWeightMap.put("Tibetan Mastiff", new Pair<>(90.0, 150.0));
        breedWeightMap.put("Tibetan Spaniel", new Pair<>(9.0, 15.0));
        breedWeightMap.put("Tibetan Terrier", new Pair<>(18.0, 30.0));
        breedWeightMap.put("Toy Fox Terrier", new Pair<>(3.0, 7.0));
        breedWeightMap.put("Treeing Walker Coonhound", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Vizsla", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Weimaraner", new Pair<>(55.0, 85.0));
        breedWeightMap.put("Welsh Springer Spaniel", new Pair<>(35.0, 55.0));
        breedWeightMap.put("Welsh Terrier", new Pair<>(20.0, 21.0));
        breedWeightMap.put("West Highland White Terrier", new Pair<>(13.0, 22.0));
        breedWeightMap.put("Whippet", new Pair<>(25.0, 40.0));
        breedWeightMap.put("Wire Fox Terrier", new Pair<>(15.0, 18.0));
        breedWeightMap.put("Wirehaired Pointing Griffon", new Pair<>(50.0, 70.0));
        breedWeightMap.put("Wirehaired Vizsla", new Pair<>(45.0, 65.0));
        breedWeightMap.put("Xoloitzcuintli", new Pair<>(10.0, 50.0));
        breedWeightMap.put("Yorkshire Terrier", new Pair<>(4.0, 7.0));

        return breedWeightMap.getOrDefault(breed, new Pair<>(0.0, 0.0));}


    private Double calculateIdealWeight(Pair<Double, Double> weightRange) {
        return (weightRange.first + weightRange.second) / 2;
    }
    private String evaluateWeight(double petWeight, Pair<Double, Double> weightRange) {
        if (petWeight < weightRange.first) {
            return "Underweight";
        } else if (petWeight >= weightRange.first && petWeight <= weightRange.second) {
            return "Ideal weight";
        } else {
            return "Overweight";
        }
    }


}