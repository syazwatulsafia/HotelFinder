package com.example.hotelfinder;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    ImageView imgProfile;
    EditText edtEmail, edtPassword;
    Button btnSave;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    StorageReference storageRef;

    Uri imageUri;

    ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imgProfile.setImageURI(uri);
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
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        if (user == null) {
            finish();
            return;
        }

        edtEmail.setText(user.getEmail());
        loadProfileImage();

        imgProfile.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfileImage() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String url = doc.getString("photoUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this).load(url).circleCrop().into(imgProfile);
                        }
                    }
                });
    }

    private void saveProfile() {

        String newEmail = edtEmail.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 UPDATE EMAIL (if changed)
        if (!newEmail.equals(user.getEmail())) {
            user.updateEmail(newEmail)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Email update failed: login again", Toast.LENGTH_LONG).show()
                    );
        }

        // 🔹 UPDATE PASSWORD (if filled)
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            user.updatePassword(newPassword)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Password update failed: login again", Toast.LENGTH_LONG).show()
                    );
        }

        // 🔹 UPDATE PHOTO
        if (imageUri != null) {
            StorageReference ref = storageRef.child(user.getUid() + ".jpg");
            ref.putFile(imageUri)
                    .continueWithTask(task -> ref.getDownloadUrl())
                    .addOnSuccessListener(uri -> saveUserToFirestore(newEmail, uri.toString()));
        } else {
            saveUserToFirestore(newEmail, null);
        }
    }

    private void saveUserToFirestore(String email, String photoUrl) {

        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        if (photoUrl != null) {
            map.put("photoUrl", photoUrl);
        }

        db.collection("users").document(user.getUid())
                .set(map, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
