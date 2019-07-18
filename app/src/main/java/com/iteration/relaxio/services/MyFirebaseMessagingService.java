package com.iteration.relaxio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iteration.relaxio.R;
import com.iteration.relaxio.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAGs = MyFirebaseMessagingService.class.getSimpleName();

    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final int NOTIFICATION_ID = 453436;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAGs, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAGs, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAGs, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAGs, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");
            //Toast.makeText(this, "" + data.toString(), Toast.LENGTH_SHORT).show();

            String title = data.getString("title");
            String message = data.getString("message");

            JSONObject payload = data.getJSONObject("payload");

            /*Log.e(TAGs, "title: " + title);
            Log.e(TAGs, "message: " + message);
            Log.e(TAGs, "payload: " + payload.toString());*/

            addNotification(title,message);

        } catch (JSONException e) {
            Log.e(TAGs, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAGs, "Exception: " + e.getMessage());
        }
    }

    private void addNotification(String title,String message) {
        Intent OpenApp = new Intent(this, MainActivity.class);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.white_nocie_logo);
        mBuilder.setContentTitle(""+title)

                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setContentIntent(PendingIntent.getActivity(this, 0, OpenApp,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentText(message).setAutoCancel(true).setOngoing(false);
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.app_name) + "_NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }
}
