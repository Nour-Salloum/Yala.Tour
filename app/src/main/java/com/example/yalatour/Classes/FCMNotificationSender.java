package com.example.yalatour.Classes;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMNotificationSender {
    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;
    private final String TripId;

    public FCMNotificationSender(String userFcmToken, String title, String body, Context context, String tripId) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;
        TripId = tripId;
    }

    public void sendNotification() {
        Log.d(TAG, "sendNotification method called");
        RequestQueue requestQueue = Volley.newRequestQueue(context);//which manages HTTP requests for networking operations.

        try {
            // Construct the JSON payload
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", title);
            notificationObject.put("body", body);
            Log.d(TAG, "Notification object created: " + notificationObject.toString());

            JSONObject dataObject = new JSONObject();
            dataObject.put("TripId", TripId);  // Include trip ID
            Log.d(" Notification Data", "Trip ID:"+TripId);
            dataObject.put("viewRequests", String.valueOf(true));
            Log.d(TAG, "Data object created: " + dataObject.toString());

            JSONObject messageObject = new JSONObject();
            messageObject.put("token", userFcmToken);
            messageObject.put("notification", notificationObject); // Include notification object in the message
            messageObject.put("data", dataObject); // Include data object in the message
            Log.d(TAG, "Message object created: " + messageObject.toString());

            JSONObject mainObj = new JSONObject();
            mainObj.put("message", messageObject);
            Log.d(TAG, "Main JSON object created: " + mainObj.toString());

            // Create the Volley request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/v1/projects/yala-tour/messages:send",
                    mainObj, response -> {
                // Notification sent successfully, handle response if needed
                Log.d(TAG, "FCM Notification Response: " + response.toString());
            }, volleyError -> {
                // Log the error for debugging
                if (volleyError.networkResponse != null) {
                    String statusCode = String.valueOf(volleyError.networkResponse.statusCode);
                    Log.e(TAG, "FCM Notification Error. Status Code: " + statusCode);
                    Log.e(TAG, "Error Response: " + new String(volleyError.networkResponse.data));
                } else {
                    Log.e(TAG, "FCM Notification Error: " + volleyError.getMessage());
                }
            }) {
                // Override getHeaders() to include authorization headers
                @Override
                public Map<String, String> getHeaders() {
                    AccessToken accessToken = new AccessToken();
                    String accessKey = accessToken.getAccessToken();
                    Log.d(TAG, "Access Key: " + accessKey);
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + accessKey);
                    return headers;
                }
            };

            // Add the request to the request queue
            Log.d(TAG, "Adding request to queue");
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
