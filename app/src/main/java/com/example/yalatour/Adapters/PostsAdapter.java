package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public PostsAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
        loadPosts();
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

        // Check if the current context is HomePage and hide the buttons accordingly
        if (context instanceof HomePage) {
            holder.hideEditAndDeleteButtons();
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
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

        public void hideEditAndDeleteButtons() {
            editPost.setVisibility(View.GONE);
            deletePost.setVisibility(View.GONE);
        }
    }

}