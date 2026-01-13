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

    RecyclerView recyclerView;
    ImageView btnBack;
    TextView txtTitle;
    FloatingActionButton fabAddReview;
    BottomNavigationView bottomNavigationView;

    DatabaseReference reviewRef;

    List<Review> reviewList;
    ReviewAdapter adapter;

    String hotelName, hotelAddress;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_review);

        // GET HOTEL INFO
        hotelName = getIntent().getStringExtra("hotelName");
        hotelAddress = getIntent().getStringExtra("hotelAddress");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        // INIT UI
        btnBack = findViewById(R.id.btn_back_arrow);
        txtTitle = findViewById(R.id.txtTitle);
        recyclerView = findViewById(R.id.recyclerView);
        fabAddReview = findViewById(R.id.fab_add_review);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Title to Hotel Name
        txtTitle.setText(hotelName != null ? hotelName : "Reviews");

        btnBack.setOnClickListener(v -> finish());

        // RECYCLER VIEW
        reviewList = new ArrayList<>();

        // NOTE: Ensure your ReviewAdapter uses the 'cutoff' logic
        // inside its onBindViewHolder method to display the reviewer's name.
        adapter = new ReviewAdapter(reviewList, false, null);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // FIREBASE
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");
        loadReviews();

        // ADD REVIEW BUTTON
        fabAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ListReviewActivity.this, CreateReviewActivity.class);
            intent.putExtra("hotelName", hotelName);
            intent.putExtra("hotelAddress", hotelAddress);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Usually reviews are accessed from Home

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, HomePage.class);
            } else if (id == R.id.nav_maps) {
                intent = new Intent(this, MapsActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadReviews() {
        // Query reviews that match this specific hotel name
        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Review review = ds.getValue(Review.class);

                    if (review != null && review.hotelName != null && review.hotelName.equals(hotelName)) {

                        // 🔹 APPLY CUTOFF LOGIC TO THE REVIEWER NAME BEFORE ADDING TO LIST
                        if (review.userEmail != null && review.userEmail.contains("@")) {
                            String cleanName = review.userEmail.split("@")[0].replaceAll("[0-9]", "");
                            if (!cleanName.isEmpty()) {
                                cleanName = cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
                                review.userEmail = cleanName; // Temporarily overwrite for display
                            }
                        }

                        reviewList.add(review);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}