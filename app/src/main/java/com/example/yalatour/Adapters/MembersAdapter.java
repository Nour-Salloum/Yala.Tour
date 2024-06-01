package com.example.yalatour.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.TripRequestsClass;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MembersAdapter  extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    Context context;
    List<String> Membersids;
   FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tripId;

    public MembersAdapter(Context context, List<String> membersids, String tripId) {
        this.context = context;
        Membersids = membersids;
        this.tripId = tripId;
    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.members_recycleritem, parent, false);
        return new MembersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersAdapter.ViewHolder holder, int position) {
        String memberId = Membersids.get(position);

        // Fetch username for memberId
        db.collection("Users").document(memberId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve username from Firestore document
                        String username = documentSnapshot.getString("username");
                        holder.Username.setText(username);
                    } else {
                        // Handle the case where the user document doesn't exist
                        holder.Username.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    holder.Username.setText("Unknown User");
                });

        holder.Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to Remove this Member?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMember(position);
                                Toast.makeText(context, "Member is Removed ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing, dismiss the dialog
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return Membersids.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Username;
        Button Remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Username = itemView.findViewById(R.id.MembersName);
            Remove=itemView.findViewById(R.id.Remove);
        }
    }
        private void deleteMember(int position) {

        String deleteMember = Membersids.get(position);
        Membersids.remove(position);
        notifyItemRemoved(position);

        // Perform deletion in Firestore
        db.collection("Trips").document(tripId)
                .update("usersid", FieldValue.arrayRemove(deleteMember))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(context, "Failed to Remove Member", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
