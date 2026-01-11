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
    TextView profileText;
    Button btnGotoReview;

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
        profileText = (TextView)findViewById(R.id.textView);
        View btnGoToReview = findViewById(R.id.btnGoToReview);

        user = auth.getCurrentUser();
        if (user != null) {
            profileText.setText(user.getEmail());
        }

        btnGoToReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, CreateReviewActivity.class);
                startActivity(intent);
            }
        });

        MaterialButton btnTeam = findViewById(R.id.btnTeam);
        btnTeam.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, TeamActivity.class);
            startActivity(intent);
        });

        MaterialButton btnAboutUs = findViewById(R.id.btnAboutUs);
        btnAboutUs.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });
        //map add (here)
        Button btnOpenMap = findViewById(R.id.btnMaps);
        //map add (here)
        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MapsActivity.class);
            startActivity(intent);
        });
    }
    public void signout(View v){
        auth.signOut();
        finish();
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);

    }
}