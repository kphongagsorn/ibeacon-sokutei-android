package producesu.ntt.com.ibeaconsokutei;

/**
 * Created by kphongagsorn on 10/27/14.
 */


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ScaleGestureDetector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.polites.android.GestureImageView;
/**
 * Created by kphongagsorn on 10/27/14.
 */
public class PredMapView extends Activity {
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;


    private RelativeLayout rl_map;
    static ImageView map ;
    private TextView coordLabel;
    private Button saveBtn;
    private Button learnXYBtn;
    private Button learnBtn;
    private Button predictBtn;
    private Button doneBtn;

    StringBuilder timeApiIsCalledStrB;




    ArrayList<MyBeacons> soinnBeaconList =null;
    ArrayList<MyWifiAP> soinnWifiApList   = null;
    ArrayList<String> signalListForLog = null;
    StringBuilder soinnResponse   =null;

    StringBuilder soinnSecRange;
    StringBuilder comingFromMainView;


    StringBuilder apiCallTypeForDataFile;
    float mapWidth;
    float mapHeight;
    double floorX;
    double floorY;
    float soinnPredictedX;
    float soinnPredictedY;



    //private boolean isTouch = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pred_map_view);
        //setContentView(R.layout.activiy_pred_map_zoomable_view);

        rl_map = (RelativeLayout) findViewById(R.id.rl_pred_map);
     //   map = new ImageView(this);
        map  = (ImageView)findViewById(R.id.predMapImageView);
        map.setImageDrawable(getResources().getDrawable(R.drawable.edited_ibeacon_map_480));
        coordLabel = (TextView)findViewById(R.id.predMapCoordLabel);
        saveBtn = (Button)findViewById(R.id.saveBtn);
        learnXYBtn = (Button)findViewById(R.id.learnXYBtn);
        learnBtn = (Button)findViewById(R.id.learnBtn);
        predictBtn = (Button)findViewById(R.id.predictBtn);
        doneBtn = (Button)findViewById(R.id.doneBtnPredMap);

        float h = map.getHeight();
        float w = map.getWidth();


        rl_map.bringToFront();
       // map.bringToFront();
        saveBtn.bringToFront();
        learnBtn.bringToFront();
        learnXYBtn.bringToFront();
        predictBtn.bringToFront();
        doneBtn.bringToFront();

        mapWidth = 0;
        mapHeight = 0;
        floorX = 0.0;
        floorY = 0.0;
        //soinnPredictedX =0;
        //soinnPredictedY =0;

        //apiCallTypeForDataFile="";

        /*
        Bundle data = getIntent().getExtras();
        soinnBeaconList = new ArrayList<MyBeacons>((ArrayList)data.getParcelable("soinnBeaconList"));
        soinnWifiApList  = new ArrayList<MyWifiAP>((ArrayList)data.getParcelable("soinnWifiApList"));
        */

        soinnBeaconList =new ArrayList<>();
        soinnWifiApList   = new ArrayList<>();
        signalListForLog = new ArrayList<>();

        soinnBeaconList = getIntent().getParcelableArrayListExtra("soinnBeaconList");
        soinnWifiApList =  getIntent().getParcelableArrayListExtra("soinnWifiApList");
        signalListForLog = getIntent().getStringArrayListExtra("signalListForLog");

        soinnResponse  = new StringBuilder(getIntent().getStringExtra("soinnResponse"));
        soinnSecRange = new StringBuilder(getIntent().getStringExtra("soinnSecRange"));
        apiCallTypeForDataFile = new StringBuilder(getIntent().getStringExtra("apiCallTypeForDataFile"));
        comingFromMainView = new StringBuilder(getIntent().getStringExtra("comingFromMain"));

        timeApiIsCalledStrB = new StringBuilder(getIntent().getStringExtra("timeApiIsCalledStrFromMV"));




        Map<String, String> soinnResponseMap = new LinkedHashMap<>();
        for(String keyValue : soinnResponse.toString().split(" *, *")) {
            String[] pairs = keyValue.split(" *: *", 2);
            soinnResponseMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }


        if (soinnResponseMap.containsKey("\"x\"")) {
           soinnPredictedX = Float.parseFloat(soinnResponseMap.get("\"x\""));
        }
        else{
            soinnPredictedX =0;
        }
        if (soinnResponseMap.containsKey("\"y\"")) {
            String yValue = soinnResponseMap.get("\"y\"").substring(0,soinnResponseMap.get("\"y\"").length()-1);
            Log.i("y:",yValue);
            soinnPredictedY = Float.parseFloat(yValue);
        }
        else{
            soinnPredictedY =0;
        }

        // View mView = new MyView(this);

