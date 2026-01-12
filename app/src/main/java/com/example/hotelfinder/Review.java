package com.example.hotelfinder;

public class Review {

    public String reviewId, userId;
    public String userEmail; // ✅ ADD THIS
    public String hotelName, hotelAddress;
    public String comment, imageUri;
    public float rating;
    public double lat, lng;
    public long timestamp;

    // REQUIRED EMPTY CONSTRUCTOR
    public Review() {}

}
