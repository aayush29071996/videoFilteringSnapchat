package com.example.aayush.videoplayer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoPlay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        Bundle extras = getIntent().getExtras();
        String i = extras.getString("i");
        String j = extras.getString("j");
        String p = extras.getString("key");



        int i1 = convertString(i);
        int i2 = Integer.parseInt(j)*1000;






        final VideoView videoView=(VideoView)findViewById(R.id.video_view);

        videoView.setVideoURI(Uri.parse(p));
        videoView.seekTo(0);
        videoView.start();
        videoView.postDelayed(new Runnable() {

            @Override
            public void run() {
                videoView.pause();

            }
        }, i2);



    }



// UPLOAD FILE FUNCTION SHOULD BE PLACED OVER HERE




    public int convertString(String s){



        String source = s;
        String[] tokens = source.split("-");
        int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
        int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
        int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
        int total = secondsToMs + minutesToMs + hoursToMs;

        return total;
    }
}
