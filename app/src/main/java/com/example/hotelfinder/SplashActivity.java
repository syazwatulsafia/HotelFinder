package com.example.hotelfinder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoView);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splashscreen);
        videoView.setVideoURI(videoUri);

        // Start video and set it to loop
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            videoView.start();
        });

        // Delay 2 seconds before entering MainActivity (Login Screen)
        new Handler().postDelayed(() -> {
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
            }
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}