package com.example.testsample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CatTraining extends AppCompatActivity {

    private TextView commandTextView;
    private Button rewardButton;
    private Button nextCommandButton;

    private TextView tvSelectedPet;
    private static List<String> basicCommands;
    private static List<String> intermediateCommands;
    private static List<String> advancedCommands;

    private List<String> currentCommands;
    private int currentCommandIndex = 0;
    private int currentLevel = 1;

    private DatabaseReference trainingLessonRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String selectedPetName;
    private HashMap<String, String> commandInstructions;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_training);

        commandTextView = findViewById(R.id.commandTextView);
        rewardButton = findViewById(R.id.rewardButton);
        nextCommandButton = findViewById(R.id.nextCommandButton);

        // Initialize the lists of commands for each level
        basicCommands = Arrays.asList("Gentle");
        intermediateCommands = Arrays.asList("Find It", "Target", "Sit");
        advancedCommands = Arrays.asList("Stay", "Come", "In The Box");


        commandInstructions = new HashMap<>();
        commandInstructions.put("Gentle", "Say 'gentle' as your cat or kitten licks your hand, pulling your hand away calmly if she begins to nip or bite.");

        commandInstructions.put("Find It", "Toss high-value treats at your cat's paws, and once your cat can follow the toss, add the phrase 'Find It'. Yes, it's that simple. ");

        commandInstructions.put("Target", "Teach your cat to be alert to the target by presenting it two inches in front of your cat's nose. The moment she touches it, click and reward her. Once your cat reliably moves to the target, say the word 'target' to put this behavior on cue.");

        commandInstructions.put("Sit", "Whenever your cat sits naturally, click and give her a reward. Soon you'll notice your cat sitting to cue you when you bring the treats out. Add the word 'sit' once you can predict her behavior. Then, try luring her into position with a target wand or pointing signal. Click and reward this pose. Gradually phase off clicking every correct response, using the clicker and treats intermittently. Intermittent rewards offer a more powerful way to teach—if kitty never knows when a reward may appear, she's more likely to perform.");

        commandInstructions.put("On Your Mat", "Create a cat mat by laying a flat mat, towel, or cloth napkin on the counter, sofa, or tabletop. Curiosity might not kill your cat, but it will get the better of her! When she steps on the mat, click. Then toss a treat slightly away from the mat, so your cat has to come back for the next round. Gradually introduce using the cue 'on your mat.'");

        commandInstructions.put("Stay", "Once your cat goes to her mat willingly and remains there, introduce the 'stay' cue.");

        commandInstructions.put("Come", "Cats can learn to come from the minute they enter your home. Pair positive experiences and the shake of a treat cup with the word 'come.' To do this, put treats in a cup or container and shake and reward until your cat recognizes the sound. Click and reward your cat when she arrives. Slowly increase the timing between saying 'come' and shaking the treats until she comes on cue. Gradually phase out the clicker and reward her intermittently");

        commandInstructions.put("In The Box", "pull out the cat carrier long before you ever need it, hiding treats and even feeding your cat or kitten portions of her meal in it. When your cat jumps into the carrier or a box, click and reward the behavior. When your cat prompts you, add the cue 'in the box.' Gradually add carrying her about in her box/carrier, rewarding her after each ride.");




        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            tvSelectedPet = findViewById(R.id.tv_selectedPet);
            tvSelectedPet.setText("Selected Pet: " + selectedPetName);
            tvSelectedPet.setVisibility(View.VISIBLE);
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);


            trainingLessonRef = usersRef.child("pets").child(selectedPetName).child("catTrainingLesson");
        }


        setLevelCommands();


        Collections.shuffle(currentCommands);


        checkAndStartNextLevel();


        updateCommand();

        rewardButton.setOnClickListener(v -> giveTreat());

        nextCommandButton.setOnClickListener(v -> nextCommand());

    }


    private void checkAndStartNextLevel() {
        trainingLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer level = dataSnapshot.child("level").getValue(Integer.class);

                if (level == null) {

                    level = 1;
                }

                Log.d("CatTraining", "Current level from Firebase: " + level);

                currentLevel = level;
                setLevelCommands();
                Collections.shuffle(currentCommands);
                updateCommand();


                nextCommandButton.setEnabled(currentLevel <= 3);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CatTraining", "Failed to read training lesson from Firebase", databaseError.toException());
            }
        });
    }
    private void setLevelCommands() {
        switch (currentLevel) {
            case 1:
                currentCommands = new ArrayList<>(basicCommands);
                break;
            case 2:
                currentCommands = new ArrayList<>(intermediateCommands);
                break;
            case 3:
                currentCommands = new ArrayList<>(advancedCommands);
                break;
            default:

                commandTextView.setText("Training completed!");
                rewardButton.setEnabled(false);
                nextCommandButton.setEnabled(false);
                return;
        }
    }


    private void updateCommand() {
        if (currentCommandIndex < currentCommands.size()) {
            String currentCommand = currentCommands.get(currentCommandIndex);
            String instruction = getInstruction(currentCommand);
            commandTextView.setText("Command: " + currentCommand + "\n\n" + instruction);
        } else {

            commandTextView.setText("Level " + currentLevel + " completed!");
            rewardButton.setEnabled(false);

            saveTrainingLesson();


            if (currentLevel < 3) {

                currentLevel++;
                setLevelCommands();
                currentCommandIndex = 0;
                updateCommand();
                nextCommandButton.setEnabled(false); //


                saveTrainingLevel();
            } else {

                commandTextView.setText("Training completed!");
                nextCommandButton.setEnabled(false);
                giveRandomCommand();

            }
        }
    }




    private void saveTrainingLevel() {

        trainingLessonRef.child("level").setValue(currentLevel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully saved the new level
                Log.d("CatTraining", "Training level saved to Firebase: " + currentLevel);
            } else {
                // Failed to save the new level
                Log.e("CatTraining", "Failed to save training level to Firebase", task.getException());
            }
        });
    }


    private void saveTrainingLesson() {

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("level", currentLevel);
        updateMap.put("completionTime", System.currentTimeMillis());

        trainingLessonRef.updateChildren(updateMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d("CatTraining", "Training lesson saved to Firebase: " + currentLevel);
            } else {

                Log.e("CatTraining", "Failed to save training lesson to Firebase", task.getException());
            }
        });
    }
    private String getInstruction(String command) {

        if (commandInstructions.containsKey(command)) {
            return "Instruction: " + commandInstructions.get(command);
        } else {
            return "No instruction available for this command.";
        }
    }
    private void giveRandomCommand() {
        if (currentLevel == 3 && currentCommandIndex >= currentCommands.size()) {
            //
            int randomLevel = new Random().nextInt(3) + 1;
            currentLevel = randomLevel;
            setLevelCommands();
            Collections.shuffle(currentCommands);
            currentCommandIndex = 0;
            updateCommand();
            nextCommandButton.setEnabled(false);


            saveTrainingLevel();

            Toast.makeText(this, "Random command from level " + currentLevel, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Complete level 3 commands first!", Toast.LENGTH_SHORT).show();
        }
    }
    private void giveTreat() {
        Toast.makeText(this, "Good job! Treat given for " + currentCommands.get(currentCommandIndex), Toast.LENGTH_SHORT).show();
        currentCommandIndex++;
        updateCommand();
    }

    private void nextCommand() {
        if (currentCommandIndex < currentCommands.size()) {
            Toast.makeText(this, "Try again: " + currentCommands.get(currentCommandIndex), Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "Level " + currentLevel + " completed!", Toast.LENGTH_SHORT).show();
            rewardButton.setEnabled(false);


            saveTrainingLesson();


            if (currentLevel < 3) {
                currentLevel++;
                setLevelCommands();
                currentCommandIndex = 0;
                updateCommand();
                nextCommandButton.setEnabled(false);
            } else {

                commandTextView.setText("Training completed!");
                nextCommandButton.setEnabled(false);
            }
        }}
}
