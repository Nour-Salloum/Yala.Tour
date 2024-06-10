package com.example.yalatour.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.Post;
import com.example.yalatour.R;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;

    public PostsAdapter(Context context) {
        this.context = context;
        this.postList = new ArrayList<>();
        loadPosts();
    }

    private void loadPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("Posts").orderBy("time", Query.Direction.ASCENDING);

        query.addSnapshotListener((value, error) -> {
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

        public void setUsername(String Username) {
            TextView username = mView.findViewById(R.id.PostUsername);
            username.setText(Username);
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
    }
}