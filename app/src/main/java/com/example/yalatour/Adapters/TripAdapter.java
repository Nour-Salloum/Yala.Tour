package com.example.yalatour.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yalatour.Activities.TripActivity;
import com.example.yalatour.Classes.FCMNotificationSender;
import com.example.yalatour.Classes.TripClass;
import com.example.yalatour.Classes.TripRequestsClass;
import com.example.yalatour.DetailsActivity.TripDetails;
import com.example.yalatour.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder>{
    private Context context;
    private List<TripClass> Trips;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUserId = currentUser.getUid() ;

    String currentUserName ;


    public TripAdapter(Context context, List<TripClass> trips) {
        this.context = context;
        Trips = trips;
    }

    @NonNull
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.triprecycleritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.ViewHolder holder, int position) {
        TripClass Trip=Trips.get(position);

        holder.TripName.setText(Trip.getTripName());
        holder.TripDate.setText(Trip.getTripDate());
        List<String> usersId = Trip.getUsersid();
        List<TripRequestsClass> Requests=Trip.getRequests();

        if (currentUserId != null && !currentUserId.equals(Trip.getTripAdminid()) && (usersId == null || !usersId.contains(currentUserId))&& !isUserInRequests(Requests,currentUserId)){
            holder.TripCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowTripRequestDialog(Trip);
                }
            });
        }
        else if (isUserInRequests(Requests,currentUserId)) {
            holder.TripCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "You Request is still Pending..." , Toast.LENGTH_SHORT).show();
                }
            });

        } else{
            holder.TripCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, TripDetails.class);
                    intent.putExtra("TripId", Trip.getTripId());
                    context.startActivity(intent);
                }
            });

        }
        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog before deleting the place
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this Trip?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteTrip(Trip);
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
        holder.Share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Create the share intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                // Set the subject and text of the share message
                String shareSubject = "The Code of the Trip " + Trip.getTripName() + " is:";
                String shareText = "The Code of the Trip " + Trip.getTripName() + " is: \n"+ Trip.getCode();

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                // Start the activity to share using an intent chooser
                context.startActivity(Intent.createChooser(shareIntent, "Share Trip Code"));
            }
        });



        // Compare the current user's ID with the ID of the user who created the trip
        if (currentUserId != null && Trip.getTripAdminid() != null && currentUserId.equals(Trip.getTripAdminid())) {
            holder.Delete.setVisibility(View.VISIBLE);
            holder.Share.setVisibility(View.VISIBLE);

        }
        else{
            holder.Delete.setVisibility(View.GONE);
            holder.Share.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return Trips.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView TripName,TripDate;
        CardView TripCard;
        private ImageButton Share,Delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            TripName=itemView.findViewById(R.id.TripName);
            TripDate=itemView.findViewById(R.id.TripDate);
            TripCard=itemView.findViewById(R.id.TripCard);
            Share=itemView.findViewById(R.id.Share);
            Delete=itemView.findViewById(R.id.DeleteTrip);




        }
    }
    private void deleteTrip(TripClass trip){
        DocumentReference tripRef = FirebaseFirestore.getInstance().collection("Trips").document(trip.getTripId());
        tripRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Trips.remove(trip);
                        notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show toast message for failure
                        Toast.makeText(context, "Failed to delete trip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void ShowTripRequestDialog(TripClass trip) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_request_trip, null);
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(dialogView);

        // Set dialog window position to the bottom
        dialog.getWindow().setGravity(Gravity.CENTER);

        // Set dialog window width to match parent
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        EditText TripCode = dialogView.findViewById(R.id.TripCode);
        Button Request = dialogView.findViewById(R.id.SendRequest);
        TextView CheckCode = dialogView.findViewById(R.id.CheckCode);

        Request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripCode = TripCode.getText().toString().trim(); // Trim the input
                String actualTripCode = trip.getCode().trim(); // Trim the actual code
                String adminUserId = trip.getTripAdminid().trim();
                if (tripCode.equals(actualTripCode)) {
                    // Retrieve the username before sending the request
                    db.collection("Users").document(currentUserId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Retrieve the username and assign it to the variable
                                    currentUserName = documentSnapshot.getString("username");
                                    // Send the request only after username retrieval
                                    String requestId = UUID.randomUUID().toString();
                                    TripRequestsClass request = new TripRequestsClass(requestId, currentUserId, currentUserName);
                                    List<TripRequestsClass> Requests = new ArrayList<>();
                                    Requests.add(request);

                                    db.collection("Trips").document(trip.getTripId())
                                            .update("requests", Requests)
                                            .addOnSuccessListener(aVoid -> {
                                                SendNotificationtoAdmin(adminUserId, currentUserName, trip.getTripName(),trip.getTripId());
                                                Log.d("TripAdapter", "Trip ID:"+trip.getTripId());
                                                dialog.dismiss();
                                                notifyDataSetChanged();

                                                Toast.makeText(context, "Your Request is Sent ", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle update failure
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to retrieve user document
                                Log.e("TripAdapter", "Failed to retrieve user document: " + e.getMessage());
                            });
                } else {
                    CheckCode.setText("The Code is Wrong");
                }
            }
        });

        dialog.show();
    }



    private void SendNotificationtoAdmin(String adminId, String userName, String tripName,String TripId) {
        FirebaseFirestore.getInstance().collection("Users").document(adminId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Admin document exists, retrieve the FCM token list
                        List<String> adminFCMTokens = (List<String>) documentSnapshot.get("fcmToken");
                        if (adminFCMTokens != null && !adminFCMTokens.isEmpty()) {
                            // Send notification to each admin FCM token in the list
                            for (String adminFCMToken : adminFCMTokens) {
                                FCMNotificationSender fcmNotificationSender = new FCMNotificationSender(adminFCMToken, "New Join Request", userName + " has requested to join your trip: " + tripName, context,TripId);
                                fcmNotificationSender.sendNotification();
                            }
                        } else {
                            // Admin FCM token list is not available or empty
                            Toast.makeText(context, "Admin FCM token list not available or empty", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Admin document does not exist
                        Toast.makeText(context, "Admin document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve admin document
                    Toast.makeText(context, "Failed to retrieve admin document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private boolean isUserInRequests(List<TripRequestsClass> requests, String userId) {
        if (requests != null) {
            for (TripRequestsClass request : requests) {
                if (request.getRequest_UserId() != null && request.getRequest_UserId().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }


}