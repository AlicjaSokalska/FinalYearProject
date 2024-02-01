package com.example.testsample;

import java.io.Serializable;

public class PetLocation  implements Serializable {
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    private float speed;
    private String address;

    public PetLocation() {
  
    }

    public PetLocation(double latitude, double longitude, double altitude, float accuracy, float speed, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.speed = speed;
        this.address = address;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public boolean hasAltitude() {
        return !Double.isNaN(altitude) && altitude != 0.0;
    }

    public boolean hasSpeed() {
        return speed > 0.0;
    }
}
