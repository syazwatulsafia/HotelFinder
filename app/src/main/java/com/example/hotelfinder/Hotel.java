package com.example.hotelfinder;

import com.google.android.gms.maps.model.LatLng;

public class Hotel {
    public String name;
    public String address;
    public LatLng location;
    public float distance; // in meters

    public Hotel(String name, String address, LatLng location, float distance) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.distance = distance;
    }
}
