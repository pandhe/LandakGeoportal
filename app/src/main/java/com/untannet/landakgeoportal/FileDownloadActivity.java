package com.untannet.landakgeoportal;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;


public class FileDownloadActivity extends Activity {
    Button btn_download;
    InputStreamVolleyRequest request;
    Service_Connector service_connector;
    int count;
    public  HashMap<String,String> myvar;
    Random rand = new Random();
    int n = rand.nextInt(999);
    private String filename = "kmlfile"+String.valueOf(n)+".kml";
    private String filepath = "MyFileStorage";
    File myExternalFile;
    String myData = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_download);
        service_connector=new Service_Connector();
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        myExternalFile = new File(path, filename);
        //btn_download =(Button)findViewById(R.id.button);

                String mUrl="http://207.148.119.223/newproject/kml/service/makekml";
                myvar=new HashMap<>();

                Intent intent=getIntent();
                download();


    }



    public void download(){
        String mUrl="http://207.148.119.223/newproject/kml/service/makekml";
        myvar=new HashMap<>();

        Intent intent=getIntent();
        myvar.put("js",intent.getStringExtra("js"));
        service_connector.sendurlpostrequest(this, mUrl, myvar, new Service_Connector.VolleyResponseListener_v3() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponese(String response) {

                //String filename = "kmlfile"+String.valueOf(n)+".kml";
                //String fileContents = "Hello world!";
                FileOutputStream outputStream;

                try {
                    FileOutputStream fos = new FileOutputStream(myExternalFile);
                    fos.write(response.getBytes());
                    fos.close();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNoConnection(String message) {

            }

            @Override
            public void OnServerError(String message) {

            }

            @Override
            public void OnTimeOut() {

            }
        });
    }
}
