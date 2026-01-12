package com.example.hotelfinder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private ImageButton btnAddPhoto;
    private ImageView btnBack;

    private Uri selectedImageUri;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupLaunchers();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAddPhoto.setOnClickListener(v -> checkPermissionAndShowDialog());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initViews() {
        ratingBar = findViewById(R.id.ratingBar);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        etComment = findViewById(R.id.et_comment);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back_arrow);
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) handleImageSelection(uri); });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> { if (result.getResultCode() == RESULT_OK) handleImageSelection(selectedImageUri); });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) showImageSourceDialog();
                    else Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkPermissionAndShowDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else galleryLauncher.launch("image/*");
                }).show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Review Picture");
        selectedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        cameraLauncher.launch(intent);
    }

    private void handleImageSelection(Uri uri) {
        if (uri != null) {
            selectedImageUri = uri;
            btnAddPhoto.setImageURI(uri);
            btnAddPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            btnAddPhoto.setBackground(null);
        }
    }

    private void submitReview() {
        String comment = etComment.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        Map<String, Object> review = new HashMap<>();
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", System.currentTimeMillis());

        // Add the image URI string if one was selected
        if (selectedImageUri != null) {
            review.put("imageUri", selectedImageUri.toString());
        }

        db.collection("reviews").add(review)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Review Submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}