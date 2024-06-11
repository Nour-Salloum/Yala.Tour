package com.example.yalatour.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.yalatour.R;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.Player;

public class FullScreenMediaActivity extends AppCompatActivity {

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private ImageView imageView;
    private ProgressBar progressBar;
    private String mediaUrl;
    private static final String TAG = "FullScreenMediaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);

        playerView = findViewById(R.id.playerView);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        mediaUrl = getIntent().getStringExtra("mediaUrl");

        if (mediaUrl != null && mediaUrl.contains(".mp4")) {
            // Video item
            playerView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            initializePlayer();
        } else {
            // Image item
            imageView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            Glide.with(this).load(mediaUrl).into(imageView);
        }
    }

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        Uri videoUri = Uri.parse(mediaUrl);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);

        // Add player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        progressBar.setVisibility(View.GONE);
                        break;
                    case Player.STATE_ENDED:
                        progressBar.setVisibility(View.GONE);
                        break;
                    case Player.STATE_IDLE:
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            }


            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

        // Prepare the player asynchronously
        player.prepare();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }
}