        MyView mV = new MyView(this, map, soinnPredictedX,soinnPredictedY);
        rl_map.addView(mV);
        //   rl_map.addView(mView);
        rl_map.bringToFront();




        Toast.makeText(getApplicationContext(),soinnResponse,Toast.LENGTH_SHORT).show();
        String coordinateStr ="predX: "+String.format("%.2f", soinnPredictedX)+ ", predY: "+String.format("%.2f", soinnPredictedY)+
                ", learnX: "+ String.format("%.2f", floorX) +", learnY: "+ String.format("%.2f", floorY) +
                ", error: "+ String.format("%.2f", calculateErrorSabun((float)floorX,(float)floorY,soinnPredictedX,soinnPredictedY));
        coordLabel.setText(coordinateStr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
         mapWidth=map.getWidth();
         mapHeight=map.getHeight();
    }

    public void onDoneButtonClickPredMap(View view){
        Intent intent = new Intent(PredMapView.this,MainView.class);
        this.startActivity(intent);

    }

    /*
    // 2.0 and above
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        //Intent intent = new Intent(PredMapView.this,MainView.class);
        //this.startActivity(intent);
    }
    */


    /**
     * write to external storage; for galaxy devices
     * */
    public  void writeFileToExternalPredMap(String fileName, String body)
    {
        String tempBody ="";
        tempBody = body;
        FileOutputStream fos = null;
        FileWriter fw = null;

        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/iBeacon/" );

            if (!dir.exists())
            {
                dir.mkdirs();
            }

            final File myFile = new File(dir, fileName);

            if (!myFile.exists())
            {
                myFile.createNewFile();
            }

            fw = new FileWriter(myFile,true);
            fw.write(tempBody);

            fw.flush();
            fw.close();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.e("stack trace",exceptionAsString);
        }
    }

    /**
     * Save beacon information; not creating custom beacon objects for over 10 secs
     * Recordings over 10 sec will be directly recorded as string
     * */
    public void onSaveButtonClickPredMap(View view){
        //Time recordedTime = new Time();
        //recordedTime.setToNow();
        //String timeStamp = timeApiIsCalled.format("%Y-%m-%d,%H:%M:%S");
        String filename= "soinnData.txt";
        try {
            FileOutputStream fos = openFileOutput(filename, MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            /*
            String tempOutputString="\n"+"Time saved:"+ timeStamp + "\n";
            tempOutputString+="Api:"+apiCallTypeForDataFile.toString() + "\n";
            tempOutputString+="predicted x:"+soinnPredictedX + ", predicted y:"+soinnPredictedY+"\n";
            tempOutputString+="learned x:"+floorX + ", learned y:"+floorY+"\n";
            tempOutputString+="Soinn seconds range:" +soinnSecRange.toString() +"\n";
            tempOutputString+="Input data:\n";
            */

            //String tempOutputString="\n"+ timeApiIsCalled + ",";
            String tempOutputString="\n"+ timeApiIsCalledStrB.toString() + ",";
            tempOutputString+=apiCallTypeForDataFile.toString() + ",";
            tempOutputString+=soinnPredictedX + ","+soinnPredictedY+",";
            tempOutputString+=floorX + ", "+floorY+",";
            tempOutputString+=soinnSecRange.toString() +",";


            //when button is first pressed; coming from MainView activity
            if (comingFromMainView.toString().equalsIgnoreCase("yes")){
                /*
                soinnBeaconList =new ArrayList<>();
                soinnBeaconList = getIntent().getParcelableArrayListExtra("soinnBeaconList");
                soinnWifiApList   = new ArrayList<>();
                soinnWifiApList =  getIntent().getParcelableArrayListExtra("soinnWifiApList");
                */
                signalListForLog =new ArrayList<>();
                signalListForLog = getIntent().getStringArrayListExtra("signalListForLog");
                comingFromMainView = new StringBuilder("no");

            }


            if(signalListForLog!=null ) {
                for (String s : signalListForLog) {
                    tempOutputString += s;
                }
            }
            /*
            if(soinnWifiApList!=null){
                if(soinnWifiApList.size()>0){
                   tempOutputString+=soinnWifiApList.toString();
                }
            }
            */

            tempOutputString+=",";
            tempOutputString+= soinnResponse+",";
            tempOutputString+=Float.toString(calculateErrorSabun((float)floorX,(float)floorY,soinnPredictedX,soinnPredictedY))+"\n";
            osw.write(tempOutputString);
            osw.flush();
            osw.close();
            writeFileToExternalPredMap(filename, tempOutputString);
            Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("onSaveButtonClick", e.toString());
        }

        //this.finish();
        /*
        //for debugging logging to text file functionality
        try {
            FileInputStream fileIn = openFileInput(filename);
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[100];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s += readString;
            }
            InputRead.close();
            Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
        }catch (IOException e){
            e.printStackTrace();
        }
        */

    }

    public void onSoinnLearnXYButtonClickPredMap(View view){
        apiCallTypeForDataFile = new StringBuilder("learnxy");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("LearnXY?");
        builder.setItems(new CharSequence[]
                        {"1 second", "3 seconds", "5 seconds", "10 seconds"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String soinnResponse;
                        switch (which) {
                            case 0:
                                soinnSecRange = new StringBuilder("1");
                                soinnResponse = postData("learnxy", soinnBeaconList, soinnWifiApList, 1, floorX, floorY);

                                //Intent intent = new Intent(PredMapView., PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                //this.startActivityForResult(intent, soinnResponse);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();

                                break;

                            case 1:
                                soinnSecRange = new StringBuilder("3");
                                soinnResponse = postData("learnxy", soinnBeaconList, soinnWifiApList, 3, floorX, floorY);

                                //intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();

                                break;

                            case 2:
                                soinnSecRange = new StringBuilder("5");
                                soinnResponse = postData("learnxy", soinnBeaconList, soinnWifiApList, 5, floorX, floorY);

                                //intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();

                                break;

                            case 3:
                                soinnSecRange = new StringBuilder("10");
                                soinnResponse = postData("learnxy", soinnBeaconList, soinnWifiApList, 10, floorX, floorY);

                               // intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();

    }

    public void onSoinnLearnButtonClickPredMap(View view){
        apiCallTypeForDataFile = new StringBuilder("learn");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Learn?");
        builder.setItems(new CharSequence[]
                        {"1 second", "3 seconds", "5 seconds", "10 seconds"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String soinnResponse;
                        switch (which) {
                            case 0:
                                soinnSecRange = new StringBuilder("1");
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 1);
                              //  Intent intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                //this.startActivityForResult(intent, soinnResponse);


                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();


                                break;


                            case 1:
                                soinnSecRange = new StringBuilder("3");
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 3);
                              //  intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();

                                break;

                            case 2:
                                soinnSecRange = new StringBuilder("5");
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 5);
                               // intent = new Intent(getApplicationContext(), PredMapView.class);
                               // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();

                                break;
                            case 3:
                                soinnSecRange = new StringBuilder("10");
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 10);
                               // intent = new Intent(getApplicationContext(), PredMapView.class);
                               // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               // getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(), soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();

    }

    public void onSoinnPredictButtonClickPredMap(View view){
        apiCallTypeForDataFile = new StringBuilder("predict");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Predict Location?");
        builder.setItems(new CharSequence[]
                        {"1 second", "3 seconds", "5 seconds", "10 seconds"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String soinnResponse;
                        switch (which) {
                            case 0:
                                soinnSecRange = new StringBuilder("1");
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,1);
                                Map<String, String> soinnResponseMap = new LinkedHashMap<>();
                                for(String keyValue : soinnResponse.toString().split(" *, *")) {
                                    String[] pairs = keyValue.split(" *: *", 2);
                                    soinnResponseMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                                }


                                if (soinnResponseMap.containsKey("\"x\"")) {
                                    // String xValue = soinnResponseMap.get("\"x\"").substring(0,soinnResponseMap.get("\"x\"").length()-1);
                                    //soinnPredictedX = Float.parseFloat(soinnResponseMap.get(xValue));
                                    soinnPredictedX = Float.parseFloat(soinnResponseMap.get("\"x\""));

                                }
                                if (soinnResponseMap.containsKey("\"y\"")) {
                                    String yValue = soinnResponseMap.get("\"y\"").substring(0,soinnResponseMap.get("\"y\"").length()-1);
                                    Log.i("y:",yValue);
                                    soinnPredictedY = Float.parseFloat(yValue);
                                }
                                //Intent intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                //this.startActivityForResult(intent, soinnResponse);

                                Toast.makeText(getApplicationContext(),soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                soinnSecRange = new StringBuilder("3");
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,3);
                                soinnResponseMap = new LinkedHashMap<>();
                                for(String keyValue : soinnResponse.toString().split(" *, *")) {
                                    String[] pairs = keyValue.split(" *: *", 2);
                                    soinnResponseMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                                }


                                if (soinnResponseMap.containsKey("\"x\"")) {
                                    // String xValue = soinnResponseMap.get("\"x\"").substring(0,soinnResponseMap.get("\"x\"").length()-1);
                                    //soinnPredictedX = Float.parseFloat(soinnResponseMap.get(xValue));
                                    soinnPredictedX = Float.parseFloat(soinnResponseMap.get("\"x\""));

                                }
                                if (soinnResponseMap.containsKey("\"y\"")) {
                                    String yValue = soinnResponseMap.get("\"y\"").substring(0,soinnResponseMap.get("\"y\"").length()-1);
                                    Log.i("y:",yValue);
                                    soinnPredictedY = Float.parseFloat(yValue);
                                }
                                //intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(),soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                soinnSecRange = new StringBuilder("5");
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,5);
                                soinnResponseMap = new LinkedHashMap<>();
                                for(String keyValue : soinnResponse.toString().split(" *, *")) {
                                    String[] pairs = keyValue.split(" *: *", 2);
                                    soinnResponseMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                                }


                                if (soinnResponseMap.containsKey("\"x\"")) {
                                    // String xValue = soinnResponseMap.get("\"x\"").substring(0,soinnResponseMap.get("\"x\"").length()-1);
                                    //soinnPredictedX = Float.parseFloat(soinnResponseMap.get(xValue));
                                    soinnPredictedX = Float.parseFloat(soinnResponseMap.get("\"x\""));

                                }
                                if (soinnResponseMap.containsKey("\"y\"")) {
                                    String yValue = soinnResponseMap.get("\"y\"").substring(0,soinnResponseMap.get("\"y\"").length()-1);
                                    Log.i("y:",yValue);
                                    soinnPredictedY = Float.parseFloat(yValue);
                                }
                                //intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(),soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                soinnSecRange = new StringBuilder("10");
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,10);
                                soinnResponseMap = new LinkedHashMap<>();
                                for(String keyValue : soinnResponse.toString().split(" *, *")) {
                                    String[] pairs = keyValue.split(" *: *", 2);
                                    soinnResponseMap.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                                }


                                if (soinnResponseMap.containsKey("\"x\"")) {
                                    // String xValue = soinnResponseMap.get("\"x\"").substring(0,soinnResponseMap.get("\"x\"").length()-1);
                                    //soinnPredictedX = Float.parseFloat(soinnResponseMap.get(xValue));
                                    soinnPredictedX = Float.parseFloat(soinnResponseMap.get("\"x\""));

                                }
                                if (soinnResponseMap.containsKey("\"y\"")) {
                                    String yValue = soinnResponseMap.get("\"y\"").substring(0,soinnResponseMap.get("\"y\"").length()-1);
                                    Log.i("y:",yValue);
                                    soinnPredictedY = Float.parseFloat(yValue);
                                }
                                //intent = new Intent(getApplicationContext(), PredMapView.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);

                                Toast.makeText(getApplicationContext(),soinnResponse,
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();

    }





    /**
     * curl POST arguments for soinn learn xy
     */
    public String postData(String resourceUrl, ArrayList<MyBeacons> soinnBeaconList, ArrayList<MyWifiAP> soinnWifiApList, int soinnSecRange, double x, double y) {

        ArrayList<Signal> signalList = new ArrayList<Signal>();
        signalListForLog = new ArrayList<>();

        String osver = "";
        String model = "";
        String phoneId = "";
        String soinnName = "";
        //String soinnName = Build.PRODUCT;

        Time timeApiIsCalled = new Time();
        timeApiIsCalled.setToNow();
        timeApiIsCalledStrB= new StringBuilder(timeApiIsCalled.format("%Y-%m-%d,%H:%M:%S"));
        //timeApiIsCalledStr = timeStamp;

        Time startOfBeaconRecording = new Time();
        Time startOfWifiRecording = new Time();
        if(soinnBeaconList!=null) {
            for (int i = 0; i < soinnBeaconList.size(); i++) {
                MyBeacons b = soinnBeaconList.get(i);
                if (i == 0) {
                    startOfBeaconRecording.set(b.timeInMilli);
                }
                Signal s = new Signal(b.timeStamp, b.timeInMilli, b.rssi, b.uuid, b.major, b.minor, true);
                signalList.add(s);
                osver = b.os;
                model = b.device;
                phoneId = b.deviceId;
                soinnName = b.device;

            }
            String arrows = "F-02G";
            String galaxyj = "SC-02F";
            String xperia = "SO-02F";

            if (soinnName.equals(arrows)){

            } else if (soinnName.equals(galaxyj)){

            } else if (soinnName.equals(xperia)){

            } else{
                soinnName ="general";
            }
        }

        if(soinnWifiApList!=null) {
            for (int i = 0; i < soinnWifiApList.size(); i++) {
                MyWifiAP w = soinnWifiApList.get(i);
                if (i == 0) {
                    startOfWifiRecording.set(w.timeInMilli);
                }
                Signal s = new Signal(w.timeStamp, w.timeInMilli, w.rssi, w.bssid, false);
                signalList.add(s);
            }
            Time startRecordingTime = new Time();
            if (startOfBeaconRecording.toMillis(true) > startOfWifiRecording.toMillis(true)) {
                startRecordingTime.set(startOfWifiRecording.toMillis(true));
            } else {
                startRecordingTime.set(startOfBeaconRecording.toMillis(true));
            }
        }

        Collections.sort(signalList, new Comparator<Signal>() {
            @Override
            public int compare(Signal signal1, Signal signal2) {
                return Long.compare(signal1.timeRecordedInMilli, signal2.timeRecordedInMilli);
            }
        });




        Signal[] signalsListArray = signalList.toArray(new Signal[signalList.size()]);
        Map<String, Signal[]> signalParameters = new LinkedHashMap<>();
        String timeStampKey = null;

        JSONObject signalParameterJSONObj = new JSONObject();
        List<String> stringArrayofSignal = new ArrayList<>();

        //organize signals into map(timeStampKey, Signal[])
        int j = 1;//comparison beacon incrementer
        int location = 0;//array location holder
        for (int i = 0; i < signalsListArray.length; i++) {
            if (j == signalsListArray.length) {
                Signal currentSignal = signalsListArray[i];
                timeStampKey = currentSignal.timeStamp;
                Signal sigArr[] = Arrays.copyOfRange(signalsListArray, location, j);


                //for debugging
                String s="";
                for(int k = 0; k<sigArr.length; k++){
                    if(sigArr[k].isABeaconSignal==true){
                        s= s+ sigArr[k].getBeaconJsonAsString() +",";
                    }else if (sigArr[k].isABeaconSignal==false) {
                        s = s+sigArr[k].getWifiJsonAsString()+ ",";
                    }
                }
                if(s!=null || !(s.isEmpty())) s = s.substring(0,s.length()-1);
                stringArrayofSignal.add(s);


                signalParameters.put(timeStampKey, sigArr);
                signalParameterJSONObj.put(timeStampKey, JSONValue.toJSONString(sigArr));
                break;

            } else {
                Signal currentSignal = signalsListArray[i];
                Signal nextSignal = signalsListArray[j];

                if (currentSignal.timeRecordedInMilli == nextSignal.timeRecordedInMilli) {
                    //keep reading
                } else {
                    timeStampKey = currentSignal.timeStamp;
                    Signal sigArr[] = Arrays.copyOfRange(signalsListArray, location, j);

                    //for debugging
                    String s="";
                    for(int k = 0; k<sigArr.length; k++){
                        if(sigArr[k].isABeaconSignal==true){
                            s= s+ sigArr[k].getBeaconJsonAsString() +",";
                        }else if (sigArr[k].isABeaconSignal==false) {
                            s = s+sigArr[k].getWifiJsonAsString()+ ",";
                        }
                    }
                    if(s!=null || !(s.isEmpty())) s = s.substring(0,s.length()-1);
                    stringArrayofSignal.add(s);

                    signalParameters.put(timeStampKey, sigArr);
                    signalParameterJSONObj.put(timeStampKey,JSONValue.toJSONString(sigArr));
                    int temp = j;
                    location = temp;

                }
            }
            j++;
        }



        //Log.i("beacon sub arrays as strings: ", stringArrayofSignal.toString());

        Object[] keysParamArr = signalParameterJSONObj.keySet().toArray();
        String[] keysParamStrArray = Arrays.copyOf(keysParamArr, keysParamArr.length, String[].class);
        Log.i("keysParamStrArray: ",keysParamStrArray.toString());

        JSONArray signalsArrayParam = new JSONArray();
        JSONArray argsArr = new JSONArray();

        String signalsArrayParamAsString ="";


        //add signals according to soinn seconds range and convert to JSONArray
        for(int i= 0; i<soinnSecRange; i++){
            signalsArrayParam.put(stringArrayofSignal.get(i));

            signalsArrayParamAsString = signalsArrayParamAsString +
                    "{" +
                    "\"datetime\":"+ "\"" + keysParamStrArray[i] + "\"" + "," +
                    "\"signal_array\":["+ stringArrayofSignal.get(i) + "]" +
                    "},";
            signalListForLog.add(stringArrayofSignal.get(i));

        }
        if( signalsArrayParamAsString!=null || !( signalsArrayParamAsString.isEmpty())) {
            signalsArrayParamAsString =  signalsArrayParamAsString.substring(0, signalsArrayParamAsString.length()-1);
        }

        Log.i(" signalsArrayParam: ",  signalsArrayParam.toString());

        //create signals array parameter
        for(int i= 0; i<signalsArrayParam.length(); i++){
            JSONObject args = new JSONObject();
            args.put("datetime",keysParamArr[i]);
            // args.put("signal_array",JSONValue.toJSONString(signalsArrayParam.opt(i)));
            args.put("signal_array",signalsArrayParam.opt(i));
            argsArr.put(args);

        }

        JSONArray argArrForEverything = new JSONArray();
        argArrForEverything.put(argsArr);

        //change thread policy to all http post on main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create a new HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://153.149.174.102/" + resourceUrl.toLowerCase());


        try {
            StringEntity strEntPara = new StringEntity(
                    "{\"osver\":"+ "\""+osver+"\"," +
                            "\"model\":"+ "\""+model+"\"," +
                            "\"phone_id\":"+ "\""+phoneId+"\"," +
                            "\"soinn_name\":"+ "\""+soinnName+"\"," +
                            "\"soinn_sec_range\":"+Integer.toString(soinnSecRange)+"," +
                            "\"signals_array\":["+signalsArrayParamAsString+"]" +"," +
                            "\"x\":"+Double.toString(x)+"," +
                            "\"y\":"+Double.toString(y) +
                            "}"
            );

            httppost.setEntity(strEntPara);

            //set headers
            httppost.setHeader("Content-Type", "application/json; charset=utf-8");
            httppost.setHeader("Accept", "application/json; charset=utf-8");

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            Log.i("JSON response obj:", result.toString());

            //for debugging on kitkat devices
            //Toast.makeText(getApplicationContext(), "soinnName: "+soinnName,Toast.LENGTH_LONG).show();

            return  result.toString();

        } catch (ClientProtocolException e) {
            //Log.e("ClientProtocolException: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();

        } catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();

        } catch (Exception e) {
            Log.e("Exception: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();
        }

    }



    /**
     * POST for soinn learn and predict
     * */
    public String postData(String resourceUrl, ArrayList<MyBeacons> soinnBeaconList, ArrayList<MyWifiAP> soinnWifiApList, int soinnSecRange) {

        ArrayList<Signal> signalList = new ArrayList<Signal>();
        signalListForLog = new ArrayList<>();
        String osver = "";
        String model = "";
        String phoneId = "";
        String soinnName = "";
        //String soinnName = Build.PRODUCT;


        Time timeApiIsCalled = new Time();
        timeApiIsCalled.setToNow();
        timeApiIsCalledStrB = new StringBuilder(timeApiIsCalled.format("%Y-%m-%d,%H:%M:%S"));
        //timeApiIsCalledStr = timeStamp;

        Time startOfBeaconRecording = new Time();
        Time startOfWifiRecording = new Time();
        if(soinnBeaconList!=null) {
            for (int i = 0; i < soinnBeaconList.size(); i++) {
                MyBeacons b = soinnBeaconList.get(i);
                if (i == 0) {
                    startOfBeaconRecording.set(b.timeInMilli);
                }
                Signal s = new Signal(b.timeStamp, b.timeInMilli, b.rssi, b.uuid, b.major, b.minor, true);
                signalList.add(s);
                osver = b.os;
                model = b.device;
                phoneId = b.deviceId;
                soinnName = b.device;

            }
            String arrows = "F-02G";
            String galaxyj = "SC-02F";
            String xperia = "SO-02F";
            if (soinnName.equals(arrows)){

            } else if (soinnName.equals(galaxyj)){

            } else if (soinnName.equals(xperia)){

            } else{
                soinnName ="general";
            }
        }

        if(soinnWifiApList!=null) {
            for (int i = 0; i < soinnWifiApList.size(); i++) {
                MyWifiAP w = soinnWifiApList.get(i);
                if (i == 0) {
                    startOfWifiRecording.set(w.timeInMilli);
                }
                Signal s = new Signal(w.timeStamp, w.timeInMilli, w.rssi, w.bssid, false);
                signalList.add(s);
            }
            Time startRecordingTime = new Time();
            if (startOfBeaconRecording.toMillis(true) > startOfWifiRecording.toMillis(true)) {
                startRecordingTime.set(startOfWifiRecording.toMillis(true));
            } else {
                startRecordingTime.set(startOfBeaconRecording.toMillis(true));
            }
        }

        Collections.sort(signalList, new Comparator<Signal>() {
            @Override
            public int compare(Signal signal1, Signal signal2) {
                return Long.compare(signal1.timeRecordedInMilli, signal2.timeRecordedInMilli);
            }
        });

        //signalListForLog = signalList;

        Signal[] signalsListArray = signalList.toArray(new Signal[signalList.size()]);
        //Map<String, Signal[]> signalParameters = new LinkedHashMap<>();
        String timeStampKey = null;

        ArrayList<Object[]> signalParametersArrayList= new ArrayList<>();

        JSONObject signalParameterJSONObj = new JSONObject();
        List<String>  stringArrayofSignal = new ArrayList<String>();

        //organize signals into map(timeStampKey, Signal[])
        int j = 1;//comparison beacon incrementer
        int location = 0;//array location holder
        for (int i = 0; i < signalsListArray.length; i++) {
            if (j == signalsListArray.length) {
                Signal currentSignal = signalsListArray[i];
                timeStampKey = currentSignal.timeStamp;
                Signal sigArr[] = Arrays.copyOfRange(signalsListArray, location, j);


                //for debugging
                String s="";
                for(int k = 0; k<sigArr.length; k++){
                    if(sigArr[k].isABeaconSignal==true){
                        s= s+ sigArr[k].getBeaconJsonAsString() +",";
                    }else if (sigArr[k].isABeaconSignal==false) {
                        s = s+sigArr[k].getWifiJsonAsString()+ ",";
                    }
                }
                if(s!=null || !(s.isEmpty())) s = s.substring(0,s.length()-1);
                stringArrayofSignal.add(s);


                //signalParameters.put(timeStampKey, sigArr);
                signalParameterJSONObj.put(timeStampKey,JSONValue.toJSONString(sigArr));
                break;

            } else {
                Signal currentSignal = signalsListArray[i];
                Signal nextSignal = signalsListArray[j];

                if (currentSignal.timeRecordedInMilli == nextSignal.timeRecordedInMilli) {
                    //keep reading
                } else {
                    timeStampKey = currentSignal.timeStamp;
                    Signal sigArr[] = Arrays.copyOfRange(signalsListArray, location, j);


                    String s="";
                    for(int k = 0; k<sigArr.length; k++){
                        if(sigArr[k].isABeaconSignal==true){
                            s= s+ sigArr[k].getBeaconJsonAsString() +",";
                        }else if (sigArr[k].isABeaconSignal==false) {
                            s = s+sigArr[k].getWifiJsonAsString()+ ",";
                        }
                    }
                    if(s!=null || !(s.isEmpty())) s = s.substring(0,s.length()-1);
                    stringArrayofSignal.add(s);


                    // signalParameters.put(timeStampKey, sigArr);
                    signalParameterJSONObj.put(timeStampKey,JSONValue.toJSONString(sigArr));
                    int temp = j;
                    location = temp;

                }
            }
            j++;
        }



        //Log.i("beacon sub arrays as strings: ", stringArrayofSignal.toString());

        Object[] keysParamArr = signalParameterJSONObj.keySet().toArray();
        String[] keysParamStrArray = Arrays.copyOf(keysParamArr, keysParamArr.length, String[].class);
        Log.i("keysParamStrArray: ",keysParamStrArray.toString());

        JSONArray  signalsArrayParam = new JSONArray();

        JSONArray argsArr = new JSONArray();

        String signalsArrayParamAsString ="";


        //add signals according to soinn seconds range and convert to JSONArray
        for(int i= 0; i<soinnSecRange; i++){
            signalsArrayParam.put(stringArrayofSignal.get(i));

            signalsArrayParamAsString = signalsArrayParamAsString +
                    "{" +
                    "\"datetime\":"+ "\"" + keysParamStrArray[i] + "\"" + "," +
                    "\"signal_array\":["+ stringArrayofSignal.get(i) + "]" +
                    "},";
            signalListForLog.add(stringArrayofSignal.get(i));

        }
        if( signalsArrayParamAsString!=null || !( signalsArrayParamAsString.isEmpty())) {
            signalsArrayParamAsString =  signalsArrayParamAsString.substring(0, signalsArrayParamAsString.length()-1);
        }

        Log.i(" signalsArrayParam: ",  signalsArrayParam.toString());

        //create signals array parameter
        for(int i= 0; i<signalsArrayParam.length(); i++){
            JSONObject args = new JSONObject();
            args.put("datetime",keysParamArr[i]);
            // args.put("signal_array",JSONValue.toJSONString(signalsArrayParam.opt(i)));
            args.put("signal_array",signalsArrayParam.opt(i));
            argsArr.put(args);

        }

        JSONArray argArrForEverything = new JSONArray();
        argArrForEverything.put(argsArr);

        //change thread policy to all http post on main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create a new HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://153.149.174.102/" + resourceUrl.toLowerCase());


        try {
            StringEntity strEntPara = new StringEntity(
                    "{" +
                            "\"osver\":"+ "\""+osver+"\"," +
                            "\"model\":"+ "\""+model+"\"," +
                            "\"phone_id\":"+ "\""+phoneId+"\"," +
                            "\"soinn_name\":"+ "\""+soinnName+"\"," +
                            "\"soinn_sec_range\":"+Integer.toString(soinnSecRange)+"," +
                            "\"signals_array\":["+signalsArrayParamAsString+"]" +
                            "}"
            );

            httppost.setEntity(strEntPara);

            //set headers
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Accept", "application/json");

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            Log.i("JSON response obj:", result.toString());

            //for debugging on kitkat devices
            //Toast.makeText(getApplicationContext(), "soinnName: "+soinnName,Toast.LENGTH_LONG).show();

            return result.toString();

        } catch (ClientProtocolException e) {
           // Log.e("ClientProtocolException: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();

        } catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();

        } catch (Exception e) {
            Log.e("Exception: ", e.getMessage());
            System.out.println(e.getMessage());
            return e.toString();
        }

    }

    public float calculateErrorSabun(float tappedX, float tappedY, float predictedX, float predictedY){
        return (float)Math.sqrt(((predictedX-tappedX)*(predictedX-tappedX))+(predictedY-tappedY)*(predictedY-tappedY));
    }

    class MyView extends View{
        Paint paint = new Paint();
        Point point = new Point();

        Point predictedPoint = new Point();
        Paint predictedPaint = new Paint();
        ImageView map;



        public MyView(Context context, ImageView map, float soinnX, float soinnY) {
            super(context);
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
            this.map  = map;

            predictedPaint.setColor(Color.RED);
            predictedPaint.setStrokeWidth(10);
            predictedPaint.setStyle(Paint.Style.STROKE);

            predictedPoint.x=soinnX/(36.65f/mapWidth);
            predictedPoint.y=soinnY/(72.00f/mapHeight);
            //this.bringToFront();


        }

        @Override
        protected void onDraw(Canvas canvas) {

            //X for the map if y on the phone bc phones default is portrait view
            floorX = point.y * (72.00/mapHeight);
            floorY = point.x * (36.65/mapWidth);

            //FOR SONY XPERIA Z3, xxhdpi
           // Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.edited_ibeacon_map_480);
           // canvas.drawBitmap(Bitmap.createScaledBitmap(mapBitmap, 1080, 1370, false),0,0,paint);

            if(floorX <= 72.0) {
                canvas.drawCircle(point.x, point.y, 9, paint);
                canvas.drawCircle(predictedPoint.x,predictedPoint.y,9,predictedPaint);
            }

            rl_map.bringToFront();
            //map.bringToFront();
            saveBtn.bringToFront();
            learnBtn.bringToFront();
            learnXYBtn.bringToFront();
            predictBtn.bringToFront();
            doneBtn.bringToFront();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    point.x = event.getX();
                    point.y = event.getY();

                    //X for the map if y on the phone bc phones default is portrait view
                    predictedPoint.x=soinnPredictedY/(36.65f/mapWidth);
                    predictedPoint.y=soinnPredictedX/(72.00f/mapHeight);
            }
            //predictedPoint.x=soinnPredictedY/(36.65f/mapWidth);
            //predictedPoint.y=soinnPredictedX/(72.00f/mapHeight);

        /*
        #define FLOOR_PLAN_HEIGHT 73.05 //73.34 //82.34  //73.34 ; true dimensions : 72 m
        #define FLOOR_PLAN_WIDTH 37.58 //38.726 //+ (550+600+650 mm) ; true dimensions : 36.65 m
        xCGFloat = tappedPoint.y * (FLOOR_PLAN_HEIGHT/mapImageView.frame.size.height);
        yCGFloat = tappedPoint.x * (FLOOR_PLAN_WIDTH/mapImageView.frame.size.width);
        */

            if(floorX < 72.0) {
                String coordinateStr ="predX: "+String.format("%.2f", soinnPredictedX)+ ", predY: "+String.format("%.2f", soinnPredictedY)+
                        ", learnX: "+ String.format("%.2f", floorX) +", learnY: "+ String.format("%.2f", floorY) +
                        ", error: "+ String.format("%.2f", calculateErrorSabun((float)floorX,(float)floorY,soinnPredictedX,soinnPredictedY));
                coordLabel.setText(coordinateStr);
            }
            rl_map.bringToFront();
            //map.bringToFront();
            saveBtn.bringToFront();
            learnBtn.bringToFront();
            learnXYBtn.bringToFront();
            predictBtn.bringToFront();
            doneBtn.bringToFront();
            invalidate();
            return true;

        }

    }

    class Point {
        float x, y;
    }


    /**
     * scale listener for zoom
     * */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            //invalidate();
            return true;
        }
    }

}