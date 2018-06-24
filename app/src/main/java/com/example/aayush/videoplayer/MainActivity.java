package com.example.aayush.videoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG ="ANDROID DEBUGER:";
    Button button;
    Context context;
    EditText mEdit;
    EditText mEdit1;
    EditText mEdit2;
    Button getToken;


    String p ;
    int a=-1;
    int fi = 0;
    ArrayList<byte[]> allframeData = new ArrayList<>(100);
    int decodedWidth1 = -1;
    int decodedHeight1 = -1;


    private static final int REQUEST_PICK_FILE = 1;

    private TextView filePath;
    private Button Browse;
    private File selectedFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);



        setContentView(R.layout.activity_main);


        context = this;
         final RequestQueue MyRequestQueue = Volley.newRequestQueue(this);


        //   Browse = (Button)findViewById(R.id.browse);
     //   Browse.setOnClickListener((View.OnClickListener)this);

        getToken = (Button)findViewById(R.id.getTokenButton);

        getToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RegisterForPushNotificationsAsync().execute();

            }
        });







        button = (Button) findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            public void onClick(View arg0) {

                String all = getIntent().getStringExtra("param");


                final String[] partsSplit = all.split("T");









                String i = partsSplit[1];
                     //   START OR END TIME OF THE VIDEO
                String j = partsSplit[2];
                      //  DURATION TO PLAY THE VIDEO
                String k =  partsSplit[0];
                      //  DATE OF THE VIDEO


                int first =convertString(i);
                int second = Integer.parseInt(j)*1000;


                File f = new File("/sdcard/LightMetrics");

                File file[] = f.listFiles();

                String l = pFileChecker(file,k,first);

                fi = pFileIndexer(file,l);
                // Start NewActivity.class
                Intent myIntent = new Intent(MainActivity.this,
                        VideoPlay.class);


                Bundle extras = new Bundle();

                extras.putString("key",magicFun(first,second,fi));
                extras.putString("i", i);
                extras.putString("j", j);

                myIntent.putExtras(extras);

                startActivity(myIntent);
            }
        });







// Get Files in array

  //      File f = new File("/sdcard/LightMetrics");
  //      File file[] = f.listFiles();

//Get the file name

  //      String fileName = new File(String.valueOf(file[0])).getName();



// GET THE MSEC OF THE VIDEO FILE DURATION.

    //    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    //    retriever.setDataSource("/sdcard/LightMetrics/2017-06-13 16:39:38 1.mp4"); // Enter Full File Path Here
    //    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    //    int VideoDuration = Integer.parseInt(time);// This will give time in millesecond

        // GET THE TIME STRING IN MILLISECONDS
  //     String source = "00:10:17";
    //    String[] tokens = source.split(":");
    //    int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
     //   int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
     //   int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
     //   long total = secondsToMs + minutesToMs + hoursToMs;



    }

