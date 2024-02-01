package com.example.testsample;

import android.content.Intent;
import android.content.SharedPreferences;
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


public class DogTraining extends AppCompatActivity {

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
    private static final String LAST_RANDOM_COMMAND_TIME_KEY = "lastRandomCommandTime";
    private static final long ONE_DAY_MILLIS = 6 * 60 * 60 * 1000;


    private static final int DAILY_REMINDER_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_training);

        commandTextView = findViewById(R.id.commandTextView);
        rewardButton = findViewById(R.id.rewardButton);
        nextCommandButton = findViewById(R.id.nextCommandButton);

        // Initialize the lists of commands for each level
        basicCommands = Arrays.asList("Attention", "Come", "Sit", "Down");
        intermediateCommands = Arrays.asList("Leave It", "Wait", "Crawl");
        advancedCommands = Arrays.asList("Loose Leash Walking", "Settle", "Take It", "Drop It", "Place", "Wave", "Touch");


        commandInstructions = new HashMap<>();
        commandInstructions.put("Attention", "Getting your dog’s attention is the foundation of all interactions. Say his name and, when he looks at you, mark the behavior you like by saying 'yes!' and giving a small tidbit of a treat. If he doesn’t look at you, show him the treat and hold it near your face. Say his name and treat and praise when he looks. Do the exercise a few times. End while he’s still happy and exuberant. Once he gets the idea that looking at you is rewarding, you can just say the name or command and praise and reward. When he understands that looking at you is fun, then you can add a 'look' or 'watch' command.");

        commandInstructions.put("Come", "In the beginning, show your pup a high-value treat. Make sure you use something he really likes. Then, say his name and 'Come!' in a happy tone. When teaching this, you can first show him the treat as a lure then give the command. Make it a party to come to you! Start with your dog on a leash at first. Once he learns it, use a long line. It’s a longer leash so that he can’t just run off. As in all commands, start without distractions. When he reaches you, mark the behavior. Say 'Yes, good come!' Give him a jackpot of treats. After he starts coming reliably, stop showing the lure treat and just give the reward treat.");

        commandInstructions.put("Leave It", "You can hold a treat in a closed fist and say 'Leave it.' Be patient. When the dog takes his nose off your fist, say 'Yes!' and give him a treat from the other hand. Don’t give the treat he was sniffing, or he’ll learn to persist in getting the treat out of that hand and not give up. Another way to teach this command is to have your dog on a six-foot leash and hold it where there’s just a little slack, but the leash isn’t tight. Throw a treat about six feet away, well out of his reach. Make sure he sees you throw it. As it hits the ground, say 'Leave it!' Be patient. When he stops pulling towards it and there’s slack in the leash, say 'Yes! Good leave it' and give a reward from your hand. He shouldn’t get the treat from the floor or he’ll learn he can get the forbidden item you want him to leave.");

        commandInstructions.put("Sit", "After your dog has had a sufficient amount of exercise, hold a treat just above his nose and slowly move it backward. When his rear hits the floor, calmly say 'Yes! Good sit!' If he jumps for the treat, you’re probably holding it too high or he’s too excited.");

        commandInstructions.put("Down", "Have your dog sit. Make sure he’s had a sufficient amount of exercise first. Then, put a treat right in front of his nose and slowly move the treat straight down towards the floor. Wait him out until he lies down. If he gets up or crouches, you may be moving the treat downward too quickly or he may be too energetic. As soon as his whole body touches the floor, calmly say 'Yes, good down.'");

        commandInstructions.put("Loose Leash Walking", "If possible, first have your dog exercised before practicing. Play fetch. Do some other training exercises first to stimulate his mind. Pick a side. You want to be consistent in which side your dog will walk on—your left or right. Have him sit or stand next to you. Give a treat when he is calm next to you. Choose what your command will be. Common commands are 'let’s go' or 'walk.' Say your dog’s name and then the command. Take a step with a treat in the hand next to him. Give the treat when he stays next to you.");

        commandInstructions.put("Wait", "Have your dog on a short leash with some slack and stop and say 'Wait.' When your dog stops pulling and is next to you, reward and praise. Make sure that he calms down for at least a few seconds before moving forward again. Make him wait longer periods as he’s able to before moving forward.");

        commandInstructions.put("Settle", "Have your pup on a loose leash and when he lies down, tell him 'settle,' calmly praise him, and give him a small treat. This is different than teaching him to lie down on cue. In teaching 'settle', the dog should be rewarded whenever he is calm on his own. You can toss the treat to him if it lands close enough and doesn’t make him get up.");

        commandInstructions.put("Take It", "Hold a favorite toy right in his reach. He should automatically want to grab it. If he doesn’t, try to make it more alluring, by squeaking it or waving it back and forth. The second he grabs the toy, praise him with 'good take it.' You can give him a small treat too, but just one treat. You want him to love grabbing the toy. Praise him if he runs around with the toy. Don’t worry if he doesn’t bring it back, but it’s great if he does! It’s ok to trade the toy for a treat if he doesn’t want to release it.");

        commandInstructions.put("Drop It", "Play tug with a favorite toy and say 'drop it.' Hold a treat right at his nose level, so he has to drop the toy to get the treat. As soon as he drops it, say 'Yes! Good drop it' and give him the treat. Toss the toy again so he can grab it, then play the game again. You can use two toys so you can be ready to engage him with the other toy when he drops the one he has. Sometimes this method works best with a toy with a squeaker or noise. It keeps his interest.");

        commandInstructions.put("Place", "Place is a location where you want your dog to go and stay until you tell him to move. You can use a dog bed or a mat. Practice by leading him there with a treat in your hand and giving him the command, 'Place.' If he doesn’t go there by himself, lure him there by tossing a treat on the mat or bed and use the command 'Place.' Reward him with a treat when he’s on the mat and not before. Once he’s on the mat, give him a treat when he stays there. Say 'Yes! Good place.'");

        commandInstructions.put("Wave", "This trick is cute, and it’s also a good stretching exercise. Have your dog sit or stand. Hold a treat just in front of his nose. You’re going to take your hand down in front of him in the direction you want his paw to go. You’re not going to let him grab the treat, but keep it close enough to his nose so he smells it. While he’s smelling it, move your hand down and to the side. The dog will lift his paw. Praise him and give him the treat.");

        commandInstructions.put("Crawl", "This is a good stretching exercise too. You can use a target if you want. Hold a treat to your dog’s nose. Keep the treat close enough so that he smells it but doesn’t grab it. Take your hand and sweep it down and away from him. If he doesn’t automatically lie down, you can tap your hand on the floor in front of him or lure him down. He will follow the treat. Keep the treat close to the floor, so he stays in a lying down position. Praise and give the treat.");

        commandInstructions.put("Touch", "Hold your hand, fist closed, right in front of his nose. Don’t let him grab the treat, but keep it close enough so he smells it. When he sniffs your hand, say 'Yes! Good touch' and give the treat. This trick is good for agility, and it’s also good for getting your dog to touch you. You can use this when you want your dog to come close to you. You can use it to move him in a certain direction. This is especially helpful if you’re working with a dog who’s afraid. You can move him closer to what he’s afraid of without making him get too close.");


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


            trainingLessonRef = usersRef.child("pets").child(selectedPetName).child("dogTrainingLesson");
        }


        setLevelCommands();


        Collections.shuffle(currentCommands);


        checkAndStartNextLevel();
        checkAndGiveRandomCommand();

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
                    // User hasn't completed any level yet, start from level 1
                    level = 1;
                }

                Log.d("DogTraining", "Current level from Firebase: " + level);

                currentLevel = level;
                setLevelCommands();
                Collections.shuffle(currentCommands);
                updateCommand();

                // Enable nextCommandButton based on the current level
                nextCommandButton.setEnabled(currentLevel <= 3);

                // Check if the pet has completed level 3 and progress to level 4
                if (currentLevel == 3) {
                    progressToLevel4();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DogTraining", "Failed to read training lesson from Firebase", databaseError.toException());
            }
        });
    }

    private void progressToLevel4() {
        // Check if pet has completed level 3
        if (currentCommandIndex >= currentCommands.size()) {
            currentLevel = 4; // Progress to level 4
            setLevelCommands();
            currentCommandIndex = 0;
            updateCommand();
            nextCommandButton.setEnabled(false); // Disable the nextCommandButton until the new level is completed

            // Save the new level to Firebase
            saveTrainingLevel();

            // Enable nextCommandButton for level 4
            nextCommandButton.setEnabled(true);

            // Check if a random command needs to be given
            checkAndGiveRandomCommand();
        }
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
                // All levels completed
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
            // All commands in the current level completed
            commandTextView.setText("Level " + currentLevel + " completed!");
            rewardButton.setEnabled(false);

            // Save the completed level and completion time as trainingLesson
            saveTrainingLesson();

            if (currentLevel < 3) {
                // Progress to the next level
                currentLevel++;
                setLevelCommands();
                currentCommandIndex = 0;
                updateCommand();
                nextCommandButton.setEnabled(false); // Disable the nextCommandButton until the new level is completed

                // Save the new level to Firebase
                saveTrainingLevel();
            } else {
                // All levels completed
                commandTextView.setText("Training completed!");
                nextCommandButton.setEnabled(false);
            }
            if (currentLevel == 3 && currentCommandIndex >= currentCommands.size()) {
                progressToLevel4();
            }
            if (currentLevel == 4) {
                // Enable nextCommandButton for level 4
                nextCommandButton.setEnabled(true);

                // Check if a random command needs to be given
                checkAndGiveRandomCommand();
            }
        }
    }

    private void checkAndGiveRandomCommand() {
        if (currentLevel == 4) {
            long lastRandomCommandTime = getLastRandomCommandTime();

            // Check if a day has passed since the last random command
            if (System.currentTimeMillis() - lastRandomCommandTime >= ONE_DAY_MILLIS) {
                showRandomCommand();
                saveLastRandomCommandTime();
            }
        }
    }
    private long getLastRandomCommandTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        return preferences.getLong(LAST_RANDOM_COMMAND_TIME_KEY, 0);
    }

    private void saveLastRandomCommandTime() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LAST_RANDOM_COMMAND_TIME_KEY, System.currentTimeMillis());
        editor.apply();
    }
    private void showRandomCommand() {
        // Give the pet one random command from any level
        String randomCommand = getRandomCommand();
        commandTextView.setText("Random Command: " + randomCommand + "\n\n" + getInstruction(randomCommand));

        // Disable the nextCommandButton after giving the random command
        nextCommandButton.setEnabled(false);
    }
    private String getRandomCommand() {
        // Combine all commands from different levels
        List<String> allCommands = new ArrayList<>();
        allCommands.addAll(basicCommands);
        allCommands.addAll(intermediateCommands);
        allCommands.addAll(advancedCommands);

        // Shuffle the combined list and pick one random command
        Collections.shuffle(allCommands);

        // Ensure only one command is given
        return allCommands.isEmpty() ? "No commands available" : allCommands.get(0);
    }
    private void saveTrainingLevel() {
        // Save the current level to Firebase
        trainingLessonRef.child("level").setValue(currentLevel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully saved the new level
                Log.d("DogTraining", "Training level saved to Firebase: " + currentLevel);
            } else {
                // Failed to save the new level
                Log.e("DogTraining", "Failed to save training level to Firebase", task.getException());
            }
        });
    }

    private String getInstruction(String command) {
        // Check if the command exists in the hardcoded instructions map
        if (commandInstructions.containsKey(command)) {
            return "Instruction: " + commandInstructions.get(command);
        } else {
            return "No instruction available for this command.";
        }
    }


    private void saveTrainingLesson() {
        // Save the completed level and completion time as trainingLesson
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("level", currentLevel);
        updateMap.put("completionTime", System.currentTimeMillis());

        trainingLessonRef.updateChildren(updateMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Successfully saved the trainingLesson
                Log.d("DogTraining", "Training lesson saved to Firebase: " + currentLevel);
            } else {
                // Failed to save the trainingLesson
                Log.e("DogTraining", "Failed to save training lesson to Firebase", task.getException());
            }
        });
    }

    private void giveTreat() {
        Toast.makeText(this, "Good job! Treat given for " + currentCommands.get(currentCommandIndex), Toast.LENGTH_SHORT).show();
        currentCommandIndex++;
        updateCommand();
    }
    // Add this method to your DogTraining class


    private void nextCommand() {
        if (currentCommandIndex < currentCommands.size()) {
            Toast.makeText(this, "Try again: " + currentCommands.get(currentCommandIndex), Toast.LENGTH_SHORT).show();
        } else {
            // All commands in the current level completed
            Toast.makeText(this, "Level " + currentLevel + " completed!", Toast.LENGTH_SHORT).show();
            rewardButton.setEnabled(false);

            // Save the completed level and completion time as trainingLesson
            saveTrainingLesson();

            // Move to the next level only if the current level is completed
            if (currentLevel < 3) {
                currentLevel++;
                setLevelCommands();
                currentCommandIndex = 0;
                updateCommand();
                nextCommandButton.setEnabled(false); // Disable the nextCommandButton until the new level is completed
            } else {
                // All levels completed
                commandTextView.setText("Training completed!");
                nextCommandButton.setEnabled(false);
            }
        }
    }
}
