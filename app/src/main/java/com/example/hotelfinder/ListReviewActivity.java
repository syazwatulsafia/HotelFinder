package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ListReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_review);

        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        ImageView btnAddReview = findViewById(R.id.fab_add_review);

        btnAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListReviewActivity.this, CreateReviewActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListReviewActivity.this, ProfileActivity.class);
                startActivity(intent);
                Toast.makeText(ListReviewActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}