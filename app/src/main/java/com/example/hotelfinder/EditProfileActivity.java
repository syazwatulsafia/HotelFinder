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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText edtEmail, edtPassword;
    private Button btnSave;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(imgProfile);
                }
            });

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
        if (user == null) { finish(); return; }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        edtEmail.setText(user.getEmail());
        edtEmail.setEnabled(false);
        edtEmail.setAlpha(0.6f);

        loadProfilePhoto();

        imgProfile.setOnClickListener(v -> checkPermission());
        btnSave.setOnClickListener(v -> saveProfile());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadProfilePhoto() {
        userRef.child("photoUri").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                String photoUri = snapshot.getValue(String.class);
                if (photoUri != null && !photoUri.isEmpty()) {
                    Glide.with(getApplicationContext())
                            .load(photoUri)
                            .placeholder(R.drawable.profile)
                            .circleCrop()
                            .into(imgProfile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkPermission() {
        String permission = (Build.VERSION.SDK_INT >= 33) ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            imagePicker.launch("image/*");
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void saveProfile() {
        btnSave.setEnabled(false);
        btnSave.setText("Uploading...");

        String newPassword = edtPassword.getText().toString().trim();
        if (!newPassword.isEmpty()) {
            user.updatePassword(newPassword);
        }

        if (selectedImageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_photos/" + user.getUid() + ".jpg");

            storageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userRef.child("photoUri").setValue(uri.toString())
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }))
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Changes");
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            finish();
        }
    }
}