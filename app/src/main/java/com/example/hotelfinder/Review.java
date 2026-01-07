package com.example.hotelfinder;

public class Review {
    String username, profileUrl, hotelImageUrl, comment;
    float rating;

    public Review(String username, String profileUrl, String hotelImageUrl, String comment, float rating) {
        this.username = username;
        this.profileUrl = profileUrl;
        this.hotelImageUrl = hotelImageUrl;
        this.comment = comment;
        this.rating = rating;
    }
}
