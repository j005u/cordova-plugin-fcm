package com.gae.scaffolder.plugin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.content.res.Resources;
import android.graphics.Color;

/**
 * Created by Felipe Echanique on 08/06/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMPlugin";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");

        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("wasTapped", false);

        if(remoteMessage.getNotification() != null){
            if(remoteMessage.getNotification().getTitle() != null) {
              data.put("title", remoteMessage.getNotification().getTitle());
            }
            if(remoteMessage.getNotification().getBody() != null) {
              data.put("body", remoteMessage.getNotification().getBody());
            }
        }

        for (String key : remoteMessage.getData().keySet()) {
                Object value = remoteMessage.getData().get(key);
                Log.d(TAG, "\tKey: " + key + " Value: " + value);
                data.put(key, value);
        }

        Log.d(TAG, "\tNotification Data: " + data.toString());
        FCMPlugin.sendPushPayload( data );
        if(remoteMessage.getNotification() != null){
          sendNotification(remoteMessage, data);
        }
    }
    // [END receive_message]
    
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
        Log.e("NEW_TOKEN",refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        FCMPlugin.sendTokenRefresh(refreshedToken);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(RemoteMessage message, Map<String, Object> data) {
        RemoteMessage.Notification notification = message.getNotification();
        Intent intent = new Intent(this, FCMPluginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (String key : data.keySet()) {
            intent.putExtra(key, data.get(key).toString());
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Resources resources = getApplicationContext().getResources();
        int resourceId;

        if(notification.getIcon() != null) {
          resourceId = resources.getIdentifier(notification.getIcon(), "drawable",
             getApplicationContext().getPackageName());
        }
        else {
          resourceId = resources.getIdentifier("fcm_push_icon", "drawable",
             getApplicationContext().getPackageName());
        }
        if(resourceId == 0) {
          resourceId = getApplicationInfo().icon;
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(resourceId)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setChannelId("default_channel");

        if(notification.getTitle() != null) {
          notificationBuilder.setContentTitle(notification.getTitle());
        }
        if(notification.getBody() != null) {
          notificationBuilder.setContentText(notification.getBody());
        }

        if(notification.getColor() != null) {
          try { //invalid color string will throw
            int color = Color.parseColor(notification.getColor());
            notificationBuilder.setColor(color);
          } catch (Exception e) {
               // This will catch any exception, because they are all descended from Exception
               System.out.println("Error " + e.getMessage());
          }

        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(message.getMessageId(), 0, notificationBuilder.build());
    }
}
