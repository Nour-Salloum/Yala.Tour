package com.example.yalatour.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    private Context context;
    private List<String> membersIds;
    private FirebaseFirestore db;
    private String tripId;

    public MembersAdapter(Context context, List<String> membersIds, String tripId) {
        this.context = context;
        this.membersIds = membersIds;
        this.tripId = tripId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.members_recycleritem, parent, false);
        return new MembersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, int position) {
        String memberId = membersIds.get(position);

        // Fetch username and profile image URL for memberId
        db.collection("Users").document(memberId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve username and profile image URL from Firestore document
                        String username = documentSnapshot.getString("username");
                        holder.Username.setText(username);
                        String imageUrl = documentSnapshot.getString("profileImageUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Load the image into the CircleImageView using Glide
                            Glide.with(context)
                                    .load(imageUrl)

                                    .into(holder.MembersProfileImage);
                        } else {
                            holder.MembersProfileImage.setImageResource(R.drawable.baseline_person_blue);
                        }
                    } else {
                        // Handle the case where the user document doesn't exist
                        holder.Username.setText("Unknown User");
                        holder.MembersProfileImage.setImageResource(R.drawable.baseline_person_blue);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    holder.Username.setText("Unknown User");
                    holder.MembersProfileImage.setImageResource(R.drawable.baseline_person_blue);
                });

        holder.Remove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to remove this member?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteMember(position);
                        Toast.makeText(context, "Member is removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return membersIds.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Username;
        CircleImageView MembersProfileImage;
        ImageButton Remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Username = itemView.findViewById(R.id.MembersName);
            MembersProfileImage = itemView.findViewById(R.id.MembersProfileImage);
            Remove = itemView.findViewById(R.id.Remove);
        }
    }

    private void deleteMember(int position) {
        String deleteMember = membersIds.get(position);
        membersIds.remove(position);
        notifyItemRemoved(position);

        // Perform deletion in Firestore
        db.collection("Trips").document(tripId)
                .update("usersid", FieldValue.arrayRemove(deleteMember))
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove member", Toast.LENGTH_SHORT).show());
    }
}
