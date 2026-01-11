package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HotelDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_detail);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtAddress = findViewById(R.id.txtAddress);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnWebsite = findViewById(R.id.btnWebsite);
        Button btnNavigate = findViewById(R.id.btnNavigate);

        String name = getIntent().getStringExtra("name");
        String address = getIntent().getStringExtra("address");
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        txtName.setText(name);
        txtAddress.setText(address);

        btnCall.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:0123456789"))));

        btnWebsite.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=" + name))));

        btnNavigate.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + lat + "," + lng))));
    }
}
