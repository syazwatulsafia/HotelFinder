package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class HotelDetailActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyBKpSvu4pYLp6Jh1PPbBr6HFPstRUzhnCU";
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        // Views
        ImageView btnBack = findViewById(R.id.btn_back_arrow);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtAddress = findViewById(R.id.txtAddress);
        TextView txtWebsite = findViewById(R.id.txtWebsite);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnNavigate = findViewById(R.id.btnNavigate);
        Button btnSeeReview = findViewById(R.id.btnSeeReview);

        // Get data from Intent
        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        txtName.setText(name);
        txtAddress.setText(address);

        // BACK BUTTON
        btnBack.setOnClickListener(v -> finish());

        // CALL
        btnCall.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:0123456789"))));

        // WEBSITE
        String websiteUrl = "https://www.google.com/search?q=" + name;
        txtWebsite.setText("Visit website");
        txtWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
            startActivity(intent);
        });

        // NAVIGATION
        btnNavigate.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + lat + "," + lng))));

        btnSeeReview.setOnClickListener(v -> {
            Intent intent = new Intent(HotelDetailActivity.this, ListReviewActivity.class);
            intent.putExtra("hotelName", name);
            startActivity(intent);
        });

        // PHOTO
        ImageView imgHotel = findViewById(R.id.imgHotel);
        String photoRef = getIntent().getStringExtra("photoRef");

        if (photoRef != null && !photoRef.isEmpty()) {
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=800" +
                    "&photo_reference=" + photoRef +
                    "&key=" + API_KEY;

            Glide.with(this)
                    .load(photoUrl)
                    .into(imgHotel);
        }
        String placeId = getIntent().getStringExtra("placeId");

        if (placeId != null) {
            String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json" +
                    "?place_id=" + placeId +
                    "&fields=formatted_phone_number" +
                    "&key=" + API_KEY;

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(detailsUrl, null,
                    response -> {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            String phone = result.optString("formatted_phone_number", null);
                            if (phone != null) {
                                btnCall.setOnClickListener(v ->
                                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))));
                            } else {
                                btnCall.setEnabled(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            btnCall.setEnabled(false);
                        }
                    },
                    error -> btnCall.setEnabled(false)
            );
            queue.add(request);
        }

        // 🔹 GO TO CREATE REVIEW WITH HOTEL INFO
        btnSeeReview.setOnClickListener(v -> {
            Intent intent = new Intent(HotelDetailActivity.this, ListReviewActivity.class);
            intent.putExtra("hotelName", name);
            intent.putExtra("hotelAddress", address);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });
    }
}