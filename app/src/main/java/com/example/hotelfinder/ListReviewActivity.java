package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListReviewActivity extends AppCompatActivity {

    // UI
    RecyclerView recyclerView;
    ImageView btnBack;
    TextView txtTitle;
    FloatingActionButton fabAddReview;
    BottomNavigationView bottomNavigationView;

    // Firebase
    DatabaseReference reviewRef;

    // Data
    List<Review> reviewList;
    ReviewAdapter adapter;

    // Hotel info
    String hotelName;
    String hotelAddress;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_review);

        // 🔹 GET HOTEL INFO FROM INTENT
        hotelName = getIntent().getStringExtra("hotelName");
        hotelAddress = getIntent().getStringExtra("hotelAddress");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        // 🔹 INIT UI
        btnBack = findViewById(R.id.btn_back_arrow);
        txtTitle = findViewById(R.id.txtTitle);
        recyclerView = findViewById(R.id.recyclerView);
        fabAddReview = findViewById(R.id.fab_add_review);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        txtTitle.setText("Reviews");

        // 🔹 BACK BUTTON
        btnBack.setOnClickListener(v -> finish());

        // 🔹 RECYCLER VIEW
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(
                reviewList,
                false, // ❌ hide delete button
                null
        );


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 🔹 FIREBASE
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        loadReviews();

        // 🔹 ADD REVIEW BUTTON
        fabAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ListReviewActivity.this, CreateReviewActivity.class);
            intent.putExtra("hotelName", hotelName);
            intent.putExtra("hotelAddress", hotelAddress);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });

        // 🔹 BOTTOM NAVIGATION
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePage.class));
                return true;
            }
            else if (id == R.id.nav_maps) {
                startActivity(new Intent(this, MapsActivity.class));
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // 🔹 LOAD REVIEWS FOR SELECTED HOTEL
    private void loadReviews() {

        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Review review = ds.getValue(Review.class);

                    if (review != null &&
                            review.hotelName != null &&
                            review.hotelName.equals(hotelName)) {

                        reviewList.add(review);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // optional: show toast/log
            }
        });
    }
}
