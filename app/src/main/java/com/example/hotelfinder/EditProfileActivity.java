package com.example.hotelfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText edtEmail, edtPassword;
    private Button btnSave;

    private FirebaseUser user;
    private DatabaseReference userRef;

    private Uri selectedImageUri;

    // IMAGE PICKER
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;

                    // Show immediately
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(imgProfile);
                }
            });

    // PERMISSION LAUNCHER
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) imagePicker.launch("image/*");
                        else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgProfile = findViewById(R.id.imgProfileEdit);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSave = findViewById(R.id.btnSaveProfile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // EMAIL DISPLAY ONLY
        edtEmail.setText(user.getEmail());
        edtEmail.setEnabled(false);
        edtEmail.setAlpha(0.6f);

        // ✅ LOAD PROFILE IMAGE USING photoUri (REALTIME)
        loadProfilePhoto();

        // Pick photo
        imgProfile.setOnClickListener(v -> checkPermission());

        // Save
        btnSave.setOnClickListener(v -> saveProfile());

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

    }

    private void loadProfilePhoto() {
        userRef.child("photoUri")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String photoUri = snapshot.getValue(String.class);

                        if (photoUri != null && !photoUri.isEmpty()) {
                            Glide.with(EditProfileActivity.this)
                                    .load(Uri.parse(photoUri))
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .circleCrop()
                                    .into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to load profile photo",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                imagePicker.launch("image/*");
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                imagePicker.launch("image/*");
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void saveProfile() {
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        String newPassword = edtPassword.getText().toString().trim();

        // ✅ Password optional
        if (!newPassword.isEmpty()) {
            user.updatePassword(newPassword)
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Password update failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }

        // ✅ Photo optional
        if (selectedImageUri != null) {
            userRef.child("photoUri")
                    .setValue(selectedImageUri.toString())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Changes");
                        Toast.makeText(this, "Failed to update photo", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
