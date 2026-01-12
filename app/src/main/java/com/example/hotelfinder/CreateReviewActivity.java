package com.example.hotelfinder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComment;
    private TextView tvCharCount;
    private Button btnSubmit;
    private ImageButton btnAddPhoto;
    private ImageView btnBack;

    private Uri selectedImageUri;

    // HOTEL INFO
    private String hotelName, hotelAddress;
    private double lat, lng;

    // Firebase Realtime DB
    private DatabaseReference reviewRef;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        // GET HOTEL INFO
        hotelName = getIntent().getStringExtra("hotelName");
        hotelAddress = getIntent().getStringExtra("hotelAddress");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        initViews();
        setupLaunchers();
        setupTextWatcher();

        btnBack.setOnClickListener(v -> finish());
        btnAddPhoto.setOnClickListener(v -> checkPermissionAndShowDialog());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initViews() {
        ratingBar = findViewById(R.id.ratingBar);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        etComment = findViewById(R.id.et_comment);
        tvCharCount = findViewById(R.id.tvCharCount);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back_arrow);
    }

    private void setupTextWatcher() {
        etComment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                tvCharCount.setText(s.length() + " / 250");
                tvCharCount.setTextColor(s.length() >= 250 ? Color.RED : Color.GRAY);
            }
        });
    }

    private void setupLaunchers() {

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) handleImageSelection(uri);
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && selectedImageUri != null) {
                        handleImageSelection(selectedImageUri);
                    }
                });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) showImageSourceDialog();
                    else Toast.makeText(this,
                            "Camera permission denied",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (d, i) -> {
                    if (i == 0) openCamera();
                    else galleryLauncher.launch("image/*");
                })
                .show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Review Photo");

        selectedImageUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        cameraLauncher.launch(intent);
    }

    private void handleImageSelection(Uri uri) {
        selectedImageUri = uri;
        btnAddPhoto.setImageURI(uri);
        btnAddPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnAddPhoto.setPadding(0, 0, 0, 0);
    }

    private void submitReview() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String comment = etComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (comment.isEmpty() || rating == 0) {
            Toast.makeText(this,
                    "Please complete your review",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        String reviewId = reviewRef.push().getKey();

        Map<String, Object> review = new HashMap<>();
        review.put("userEmail", user.getEmail());
        review.put("reviewId", reviewId);
        review.put("userId", user.getUid());
        review.put("hotelName", hotelName);
        review.put("hotelAddress", hotelAddress);
        review.put("lat", lat);
        review.put("lng", lng);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", System.currentTimeMillis());

        // ✅ MATCH Review.java EXACTLY
        if (selectedImageUri != null) {
            review.put("imageUri", selectedImageUri.toString());
        }

        reviewRef.child(reviewId)
                .setValue(review)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Review submitted successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit");
                    Toast.makeText(this,
                            "Failed to submit review",
                            Toast.LENGTH_SHORT).show();
                });

        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

    }
}
