package com.example.testsample;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private String currentUserId;
    private List<Reminder> reminderList;
    private Context context;

    // Constructor to initialize the adapter with the list of reminders
    public ReminderAdapter(Context context, List<Reminder> reminderList, String currentUserId) {
        this.context = context;
        this.reminderList = reminderList;
        this.currentUserId = currentUserId; // Initialize currentUserId
    }


    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_list_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        // Bind data to the views in each item
        Reminder reminder = reminderList.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        // Return the size of the dataset
        return reminderList.size();
    }

    // View holder class to hold references to the views for each item
    public class ReminderViewHolder extends RecyclerView.ViewHolder {
        private TextView petNameTextView;
        private TextView tagTextView;
        private TextView descriptionTextView;
        private TextView dateTextView;
        private Switch statusSwitch; // Add Switch for status
        private TextView repeatTextView;
        private String selectedRepeatOption;
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            petNameTextView = itemView.findViewById(R.id.pet_name_textview);
            tagTextView = itemView.findViewById(R.id.tag_textview);
            descriptionTextView = itemView.findViewById(R.id.description_textview);
            dateTextView = itemView.findViewById(R.id.date_textview);
            statusSwitch = itemView.findViewById(R.id.status_switch); // Find Switch for status
            repeatTextView = itemView.findViewById(R.id.repeat_textview);
            // Set click listener on the itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Reminder reminder = reminderList.get(position);
                    showOptionsDialog(reminder);
                }
            });

            // Set OnCheckedChangeListener for the Switch
            statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    Reminder reminder = reminderList.get(position);
                    updateStatus(reminder, isChecked);
                }
            });
        }

        public void bind(Reminder reminder) {
            petNameTextView.setText("Pet Name: " + reminder.getPetName());
            tagTextView.setText("Tag: " + reminder.getTag());
            descriptionTextView.setText("Description: " + reminder.getDescription());
            dateTextView.setText("Date: " + reminder.getDate());
            repeatTextView.setText("Repeat: " + reminder.getRepeatOption());
            // Set the Switch state based on reminder status
            statusSwitch.setChecked(reminder.isStatus());

            // Set the text for the status based on the reminder status
            if (reminder.isStatus()) {
                statusSwitch.setText("Complete");
            } else {
                statusSwitch.setText("Incomplete");
            }
        }

        private void updateStatus(final Reminder reminder, final boolean isChecked) {
            DatabaseReference reminderRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUserId)
                    .child("pets")
                    .child(reminder.getPetName())
                    .child("reminders")
                    .child(reminder.getReminderId())
                    .child("status");

            reminderRef.setValue(isChecked)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update the UI text based on the new status
                            String statusText = isChecked ? "Complete" : "Incomplete";
                            statusSwitch.setText(statusText);

                            // Display a toast message to confirm the update
                            Toast.makeText(context, "Status updated to " + statusText, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle any errors
                            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        // Method to show options dialog for a reminder
    private void showOptionsDialog(final Reminder reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options");

        // Add options to the dialog
        String[] options = {"Update", "Delete"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showUpdateDialog(reminder);
                        break;
                    case 1: // Delete
                        // Call method to delete reminder
                        deleteReminder(reminder);
                        break;
                }
            }
        });

        builder.show();
    }
        private void showUpdateDialog(final Reminder reminder) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Reminder");

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.updat_reminder_dialog, null);

            // Initialize views in the dialog layout
            final EditText petNameEditText = view.findViewById(R.id.pet_name_edit_text);
            final EditText tagEditText = view.findViewById(R.id.tag_edit_text);
            final EditText descriptionEditText = view.findViewById(R.id.description_edit_text);
            final EditText dateEditText = view.findViewById(R.id.date_edit_text);
            final Switch statusSwitch = view.findViewById(R.id.status_switch);
            final RadioGroup repeatRadioGroup = view.findViewById(R.id.repeat_radio_group);
            final RadioButton neverRadioButton = view.findViewById(R.id.never_radio_button);
            final RadioButton dailyRadioButton = view.findViewById(R.id.daily_radio_button);
            final RadioButton weeklyRadioButton = view.findViewById(R.id.weekly_radio_button);
            final RadioButton monthlyRadioButton = view.findViewById(R.id.monthly_radio_button);
            final RadioButton yearlyRadioButton = view.findViewById(R.id.yearly_radio_button);

            // Set initial values
            petNameEditText.setText(reminder.getPetName()); // Populate the pet name field
            tagEditText.setText(reminder.getTag());
            descriptionEditText.setText(reminder.getDescription());
            dateEditText.setText(reminder.getDate());
            statusSwitch.setChecked(reminder.isStatus());

            // Set radio button based on reminder's repeat option
            switch (reminder.getRepeatOption()) {
                case "Never":
                    neverRadioButton.setChecked(true);
                    break;
                case "Daily":
                    dailyRadioButton.setChecked(true);
                    break;
                case "Weekly":
                    weeklyRadioButton.setChecked(true);
                    break;
                case "Monthly":
                    monthlyRadioButton.setChecked(true);
                    break;
                case "Yearly":
                    yearlyRadioButton.setChecked(true);
                    break;
            }

            builder.setView(view);

            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Get updated values
                    String updatedPetName = petNameEditText.getText().toString().trim(); // Update pet name
                    String updatedTag = tagEditText.getText().toString().trim();
                    String updatedDescription = descriptionEditText.getText().toString().trim();
                    String updatedDate = dateEditText.getText().toString().trim();
                    boolean updatedStatus = statusSwitch.isChecked();
                    String updatedRepeatOption = getRepeatOption(repeatRadioGroup.getCheckedRadioButtonId());

                    updateReminder(reminder, updatedTag, updatedDescription, updatedDate, updatedStatus, updatedRepeatOption); // Pass updatedRepeatOption here
                }
            });


            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }


        private String getRepeatOption(int radioButtonId) {
            if (radioButtonId == R.id.never_radio_button) {
                return "Never";
            } else if (radioButtonId == R.id.daily_radio_button) {
                return "Daily";
            } else if (radioButtonId == R.id.weekly_radio_button) {
                return "Weekly";
            } else if (radioButtonId == R.id.monthly_radio_button) {
                return "Monthly";
            } else if (radioButtonId == R.id.yearly_radio_button) {
                return "Yearly";
            } else {
                return "";
            }
        }
        private void updateReminder(final Reminder reminder, final String updatedTag, final String updatedDescription, final String updatedDate, final boolean updatedStatus, final String updatedRepeatOption) {
            if (currentUserId == null || reminder == null) {
                // Handle null values gracefully
                Toast.makeText(context, "User ID or reminder is null", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure reminder is not null before accessing its properties
            if (reminder.getPetName() == null || reminder.getReminderId() == null) {
                Toast.makeText(context, "Reminder data is incomplete", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUserId)
                    .child("pets")
                    .child(reminder.getPetName())
                    .child("reminders")
                    .child(reminder.getReminderId());

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("tag", updatedTag);
            updateData.put("description", updatedDescription);
            updateData.put("repeatOption", updatedRepeatOption); // Add the repeat option

            // Format the updated date string to ensure consistency
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date formattedDate = dateFormat.parse(updatedDate);
                String formattedDateString = dateFormat.format(formattedDate);
                updateData.put("date", formattedDateString);
            } catch (ParseException e) {
                e.printStackTrace();
                // Handle date parsing error
                Toast.makeText(context, "Failed to parse date", Toast.LENGTH_SHORT).show();
                return;
            }

            updateData.put("status", updatedStatus);

            remindersRef.updateChildren(updateData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Reminder updated successfully
                                Toast.makeText(context, "Reminder updated", Toast.LENGTH_SHORT).show();
                            } else {
                                // Failed to update reminder
                                Toast.makeText(context, "Failed to update reminder", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


        private void deleteReminder(final Reminder reminder) {
            if (currentUserId != null) { // Ensure currentUserId is not null
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Reminder");
                builder.setMessage("Are you sure you want to delete this reminder?");

                // Add buttons to the dialog
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserId) // Assuming currentUserId is accessible here
                                .child("pets")
                                .child(reminder.getPetName())
                                .child("reminders")
                                .child(reminder.getReminderId()); // Assuming there's a method to get reminderId

                        remindersRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Reminder deleted successfully
                                        Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to delete reminder
                                        Toast.makeText(context, "Failed to delete reminder", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            } else {
                // Handle the case where currentUserId is null
                Toast.makeText(context, "Current user ID is null", Toast.LENGTH_SHORT).show();
            }
        }
    }}