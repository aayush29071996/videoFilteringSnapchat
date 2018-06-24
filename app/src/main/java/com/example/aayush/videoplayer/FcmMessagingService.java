package com.example.aayush.videoplayer;

/**
 * Created by aayush on 16/6/17.
 */

/*
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;




    public class FcmMessagingService extends FirebaseMessagingService {
        private static final String TAG ="ERROR" ;
        String type = "";



        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            if (remoteMessage.getData().size() > 0) {
                type = "json";
                sendNotification(remoteMessage.getData().toString());
            }
            if (remoteMessage.getNotification() !=null) {
                type = "message";
                sendNotification(remoteMessage.getNotification().getBody());
            }
        }
        private void sendNotification(String messageBody){
            String id="",message="",title="", all ="";

            if(type.equals("json")) {
                try {
                    JSONObject jsonObject = new JSONObject(messageBody);
                    String  date = jsonObject.getString("date1");
                    String  startTime = jsonObject.getString("startTime");
                    String  duration = jsonObject.getString("duration");
                    all = date + startTime + duration;

                } catch (JSONException e) {
                    //            }
                }
            }
            else if(type.equals("message"))
            {
                all = messageBody;

            }


            Intent intent = new Intent(FcmMessagingService .this, MainActivity.class);
            intent.putExtra("param", all);
            startActivity(intent);




            NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this);
            notificationBuilder.setContentTitle(getString(R.string.app_name));
            notificationBuilder.setContentText(all);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSound(soundUri);
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher));
            notificationBuilder.setAutoCancel(true);
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
            NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0,notificationBuilder.build());



        }
    }


*/