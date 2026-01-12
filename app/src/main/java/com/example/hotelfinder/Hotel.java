package com.example.hotelfinder;
import com.google.android.gms.maps.model.LatLng;

public class Hotel {
    public String name;
    public String address;
    public LatLng location;
    public float distance; //in meters
    public String photoReference; // new field
    public String phoneNumber;
    public String placeId;

    public Hotel(String name, String address, LatLng location, float distance, String photoReference, String placeId) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.distance = distance;
        this.photoReference = photoReference;
        this.placeId = placeId;
        this.phoneNumber = null; // will fetch later
    }
}
