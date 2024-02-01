package com.example.testsample;


import android.app.Application;
import android.location.Location;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {


    private List<Location> myLocations;
    private static MyApplication singleton;
    private DatabaseReference databaseReference;
    public static MyApplication getInstance(){
        return singleton;
    }
    public void onCreate(){
        super.onCreate();
        singleton=this;
        myLocations= new ArrayList<>();

    }

    public List<Location> getMyLocations() {
        return myLocations;
    }

    public void setMyLocations(List<Location> myLocations) {
        this.myLocations = myLocations;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
