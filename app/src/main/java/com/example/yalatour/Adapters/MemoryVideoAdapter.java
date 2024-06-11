package com.example.yalatour.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yalatour.Activities.FullScreenMediaActivity;
import com.example.yalatour.Classes.CustomVideoView;
import com.example.yalatour.R;

import java.util.List;

public class MemoryVideoAdapter extends RecyclerView.Adapter<MemoryVideoAdapter.ViewHolder> {
    private static final String TAG = "MemoryVideoAdapter";

    private Context context;
    private List<String> videoUrls;

    public MemoryVideoAdapter(Context context, List<String> videoUrls) {
        this.context = context;
        this.videoUrls = videoUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.memory_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String videoUrl = videoUrls.get(position);
        Uri videoUri = Uri.parse(videoUrl);

        // Load the video thumbnail using Glide
        Glide.with(context)
                .load(videoUrl)
                .into(holder.thumbnailView);

        holder.videoView.setVideoURI(videoUri);
        holder.videoView.pause(); // Ensure the video is not playing

        Log.d(TAG, "Binding video at position: " + position + ", URL: " + videoUrl);

        holder.playButton.setOnClickListener(v -> {
            Log.d(TAG, "Play button clicked at position: " + position);
            Intent intent = new Intent(context, FullScreenMediaActivity.class);
            intent.putExtra("mediaUrl", videoUrl); // Pass the clicked video URL
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CustomVideoView videoView;
        ImageButton playButton;
        ImageView thumbnailView;

        public ViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.MemoryVideo);
            playButton = itemView.findViewById(R.id.playButton);
            thumbnailView = itemView.findViewById(R.id.thumbnailView);
        }
    }
}
