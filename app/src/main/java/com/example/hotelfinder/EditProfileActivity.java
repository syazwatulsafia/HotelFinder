package com.example.hotelfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    ImageView imgProfile;
    EditText edtEmail, edtPassword;
    Button btnSave;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference userRef;

    Uri selectedImageUri;

    // 🔹 IMAGE PICKER
    ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);
                }
            });

    // 🔹 PERMISSION LAUNCHER
    ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            imagePicker.launch("image/*");
                        } else {
                            Toast.makeText(this,
                                    "Permission denied",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgProfile = findViewById(R.id.imgProfileEdit);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSave = findViewById(R.id.btnSaveProfile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        if (user == null) {
            finish();
            return;
        }

        // 🔹 PRE-FILL EMAIL
        edtEmail.setText(user.getEmail());

        // 🔹 LOAD PROFILE IMAGE (IF EXISTS)
        userRef.child(user.getUid()).child("photoUri")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String uri = snapshot.getValue(String.class);
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(imgProfile);
                    }
                });

        imgProfile.setOnClickListener(v -> checkPermission());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    // 🔹 ANDROID 12 / 13 PERMISSION HANDLING
    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 33) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED) {

                imagePicker.launch("image/*");

            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }

        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {

                imagePicker.launch("image/*");

            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    // 🔹 SAVE PROFILE (PHOTO / EMAIL / PASSWORD OPTIONAL)
    private void saveProfile() {

        String newEmail = edtEmail.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();

        // UPDATE EMAIL
        if (!newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
            user.updateEmail(newEmail)
                    .addOnSuccessListener(unused ->
                            userRef.child(user.getUid())
                                    .child("email")
                                    .setValue(newEmail));
        }

        // UPDATE PASSWORD (OPTIONAL)
        if (!newPassword.isEmpty()) {
            user.updatePassword(newPassword);
        }

        // UPDATE PROFILE PHOTO (OPTIONAL)
        if (selectedImageUri != null) {
            userRef.child(user.getUid())
                    .child("photoUri")
                    .setValue(selectedImageUri.toString());
        }

        Toast.makeText(this,
                "Profile updated successfully",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}
