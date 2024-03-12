package com.example.testsample;

import static androidx.fragment.app.FragmentManager.TAG;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import android.app.Notification;
import android.app.Notification.Builder;


public class Reminders extends AppCompatActivity {

    private Button addReminderButton;
    private String currentUserId;
    private ArrayList<Reminder> reminderList;
    private ReminderAdapter reminderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        addReminderButton = findViewById(R.id.addReminderBtn);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reminderList = new ArrayList<>();
        // Instantiate ReminderAdapter in Reminders activity
        reminderAdapter = new ReminderAdapter(this, reminderList, currentUserId);


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
        scheduleReminderNotifications();
    }

    private void scheduleReminderNotifications() {
        for (Reminder reminder : reminderList) {
            if (isReminderDueInSevenDays(reminder)) {
                scheduleNotification(getNotification("Reminder Due", "Schedule for " + reminder.getPetName() + " is due in 7 days."), 604800000); // 7 days in milliseconds
                scheduleNotification(getNotification("Reminder Due", "Schedule for " + reminder.getPetName() + " is due in 24 hours."), 86400000); // 24 hours in milliseconds
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
            return reminderDate.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
        }
    }

    private Notification getNotification(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "reminder_channel";
            NotificationChannel channel = new NotificationChannel(channelId, "Reminder Channel", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        return builder.build();
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
                addReminder(petName, tag, description, date, Boolean.parseBoolean(status));

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

    private void addReminder(String petName, String tag, String description, String date, boolean status) {
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
        Reminder reminder = new Reminder(petName, tag, description, date, status, reminderId);

        // Set the value of the reminder using the generated key
        remindersRef.setValue(reminder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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


    private void fetchRemindersAndUpdateUI() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

}
