package com.example.hotelfinder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_success);

        // 🔹 2 Second Delay Logic
        // After 2000ms (2 seconds), the activity will finish and return
        // the user to the previous screen (likely the Hotel Detail or Home).
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        // Optional: Disable back button during the 2-second success animation
        super.onBackPressed();
    }
}