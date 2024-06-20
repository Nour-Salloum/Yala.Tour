package com.example.yalatour.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Activities.HomePage;
import com.example.yalatour.Classes.Post;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;
    private boolean isAdmin;

    public PostsAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
        loadPosts();
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("admin");
                        this.isAdmin = isAdmin != null && isAdmin;
                        notifyDataSetChanged(); // Notify adapter to update views

                        // Show a toast indicating admin status
                        if (isAdmin != null) {
                            showToast(isAdmin ? "You are an admin" : "You are not an admin");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to check admin status");
                });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void loadPosts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Posts")
                .whereNotEqualTo("userId", currentUserId)
                .orderBy("userId")
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
                            post.setPostId(doc.getId());
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

        // Set visibility of deletePost based on isAdmin
        holder.deletePost.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // Check if the current context is HomePage and hide the buttons accordingly
        if (context instanceof HomePage) {
            holder.hideEditAndDeleteButtons(isAdmin);
        }

        // Setup delete button listener
        holder.bindDeleteButton(post, context, postList, this);

        // Setup like and dislike button listeners
        holder.bindLikeDislikeButtons(post, this);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void removePost(int position) {
        postList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView editPost;
        ImageView deletePost;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            editPost = mView.findViewById(R.id.EditPost);
            deletePost = mView.findViewById(R.id.DeletePost);
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
            CircleImageView profileImage = mView.findViewById(R.id.ProfileImage);
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Picasso.get().load(profileImageUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.baseline_person_24);
            }
        }

        public void hideEditAndDeleteButtons(boolean isAdmin) {
            if (!isAdmin) {
                editPost.setVisibility(View.GONE);
                deletePost.setVisibility(View.GONE);
            }
        }

        public void bindDeleteButton(Post post, Context context, List<Post> postList, PostsAdapter adapter) {
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

        public void bindLikeDislikeButtons(Post post, PostsAdapter adapter) {
            ImageButton likeButton = mView.findViewById(R.id.LikeButton);
            ImageButton dislikeButton = mView.findViewById(R.id.DislikeButton);

            likeButton.setOnClickListener(v -> {
                // Increment likes by 1
                post.setLikes(post.getLikes() + 1);

                // Update UI
                likeButton.setVisibility(View.GONE);
                dislikeButton.setVisibility(View.VISIBLE);

                // Update likes text view
                updateLikesTextView(post.getLikes());

                // Notify adapter
                adapter.notifyDataSetChanged();
            });

            dislikeButton.setOnClickListener(v -> {
                // Decrement likes by 1
                post.setLikes(post.getLikes() - 1);

                // Update UI
                likeButton.setVisibility(View.VISIBLE);
                dislikeButton.setVisibility(View.GONE);

                // Update likes text view
                updateLikesTextView(post.getLikes());

                // Notify adapter
                adapter.notifyDataSetChanged();
            });
        }

        private void updateLikesTextView(int likes) {
            TextView likesTextView = mView.findViewById(R.id.Likes);
            likesTextView.setText(likes + " likes");
        }
    }

}
