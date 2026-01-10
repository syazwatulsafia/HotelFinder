package com.example.hotelfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class CreateReviewActivity extends AppCompatActivity {

    RatingBar ratingBar;
    TextView txtRatingMessage;
    EditText editReview;
    Button btnSubmit;

    ImageView img1, img2, img3;
    int imageCount = 0;

    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        ratingBar = findViewById(R.id.hotelRatingBar);
        editReview = findViewById(R.id.editReviewText);
        btnSubmit = findViewById(R.id.btnSubmitReview);

        img1 = findViewById(R.id.imgSlot1);
        img2 = findViewById(R.id.imgSlot2);
        img3 = findViewById(R.id.imgSlot3);

        // ⭐ Rating listener
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            txtRatingMessage.setVisibility(View.VISIBLE);
            txtRatingMessage.setText("Wowww, you rated " + (int) rating + " stars!!!");
        });

        // 📸 Image picker
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) setImage(uri);
                });

        View.OnClickListener imageClick = v -> imagePicker.launch("image/*");

        img1.setOnClickListener(imageClick);
        img2.setOnClickListener(imageClick);
        img3.setOnClickListener(imageClick);

        // ✅ Submit review
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setImage(Uri uri) {
        if (imageCount == 0) img1.setImageURI(uri);
        else if (imageCount == 1) img2.setImageURI(uri);
        else if (imageCount == 2) img3.setImageURI(uri);

        imageCount++;
    }

    private void submitReview() {
        if (ratingBar.getRating() == 0) {
            Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ReviewSuccessActivity.class);
        startActivity(intent);
        finish();
    }
}
