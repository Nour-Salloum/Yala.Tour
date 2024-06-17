package com.example.yalatour.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.Post;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder> {
    private List<Post> postList;
    private Context context;

    public ProfilePostsAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
        loadPosts();
    }

    private void loadPosts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("date", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle error
                        return;
                    }

                    if (value != null) {
                        postList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Post post = doc.toObject(Post.class);
                            post.setPostId(doc.getId()); // Set the postId from document ID
                            postList.add(post);
                        }
                        notifyDataSetChanged();
                    }
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.setUsername(post.getUsername());
        holder.setTime(post.getTime());
        holder.setDate(post.getDate());
        holder.setDescription(post.getDescription());
        holder.setPlacename(post.getPlacename());
        holder.setPostimage(context, post.getPostimage());
        holder.setProfileImage(context, post.getProfileImageUrl());
        holder.bindDeleteButton(post, context, postList, this); // Bind the delete button
        holder.bindEditButton(post, context); // Bind the edit button
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView postUsername = mView.findViewById(R.id.PostUsername);
            postUsername.setText(username);
        }

        public void setTime(String time) {
            TextView postTime = mView.findViewById(R.id.post_time);
            postTime.setText("   " + time);
        }

        public void setDate(String date) {
            TextView postDate = mView.findViewById(R.id.post_date);
            postDate.setText("   " + date);
        }

        public void setDescription(String description) {
            TextView postDescription = mView.findViewById(R.id.PostDescription);
            postDescription.setText(description);
        }

        public void setPlacename(String placename) {
            TextView placeName = mView.findViewById(R.id.PlaceName);
            placeName.setText(placename);
        }

        public void setPostimage(Context ctx, String postimage) {
            ImageView postImage = mView.findViewById(R.id.PostImage);
            Picasso.get().load(postimage).into(postImage);
        }

        public void setProfileImage(Context ctx, String profileImageUrl) {
            CircleImageView profileImage = (CircleImageView) mView.findViewById(R.id.ProfileImage);

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Picasso.get().load(profileImageUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.baseline_person_24);
            }
        }

        public void bindDeleteButton(Post post, Context context, List<Post> postList, ProfilePostsAdapter adapter) {
            ImageView deletePost = mView.findViewById(R.id.DeletePost);
            deletePost.setOnClickListener(v -> {
                if (post.getPostId() == null) {
                    Toast.makeText(context, "Post ID is null, cannot delete post", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(context)
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Posts").document(post.getPostId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        postList.remove(post);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error deleting post", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            });
        }

        public void bindEditButton(Post post, Context context) {
            ImageView editPost = mView.findViewById(R.id.EditPost);
            editPost.setOnClickListener(v -> {
                Intent editIntent = new Intent(context, UploadPostActivity.class);
                editIntent.putExtra("PostId", post.getPostId());
                editIntent.putExtra("Description", post.getDescription());
                editIntent.putExtra("PlaceName", post.getPlacename());
                editIntent.putExtra("PostImage", post.getPostimage());
                context.startActivity(editIntent);
            });
        }
    }
}