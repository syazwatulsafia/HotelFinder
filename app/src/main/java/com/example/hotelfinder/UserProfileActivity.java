package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView imgUser;
    TextView txtEmail;
    Button btnEditProfile;
    RecyclerView recyclerReviews;

    FirebaseAuth auth;
    FirebaseFirestore db;

    List<Review> reviewList = new ArrayList<>();
    ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 🔹 Bind views
        imgUser = findViewById(R.id.imgUser);
        txtEmail = findViewById(R.id.txtEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        // 🔹 Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        txtEmail.setText(user.getEmail());

        // 🔹 RecyclerView setup
        adapter = new ReviewAdapter(reviewList, this);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(adapter);

        loadUserProfile(user.getUid());
        loadUserReviews(user.getUid());

        // 🔹 Edit profile navigation
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    private void loadUserProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(imgUser);
                        }
                    }
                });
    }

    private void loadUserReviews(String uid) {
        db.collection("reviews")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Review review = new Review(
                                doc.getString("username"),
                                doc.getString("profileUrl"),
                                doc.getString("hotelImageUrl"),
                                doc.getString("comment"),
                                doc.getDouble("rating").floatValue()
                        );
                        reviewList.add(review);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserProfile(user.getUid());
        }
    }
}
