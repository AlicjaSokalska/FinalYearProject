package com.example.testsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceive";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Geofence event received.");
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "onReceive: Geofence transition ENTER");
                notificationHelper.sendHighPriorityNotification("Pet Inside Geofence", "Your pet marker is inside the geofence.", MapsActivity.class, "GeofenceEnterChannel");
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "onReceive: Geofence transition EXIT");
                notificationHelper.sendHighPriorityNotification("Pet Outside Geofence", "Your pet marker is outside the geofence.", MapsActivity.class, "GeofenceExitChannel");
                Toast.makeText(context, "Pet is outside the geofence", Toast.LENGTH_SHORT).show();

                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "onReceive: Geofence transition DWELL");
                notificationHelper.sendHighPriorityNotification("Pet Dwelling in Geofence", "Your pet marker is dwelling inside the geofence.", MapsActivity.class, "GeofenceDwellChannel");
                break;

            default:
                Log.d(TAG, "onReceive: Unknown geofence transition type");
                break;
        }
    }
}
