package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        TextView welcomeTitle = findViewById(R.id.textViewWelcome); // WELCOME!
        TextView profileText = findViewById(R.id.textViewEmail);   // Email text

        user = auth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            profileText.setText(email);

            if (email != null && email.contains("@")) {
                // Extract name before the '@'
                String name = email.split("@")[0];

                // Remove numbers or symbols if any (e.g., john22 -> John)
                name = name.replaceAll("[0-9]", "");

                if (name.length() > 0) {
                    // Capitalize first letter
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    welcomeTitle.setText("Welcome back, " + name + "!");
                }
            }
        }

        // Write Review button
        findViewById(R.id.btnGoToReview).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateReviewActivity.class));
        });

        // Team button
        MaterialButton btnTeam = findViewById(R.id.btnTeam);
        btnTeam.setOnClickListener(v -> {
            startActivity(new Intent(this, TeamActivity.class));
        });

        // About Us button
        MaterialButton btnAboutUs = findViewById(R.id.btnAboutUs);
        btnAboutUs.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutUsActivity.class));
        });

        // Maps button
        Button btnMaps = findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));
        });
    }

    public void signout(View v) {
        auth.signOut();
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
