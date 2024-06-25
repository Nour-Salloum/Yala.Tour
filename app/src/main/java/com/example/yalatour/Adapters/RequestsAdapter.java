package com.example.yalatour.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Classes.FCMNotificationSender;
import com.example.yalatour.Classes.TripRequestsClass;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder>{
    private Context context;
    private List<TripRequestsClass> Requests;
    private String tripId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RequestsAdapter(Context context, List<TripRequestsClass> requests, String tripId) {
        this.context = context;
        Requests = requests;
        this.tripId = tripId;
    }

    @NonNull
    @Override
    public RequestsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.requests_recycleritem, parent, false);
        return new RequestsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestsAdapter.ViewHolder holder, int position) {
        TripRequestsClass Request = Requests.get(position);
        holder.Username.setText(Request.getRequest_Username());
        String Userid = Request.getRequest_UserId();
        holder.Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AcceptRequest(Userid, position);
            }
        });
        holder.Deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to deny this request?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRequest(position);
                                Toast.makeText(context, "Request is denied", Toast.LENGTH_SHORT).show();
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
        return Requests.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Username;
        ImageButton Accept,Deny;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Username =itemView.findViewById(R.id.RequestUsername);
            Accept=itemView.findViewById(R.id.Accept);
            Deny=itemView.findViewById(R.id.Deny);
        }
    }
    private void AcceptRequest(String userId, int position) {
        if (tripId != null) {
            db.collection("Trips").document(tripId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String tripName = documentSnapshot.getString("tripName");
                            List<String> userIds = new ArrayList<>();
                            userIds.addAll((List<String>) documentSnapshot.get("usersid"));
                            userIds.add(userId);
                            db.collection("Trips").document(tripId)
                                    .update("usersid", userIds)
                                    .addOnSuccessListener(aVoid -> {
                                        notifyDataSetChanged();
                                        deleteRequest(position);
                                        Toast.makeText(context, "Request is Accepted", Toast.LENGTH_SHORT).show();
                                        SendNotificationtoMember(userId, tripName, tripId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e("TripDetails", "Trip document does not exist");
                            Toast.makeText(context, "Failed to accept request: Trip document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TripDetails", "Failed to retrieve trip details", e);
                        Toast.makeText(context, "Failed to retrieve trip details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("TripDetails", "TripId is null");
            Toast.makeText(context, "Failed to accept request: TripId is null", Toast.LENGTH_SHORT).show();
        }
    }



    private void deleteRequest(int position) {

        TripRequestsClass deletedRequest = Requests.get(position);
        Requests.remove(position);
        notifyItemRemoved(position);

        // Perform deletion in Firestore
        db.collection("Trips").document(tripId)
                .update("requests", FieldValue.arrayRemove(deletedRequest))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(context, "Failed to Deny request", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void SendNotificationtoMember(String userId, String tripName,String TripId) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Admin document exists, retrieve the FCM token list
                        List<String> MemberFCMTokens = (List<String>) documentSnapshot.get("fcmToken");
                        if (MemberFCMTokens != null && !MemberFCMTokens.isEmpty()) {
                            // Send notification to each admin FCM token in the list
                            for (String MemberFCMToken :MemberFCMTokens) {
                                FCMNotificationSender fcmNotificationSender = new FCMNotificationSender(MemberFCMToken, "Acceptance to Trip", "You are Accepted to join the Trip " + tripName, context,TripId);
                                fcmNotificationSender.sendNotification();
                            }
                        } else {
                            // Admin FCM token list is not available or empty
                            Toast.makeText(context, "User FCM token list not available or empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Admin document does not exist
                        Toast.makeText(context, "User document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve admin document
                    Toast.makeText(context, "User to retrieve admin document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
