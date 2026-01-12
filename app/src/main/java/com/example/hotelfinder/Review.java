package com.example.hotelfinder;

public class Review {
    private String comment;
    private float rating;
    private String imageUri;

    public Review() {} // Needed for Firebase

    public Review(String comment, float rating, String imageUri) {
        this.comment = comment;
        this.rating = rating;
        this.imageUri = imageUri;
    }

    public String getComment() { return comment; }
    public float getRating() { return rating; }
    public String getImageUri() { return imageUri; }
}