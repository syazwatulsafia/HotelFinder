package com.example.hotelfinder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Our Team");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}