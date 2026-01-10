package com.example.hotelfinder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        // Optional: Set a title for the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Our Team");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Handles the back button in the top action bar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}