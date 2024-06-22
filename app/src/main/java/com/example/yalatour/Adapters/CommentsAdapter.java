package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.Comment;
import com.example.yalatour.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private Context context;
    private List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView dateTextView;
        TextView commentTextView;
        ImageView profileImageView; // Added ImageView for profile image

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.CommentUsername);
            dateTextView = itemView.findViewById(R.id.CommentDate);
            commentTextView = itemView.findViewById(R.id.CommentText);
            profileImageView = itemView.findViewById(R.id.ProfileProfileImage); // Initialize profile image view
        }

        public void bind(Comment comment) {
            usernameTextView.setText(comment.getUsername());
            dateTextView.setText(comment.getCommentDate());
            commentTextView.setText(comment.getCommentDescription());

            // Load profile image using Picasso
            if (comment.getProfileImageUrl() != null && !comment.getProfileImageUrl().isEmpty()) {
                Picasso.get().load(comment.getProfileImageUrl()).into(profileImageView);
            } else {
                // Handle case where profile image URL is empty or null
                profileImageView.setImageResource(R.drawable.baseline_person_24); // Default image resource
            }
        }
    }
}