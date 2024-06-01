package com.example.yalatour.Classes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.yalatour.Activities.TripActivity;
import com.example.yalatour.DetailsActivity.TripDetails;
import com.example.yalatour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

public class MessageService extends FirebaseMessagingService {

    private static final String TAG = "MessageService";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        updateNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "onMessageReceived: Message received");

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 10, 100, 200};
        vibrator.vibrate(pattern, -1);

        // Get the trip ID and viewRequests flag from the message data
        String tripId = message.getData().get("TripId");
        Log.d("Message", "Trip ID  in notification1:"+tripId);
        boolean viewRequests = Boolean.parseBoolean(message.getData().get("viewRequests"));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Fetch the trip details from Firestore
            db.collection("Trips").document(tripId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    TripClass trip = documentSnapshot.toObject(TripClass.class);
                    String tripAdminId = trip.getTripAdminid();
                    List<String> membersIdsList = trip.getUsersid();

                    if (tripAdminId.equals(currentUserId) || (membersIdsList != null && membersIdsList.contains(currentUserId))) {
                        Intent resultIntent = new Intent(this, TripDetails.class);
                        resultIntent.putExtra("TripId", tripId);
                        resultIntent.putExtra("ViewRequests", viewRequests);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        // Create notification builder
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YalaTour.5");
                        builder.setContentTitle(message.getNotification() != null ? message.getNotification().getTitle() : "YalaTour")
                                .setContentText(message.getNotification() != null ? message.getNotification().getBody() : "You have a new message.")
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getNotification() != null ? message.getNotification().getBody() : ""))
                                .setAutoCancel(true)
                                .setVibrate(pattern)
                                .setSmallIcon(R.drawable.add)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent);

                        // Show the notification
                        Log.d(TAG, "Showing notification...");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            String channelId = "YalaTour.5";
                            NotificationChannel channel = new NotificationChannel(
                                    channelId, "YalaTour", NotificationManager.IMPORTANCE_HIGH
                            );
                            channel.enableLights(true);
                            channel.enableVibration(true);
                            channel.setVibrationPattern(pattern);
                            notificationManager.createNotificationChannel(channel);

                            builder.setChannelId(channelId);
                        }
                        notificationManager.notify(100, builder.build());
                    } else {
                        // Current user is neither an admin nor a member
                        Log.e(TAG, "User is not authorized to view the trip details");
                        Toast.makeText(this, "You are not authorized to view this trip", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Trip not found");
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Error fetching trip details", e));
        } else {
            Log.e(TAG, "User not logged in");
        }
    }


    private void updateNewToken(String token){

    }
}
