package com.example.yalatour.Adapters;

import static com.example.yalatour.Classes.MessageService.TAG;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.yalatour.Activities.CommentsActivity;
import com.example.yalatour.Activities.ProfileActivity;
import com.example.yalatour.Classes.Comment;
import com.example.yalatour.Classes.Post;
import com.example.yalatour.R;
import com.example.yalatour.UploadActivities.UploadPostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder> {
    private List<Post> postList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        holder.setProfileImage(context, post.getProfileImageUrl());
        holder.bindDeleteButton(post, context, postList, this); // Bind the delete button
        holder.bindEditButton(post, context); // Bind the edit button

        holder.setLikes(post.getNumLikes());


        // Set images in ViewPager2
        holder.setPostImages(context, post.getPostImages());

        // Bind like/dislike button click listeners
        holder.bindLikeButton(post, context, this);

        // Load Comments
        loadComments(post.getPostId(), holder);

        // Pass adapter and position
        holder.saveCommentData(post.getPostId(), context, this, position);

        holder.allComments.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("username", post.getUsername());
            intent.putExtra("date", post.getDate());
            intent.putExtra("time", post.getTime());
            intent.putExtra("description", post.getDescription());
            intent.putExtra("placeName", post.getPlacename());
            intent.putExtra("profileImageUrl", post.getProfileImageUrl());
            context.startActivity(intent);
        });
    }

    private void loadComments(String postId, ProfilePostsAdapter.ViewHolder holder) {
        db.collection("Comments")
                .whereEqualTo("postId", postId)
                .orderBy("commentDate", Query.Direction.DESCENDING)
                .limit(1) // Limit to the last 2 comments
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        comment.setCommentId(doc.getId());
                        comments.add(comment);
                    }

                    // Bind comments to CommentsRecyclerView using CommentsAdapter
                    CommentsAdapter commentsAdapter = new CommentsAdapter(context, comments);
                    holder.CommentsRecyclerView.setAdapter(commentsAdapter);
                    holder.CommentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading comments", e);
                    Toast.makeText(context, "Failed to load comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button Send;
        EditText CommentText;
        TextView allComments;
        RecyclerView CommentsRecyclerView;

        private ImageButton likeButton;
        private ImageButton dislikeButton;
        private TextView likes;
        ViewPager2 PostImagePager;
        // Add a LinearLayout or any other suitable layout to hold indicators
        LinearLayout imageIndicatorContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            Send = mView.findViewById(R.id.Send);
            CommentText = mView.findViewById(R.id.CommentText);
            allComments = mView.findViewById(R.id.AllComments);
            CommentsRecyclerView = mView.findViewById(R.id.CommentsRecyclerView);

            likeButton = mView.findViewById(R.id.LikeButton);
            dislikeButton = mView.findViewById(R.id.DislikeButton);
            likes = mView.findViewById(R.id.Likes);

            PostImagePager = itemView.findViewById(R.id.PostImagePager);

            // Initialize imageIndicatorContainer
            imageIndicatorContainer = mView.findViewById(R.id.imageIndicatorContainer);
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

        public void setProfileImage(Context ctx, String profileImageUrl) {
            CircleImageView profileImage = (CircleImageView) mView.findViewById(R.id.ProfileImage);

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Picasso.get().load(profileImageUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.baseline_person_24);
            }
        }
        public void setPostImages(Context context, List<String> postImages) {
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(context, postImages);
            PostImagePager.setAdapter(imagePagerAdapter);

            // Add image indicators
            setupImageIndicators(postImages.size());
        }

        private void setupImageIndicators(int numImages) {
            imageIndicatorContainer.removeAllViews(); // Clear existing indicators

            // Create indicators for each image
            ImageView[] indicators = new ImageView[numImages];
            for (int i = 0; i < numImages; i++) {
                indicators[i] = new ImageView(itemView.getContext());
                indicators[i].setImageResource(R.drawable.indicator_inactive); // Set inactive indicator initially

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(8, 0, 8, 0); // Adjust margins as needed
                indicators[i].setLayoutParams(layoutParams);

                imageIndicatorContainer.addView(indicators[i]);
            }

            // Highlight the first indicator initially
            if (numImages > 0) {
                indicators[0].setImageResource(R.drawable.indicator_active); // Set the first indicator active
            }

            // ViewPager2 page change listener to update indicators
            PostImagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    // Update indicators based on current page
                    for (int i = 0; i < numImages; i++) {
                        // Calculate the "opposite" position
                        int oppositePosition = (numImages - 1) - position;

                        indicators[i].setImageResource(
                                i == oppositePosition ? R.drawable.indicator_active : R.drawable.indicator_inactive);
                    }
                }
            });
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

                            // First, query the comments outside of the batch
                            db.collection("Comments")
                                    .whereEqualTo("postId", post.getPostId())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<String> commentIds = new ArrayList<>();
                                            for (QueryDocumentSnapshot commentDoc : task.getResult()) {
                                                commentIds.add(commentDoc.getId());
                                            }

                                            // Now delete the post and comments in a batch
                                            db.runBatch(batch -> {
                                                // Delete the post
                                                batch.delete(db.collection("Posts").document(post.getPostId()));

                                                // Delete each comment
                                                for (String commentId : commentIds) {
                                                    batch.delete(db.collection("Comments").document(commentId));
                                                }
                                            }).addOnSuccessListener(aVoid -> {
                                                postList.remove(post);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(context, "Post and related comments deleted", Toast.LENGTH_SHORT).show();
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(context, "Error deleting post and comments", Toast.LENGTH_SHORT).show();
                                            });
                                        } else {
                                            Toast.makeText(context, "Failed to query comments", Toast.LENGTH_SHORT).show();
                                        }
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
                editIntent.putStringArrayListExtra("PostImages", (ArrayList<String>) post.getPostImages()); // Pass postImages list
                context.startActivity(editIntent);
            });
        }

        public void saveCommentData(String postId, Context context, ProfilePostsAdapter adapter, int position) {
            Send.setOnClickListener(v -> {
                String commentText = CommentText.getText().toString().trim();
                if (!TextUtils.isEmpty(commentText)) {
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Get username and profileImageUrl from Firestore "Users" collection
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Users").document(currentUserId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String username = documentSnapshot.getString("username");
                                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                                    String commentDate = getCurrentDateTime(); // Ensure this method returns a valid date format

                                    // Create a Comment object
                                    Comment comment = new Comment(null, username, postId, commentDate, commentText, profileImageUrl);

                                    // Access Firestore instance and add comment
                                    FirebaseFirestore dbComments = FirebaseFirestore.getInstance();
                                    dbComments.collection("Comments")
                                            .add(comment)
                                            .addOnSuccessListener(documentReference -> {
                                                Toast.makeText(context, "Comment added successfully", Toast.LENGTH_SHORT).show();
                                                adapter.notifyDataSetChanged();
                                                CommentText.setText(""); // Clear comment text field after successful addition
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, "Error adding comment", e); // Log error for debugging
                                            });
                                } else {
                                    Toast.makeText(context, "User document doesn't exist", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error fetching user data", e); // Log error for debugging
                            });
                } else {
                    Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String getCurrentDateTime() {
            // Replace with your date formatting logic
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return df.format(new Date());
        }

        public void bindLikeButton(Post post, Context context, ProfilePostsAdapter adapter) {
            likeButton.setOnClickListener(v -> {
                likeButton.setVisibility(View.GONE);
                dislikeButton.setVisibility(View.VISIBLE);

                // Update number of likes in Firestore
                int updatedLikes = post.getNumLikes() + 1;
                updateLikes(post.getPostId(), updatedLikes);

                // Update local post object
                post.setNumLikes(updatedLikes);
                setLikes(updatedLikes); // Update likes count in UI
            });

            dislikeButton.setOnClickListener(v -> {
                dislikeButton.setVisibility(View.GONE);
                likeButton.setVisibility(View.VISIBLE);

                // Update number of likes in Firestore
                int updatedLikes = post.getNumLikes() - 1;
                updateLikes(post.getPostId(), updatedLikes);

                // Update local post object
                post.setNumLikes(updatedLikes);
                setLikes(updatedLikes); // Update likes count in UI
            });
        }

        public void setLikes(int likesCount) {
            likes.setText(likesCount + (likesCount == 1 ? " like" : " likes"));
        }

        private void updateLikes(String postId, int updatedLikes) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Posts").document(postId)
                    .update("numLikes", updatedLikes)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
        }
    }
}