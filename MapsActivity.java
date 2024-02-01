package com.example.testsample;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final float GEOFENCE_RADIUS = 200;
    private static final String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    private static final String TAG = "MapsActivity";
    private static final int FINE_LOCATION_ACCESS_CODE = 10001;

    private GoogleMap mMap;

    private Pet pet;
    private Marker petMarker;

    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    private Handler updateHandler = new Handler();
    private final int PET_UPDATE_INTERVAL = 10 * 60 * 1000; // 10 minutes


    private ValueEventListener locationListener;
    private DatabaseReference databaseReference;
    private Button saveGeofenceButton;
    private LatLng savedGeofenceLocation;
    private static final String PREFS_NAME = "GeofencePrefs";
    private static final String KEY_GEOFENCE_LATITUDE = "geofence_latitude";
    private static final String KEY_GEOFENCE_LONGITUDE = "geofence_longitude";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        databaseReference = FirebaseDatabase.getInstance().getReference(); // Initialize the database reference

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (getIntent().hasExtra("pet")) {
            pet = (Pet) getIntent().getSerializableExtra("pet");

        }


        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updatePetMarker();


                if (pet != null) {
                    Intent updateIntent = new Intent(MapsActivity.this, ViewPetLocation.class);
                    updateIntent.putExtra("selectedPetName", pet.getName());
                    startActivity(updateIntent);
                }

                Toast.makeText(MapsActivity.this, "Pet marker and intent refreshed", Toast.LENGTH_SHORT).show();
            }
        });


        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);


        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        double savedLatitude = prefs.getFloat(KEY_GEOFENCE_LATITUDE, 0f);
        double savedLongitude = prefs.getFloat(KEY_GEOFENCE_LONGITUDE, 0f);

        if (savedLatitude != 0 && savedLongitude != 0) {
            savedGeofenceLocation = new LatLng(savedLatitude, savedLongitude);
        }

        saveGeofenceButton = findViewById(R.id.saveGeofenceButton); // Assuming you have a button in your layout with the id saveGeofenceButton
        saveGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savedGeofenceLocation != null) {
                    saveGeofence(savedGeofenceLocation, GEOFENCE_RADIUS);
                    Toast.makeText(MapsActivity.this, "Geofence saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "No geofence location to save", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startPetUpdateTask();

    }


    private void refreshPetMarker() {

        updatePetMarker();
        Toast.makeText(this, "Pet marker refreshed", Toast.LENGTH_SHORT).show();
    }

    private void startPetUpdateTask() {

        updateHandler.post(updatePetMarkerTask);

        updatePetMarker();
    }


    private Runnable updatePetMarkerTask = new Runnable() {
        @Override
        public void run() {

            updatePetMarker();

            updateHandler.postDelayed(this, PET_UPDATE_INTERVAL);
        }
    };


    protected void onResume() {
        super.onResume();


        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        double savedLatitude = prefs.getFloat(KEY_GEOFENCE_LATITUDE, 0f);
        double savedLongitude = prefs.getFloat(KEY_GEOFENCE_LONGITUDE, 0f);

        if (savedLatitude != 0 && savedLongitude != 0) {
            savedGeofenceLocation = new LatLng(savedLatitude, savedLongitude);
        }
        updatePetMarker();
    }

    private void movePetMarker(LatLng petLatLng) {
        if (mMap != null && petLatLng != null) {
            if (petMarker != null) {

                petMarker.setPosition(petLatLng);
            } else {

                petMarker = mMap.addMarker(new MarkerOptions()
                        .position(petLatLng)
                        .title("Selected Pet Marker")
                        .snippet("Lat: " + petLatLng.latitude + " Lon: " + petLatLng.longitude)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petLatLng, 16));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (locationListener != null) {
            DatabaseReference petLocationRef = databaseReference.child("pets").child(pet.getName()).child("location");
            petLocationRef.removeEventListener(locationListener);
        }
    }


    private void updatePetMarker() {
        if (pet != null) {
            DatabaseReference petLocationRef = databaseReference.child("pets").child(pet.getName()).child("location");
            locationListener = petLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the dataSnapshot exists and has children
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("latitude") && dataSnapshot.hasChild("longitude")) {
                        double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                        double longitude = dataSnapshot.child("longitude").getValue(Double.class);

                        LatLng petLatLng = new LatLng(latitude, longitude);
                        movePetMarker(petLatLng);


                        checkGeofenceStatus(petLatLng);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        }
    }


   private void checkGeofenceStatus(LatLng petLatLng) {
        if (savedGeofenceLocation != null && petLatLng != null) {
            float[] distance = new float[1];
            Location.distanceBetween(
                    petLatLng.latitude, petLatLng.longitude,
                    savedGeofenceLocation.latitude, savedGeofenceLocation.longitude,
                    distance
            );


            if (distance[0] > GEOFENCE_RADIUS) {

                Toast.makeText(this, "Pet is outside the geofence", Toast.LENGTH_SHORT).show();
                showGeofenceNotification();
            }
        }
    }
    private void checkGeofenceStatusTwo(LatLng currentLocation) {
        if (savedGeofenceLocation != null && currentLocation != null) {
            float[] distance = new float[1];
            Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    savedGeofenceLocation.latitude, savedGeofenceLocation.longitude,
                    distance
            );

            if (distance[0] > GEOFENCE_RADIUS) {

                Toast.makeText(this, " Pet is outside the geofence", Toast.LENGTH_SHORT).show();
                showGeofenceNotification();
            }
        }
    }



    private void showGeofenceNotification() {
        String channelId = "geofence_channel";
        String channelName = "Geofence Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("Geofence Notifications");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Pet Outside Geofence")
                .setContentText("Your pet is outside the geofence.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManagerCompat.notify(1, builder.build());
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableUserLocation();
        mMap.setOnMapLongClickListener(this);

//geofence

        if (savedGeofenceLocation != null) {
            addCircle(savedGeofenceLocation, GEOFENCE_RADIUS);
            addGeofence(savedGeofenceLocation, GEOFENCE_RADIUS);
        }




        Intent intent = getIntent();
        if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            float markerColor = intent.getFloatExtra("markerColor", BitmapDescriptorFactory.HUE_BLUE);
            LatLng currentLocation = new LatLng(latitude, longitude);

            checkGeofenceStatusTwo(currentLocation);
            mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Current Location Marker")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
        } else {
            Toast.makeText(this, "Location information not available", Toast.LENGTH_SHORT).show();
        }


//intent from view pet
        if (pet != null && pet.hasLocation()) {
            LatLng petLatLng = new LatLng(pet.getLocation().getLatitude(), pet.getLocation().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(petLatLng)
                    .title("Selected Pet Marker")
                    .snippet("Lat: " + pet.getLocation().getLatitude() + " Lon: " + pet.getLocation().getLongitude())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petLatLng, 16));
            checkGeofenceStatus(petLatLng); // Call checkGeofenceStatus with the pet's location
        } else {
            Toast.makeText(this, "Pet location not available", Toast.LENGTH_SHORT).show();
        }
        Button btnRefresh = findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPetMarker();
            }
        });

    }




    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                // Handle denied permission
                Toast.makeText(this, "Location permission denied. Unable to show user location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        removeGeofence();
        savedGeofenceLocation = latLng;
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(KEY_GEOFENCE_LATITUDE, (float) latLng.latitude);
        editor.putFloat(KEY_GEOFENCE_LONGITUDE, (float) latLng.longitude);
        editor.apply();


    }
    private void removeGeofence() {

        if (savedGeofenceLocation != null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(savedGeofenceLocation);
            circleOptions.radius(GEOFENCE_RADIUS);
            mMap.addCircle(circleOptions);
        }

        // Remove the previous geofence from the GeofencingClient
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Removed...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }


    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = geofenceHelper.getPendingIntent();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }
    private void saveGeofence(LatLng latLng, float radius) {

        addCircle(latLng, radius);


        addGeofence(latLng, radius);


        saveGeofenceToFirebase(latLng, radius);
    }

    private void saveGeofenceToFirebase(LatLng latLng, float radius) {
        if (pet != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();


                DatabaseReference userRef = databaseReference.child("users").child(userId);

                DatabaseReference petRef = userRef.child("pets").child(pet.getName());

                DatabaseReference geofenceRef = petRef.child("geofence");

                Map<String, Object> geofenceData = new HashMap<>();
                geofenceData.put("latitude", latLng.latitude);
                geofenceData.put("longitude", latLng.longitude);
                geofenceData.put("radius", radius);

                geofenceRef.setValue(geofenceData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Geofence data saved under pet's node in Firebase...");
                                Toast.makeText(MapsActivity.this, "Geofence saved to Firebase", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to save geofence data under pet's node in Firebase. " + e.getMessage());
                                Toast.makeText(MapsActivity.this, "Failed to save geofence to Firebase", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Handle the case where the current user is null
                Log.d(TAG, "saveGeofenceToFirebase: Current user is null");
                Toast.makeText(MapsActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}



