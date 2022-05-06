package com.example.goaltracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessages extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        generateNotification(remoteMessage);
        Log.d("Messaging", "message received: " + remoteMessage.getData());
    }

    private void generateNotification(@NonNull RemoteMessage remoteMessage) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // notification icon
                .setContentTitle(remoteMessage.getData().get("title")) // title for notification
                .setContentText(remoteMessage.getData().get("body")) // message for notification
                .setAutoCancel(true) // clear notification after click
                .setChannelId(CHANNEL_ID);
        Intent intent = new Intent(this, LoadingWindow.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE);
        mBuilder.setContentIntent(pi);

        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i("TOKEN", "Refreshed token: " + token);
    }
}