// Browse on Click Activity
    public void onClick(View v) {


        switch(v.getId()) {

    //        case R.id.browse:
       //         Intent intent = new Intent(this, FilePicker.class);
       //         startActivityForResult(intent, REQUEST_PICK_FILE);

      //          break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {

            switch(requestCode) {

                case REQUEST_PICK_FILE:

                    if(data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {

                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        p =  selectedFile.getPath();
                        File f = new File("/sdcard/LightMetrics");
                        File file[] = f.listFiles();

     //                    fi = pFileChecker(file,p);


                        filePath.setText(selectedFile.getPath());
                    }
                    break;
            }
        }
    }


    // File Checker and Comparator Function
public String pFileChecker(File file[] , String p, int q  ){
    ArrayList<String> validated = new ArrayList<>();

    int startTime = q;

    //parse the input string
    for(int i =0; i<=(file.length)-1;i++) {

    String initial = new File(String.valueOf(file[i])).getName();
    String[] parts = initial.split("T");


    if (parts[0].equals(p)) {

        validated.add(new File(String.valueOf(file[i])).getPath());

    }

}
// Parse in parsed folder
    for(int j=0 ; j<=(validated.size())-1; j++) {

       String wholePath = validated.get(j);
       String[] initParts = wholePath.split("/");
        String initial = initParts[3];
        String[] parts = initial.split("T");
        String parts2 = parts[2];
        String parts3 =parts2.substring(0, parts2.lastIndexOf('.'));


        int endTime = convertString(parts3);

        if (startTime <= endTime) {

            return "/sdcard/LightMetrics/" + initial;

        }

    }

return null;

}

//Index of File in the Database

public int pFileIndexer(File file[],String r){
    for(int i = 0; i<=(file.length)-1;i++){

        String filePath = new File(String.valueOf(file[i])).getPath();


        if(filePath.equals(r)){
            a= i;
        }
    }
    return a;
}

//Construct "P" string for input to the video

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public String magicFun(int s, int d, int filenum){

        int snum = 0;
        int dnum = d;
        int tot = snum+dnum;
        int filedur;
        File f = new File("/sdcard/LightMetrics");
        File file[] = f.listFiles();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file[filenum].getPath()); // Enter Full File Path Here
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        filedur = Integer.parseInt(time);


        if(tot<=filedur){

            String path = file[filenum].getPath();
            return path;
        }
        else {
            int a=0;

            outside:
            for (int i = filenum + 1; i <= file.length; i++) {

                retriever.setDataSource(file[filenum + i].getPath()); // Enter Full File Path Here
                String time1 = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
               int filedur1 = Integer.parseInt(time1);

                filedur = filedur + filedur1;
                if (tot <= filedur) {
                      a = i;
                    break outside;
                }
            }

            for(int i=filenum; i<=a; i++){
                {
                    DecodeEncode convert = new DecodeEncode(file[i].getPath());
                    try {

                        allframeData = convert.extractMpegFrames(allframeData);
                        decodedHeight1 = convert.getDecodedHeight();
                        decodedWidth1 = convert.getDecodedWidth();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            {
                DecodeEncode fconvert = new DecodeEncode("");
                fconvert.encodeAllFrames(allframeData, decodedWidth1, decodedHeight1);

            }

            String newPath ="/sdcard/LightMetrics/encodedOut.mp4";

            return newPath;
        }
    }


    public int convertString(String s){

        String source = s;
        String[] tokens = source.split("-");
        int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
        int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
        int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
        int total = secondsToMs + minutesToMs + hoursToMs;

        return total;
    }

    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {
        protected Exception doInBackground(Void... params) {
            try {
                final RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

                // Assign a unique token to this device
                String token = Pushy.register(getApplicationContext());

                // Log it for debugging purposes
                Log.d("MyApp", "Pushy device token: " + token);
                //TIME STAMP TO SERVER
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final  String timeStamp  = dateFormat.format(new Date());


                //IMEI NUMBER TO SEND TO SERVER
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                final  String deviceId = telephonyManager.getDeviceId();

                //TOKEN TO SEND TO THE SERVER
                final String driverId = deviceId;

                JSONObject jsonBodyObj = new JSONObject();
                try{
                          jsonBodyObj.put("token", token);
                    jsonBodyObj.put("deviceId", deviceId);
                    jsonBodyObj.put("driverId", driverId);
                    jsonBodyObj.put("timeStamp", timeStamp);




                }catch (JSONException e){
                    e.printStackTrace();
                }
                final String requestBody = jsonBodyObj.toString();
                String url = "https://lmremotevideo.mybluemix.net/getToken";


                JsonObjectRequest JOPR = new JsonObjectRequest(Request.Method.POST,
                        url, null


                        , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }


                    @Override
                    public byte[] getBody() {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                                    requestBody, "utf-8");
                            return null;
                        }
                    }


                };

                MyRequestQueue.add(JOPR);


                // Send the token to your backend server via an HTTP GET request
               // new URL("https://http://172.31.99.244:8081/getToken/token=" + deviceToken).openConnection();
            }
            catch (Exception exc) {
                // Return exc to onPostExecute
                return exc;
            }

            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Failed?
            if (exc != null) {
                // Show error as toast message
                Toast.makeText(getApplicationContext(), exc.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            // Succeeded, do something to alert the user
        }
    }





}
