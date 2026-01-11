package com.example.hotelfinder;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class ReviewSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_success);

        VideoView videoView = findViewById(R.id.success);

        // Path to your video in res/raw
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.success;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Start playing
        videoView.start();

        // Automatically close the screen when the video is done
        videoView.setOnCompletionListener(mp -> {
            finish();
        });
    }
}