package com.example.hotelfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePage extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        MaterialButton btnFindHotels = findViewById(R.id.btnFindHotels);

        btnFindHotels.setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, MapsActivity.class));
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Welcome text
        TextView welcomeTitle = findViewById(R.id.textViewWelcome);

        if (user != null) {
            String email = user.getEmail();

            if (email != null && email.contains("@")) {
                String name = email.split("@")[0];
                name = name.replaceAll("[0-9]", "");

                if (!name.isEmpty()) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    welcomeTitle.setText( name + "!");
                }
            }
        }

        // Bottom Navigation
        BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottom_navigation);

        // Highlight Home icon
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already on Home
                return true;
            }
            else if (id == R.id.nav_maps) {
                startActivity(new Intent(HomePage.this, MapsActivity.class));
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomePage.this, UserProfileActivity.class));
                return true;
            }

            return false;
        });
    }
}