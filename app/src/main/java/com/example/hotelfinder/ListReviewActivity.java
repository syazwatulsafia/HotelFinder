package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ListReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_review);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(reviewList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAddReview = findViewById(R.id.fab_add_review);
        fabAddReview.setOnClickListener(v -> {
            Intent intent = new Intent(ListReviewActivity.this, CreateReviewActivity.class);
            startActivity(intent);
        });
        fetchReviews();
        setupNavigation();
    }

    private void fetchReviews() {
        // Query: Get "reviews" collection, order by newest timestamp
        db.collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading reviews", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        reviewList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Review review = doc.toObject(Review.class);
                            reviewList.add(review);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                 startActivity(new Intent(this, ProfileActivity.class));
                 return true;
            }
            return false;
        });

    }
}