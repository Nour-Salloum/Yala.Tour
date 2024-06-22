package com.example.yalatour.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Uri> imageUris;
    private OnImageClickListener onImageClickListener;

    public ImageAdapter(Context context, List<Uri> imageUris, OnImageClickListener onImageClickListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_display, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Picasso.get().load(imageUri).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageButton deleteImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.placeImage);
            deleteImage = itemView.findViewById(R.id.DeleteImage);

            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onImageClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onImageClickListener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnImageClickListener {
        void onDeleteClick(int position);
    }
}
