package com.example.hotelfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CreateReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText editReview;
    private ImageView imgSlot1, imgSlot2, imgSlot3, activeSlot;
    private Button btnSubmit;

    // Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && activeSlot != null) {
                    activeSlot.setPadding(0,0,0,0); // Remove padding to show full image
                    activeSlot.setImageURI(uri);
                }
            });

    // Camera Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    activeSlot.setPadding(0,0,0,0);
                    activeSlot.setImageBitmap(photo);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        // Initialize views
        ratingBar = findViewById(R.id.hotelRatingBar);
        editReview = findViewById(R.id.editReviewText);
        btnSubmit = findViewById(R.id.btnSubmitReview);
        imgSlot1 = findViewById(R.id.imgSlot1);
        imgSlot2 = findViewById(R.id.imgSlot2);
        imgSlot3 = findViewById(R.id.imgSlot3);

        // Set listeners for slots
        imgSlot1.setOnClickListener(v -> { activeSlot = imgSlot1; showPicker(); });
        imgSlot2.setOnClickListener(v -> { activeSlot = imgSlot2; showPicker(); });
        imgSlot3.setOnClickListener(v -> { activeSlot = imgSlot3; showPicker(); });

        btnSubmit.setOnClickListener(v -> submitData());
    }

    private void showPicker() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else galleryLauncher.launch("image/*");
                }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void submitData() {
        float rating = ratingBar.getRating();
        String comment = editReview.getText().toString();
        Toast.makeText(this, "Rating: " + rating + " stars submitted!", Toast.LENGTH_SHORT).show();
        finish();
    }
}