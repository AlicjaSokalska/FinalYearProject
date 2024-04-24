package com.example.testsample;

import static androidx.fragment.app.FragmentManager.TAG;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Reminders extends AppCompatActivity {

    private ImageButton addReminderButton;
    private String currentUserId;
    private ArrayList<Reminder> reminderList;
    private ReminderAdapter reminderAdapter;
    private FirebaseAuth mAuth;
    private CustomNotificationHelper customNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        addReminderButton = findViewById(R.id.addReminderBtn);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reminderList = new ArrayList<>();
        // Instantiate ReminderAdapter in Reminders activity
        reminderAdapter = new ReminderAdapter(this, reminderList, currentUserId);
       // customNotificationHelper = new CustomNotificationHelper(this); // Instantiate CustomNotificationHelper


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView remindersRecyclerView = findViewById(R.id.remindersRecyclerView);
        remindersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        remindersRecyclerView.setAdapter(reminderAdapter); // Attach the adapter to the RecyclerView


        addReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddReminderDialog();
            }
        });
        fetchRemindersAndUpdateUI();
        customNotificationHelper = new CustomNotificationHelper(this); // Instantiate CustomNotificationHelper after fetching reminders
        scheduleReminderNotifications(); // Schedule notifications after fetching reminders


        //  checkPastReminders();


        BottomNavigationView bottomNavigationView = findViewById(R.id.mainBottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("BottomNavigation", "Item selected: " + item.getTitle());
                if (item.getItemId() == R.id.action_add_pet) {
                    getSupportActionBar().setTitle("Add Pet");
                    startActivity(new Intent(Reminders.this, AddFullPetDetails.class));
                    return true;
                } else if (item.getItemId() == R.id.action_user_profile) {
                    getSupportActionBar().setTitle("User Profile");
                    startActivity(new Intent(Reminders.this,UserProfile.class));
                    return true;
                } else if (item.getItemId() == R.id.navigate_home) {
                    getSupportActionBar().setTitle("Home");
                    startActivity(new Intent(Reminders.this, StartUpPage.class));
                    return true;
                }
                else if (item.getItemId() == R.id.navigate_reminder) {
                    getSupportActionBar().setTitle("Reminders");
                    startActivity(new Intent(Reminders.this, Reminders.class));
                    return true;
                }
                return false;
            }
        });
    }
    private void checkPastReminders() {
        for (final Reminder reminder : reminderList) {
            if (reminder.isStatus() && reminder.getRepeatOption() != null && !reminder.getRepeatOption().equalsIgnoreCase("never")) {
                showRemoveDialog("Reminder is Marked Complete", "The reminder for " + reminder.getPetName() + " is marked as complete and has repeat options. Do you want to remove it?", reminder);
                // Set marker as unchecked
                reminder.setStatus(false);
            } else if (reminder.isStatus() && (reminder.getRepeatOption() == null || reminder.getRepeatOption().equalsIgnoreCase("never"))) {
                showRemoveDialog("Reminder is Marked Complete", "The reminder for " + reminder.getPetName() + " is marked as complete. Do you want to remove it?", reminder);
            } else if (isReminderInPast(reminder) && (reminder.getRepeatOption() == null || reminder.getRepeatOption().equalsIgnoreCase("never"))) {
                showRemoveDialog("Reminder in the past", "The reminder for " + reminder.getPetName() + " is scheduled for a past date and repeat is set to never or is null. Do you want to remove it?", reminder);
            } else if (isReminderInPast(reminder)) {
                updateReminderDate(reminder);
            }
        }
    }



    private void updateReminderDate(Reminder reminder) {
        if (reminder.getRepeatOption() != null && !reminder.getRepeatOption().equals("Never")) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date reminderDate = dateFormat.parse(reminder.getDate());

                // Check if the reminder date is in the past
                if (reminderDate.before(calendar.getTime())) {
                    calendar.setTime(reminderDate);

                    // Reset the time to avoid issues with daylight saving time changes
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    switch (reminder.getRepeatOption()) {
                        case "Daily":
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                            break;
                        case "Weekly":
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                            break;
                        case "Monthly":
                            calendar.add(Calendar.MONTH, 1);
                            break;
                        case "Yearly":
                            calendar.add(Calendar.YEAR, 1);
                            break;
                        default:
                            // Handle unrecognized repeat options
                            break;
                    }

                    // Update the reminder date
                    Date updatedDate = calendar.getTime();
                    String updatedDateString = dateFormat.format(updatedDate);
                    reminder.setDate(updatedDateString);
                }

                // Update the reminder in the database
                DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(currentUserId)
                        .child("pets")
                        .child(reminder.getPetName())
                        .child("reminders")
                        .child(reminder.getReminderId());

                remindersRef.child("date").setValue(reminder.getDate())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Handle success
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure
                            }
                        });

            } catch (ParseException e) {
                e.printStackTrace();
                // Handle parsing exception
            }
        }
    }




    private void showRemoveDialog(String title, String message, final Reminder reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the reminder
                removeReminder(reminder);
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close the dialog
            }
        });
        builder.setCancelable(false);
        builder.show();

    }

    private boolean isReminderInPast(Reminder reminder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date reminderDate = dateFormat.parse(reminder.getDate());
            Date currentDate = new Date();
            return reminderDate != null && reminderDate.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void removeReminder(Reminder reminder) {
        // Remove from the local list
        reminderList.remove(reminder);
        // Notify the adapter about the data change
        reminderAdapter.notifyDataSetChanged();

        // Remove from the Firebase database
        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("pets")
                .child(reminder.getPetName())
                .child("reminders")
                .child(reminder.getReminderId()); // Assuming reminderId is the key in Firebase

        remindersRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Reminders.this, "Reminder removed successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error removing reminder from Firebase: " + e.getMessage());
                        Toast.makeText(Reminders.this, "Failed to remove reminder!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleReminderNotifications() {
        Log.d(TAG, "Scheduling reminder notifications...");
        Log.d(TAG, "Reminder list size: " + reminderList.size());
        for (Reminder reminder : reminderList) {
            if (isReminderDueInSevenDays(reminder)) {
                customNotificationHelper.sendNotification("Reminder Due", "Schedule for " + reminder.getPetName() + " is due in 7 days.", null);
                customNotificationHelper.sendNotification("Reminder Due", "Schedule for " + reminder.getPetName() + " is due in 24 hours.", null);
            }
        }
    }


    private boolean isReminderDueInSevenDays(Reminder reminder) {
        // Parse reminder date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date reminderDate = dateFormat.parse(reminder.getDate());
            // Get current date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            Date currentDate = calendar.getTime();
            // Check if reminder date is within 7 days
            boolean isDue = reminderDate.before(currentDate);
            Log.d("ReminderNotifications", "Reminder for " + reminder.getPetName() + " is due in 7 days: " + isDue);
            return isDue;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showAddReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Reminder");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);

        final Spinner petNameSpinner = viewInflated.findViewById(R.id.petSpinner);
        final EditText tagEditText = viewInflated.findViewById(R.id.tag_edittext);
        final EditText descriptionEditText = viewInflated.findViewById(R.id.description_edittext);
        final EditText dateEditText = viewInflated.findViewById(R.id.date_edittext);
        final Switch statusSwitch = viewInflated.findViewById(R.id.statusSwitch);

        final RadioGroup repeatRadioGroup = viewInflated.findViewById(R.id.repeat_radio_group);
        fetchPetNames(petNameSpinner);

        // Set click listener for date EditText to show DatePickerDialog
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateEditText);
            }
        });

        // Set default status to Incomplete
        statusSwitch.setChecked(false); // or true if you want it to default to Complete

        builder.setView(viewInflated);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String petName = petNameSpinner.getSelectedItem().toString();
                String tag = tagEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String date = dateEditText.getText().toString();
                String status = statusSwitch.isChecked() ? "Complete" : "Incomplete";
                String repeatOption = getRepeatOption(repeatRadioGroup.getCheckedRadioButtonId());
                addReminder(petName, tag, description, date, Boolean.parseBoolean(status), repeatOption);

                // Here you can handle the reminder data, for example, save it to a database
                // or display it in a list.
                Toast.makeText(Reminders.this, "Reminder added!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();
    }

    private String getRepeatOption(int checkedRadioButtonId) {
        if (checkedRadioButtonId == R.id.never_radio_button) {
            return "Never";
        } else if (checkedRadioButtonId == R.id.daily_radio_button) {
            return "Daily";
        } else if (checkedRadioButtonId == R.id.weekly_radio_button) {
            return "Weekly";
        } else if (checkedRadioButtonId == R.id.monthly_radio_button) { // Add this line for monthly option
            return "Monthly";
        } else if (checkedRadioButtonId == R.id.yearly_radio_button) {
            return "Yearly";
        } else {
            return "Never"; // Default to "Never" if no option is selected
        }
    }


    private void fetchPetNames(final Spinner petNameSpinner) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("pets");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> petNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String petName = snapshot.getKey();
                    petNames.add(petName);
                }
                populatePetNameSpinner(petNames, petNameSpinner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void populatePetNameSpinner(ArrayList<String> petNames, Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateEditText.setText(dateFormat.format(calendar.getTime()));
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void addReminder(String petName, String tag, String description, String date, boolean status,String repeatOption) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date reminderDate = dateFormat.parse(date);
            Date currentDate = new Date();
            if (reminderDate != null && reminderDate.before(currentDate)) {
                // Show message that reminder with a past date cannot be added
                Toast.makeText(this, "Reminder with past date cannot be added", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parsing exception
            return;
        }

        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("pets")
                .child(petName)
                .child("reminders")
                .push(); // Use push() to generate a unique key for the reminder

        // Get the unique key generated by push()
        String reminderId = remindersRef.getKey();

        // Ensure that the reminderId is not null
        if (reminderId == null) {
            // Handle the case where reminderId is null
            Log.e(TAG, "Failed to generate reminderId");
            return;
        }

        // Create a new Reminder object
        Reminder reminder = new Reminder(petName, tag, description, date, status, reminderId, repeatOption);
        // Set the value of the reminder using the generated key
        remindersRef.setValue(reminder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Fetch reminders and update UI only if the reminder is successfully added
                        fetchRemindersAndUpdateUI();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }
    public void fetchRemindersAndUpdateUI() {
        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("pets");

        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                reminderList.clear();

                // Iterate through pets
                for (DataSnapshot petSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot remindersSnapshot = petSnapshot.child("reminders");
                    // Iterate through reminders for each pet
                    for (DataSnapshot reminderSnapshot : remindersSnapshot.getChildren()) {
                        Reminder reminder = reminderSnapshot.getValue(Reminder.class);
                        reminderList.add(reminder);
                    }
                }

                // Notify adapter about data change
                reminderAdapter.notifyDataSetChanged();

                // Check if the reminder list is populated
                if (reminderList.size() > 0) {
                    // If the reminder list is populated, schedule notifications
                    customNotificationHelper = new CustomNotificationHelper(Reminders.this);
                    scheduleReminderNotifications();
                } else {
                    // Log a message if the reminder list is empty
                    Log.d(TAG, "Reminder list is empty");
                }

                // Call method to check past reminders
                checkPastReminders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
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

            Intent returnToModeIntent = new Intent(Reminders.this, SelectMode.class);
            startActivity(returnToModeIntent);
            finish();
            return true;
        } else if (itemId == R.id.menuSignOut) {

            mAuth.signOut();
            Intent signOutIntent = new Intent(Reminders .this, Login.class);
            startActivity(signOutIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
