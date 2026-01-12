package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    ImageView imgUser, btnMenu;
    TextView txtEmail, txtReviewCount;
    RecyclerView recyclerReviews;

    FirebaseAuth auth;
    FirebaseFirestore db;

    List<Review> reviewList = new ArrayList<>();
    ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        imgUser = findViewById(R.id.imgUser);
        btnMenu = findViewById(R.id.btnMenu);
        txtEmail = findViewById(R.id.txtEmail);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        txtEmail.setText(user.getEmail());

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(reviewList, this);
        recyclerReviews.setAdapter(adapter);

        loadProfile(user.getUid());
        loadReviews(user.getUid());

        btnMenu.setOnClickListener(v -> showMenu());
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadProfile(user.getUid());
        }
    }

    private void showMenu() {
        PopupMenu popup = new PopupMenu(this, btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_user_profile, popup.getMenu());
        popup.setOnMenuItemClickListener(this::handleMenuClick);
        popup.show();
    }

    private boolean handleMenuClick(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_edit_profile) {
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;

        } else if (id == R.id.menu_about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;

        } else if (id == R.id.menu_logout) {
            auth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }

        return false;
    }

    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                    .into(imgUser);

                        }
                    }
                });
    }

    private void loadReviews(String uid) {
        db.collection("reviews")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(qs -> {
                    reviewList.clear();
                    reviewList.addAll(qs.toObjects(Review.class));
                    txtReviewCount.setText(qs.size() + " reviews");
                    adapter.notifyDataSetChanged();
                });
    }
}
