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

        // Views
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtAddress = findViewById(R.id.txtAddress);
        TextView txtWebsite = findViewById(R.id.txtWebsite);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnNavigate = findViewById(R.id.btnNavigate);

        // Get data from Intent
        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        txtName.setText(name);
        txtAddress.setText(address);

        // BACK BUTTON
        btnBack.setOnClickListener(v -> finish()); // simply finish the activity

        // CALL
        btnCall.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:0123456789"))));

        // WEBSITE (HYPERLINK)
        String websiteUrl = "https://www.google.com/search?q=" + name;
        txtWebsite.setText("Visit website");

        txtWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
            startActivity(intent);
        });

        // 🗺️ NAVIGATION
        btnNavigate.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + lat + "," + lng))));
    }
}
