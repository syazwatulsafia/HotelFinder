package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    // UI
    ImageView imgUser, btnMenu, btnBack;
    TextView txtEmail, txtReviewCount;
    RecyclerView recyclerReviews;

    // Firebase
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference userRef, reviewRef;

    // Adapter
    List<Review> reviewList;
    ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 🔹 Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        // 🔹 Views
        imgUser = findViewById(R.id.imgUser);
        btnMenu = findViewById(R.id.btnMenu);
        txtEmail = findViewById(R.id.txtEmail);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        btnBack = findViewById(R.id.btn_back_arrow);

        txtEmail.setText(user.getEmail());

        // 🔹 Back button
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 🔹 Database references
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid());

        reviewRef = FirebaseDatabase.getInstance()
                .getReference("reviews");

        // 🔹 RecyclerView
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(
                reviewList,
                true, // show delete button
                review -> {
                    reviewRef.child(review.reviewId)
                            .removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this,
                                            "Review deleted",
                                            Toast.LENGTH_SHORT).show());
                }
        );

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(adapter);

        // 🔹 Load data
        loadUserProfile();
        loadUserReviews();

        // 🔹 Click profile photo → Edit profile
        imgUser.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );

        // 🔹 Menu
        btnMenu.setOnClickListener(v -> showMenu());
    }

    // 🔹 Load user profile (REALTIME)
    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) return;

                // Email
                String email = snapshot.child("email").getValue(String.class);
                if (email != null) txtEmail.setText(email);

                // Profile photo
                String photoUri = snapshot.child("photoUri").getValue(String.class);
                if (photoUri != null && !photoUri.isEmpty()) {
                    Glide.with(UserProfileActivity.this)
                            .load(Uri.parse(photoUri))
                            .circleCrop()
                            .into(imgUser);
                } else {
                    imgUser.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Load user's reviews
    private void loadUserReviews() {
        reviewRef.orderByChild("userId")
                .equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviewList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Review review = ds.getValue(Review.class);
                            if (review != null) reviewList.add(review);
                        }

                        txtReviewCount.setText(reviewList.size() + " reviews");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserProfileActivity.this,
                                "Failed to load reviews",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 Popup menu
    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnMenu);
        popupMenu.getMenuInflater()
                .inflate(R.menu.menu_user_profile, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.menu_edit_profile) {
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.menu_about_us) {
                startActivity(new Intent(this, AboutUsActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}
