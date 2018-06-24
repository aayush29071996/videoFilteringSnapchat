package com.example.aayush.videoplayer;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.content.Context;
import android.media.RingtoneManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.nfc.Tag;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by aayush on 6/7/17.
 */

public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle="" ;
        String notificationText="" ;
        String all="";

        // Attempt to extract the "message" property from the payload: {"message":"Hello World!"}
        if (intent.getStringExtra("title") != null) {
            notificationTitle = intent.getStringExtra("title");
        }
        if (intent.getStringExtra("message") != null) {
            notificationText = intent.getStringExtra("message");
        }

        if (intent.getStringExtra("body") != null) {
            all = intent.getStringExtra("body");
            Log.e("dataaaaa",all);
        }

        Intent intent1 = new Intent(context ,MainActivity.class);
        intent1.putExtra("param", all);
        context.startActivity(intent1);


        // Prepare a notification with vibration, sound and lights
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
               .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Build the notification and display it
        notificationManager.notify(1, builder.build());



    }



}