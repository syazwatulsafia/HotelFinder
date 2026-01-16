package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    ImageView imgUser, btnMenu, btnBack;
    TextView txtEmail, txtReviewCount;
    RecyclerView recyclerReviews;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference userRef, reviewRef;
    List<Review> reviewList;
    ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        imgUser = findViewById(R.id.imgUser);
        btnMenu = findViewById(R.id.btnMenu);
        txtEmail = findViewById(R.id.txtEmail);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        btnBack = findViewById(R.id.btn_back_arrow);

        updateDisplayName(user.getEmail());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(
                reviewList,
                true,
                review -> {
                    reviewRef.child(review.reviewId)
                            .removeValue()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show());
                }
        );

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(adapter);

        loadUserProfile();
        loadUserReviews();

        imgUser.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );

        btnMenu.setOnClickListener(v -> showMenu());
    }

    private void updateDisplayName(String email) {
        if (email != null && email.contains("@")) {
            String name = email.split("@")[0];
            name = name.replaceAll("[0-9]", "");
            txtEmail.setText(name);
        } else if (email != null) {
            txtEmail.setText(email);
        }
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                if (user != null) {
                    updateDisplayName(user.getEmail());
                }

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
                Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                        Toast.makeText(UserProfileActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnMenu);
        popupMenu.getMenuInflater().inflate(R.menu.user_profile_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            }else if (id == R.id.menu_reminders) {
                startActivity(new Intent(this, ReminderActivity.class));
                return true;
            } else if (id == R.id.menu_about_us) {
                startActivity(new Intent(this, AboutUsActivity.class));
                return true;
            } else if (id == R.id.menu_logout) {
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