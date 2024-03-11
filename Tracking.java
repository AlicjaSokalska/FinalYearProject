package com.example.testsample;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Tracking extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_updates, tv_address, tv_sensor, tv_wayPointCounts;
    Switch sw_locationsupdates, sw_gps;
    Button btn_newWaypoint, btn_showWaypoint, btn_showMap;
    //current loc
    Location currentLocation;
    //list of saved loc
    List<Location> savedLocations;

    boolean updates = false;
    //google Api
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallBack;
    LocationRequest locationRequest;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String selectedPetName;
    //timer
    private Handler locationUpdateHandler;
    private boolean isLocationUpdatesStarted = false;

    // private static final long LOCATION_UPDATE_INTERVAL = 20 * 60 * 1000;
    //private static final long LOCATION_UPDATE_INTERVAL = 30 * 1000;

    private DatabaseReference geofenceRef;
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "geofence_channel";


    private SensorManager sensorManager;
    private Sensor accelerometer;


    private int stepCount = 0;
    private double distance = 0.0;
    private double speed = 0.0;
    private long lastUpdateTime = 0;

    private TextView distanceTextView;
    private TextView speedTextView;
    private TextView stepCountTextView;
    private DatabaseReference dailyTotalRef;
    private TextView heartRateTextView;

    private Switch trackingSwitch;
    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_speed = findViewById(R.id.tv_speed);
        tv_updates = findViewById(R.id.tv_updates);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_address = findViewById(R.id.tv_address);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);
        btn_newWaypoint = findViewById(R.id.btn_newWaypoint);
        btn_showWaypoint = findViewById(R.id.btn_showWaypoint);
        btn_showMap = findViewById(R.id.btn_showMap);
        tv_wayPointCounts = findViewById(R.id.tv_breadcrumbs);

        locationUpdateHandler = new Handler();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        Intent intent = getIntent();
        if (intent.hasExtra("selectedPetName")) {
            selectedPetName = intent.getStringExtra("selectedPetName");
            TextView tv_selectedPet = findViewById(R.id.tv_selectedPet);
            tv_selectedPet.setText("Selected Pet: " + selectedPetName);
            tv_selectedPet.setVisibility(View.VISIBLE); // Set visibility to VISIBLE
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        geofenceRef = usersRef.child("pets").child(selectedPetName).child("geofence");


        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save
                updateFirebaseLocation(locationResult.getLastLocation());
                updateUIValues(locationResult.getLastLocation());
                checkGeofence(locationResult.getLastLocation());
            }
        };

        btn_newWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get gps
                // add new loc
                MyApplication myapp = (MyApplication) getApplicationContext();
                savedLocations = myapp.getMyLocations();
                savedLocations.add(currentLocation);

                tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
            }
        });
        btn_showWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to map
                Intent intent = new Intent(Tracking.this, ShowSavedLocationList.class);
                startActivity(intent);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {

                    Intent intent = new Intent(Tracking.this, MapsActivity.class);


                    intent.putExtra("latitude", currentLocation.getLatitude());
                    intent.putExtra("longitude", currentLocation.getLongitude());
                    intent.putExtra("accuracy", currentLocation.getAccuracy());

                    startActivity(intent);
                } else {
                    Toast.makeText(Tracking.this, "Location information not available", Toast.LENGTH_SHORT).show();
                }
            }
        });


        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });


        sw_locationsupdates.setChecked(false);

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationsupdates.isChecked()) {
                    // turn on
                    updateGPS();
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });
        if (sw_locationsupdates.isChecked()) {

            updateGPS();
            startLocationUpdates();
        } else {

            stopLocationUpdates();
        }
        //exercise





        dailyTotalRef = usersRef.child("pets").child(selectedPetName).child("daily_totals");

        distanceTextView = findViewById(R.id.distanceTextView);
        speedTextView = findViewById(R.id.speedTextView);
        stepCountTextView = findViewById(R.id.stepCountTextView);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        trackingSwitch = findViewById(R.id.trackingSwitch);
        trackingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTracking = isChecked;
            if (isChecked) {
                startTracking();
            } else {
                stopTracking();
            }
        });

        isTracking = trackingSwitch.isChecked();
        updateTrackingUI();
        fetchAndDisplayExerciseData();
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Geofence Channel";
            String description = "Notifications for geofence events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Geofence Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
    private void checkGeofence(Location currentLocation) {
        geofenceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Double geofenceLatitude = snapshot.child("latitude").getValue(Double.class);
                    Double geofenceLongitude = snapshot.child("longitude").getValue(Double.class);
                    Float geofenceRadius = snapshot.child("radius").getValue(Float.class);

                    if (geofenceLatitude != null && geofenceLongitude != null && geofenceRadius != null) {

                        float[] distance = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                geofenceLatitude, geofenceLongitude,
                                distance
                        );

                        if (distance[0] > geofenceRadius) {

                            Toast.makeText(Tracking.this, "Pet is outside the geofence!", Toast.LENGTH_SHORT).show();
                            sendNotification("Pet is outside the geofence!");
                        } else {

                            Toast.makeText(Tracking.this, "Pet is inside the geofence!", Toast.LENGTH_SHORT).show();
                            sendNotification("Pet is inside the geofence!");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching geofence data: " + error.getMessage());
            }
        });
    }


    /* private void startLocationUpdates() {
         locationUpdateHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 // Start location updates
                 if (ActivityCompat.checkSelfPermission(Tracking.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                     fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
                 }
                 // Schedule the next update after 20 minutes
                 locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
             }
         }, LOCATION_UPDATE_INTERVAL);
     }

     private void stopLocationUpdates() {
         locationUpdateHandler.removeCallbacksAndMessages(null);
         tv_updates.setText("Location is NOT being tracked");
         tv_lat.setText("Not tracking Location");
         tv_lon.setText("Not tracking Location");
         tv_speed.setText("Not tracking Location");
         tv_address.setText("Not tracking Location");
         tv_accuracy.setText("Not tracking Location");
         tv_altitude.setText("Not tracking Location");
         tv_sensor.setText("Not tracking Location");
         fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
     }
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // Stop location updates when the activity is destroyed
         stopLocationUpdates();
     }*/
    private void startLocationUpdates() {
        if (!isLocationUpdatesStarted) {
            // Start location updates only if not already started
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
           tv_updates.setText("Location is being tracked");
           isLocationUpdatesStarted = true;
       }
   }

    private void stopLocationUpdates() {
        if (isLocationUpdatesStarted) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
            tv_updates.setText("Location is NOT being tracked");
            isLocationUpdatesStarted = false;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "This app requires permission to be granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Tracking.this);
        // get perm
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // location var;lat,lon...
                    updateUIValues(location);
                    currentLocation = location;
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        // get location
        // update UI
    }

    private void updateUIValues(Location location) {
        // updated all the values
        if (location != null) {
            tv_lat.setText(String.valueOf(location.getLatitude()));
            tv_lon.setText(String.valueOf(location.getLongitude()));
            tv_accuracy.setText(String.valueOf(location.getAccuracy()));

            if (location.hasAltitude()) {
                tv_altitude.setText(String.valueOf(location.getAltitude()));
            } else {
                tv_altitude.setText("Not Available");
            }
            if (location.hasSpeed()) {
                tv_speed.setText(String.valueOf(location.getSpeed()));
            } else {
                tv_speed.setText("Not Available");
            }
        } else {
            Toast.makeText(this, "This Location NOT available", Toast.LENGTH_SHORT).show();
            finish();
        }

        // geo
        Geocoder geocoder = new Geocoder(Tracking.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                tv_address.setText(addresses.get(0).getAddressLine(0));
            } else {
                tv_address.setText("Address not found");
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting address", e);
            tv_address.setText("Unable to get street address");
        }

        // add new loc
        MyApplication myApp =(MyApplication)getApplicationContext();
        savedLocations = myApp.getMyLocations();
        // show the no. of waypoints
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));

    }
    private void updateFirebaseLocation(Location location) {
        if (selectedPetName != null && usersRef != null) {
            PetLocation petLocation = new PetLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getAccuracy(),
                    location.getSpeed(),
                    getAddressFromLocation(location)
            );
            usersRef.child("pets").child(selectedPetName).child("location").setValue(petLocation);


            Intent intent = new Intent("LOCATION_UPDATED");
            intent.putExtra("petLocation", petLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(Tracking.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            } else {
                return "Address not found";
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting address", e);
            return "Unable to get street address";
        }
    }



    // exercise



    private void startTracking() {

        Log.d("ExerciseActivity", "Pet activity is being tracked");

    }

    private void stopTracking() {
        Log.d("ExerciseActivity", "Daily total saved");
        saveDailyTotal(stepCount, distance);

        stepCount = 0;
        distance = 0.0;

        saveExerciseData(selectedPetName, stepCount, distance);

        Log.d("ExerciseActivity", "Data resetted");
    }

    private void updateTrackingUI() {
        if (isTracking) {
            Log.d("ExerciseActivity", "Pet activity is being tracked");
        } else {
            Log.d("ExerciseActivity", "Activity is not tracked");
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        fetchAndDisplayExerciseData();


    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTracking && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastUpdateTime > 500) { // Update every 500 milliseconds
                lastUpdateTime = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                if (acceleration > 2.0) {
                    stepCount++;
                    updateStepCount();

                    distance += 0.76;
                    updateDistance();

                    // Speed = Distance / Time
                    speed = distance / ((currentTime - lastUpdateTime) / 1000.0);
                    updateSpeed();

                    saveExerciseData(selectedPetName, stepCount, distance);


                }}


            Calendar currentCalendar = Calendar.getInstance();
            if (currentCalendar.get(Calendar.HOUR_OF_DAY) == 11 && currentCalendar.get(Calendar.MINUTE) == 59) {
                saveDailyTotal(stepCount, distance);
            }


            Calendar midnightCalendar = Calendar.getInstance();
            if (midnightCalendar.get(Calendar.HOUR_OF_DAY) == 0 && midnightCalendar.get(Calendar.MINUTE) == 0) {

                stepCount = 0;
                distance = 0.0;
                saveExerciseData(selectedPetName, stepCount, distance);
            }
        }
    }









    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }

    private void fetchAndDisplayExerciseData() {
        if (selectedPetName != null) {
            DatabaseReference exerciseRef = usersRef.child("pets").child(selectedPetName).child("current_exercise_data");

            exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Get exercise data
                        stepCount = snapshot.child("stepCount").getValue(Integer.class);
                        distance = snapshot.child("distance").getValue(Double.class);

                        // Update UI with fetched data
                        updateStepCount();
                        updateDistance();
                        updateSpeed();
                    } else {
                        // If data doesn't exist, set counts to zero
                        stepCount = 0;
                        distance = 0.0;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void saveDailyTotal(int stepCount, double distance) {
        Log.d("ExerciseActivity", "Saving daily total exercise data");

        // Format the date as "yyyyMMdd"
        String formattedDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        DatabaseReference dailyTotalDataRef = dailyTotalRef.child(formattedDate);

        dailyTotalDataRef.child("stepCount").setValue(stepCount);
        dailyTotalDataRef.child("distance").setValue(distance);


        // Reset counts for the next day
        stepCount = 0;
        distance = 0.0;


        Log.d("ExerciseActivity", "Daily total exercise data saved successfully.");
    }

    private void saveExerciseData(String petName, int stepCount, double distance) {
        Log.d("ExerciseActivity", "Saving exercise data for pet: " + petName);

        if (petName != null) {
            DatabaseReference exerciseRef = usersRef.child("pets").child(petName).child("current_exercise_data");

            exerciseRef.child("stepCount").setValue(stepCount);
            exerciseRef.child("distance").setValue(distance);


            Log.d("ExerciseActivity", "Exercise data saved successfully.");
        } else {
            Log.e("ExerciseActivity", "Pet name is null. Unable to save exercise data.");
        }
    }

    private void updateStepCount() {
        runOnUiThread(() -> stepCountTextView.setText("Step Count: " + stepCount));
    }

    private void updateDistance() {
        runOnUiThread(() -> distanceTextView.setText("Distance: " + String.format("%.2f meters", distance)));
    }

    private void updateSpeed() {
        runOnUiThread(() -> speedTextView.setText("Speed: " + String.format("%.2f m/s", speed)));
    }





}