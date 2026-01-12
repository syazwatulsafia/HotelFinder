package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HotelDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtAddress = findViewById(R.id.txtAddress);
        TextView txtWebsite = findViewById(R.id.txtWebsite);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnNavigate = findViewById(R.id.btnNavigate);
        Button btnSeeReview = findViewById(R.id.btnSeeReview);

        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        txtName.setText(name);
        txtAddress.setText(address);

        btnBack.setOnClickListener(v -> finish());

        btnCall.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:0123456789"))));

        String websiteUrl = "https://www.google.com/search?q=" + name;
        txtWebsite.setText("Visit website");
        txtWebsite.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
        );

        btnNavigate.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + lat + "," + lng)))
        );

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
