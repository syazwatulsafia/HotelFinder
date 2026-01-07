package com.example.hotelfinder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class CreateReviewActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private RatingBar ratingBar;
    private EditText etComment;
    private Uri currentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        imgPreview = findViewById(R.id.imgReviewPreview);
        ratingBar = findViewById(R.id.ratingCreate);
        etComment = findViewById(R.id.etReviewComment);
        Button btnPost = findViewById(R.id.btnPostReview);
        Button btnSelect = findViewById(R.id.btnSelectImage);

        // Camera Launcher
        ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), result -> {
                    if (result) imgPreview.setImageURI(currentImageUri);
                });

        // Gallery Launcher
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        currentImageUri = result.getData().getData();
                        imgPreview.setImageURI(currentImageUri);
                    }
                });

        btnSelect.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Gallery"};
            new AlertDialog.Builder(this).setItems(options, (dialog, which) -> {
                if (which == 0) {
                    currentImageUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg"));
                    cameraLauncher.launch(currentImageUri);
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(i);
                }
            }).show();
        });

        btnPost.setOnClickListener(v -> {
            // Here you would upload currentImageUri to Firebase/Server
            Toast.makeText(this, "Review Posted!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}