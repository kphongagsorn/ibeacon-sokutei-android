package producesu.ntt.com.ibeaconsokutei;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.os.Handler;


//import java.util.logging.Handler;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;



public class MainView extends Activity implements BeaconConsumer {
    private RelativeLayout rl_Main;
    static final String APPLIX_BEACON = "00000000-8e5b-1001-b000-001c4db3db2c";

    static final String DEFAULT_TIMER_TIME="3";
    private BeaconManager beaconManager;
    private int foundBeaconsNum =0;
    private  int foundWifiAPNum=0;

    String timeApiIsCalledStrFromMV = "";


    MyCountDownTimer myCountDownTimer;
    Thread readingThread;
    //Runnable readingRunnable;

    //private ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
    private Collection<Beacon> beaconList =new CopyOnWriteArrayList<Beacon>();

    private StringBuilder recordedBeaconListStr=null;

    private ArrayList<Beacon> recordedBeaconListFinal = null;
    private ArrayList<ScanResult> recordedWifiList = null;
    private StringBuilder recordedWifiListStr=null;


    ArrayList<String> signalListForLog = null;

    //private ArrayList<String> recordedBeaconList = null;
    //StringBuilder recordedBeaconList;

    //private String wifiList ="";
    private ArrayList <ScanResult>wifiList =null;

    boolean timerTapped = false;
    boolean notFirstTime = false;
    boolean recordBeacons = false;

    boolean startReadingFlag = false;

    private CountDownTimer cdTimer;
    private Timer rpsTimer;
    long time;
    long rps;//default value is one
    EditText timerText;
    Button timerButton;

    //for continuously sending data to SOINN
    //Button contReadButton;
    Button backButton;
    //Handler contReadTimerHandler;
    //StringBuilder soinnSecRangeContRead= new StringBuilder("null");

    EditText readingPerSec;



    boolean rpsTimerCancelled = false;
    Timer timer = null;
    TimerTask timerTask = null;


    //stuff from logview to accommodate large data
    // Button doneButton = (Button)findViewById(R.id.doneBtn);
    TextView rangingTextTV;
    ArrayList<Beacon>recordedBeaconListFromMain=null;
    ArrayList<MyBeacons>blist=new ArrayList<MyBeacons>();
    ArrayList<MyWifiAP>wlist=new ArrayList<MyWifiAP>();


    // String wifiListFromMain="";
    ArrayList<ScanResult> wifiListFromMain=null;

    StringBuilder recordedBeaconListFromMainStr =null;
    StringBuilder wifiListFromMainStr =null;

    Timer wifiAPDetectorTimer;
    Timer beaconUpdaterTimer;

    int wifiAPDectorTimerObjNum =1;

    boolean currentlyOnMainScreen=true;


    //soinn
    ArrayList<MyBeacons>soinnBeaconList = new ArrayList<MyBeacons>();
    ArrayList<MyWifiAP>soinnWifiApList = new ArrayList<MyWifiAP>();

    String soinnSpinnerValue;


    float mainViewContReadMapWidth;
    float mainViewContReadMapHeight;
    double mainViewfloorX;
    double mainViewfloorY;
    float mainViewContViewSoinnPredictedX;
    float mainViewContViewSoinnPredictedY;

    RelativeLayout rl_cont_read_map;
    ImageView conReadMainViewImageView;
    TextView mainViewContReadCoordLabel;


    int soinnSecRangeContReadInt = 0;

    Spinner contReadMapViewSoinnSpinner;
    Spinner contReadMapViewSoinnSecIntervalSpinner;

    Timer contReadTimer;
    Timer contReadLoadDataTimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main_view);

        timerText = (EditText)findViewById(R.id.timerTime);
        readingPerSec = (EditText)findViewById(R.id.readingsPerSec);
        timerButton = (Button)findViewById(R.id.timerBtn);
        timerButton.setText("Start");
        timerButton.setBackgroundColor(Color.GREEN);
        timerText.setText(DEFAULT_TIMER_TIME);
        readingPerSec.setText("1");
        //contReadButton=(Button)findViewById(R.id.contReadBtn);
        backButton = (Button)findViewById(R.id.backBtn);


        /**
         * check that wifi and bluetooth have been enabled
         */
        verifyWifi();
        verifyBluetooth();


        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        /**
         *  By default the AndroidBeaconLibrary will only find AltBeacons (http://altbeacon.org/).  If you wish to make it
         *  find a different type of beacon, you must specify the byte layout for that beacon's
         *  advertisement with a line like below.  The example shows how to find a beacon with the
         *  same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb
         *  beaconManager.getBeaconParsers().add(new BeaconParser().
         *         setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
         */
        //this byte layout detects all iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);

        //view debug messages from RN android beacon library
        //beaconManager.debug = true;

        /**
         * refresh wifi AP list after every 1 second
         */

        //if( wifiAPDectorTimerObjNum<=0) {
        if(wifiAPDetectorTimer!=null){
            wifiAPDetectorTimer.cancel();
            wifiAPDetectorTimer =null;
        }



        wifiAPDetectorTimer = new Timer();
        wifiAPDetectorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        StringBuilder tempWifiStr = new StringBuilder();
                        int wifiCounter = 0;
                        if (getWifiAP(getApplicationContext()) != null) {
                            for (ScanResult r : getWifiAP(getApplicationContext())) {
                                if (r.SSID.contentEquals("CLOC_40") || r.SSID.contentEquals("iac-sphone-ac") || r.SSID.contentEquals("iac-sphone-n")) {
                                    wifiCounter++;
                                    tempWifiStr.append(r.SSID + ", " + r.BSSID + ", " + r.level + "\n");
                                    foundWifiAPNum = wifiCounter;
                                    //Log.e("wifilist from onCreate",tempWifiStr.toString());
                                }
                            }
                            wifiList = (ArrayList<ScanResult>) getWifiAP(getApplicationContext());
                            if (wifiCounter > 0) {
                                //logToWifiDisplay(tempWifiStr.toString());
                                // TextView foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);
                                TextView  foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);
                                TextView foundWifiAPCount = (TextView) MainView.this.findViewById(R.id.WifiAPLabel);
                                if (foundWifiAPtextView != null && foundWifiAPCount != null) {
                                    foundWifiAPtextView.setText("");
                                    foundWifiAPtextView.setText(tempWifiStr.toString());
                                    foundWifiAPtextView.setTextSize(14);
                                    foundWifiAPtextView.setMovementMethod(new ScrollingMovementMethod());
                                    foundWifiAPCount.setText("Wifi AP" + " (" + foundWifiAPNum + ")");

                                    //Log.i("Spinner Value:", getSoinnApiResourceUrl());

                                }
                            }
                        }

                    }


                });
            }
        }, 1000, 1000);





        /*
        MyView  contReadMapUpdateMV = new MyView(this, conReadMainViewImageView, mainViewContViewSoinnPredictedX, mainViewContViewSoinnPredictedY);

        rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
        rl_cont_read_map.addView(contReadMapUpdateMV);
        //rl_cont_read_map.bringToFront();
        mainViewContReadCoordLabel = (TextView)findViewById(R.id.mainViewContReadCoordLabel);
        String coordinateStr ="SoinnX: "+String.format("%.2f",  mainViewContViewSoinnPredictedX)+ ", SoinnY: "+String.format("%.2f",  mainViewContViewSoinnPredictedY)+
                ", learnX: "+ String.format("%.2f", mainViewfloorX) +", learnY: "+ String.format("%.2f", mainViewfloorY);
        mainViewContReadCoordLabel.setText(coordinateStr);
        */

    }

    private class Connection extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            connectPredictThreeSec();
            return null;
        }

    }

    private void connectPredictThreeSec() {
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("osver","4.4.2"));
            nameValuePairs.add(new BasicNameValuePair("model", "SO-02F"));
            nameValuePairs.add(new BasicNameValuePair("phone_id", "357931051734908"));
            nameValuePairs.add(new BasicNameValuePair("soinn_name", "xperia"));
            nameValuePairs.add(new BasicNameValuePair("soinn_sec_range", "3"));
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("http://153.149.174.102/predict");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            //Log.i("JSON response obj:", result.toString());

        } catch (ClientProtocolException e) {
            Log.d("HTTPCLIENT", e.getLocalizedMessage());
        } catch (IOException e) {
            Log.d("HTTPCLIENT", e.getLocalizedMessage());
        }
    }


    public void setSoinnApiResourceUrl(String url){
        this.soinnSpinnerValue = url;
    }

    public String getSoinnApiResourceUrl() {
        return this.soinnSpinnerValue;
    }



    /**
     * curl POST for learn xy
     */
    public String postData(String resourceUrl, ArrayList<MyBeacons> soinnBeaconList, ArrayList<MyWifiAP> soinnWifiApList, int soinnSecRange, int x, int y) {

        ArrayList<Signal> signalList = new ArrayList<Signal>();
        signalListForLog = new ArrayList<>();
        String osver = "";
        String model = "";
        String phoneId = "";
        String soinnName = "";
        //String soinnName = Build.PRODUCT;

        Time timeApiIsCalled = new Time();
        timeApiIsCalled.setToNow();
        String timeStamp = timeApiIsCalled.format("%Y-%m-%d,%H:%M:%S");
        timeApiIsCalledStrFromMV = timeStamp;

        Time startOfBeaconRecording = new Time();
        Time startOfWifiRecording = new Time();
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
            soinnName=b.device;

        }

        String arrows ="F-02G";
        String galaxyj = "SC-02F";
        String xperia = "SO-02F";
        if (soinnName.equals(arrows)){

        } else if (soinnName.equals(galaxyj)){

        } else if (soinnName.equals(xperia)){

        } else{
            soinnName ="general";
        }

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
        List<String>  stringArrayofSignal = new ArrayList<>();

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
        //Log.i("keysParamStrArray: ",keysParamStrArray.toString());

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

        //Log.i(" signalsArrayParam: ",  signalsArrayParam.toString());

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
                            "\"x\":"+Integer.toString(x)+"," +
                            "\"y\":"+Integer.toString(y)+"," +
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
            //Log.i("JSON response obj:", result.toString());

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



    //post for soinn learn and predict
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
        String timeStamp = timeApiIsCalled.format("%Y-%m-%d,%H:%M:%S");
        timeApiIsCalledStrFromMV  = timeStamp;

        Time startOfBeaconRecording = new Time();
        Time startOfWifiRecording = new Time();
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
        String arrows ="F-02G";
        String galaxyj = "SC-02F";
        String xperia = "SO-02F";

        if (soinnName.equals(arrows)){

        } else if (soinnName.equals(galaxyj)){

        } else if (soinnName.equals(xperia)){

        } else{
            soinnName ="general";
        }


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

        Collections.sort(signalList, new Comparator<Signal>() {
            @Override
            public int compare(Signal signal1, Signal signal2) {
                return Long.compare(signal1.timeRecordedInMilli, signal2.timeRecordedInMilli);
            }
        });




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


        // Log.i("beacon sub arrays as strings: ", stringArrayofSignal.toString());

        Object[] keysParamArr = signalParameterJSONObj.keySet().toArray();
        String[] keysParamStrArray = Arrays.copyOf(keysParamArr, keysParamArr.length, String[].class);
        //Log.i("keysParamStrArray: ",keysParamStrArray.toString());

        JSONArray  signalsArrayParam = new JSONArray();

        JSONArray argsArr = new JSONArray();

        String signalsArrayParamAsString ="";

        //add signals according to soinn seconds range and convert to JSONArray
        for (int i = 0; i < soinnSecRange; i++) {
            signalsArrayParam.put(stringArrayofSignal.get(i));
            signalsArrayParamAsString = signalsArrayParamAsString +
                    "{" +
                    "\"datetime\":" + "\"" + keysParamStrArray[i] + "\"" + "," +
                    "\"signal_array\":[" + stringArrayofSignal.get(i) + "]" +
                    "},";

            signalListForLog.add(stringArrayofSignal.get(i));
        }

        if( signalsArrayParamAsString!=null || !( signalsArrayParamAsString.isEmpty())) {
            signalsArrayParamAsString =  signalsArrayParamAsString.substring(0, signalsArrayParamAsString.length()-1);
        }

        //Log.i(" signalsArrayParam: ",  signalsArrayParam.toString());

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
            //Log.i("JSON response obj:", result.toString());

            //for debugging on kitkat devices
            //Toast.makeText(getApplicationContext(), "soinnName: "+soinnName,Toast.LENGTH_LONG).show();


            return result.toString();

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

    public float [] parseSoinnResponseForCoords(String jsonResp){
        float xCoordValue=0;
        float yCoordValue=0;
        String[] parts = jsonResp.split(",");
        for(String p: parts){
            String[] subParts = p.split(":");
            if (subParts[0].equals("\"x\"")){
                xCoordValue = Float.parseFloat(subParts[1]);

            }
            if (subParts[0].equals("\"y\"")){
                yCoordValue = Float.parseFloat(subParts[1].substring(0,subParts.length-1));
            }
        }
        float [] coordValues = {xCoordValue,yCoordValue};
        return coordValues;
    }


    public void onContReadButtonClick(View view){
        setContentView(R.layout.activity_cont_read_view);
        rl_cont_read_map = (RelativeLayout)findViewById(R.id.rl_cont_read_map);
        Log.i("rl_cont_read_map from onContRead",rl_cont_read_map.toString());

        if(rl_cont_read_map == null){
            Log.i("rl_cont_read_map", "null");
        }

        /**
         * set soinn api spinner on Continuous Read Map xml
         */

        contReadMapViewSoinnSpinner = (Spinner) findViewById(R.id.contReadMapViewSoinnSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence>  contReadMapViewAdapter = ArrayAdapter.createFromResource(this,
                R.array.soinn_api_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        contReadMapViewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        contReadMapViewSoinnSpinner.setAdapter(contReadMapViewAdapter);

        /**
         * set soinn sec interval spinner on Continuous Read Map xml
         */

        /*
        contReadMapViewSoinnSecIntervalSpinner = (Spinner) findViewById(R.id.secIntervalSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence>  contReadMapViewSoinnSecIntervalAdapter = ArrayAdapter.createFromResource(this,
                R.array.soinn_sec_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        contReadMapViewSoinnSecIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        contReadMapViewSoinnSecIntervalSpinner.setAdapter(contReadMapViewSoinnSecIntervalAdapter);
        */

    }




    public void onIntervalButtonClick(View view){
        conReadMainViewImageView = (ImageView)findViewById(R.id.conReadMainViewImageView);
        conReadMainViewImageView.setImageDrawable(getResources().getDrawable(R.drawable.edited_ibeacon_map_480));

        Log.i("Spinner Value:", (String)contReadMapViewSoinnSpinner.getSelectedItem());
        //Log.i("Interval Spinner Value:", (String)contReadMapViewSoinnSecIntervalSpinner.getSelectedItem());

        final Handler mHandler = new Handler();

        final ArrayList<MyBeacons> contReadBeaconList = new ArrayList<>();
        final ArrayList<MyWifiAP> contReadWifiAPList = new ArrayList<>();

        contReadLoadDataTimer = new Timer();
        contReadLoadDataTimer.schedule(new TimerTask() {
        //new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!(beaconList.isEmpty())) {
                    for (Beacon b : beaconList) {
                        if (b.getId1().toString().equals(APPLIX_BEACON)) {
                            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            String deviceUuid = tManager.getDeviceId();
                            Time now = new Time();
                            now.setToNow();
                            String timeStamp = now.format("%Y-%m-%dT%H:%M:%S");
                            //long timeInMilliSecs = now.toMillis(true);
                            MyBeacons myBeacon = new MyBeacons(timeStamp, Build.VERSION.RELEASE, Build.MODEL, deviceUuid, b.getId1().toString(), b.getId2().toInt(), b.getId3().toInt(), b.getRssi(), b.getDistance());
                            contReadBeaconList.add(myBeacon);
                        }//if filter for applix beacons
                    }
                }

                if (!(wifiList.isEmpty())) {
                    for (ScanResult r : wifiList) {
                        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        String deviceUuid = tManager.getDeviceId();
                        Time now = new Time();
                        now.setToNow();
                        String timeStamp = now.format("%Y-%m-%dT%H:%M:%S");
                        //long timeInMilliSecs = now.toMillis(true);

                        MyWifiAP myWifiAp = new MyWifiAP(timeStamp, Build.VERSION.RELEASE, Build.MODEL, deviceUuid, r.SSID.toString(), r.BSSID.toString(), r.level);
                        contReadWifiAPList.add(myWifiAp);
                    }

                }

                //Toast.makeText(getApplicationContext(), soinnResponseforContRead, Toast.LENGTH_SHORT).show();
                //Log.i("contReadBeaconList",contReadBeaconList.toString());

                String soinnResponse= postData((String)contReadMapViewSoinnSpinner.getSelectedItem(),contReadBeaconList,contReadWifiAPList,1);

                Log.i("SOINNResponseContRead:",soinnResponse);

                float []soinnResponseCoordValues = parseSoinnResponseForCoords(soinnResponse);
                mainViewContViewSoinnPredictedX = soinnResponseCoordValues[0];
                mainViewContViewSoinnPredictedY = soinnResponseCoordValues[1];

               // rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
               // Log.i("rl_cont_read_map", rl_cont_read_map.toString());

                /*
                //for debugging
                if (rl_cont_read_map==null){
                    Log.i("rl_cont_read_map", "null");
                }

                if (conReadMainViewImageView==null){
                    Log.i("conReadMainViewImageView", "null");
                }
                */

               //final MyView  contReadMapUpdateMV = new MyView( MainView.this.getApplicationContext(), conReadMainViewImageView, mainViewContViewSoinnPredictedX, mainViewContViewSoinnPredictedY);

                /*
                MainView.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);


                        MyView  contReadMapUpdateMV = new MyView( MainView.this.getApplicationContext(), conReadMainViewImageView, mainViewContViewSoinnPredictedX, mainViewContViewSoinnPredictedY);
                        Log.i("contReadMapUpdateMV", contReadMapUpdateMV.toString());
                        if (rl_cont_read_map==null){
                            Log.i("rl_cont_read_map", "null");
                        }

                        Log.i("rl_cont_read_map from runOnUiThread", rl_cont_read_map.toString());

                        //rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
                        rl_cont_read_map.addView(contReadMapUpdateMV);
                        //rl_cont_read_map.bringToFront();
                        mainViewContReadCoordLabel = (TextView)findViewById(R.id.mainViewContReadCoordLabel);
                        String coordinateStr ="SoinnX: "+String.format("%.2f",  mainViewContViewSoinnPredictedX)+ ", SoinnY: "+String.format("%.2f",  mainViewContViewSoinnPredictedY)+
                                ", learnX: "+ String.format("%.2f", mainViewfloorX) +", learnY: "+ String.format("%.2f", mainViewfloorY);
                        mainViewContReadCoordLabel.setText(coordinateStr);

                    }
                });
                */

            }

        }, 0, 1000); //update map at spinner interval



        /*
        contReadTimer = new Timer();
        contReadTimer.schedule(new TimerTask() {
                                   @Override
                                   public void run() {

                                       String soinnResponse= postData((String)contReadMapViewSoinnSpinner.getSelectedItem(),contReadBeaconList,contReadWifiAPList,
                                               Integer.parseInt((String)contReadMapViewSoinnSecIntervalSpinner.getSelectedItem()));

                                       Log.i("SOINNResponseContRead:",soinnResponse);

                                       float []soinnResponseCoordValues = parseSoinnResponseForCoords(soinnResponse);
                                       mainViewContViewSoinnPredictedX = soinnResponseCoordValues[0];
                                       mainViewContViewSoinnPredictedY = soinnResponseCoordValues[1];

                                       Runnable runnable = new Runnable() {
                                           @Override
                                           public void run() {
                                               {
                                                   conReadMainViewImageView = (ImageView)findViewById(R.id.conReadMainViewImageView);
                                                   MyView  contReadMapUpdateMV = new MyView(MainView.this.getApplicationContext(), conReadMainViewImageView, mainViewContViewSoinnPredictedX, mainViewContViewSoinnPredictedY);

                                                   rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
                                                   rl_cont_read_map.addView(contReadMapUpdateMV);
                                                   rl_cont_read_map.bringToFront();
                                                   mainViewContReadCoordLabel = (TextView)findViewById(R.id.mainViewContReadCoordLabel);
                                                   String coordinateStr ="SoinnRespX: "+String.format("%.2f",  mainViewContViewSoinnPredictedX)+ ", SoinnRespY: "+String.format("%.2f",  mainViewContViewSoinnPredictedY)+
                                                           ", learnX: "+ String.format("%.2f", mainViewfloorX) +", learnY: "+ String.format("%.2f", mainViewfloorY);
                                                   mainViewContReadCoordLabel.setText(coordinateStr);

                                                   mHandler.postDelayed(this, 1000);

                                               }
                                           }
                                       };
                                       mHandler.post(runnable);


                                   }
                               }, 0, 1000* Integer.parseInt((String)contReadMapViewSoinnSecIntervalSpinner.getSelectedItem()));
*/

                //Log.i("contReadBeaconList", contReadBeaconList.toString());

        //soinnResponseforContRead= postData("predict",contReadBeaconList,contReadWifiAPList,1);



    }

    public void onBackButtonClick(View view){
        if (contReadTimer != null){
            contReadTimer.cancel();
        }
        if (contReadLoadDataTimer != null){
            contReadLoadDataTimer.cancel();
        }
        setContentView(R.layout.activity_main_view);

    }


    public void onTimerButtonClick(View view) {

        if (TextUtils.isEmpty(timerText.getText()) || timerText.getText().toString().contentEquals("0")) {
            timerText.setText(DEFAULT_TIMER_TIME);
        }
        if (TextUtils.isEmpty(readingPerSec.getText()) || readingPerSec.getText().toString().contentEquals("0")) {
            readingPerSec.setText("1");
        }

        //start timer for first time
        if (timerTapped == false && notFirstTime == false) {
            this.timer = new Timer();
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    recordBeacons();
                    recordWifiAPs();
                }
            };

            Double dtime = new Double (Double.valueOf(timerText.getText().toString())) *1000;
            this.time = dtime.longValue();

            Double drps = new Double (Double.valueOf(readingPerSec.getText().toString())) *1000;
            this.rps = drps.longValue();
            this.myCountDownTimer = new MyCountDownTimer(time, 1);
            this.myCountDownTimer.start();
            timer.scheduleAtFixedRate(timerTask,this.rps ,this.rps);


            timerButton.setBackgroundColor(Color.RED);
            timerButton.setText("Stop");
            notFirstTime = true;
            timerTapped = true;
            rpsTimerCancelled = false;

        }


        //stop timer
        else if(timerTapped==true && notFirstTime==true) {
            this.myCountDownTimer.cancel();
            this.timer.cancel();
            rpsTimerCancelled = true;
            timerTapped = false;
            timerButton.setBackgroundColor(Color.GREEN);
            timerButton.setText("Start");
        }

        //restart timer
        else if (timerTapped == false && notFirstTime ==true ){
            this.timer = new Timer();
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    recordBeacons();
                    recordWifiAPs();
                }
            };

            Double dtime = new Double (Double.valueOf(timerText.getText().toString())) *1000;
            this.time = dtime.longValue();

            Double drps = new Double (Double.valueOf(readingPerSec.getText().toString())) *1000;
            this.rps = drps.longValue();

            this.myCountDownTimer = new MyCountDownTimer(this.time, 1);
            this.myCountDownTimer.start();
            this.timer.scheduleAtFixedRate(timerTask,this.rps,this.rps);

            timerButton.setBackgroundColor(Color.RED);
            timerButton.setText("Stop");
            timerTapped = true;
            rpsTimerCancelled = false;

        }

    }

    public void onRecordLogButtonClick(View view) {
        if (recordedBeaconListStr == null || recordedWifiListStr == null) {
            //rangingTextTV.setText("No recorded data");
            Toast.makeText(getApplicationContext(), "No recorded data", Toast.LENGTH_SHORT).show();
        }
        else {

            // wifiAPDetectorTimer.cancel();
            wifiAPDetectorTimer.cancel();
            beaconManager.unbind(this);

            setContentView(R.layout.activity_log_view);
            wifiAPDetectorTimer.cancel();
            currentlyOnMainScreen = false;
            //doneButton = (Button)findViewById(R.id.doneBtn);
            rangingTextTV = (TextView) findViewById(R.id.rangingText);
            rangingTextTV.setTextIsSelectable(true);
            rangingTextTV.setMovementMethod(new ScrollingMovementMethod());



            // wlist = new ArrayList<MyWifiAP>();
            // blist = new ArrayList<MyBeacons>();
            //Time now = new Time();
            //now.setToNow();
            //String timeStamp = now.format("%Y-%m-%d,%H:%M:%S");


            String[] tempBeacons = recordedBeaconListStr.toString().split("\n");

            if (tempBeacons[0] != "") {
                if (blist != null) {
                    blist.clear();
                }/*
                for (int i = 0; i < tempBeacons.length; i++) {
                    String[] aBeacon = tempBeacons[i].split(",");
                    String timeStmp = aBeacon[0];
                    String uuid = aBeacon[1];
                    int major = Integer.parseInt(aBeacon[2]);
                    int minor = Integer.parseInt(aBeacon[3]);
                    int rssi = Integer.parseInt(aBeacon[4]);
                    double distance = Double.parseDouble(aBeacon[5]);

                    TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String deviceUuid = tManager.getDeviceId();

                    MyBeacons b = new MyBeacons(timeStmp, Build.VERSION.RELEASE, Build.MODEL, deviceUuid, uuid, major, minor, rssi, distance);
                    if (blist == null) {
                        blist = new ArrayList<MyBeacons>();
                    }

                    blist.add(b);
                    //Log.e("tempBeacons", timeStmp+uuid+ rssi+ major+minor);
                }*/
            }

            String[] tempWAP = recordedWifiListStr.toString().split("\n");
            Log.e("recordedWifiListStr", recordedWifiListStr.toString() + "\n");

            if (tempWAP[0] != "") {
                if (wlist != null) {
                    wlist.clear();
                }/*
                for (int i = 0; i < tempWAP.length; i++) {
                    String[] aWAP = tempWAP[i].split(",");
                    String timeStmp = aWAP[0];
                    String ssid = aWAP[1];
                    String bssid = aWAP[2];
                    int rssi = Integer.parseInt(aWAP[3]);

                    TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String deviceUuid = tManager.getDeviceId();

                    MyWifiAP w = new MyWifiAP(timeStmp, Build.VERSION.RELEASE, Build.MODEL, deviceUuid, ssid, bssid, rssi);
                    if (wlist == null) {
                        wlist = new ArrayList<MyWifiAP>();
                    }

                    wlist.add(w);
                    Log.e("tempWap", timeStmp + "," + deviceUuid + "," + ssid + "," + bssid + "," + rssi);

                }
                */
            }

            //recordedBeaconListFromMain = (ArrayList<Beacon>) getIntent().getExtras().getSerializable("recordedBeaconList");
            //wifiListFromMain = (ArrayList<ScanResult>) getIntent().getExtras().getSerializable("wifiList");

            /*
            if (blist != null) {
                Collections.sort(blist, new Comparator<MyBeacons>() {
                    @Override
                    public int compare(MyBeacons myBeacons, MyBeacons myBeacons2) {
                        return Integer.compare(myBeacons2.rssi, myBeacons.rssi);
                    }
                });

                Collections.sort(blist, new Comparator<MyBeacons>() {
                    @Override
                    public int compare(MyBeacons myBeacons, MyBeacons myBeacons2) {
                        return Integer.compare(myBeacons2.minor, myBeacons.minor);
                    }
                });

                Collections.sort(blist, new Comparator<MyBeacons>() {
                    @Override
                    public int compare(MyBeacons myBeacons, MyBeacons myBeacons2) {
                        return Integer.compare(myBeacons2.major, myBeacons.major);
                    }
                });

                Collections.sort(blist, new Comparator<MyBeacons>() {
                    @Override
                    public int compare(MyBeacons myBeacons, MyBeacons myBeacons2) {
                        return myBeacons2.uuid.compareTo(myBeacons.uuid);
                    }
                });
            /*
            for (MyBeacons b : blist) {
                rangingTextTV.append(b.major + "/" +b.minor + "/" +b.rssi+"\n");

            }
            */

            //rangingTextTV.append(blist.indexOf(1) + "\n");
            //rangingTextTV.append(recordedBeaconListStr.toString());
            //rangingTextTV.append(recordedBeaconListStr.toString());
                /*
                for(MyBeacons b : blist) {
                    rangingTextTV.append(blist.toString() + "\n");
                }


            }
        */
            rangingTextTV.append(recordedBeaconListStr.toString());

            /*
            if (wlist != null) {
                Collections.sort(wlist, new Comparator<MyWifiAP>() {
                    @Override
                    public int compare(MyWifiAP myWifiAP, MyWifiAP myWifiAP2) {
                        return Integer.compare(myWifiAP2.rssi, myWifiAP.rssi);
                    }
                });

                Collections.sort(wlist, new Comparator<MyWifiAP>() {
                    @Override
                    public int compare(MyWifiAP myWifiAP, MyWifiAP myWifiAP2) {
                        return myWifiAP2.bssid.compareTo(myWifiAP.bssid);
                    }
                });

                Collections.sort(wlist, new Comparator<MyWifiAP>() {
                    @Override
                    public int compare(MyWifiAP myWifiAP, MyWifiAP myWifiAP2) {
                        return myWifiAP2.ssid.compareTo(myWifiAP.ssid);
                    }
                });

            /*
            for (MyWifiAP w: wlist){
                rangingTextTV.append(w.ssid+","+w.rssi+"\n");
            }
            */
                /*
                for(MyWifiAP w: wlist) {
                    rangingTextTV.append(wlist.toString() + "\n");
                }
            }

                //rangingTextTV.append(recordedWifiListStr.toString());
                //rangingTextTV.append(wlist.toString() + "\n");
                //recordedWifiListStr.setLength(0);
                //blist.clear();
                //wlist.clear();


            }
            */
            rangingTextTV.append(recordedWifiListStr.toString());

/*

        boolean emptyList = true;
      //  Intent intent = new Intent(this, LogView.class);


            if (recordedBeaconListStr != null) {
              //  intent.putExtra("recordedBeaconList", recordedBeaconListStr.toString());
                emptyList = false;

            }
            //intent.putExtra("recordedBeaconList", (Serializable) recordedBeaconList);]
            if (recordedWifiListStr != null) {
             //   intent.putExtra("wifiList", recordedWifiListStr.toString());
                emptyList = false;
            }
            if (emptyList) {
                Toast.makeText(getBaseContext(), "No recorded data", Toast.LENGTH_SHORT).show();

            }


            //wifiList = (ArrayList<ScanResult>) getWifiAP(getApplicationContext());
            //intent.putExtra("wifiList", (Serializable) recordedWifiList);
            else {
                //this.startActivity(intent);
                recordedBeaconListStr = null;
                recordedWifiListStr = null;
            }
            */
        /*
        if(recordedBeaconList!= null ) {
            recordedBeaconList.clear();
        }
        if(recordedWifiList!=null) {
            recordedWifiList.clear();
        }
        */


        }
    }
    //write to external storage; for galaxy devices
    public  void writeFileToExternal(String fileName, String body)
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
     * Save beacon data to text file
     * -does not currently save beacon proximity distance
     * @param view
     */
    public void onSaveButtonClick(View view){
        String filename= "logFile.txt";
        try {
            //Time now = new Time();
            //now.setToNow();
            //String timeStamp = now.format("%Y-%m-%d,%H:%M:%S");
            FileOutputStream fos = openFileOutput(filename, MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            String tempOutputString="";



            if (recordedBeaconListStr.length()>0){
                tempOutputString+=recordedBeaconListStr.toString();
            }

            /*
            if (blist !=null){


                //tempOutputString+=blist.toString()+"\n";
                tempOutputString+=recordedBeaconListStr.toString();


               blist.clear();
            }*/

            /*
            if(recordedBeaconListFromMain!=null) {
                for (Beacon b : recordedBeaconListFromMain) {
                    //osw.write(timeStamp + "," + Build.SERIAL + "," + Build.DISPLAY + "," + Build.BOOTLOADER + "," + Build.BRAND + "," + Build.BOARD + "," + Build.FINGERPRINT + "," + Build.PRODUCT + "," + Build.MODEL + "," + Build.DEVICE + "," + b.getId1().toString() + "," + b.getId2() + "," + b.getId3() + "," + b.getRssi() + "\n");
                     tempOutputString+=timeStamp + "," + Build.SERIAL + "," + Build.DISPLAY + "," + Build.FINGERPRINT  + "," + Build.MODEL + "," + Build.DEVICE + "," + b.getId1().toString() + "," + b.getId2() + "," + b.getId3() + "," + b.getRssi() + "\n";
                }
                //osw.write(wifiListFromMain);
            }
            */

            if(recordedWifiListStr.length()>0){
                tempOutputString+=recordedWifiListStr.toString();
            }
            /*
            if(wlist !=null){

                //for(MyWifiAP w: wlist){
                    //tempOutputString+=w.toString()+"\n";
                //}
                //tempOutputString+=wlist.toString()+"\n";
                tempOutputString+=recordedWifiListStr.toString();

                //tempOutputString+=wlist.toString();
                wlist.clear();
                Log.e("wlist",wlist.toString());
            }
            */
            /*
            if(wifiListFromMain!=null) {
                for(ScanResult r: wifiListFromMain){
                    //osw.write(timeStamp + ","+r.SSID+","+r.level+"\n");
                    tempOutputString+=timeStamp + ","+r.SSID+","+r.level+"\n";
                }
            }
            */
            tempOutputString+="\n"; //extra line break to separate readings
            //tempOutputString+="\n"; //extra line break to separate readings

            // Log.i("logFile output:",tempOutputString);


            osw.write(tempOutputString);
            osw.flush();
            osw.close();
            writeFileToExternal(filename,tempOutputString);


            recordedWifiListStr.setLength(0);
            Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
            // blist.clear();
            // wlist.clear();
            // setContentView(R.layout.activity_main_view);
        }
        catch (IOException e) {
            //e.printStackTrace();
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

    public float calculateErrorSabun(float tappedX, float tappedY, float predictedX, float predictedY){
        return (float)Math.sqrt(((predictedX-tappedX)*(predictedX-tappedX))+(predictedY-tappedY)*(predictedY-tappedY));
    }


    public void onSoinnLearnButtonClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Learn?");
        builder.setItems(new CharSequence[]
                        {"1 second", "3 seconds", "5 seconds", "10 seconds"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String soinnResponse;
                        switch (which) {
                            case 0:
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 1);
                                Intent intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // getApplicationContext().startActivity(intent);intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","1");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","learn");
                                intent.putExtra("api","yes");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);

                                startActivityForResult(intent,1);


                                //  Toast.makeText(getApplicationContext(), soinnResponse,
                                //        Toast.LENGTH_SHORT).show();


                                break;


                            case 1:
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 3);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","3");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","learn");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putStringArrayListExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(), soinnResponse,
                                //       Toast.LENGTH_SHORT).show();

                                break;

                            case 2:
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 5);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","5");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","learn");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(), soinnResponse,
                                //       Toast.LENGTH_SHORT).show();

                                break;
                            case 3:
                                soinnResponse = postData("learn", soinnBeaconList, soinnWifiApList, 10);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","10");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","learn");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(), soinnResponse,
                                //       Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();

    }

    public void onSoinnPredictButtonClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Predict Location?");
        builder.setItems(new CharSequence[]
                        {"1 second", "3 seconds", "5 seconds", "10 seconds"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String soinnResponse;
                        switch (which) {
                            case 0:
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,1);
                                Intent intent = new Intent(MainView.this, PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                //this.startActivityForResult(intent, soinnResponse);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","1");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","predict");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                //Toast.makeText(getApplicationContext(),soinnResponse,
                                //      Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,3);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","3");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","predict");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);

                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(),soinnResponse,
                                //         Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,5);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","5");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","predict");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(),soinnResponse,
                                //         Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                soinnResponse= postData("predict",soinnBeaconList,soinnWifiApList,10);
                                intent = new Intent(getApplicationContext(), PredMapView.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //getApplicationContext().startActivity(intent);
                                intent.putExtra("soinnBeaconList", soinnBeaconList);
                                intent.putExtra("soinnWifiApList", soinnWifiApList);
                                intent.putExtra("soinnResponse",soinnResponse);
                                intent.putExtra("soinnSecRange","10");
                                intent.putExtra("comingFromMain","yes");
                                intent.putExtra("apiCallTypeForDataFile","predict");
                                intent.putExtra("timeApiIsCalledStrFromMV",timeApiIsCalledStrFromMV);
                                intent.putExtra("signalListForLog", signalListForLog);
                                startActivityForResult(intent,1);

                                // Toast.makeText(getApplicationContext(),soinnResponse,
                                //         Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
        builder.create().show();


        // postData("predict",soinnBeaconList,soinnWifiApList,3);
        //connectPredict();
        //new Connection().execute();


    }



    /**
     * return to MainView
     * @param view
     */
    public void onDoneButtonClick(View view){
        //recordedBeaconListFromMain = null;
        //wifiListFromMain = null;
        currentlyOnMainScreen = true;
        rangingTextTV.setText("");
        beaconList.clear();
        recordedBeaconListStr=null;
        recordedWifiListStr=null;
        blist=null;
        wlist=null;

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        // beaconManager.debug = true;

        wifiAPDetectorTimer = new Timer();
        wifiAPDetectorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        StringBuilder tempWifiStr = new StringBuilder();
                        int wifiCounter = 0;
                        if (getWifiAP(getApplicationContext()) != null) {
                            //StringBuilder tempWifiStr = new StringBuilder();
                            //int wifiCounter=1;
                            for (ScanResult r : getWifiAP(getApplicationContext())) {
                                if (r.SSID.contentEquals("CLOC_40") || r.SSID.contentEquals("iac-sphone-ac") || r.SSID.contentEquals("iac-sphone-n")) {
                                    wifiCounter++;

                                    tempWifiStr.append(r.SSID + ", " + r.BSSID + ", " + r.level + "\n");
                                    foundWifiAPNum = wifiCounter;
                                    //Log.e("wifilist from onCreate",tempWifiStr.toString());
                                }
                            }
                            wifiList = (ArrayList<ScanResult>) getWifiAP(getApplicationContext());
                            //  for (ScanResult r : wifiList) {
                            // Log.e("wifilist from onCreate", r.SSID + "," + r.BSSID + ", " + r.level + "\n");
                            // }

                            if (wifiCounter > 0) {
                                //logToWifiDisplay(tempWifiStr.toString());

                                TextView foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);
                                foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);

                                TextView foundWifiAPCount = (TextView) MainView.this.findViewById(R.id.WifiAPLabel);
                                if(foundWifiAPtextView!=null && foundWifiAPCount != null) {
                                    foundWifiAPtextView.setText("");
                                    foundWifiAPtextView.setText(tempWifiStr.toString());
                                    foundWifiAPtextView.setTextSize(14);
                                    foundWifiAPtextView.setMovementMethod(new ScrollingMovementMethod());
                                    foundWifiAPCount.setText("Wifi AP" + " (" + foundWifiAPNum + ")");
                                }


                            }
                        }

                    }


                });
            }
        }, 1000, 1000);
        /*
        wifiAPDetectorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                StringBuilder tempWifiStr = new StringBuilder();
                int wifiCounter = 0;
                if (getWifiAP(getApplicationContext()) != null) {
                    //StringBuilder tempWifiStr = new StringBuilder();
                    //int wifiCounter=1;
                    for (ScanResult r : getWifiAP(getApplicationContext())) {
                        if (r.SSID.contentEquals("CLOC_40") || r.SSID.contentEquals("iac-sphone-ac") || r.SSID.contentEquals("iac-sphone-n")) {
                            wifiCounter++;
                            tempWifiStr.append(r.SSID + ", " + r.BSSID + ", " + r.level + "\n");
                            foundWifiAPNum = wifiCounter;
                        }
                    }
                    wifiList = (ArrayList<ScanResult>) getWifiAP(getApplicationContext());
                    if(wifiCounter>0) {
                        logToWifiDisplay(tempWifiStr.toString());
                    }
                }
            }
        }, 1000, 1000);*/


        setContentView(R.layout.activity_main_view);

        timerText = (EditText)findViewById(R.id.timerTime);
        readingPerSec = (EditText)findViewById(R.id.readingsPerSec);
        timerButton = (Button)findViewById(R.id.timerBtn);
        timerButton.setText("Start");
        timerButton.setBackgroundColor(Color.GREEN);
        timerText.setText(DEFAULT_TIMER_TIME);
        readingPerSec.setText("1");

        //Plan B; reloads activity
        // finish();
        // startActivity(getIntent());
    }

    public void onShowOnlyBeaconsClick(View view){
        rangingTextTV.setText("");

        if (blist !=null){
            /*
            for(MyBeacons b: blist){
                rangingTextTV.append(b.toString());
            }
            */
            rangingTextTV.append(blist.toString()+"\n");
        }
        else{
            rangingTextTV.setText("No beacons recorded");
        }
        /*
        if (recordedBeaconListFromMain != null) {
            for(Beacon b: recordedBeaconListFromMain){
                rangingTextTV.append(b.getId2()+"/"+b.getId3()+", rssi "+b.getRssi()+", dist. "+String.format("%.2f",b.getDistance())+"m\n");
            }

        }
        else {
            rangingTextTV.setText("No beacons recorded");
        }
        */

    }

    /**
     * show wifi ap's only
     * @param view
     */
    public void onShowOnlyWifiAPClick(View view){
        rangingTextTV.setText("");
        //rangingTextTV.append(wifiListFromMain);

        if(wlist !=null){
            /*
            for(MyWifiAP w: wlist){
                rangingTextTV.append(w.toString());
            }
            */
            rangingTextTV.append(wlist.toString()+"\n");
        }
        else{
            rangingTextTV.setText("No wifi AP recorded");
        }
        /*
        if(wifiListFromMain != null) {
            for(ScanResult r: wifiListFromMain){
                rangingTextTV.append(r.SSID+","+r.level+"\n");

            }
        }
        else{
            rangingTextTV.setText("No wifi AP recorded");
        }
        */

    }

    /**
     * show all beacons and wifi access points
     * @param view
     */
    public void onShowAllClick(View view){
        rangingTextTV.setText("");
        if (blist !=null) {/*
            for (MyBeacons b : blist) {
                rangingTextTV.append(b.toString());
            }
            */
            rangingTextTV.append(blist.toString() + "\n");
        }

        if(wlist !=null){
            /*
            for(MyWifiAP w: wlist){
                rangingTextTV.append(w.toString());
            }
            */
            rangingTextTV.append(wlist.toString() + "\n");
        }

        /*
        if (recordedBeaconListFromMain != null) {
            for(Beacon b: recordedBeaconListFromMain){
                rangingTextTV.append(b.getId2()+"/"+b.getId3()+","+b.getRssi()+","+String.format("%.2f",b.getDistance())+"m\n");
            }
            //rangingTextTV.append(wifiListFromMain);
            //rangingTextTV.append(wifiListFromMain.toString());
        }
        if(wifiListFromMain!=null) {
            for(ScanResult r: wifiListFromMain){
                rangingTextTV.append(r.SSID + "," + r.level + "\n");

            }

        }*/


    }


    public void onSetCoordinatesButtonClick(View view){
        Intent intent = new Intent(this, PredMapView.class);
        this.startActivity(intent);
        //this.startActivityForResult(intent, MAP_COORDINATE_REQUEST);

    }

    /**
     * record ibeacons
     */
    private void recordBeacons() {
        /*
        if(recordedBeaconList == null) {
            recordedBeaconList = new ArrayList<Beacon>();
        }
        */
        if (recordedBeaconListStr == null){
            recordedBeaconListStr = new StringBuilder();
        }

        if (beaconList==null) {
            //don't attempt to record beacons
        }
        else {
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceUuid = tManager.getDeviceId();

            //create MyBeacon objects for soinn api call, max 10
            int soinnBeaconCounter = 0;
            for(Beacon b: beaconList){
                if(b.getId1().toString().equals(APPLIX_BEACON)) {
                    //recordedBeaconList.add(b);
                    Time now = new Time();
                    now.setToNow();
                    String timeStamp = now.format("%Y-%m-%dT%H:%M:%S");
                    long timeInMilliSecs = now.toMillis(true);
                    //recordedBeaconListStr.append(timeStamp+","+b.getId1().toString()+","+b.getId2()+","+b.getId3()+","+b.getRssi()+","+b.getDistance()+"\n");
                    recordedBeaconListStr.append(timeStamp+","+timeInMilliSecs+ ","+Build.VERSION.RELEASE+","+ Build.MODEL+ ","+ deviceUuid+","+
                            b.getRssi()+","+b.getId1().toString()+","+b.getId2()+","+b.getId3()+"\n");

                    if(soinnBeaconCounter<10) {
                        MyBeacons myB = new MyBeacons(timeStamp, timeInMilliSecs, Build.VERSION.RELEASE, Build.MODEL,
                                deviceUuid, b.getId1().toString(), b.getId2().toInt(), b.getId3().toInt(), b.getRssi(), b.getDistance());
                        soinnBeaconList.add(myB);
                        soinnBeaconCounter++;
                    }

                }//if filter for applix beacons


            }

        }

    }

    /**
     * record wifi ap's
     */
    private void recordWifiAPs() {
        /*
        if(recordedWifiList == null) {
            recordedWifiList = new ArrayList<ScanResult>();
        }
        */

        if (recordedWifiListStr == null){
            recordedWifiListStr = new StringBuilder();
        }

        if (wifiList==null) {
            //don't attempt to record beacons
        }
        else {
            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceUuid = tManager.getDeviceId();

            //create MyWifiAp objects for soinn api call
            int soinnWifiApCounter =0;
            for(ScanResult r: (ArrayList<ScanResult>)wifiList){
                if(r.SSID.contentEquals("CLOC_40")||r.SSID.contentEquals("iac-sphone-ac")||r.SSID.contentEquals("iac-sphone-n")) {
                    Time now = new Time();
                    now.setToNow();
                    String timeStamp = now.format("%Y-%m-%dT%H:%M:%S");
                    long nowInMillis = now.toMillis(true);
                    //recordedWifiListStr.append(timeStamp + "," + ","+nowInMillis + ","+r.SSID + "," + r.BSSID + "," + r.level + "\n");
                    recordedWifiListStr.append(timeStamp + ","+nowInMillis + ","+ Build.VERSION.RELEASE+","+ Build.MODEL+ ","+
                            deviceUuid+","+r.SSID+","+r.BSSID + "," + r.level + "\n");

                    if(soinnWifiApCounter<10) {
                        MyWifiAP myW = new MyWifiAP(timeStamp, nowInMillis, Build.VERSION.RELEASE, Build.MODEL,
                                deviceUuid, r.SSID, r.BSSID, r.level);
                        soinnWifiApList.add(myW);
                        soinnWifiApCounter++;
                    }

                }//if filter for wifi aps


            }

            wifiList.clear();/*
            for(ScanResult r: (ArrayList<ScanResult>)wifiList) {
                Log.e("wifilist in record wifi ap after clear", r.timestamp +","+ Build.VERSION.RELEASE+","+ Build.MODEL+ ","+
                        deviceUuid+","+r.level + "," + r.BSSID + "\n");
            }
            */

        }

    }

   /*
    @Override
    protected void onStart(){
        super.onStart();

    }
    */

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        //      beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        //    beaconManager.bind(this);

        //view debug messages from RN android beacon library
        //beaconManager.debug = true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconList.clear();
        beaconManager.unbind(this);
    }

    /**
     * Check if bluetooth le is enabled or supported
     */
    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings to detect iBeacons");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //Intent intentOpenBluetoothSettings = new Intent();
                        //intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        //startActivity(intentOpenBluetoothSettings);
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();


        }

    }

    /**
     * Check if wifi is on
     */
    private void verifyWifi() {
        WifiManager mng = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        try {
            if (!mng.isWifiEnabled()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Wifi not enabled");
                builder.setMessage("Please enable wifi in settings to detect wifi access points");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //Intent intentOpenWifiSettings = new Intent();
                        //intentOpenWifiSettings.setAction(Settings.ACTION_WIFI_SETTINGS);
                        //startActivity(intentOpenWifiSettings);
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Wifi not available");
            builder.setMessage("Sorry, this device does not wifi");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();


        }

    }

    /**
     * print out beacons
     */


    private void logToBeaconDisplay(final String line) {
        runOnUiThread(new Runnable() {
            TextView beaconsFoundTextView = (TextView) MainView.this.findViewById(R.id.beaconsFound);
            TextView foundBeaconsCount = (TextView) MainView.this.findViewById(R.id.foundBeaconsLV);

            public void run() {
                if (beaconsFoundTextView != null && foundBeaconsCount != null) {
                    String tempLine = line;
                    if (tempLine.length() <= 0 || tempLine == null) {
                        tempLine = "";
                        beaconsFoundTextView.setText(tempLine);
                    } else {
                        beaconsFoundTextView.setText(tempLine);
                        beaconsFoundTextView.setTextSize(14);
                        beaconsFoundTextView.setMovementMethod(new ScrollingMovementMethod());

                        //TextView foundBeaconsCount = (TextView) MainView.this.findViewById(R.id.foundBeaconsLV);

                        foundBeaconsCount.setText("Found Beacons" + " (" + foundBeaconsNum + ")");
                    }
                }
            }
        });

    }



    /**
     * print out wifi ap's
     */


    private void logToWifiDisplay(final String line) {
        runOnUiThread(new Runnable() {
            TextView foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);

            public void run() {
                foundWifiAPtextView = (TextView) MainView.this.findViewById(R.id.foundWifiAP);
                if (foundWifiAPtextView != null) {
                    String tempLine = line;
                    if (tempLine.length() <= 0 || tempLine == null) {
                        tempLine = "";
                        foundWifiAPtextView.setText(tempLine);
                    } else {
                        foundWifiAPtextView.setText("");
                        foundWifiAPtextView.setText(tempLine);
                        foundWifiAPtextView.setTextSize(14);
                        foundWifiAPtextView.setMovementMethod(new ScrollingMovementMethod());
                        TextView foundWifiAPCount = (TextView) MainView.this.findViewById(R.id.WifiAPLabel);
                        foundWifiAPCount.setText("Wifi AP" + " (" + foundWifiAPNum + ")");
                    }
                }
            }
        });
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // beaconList= beacons;
                StringBuilder tempBeaconStr = new StringBuilder();
                int beaconCounter = 0;
                foundBeaconsNum = 0;


                for (Beacon b : beacons) {
                    //only show our beacons
                    if (b.getId1().toString().equals(APPLIX_BEACON)) {
                        beaconCounter++;
                        tempBeaconStr.append(b.getId2() + "/" + b.getId3() + ", " + b.getRssi() + ", " + String.format("%.2f", b.getDistance()) + "m\n");
                        //beaconList.add(b);
                        //beaconList= beacons;
                        foundBeaconsNum = beaconCounter;
                    }
                }

                if (beaconCounter > 0) {
                    // Toast.makeText(getApplicationContext(), "No Beacons Found", Toast.LENGTH_SHORT).show();
                    logToBeaconDisplay(tempBeaconStr.toString());
                }


                beaconList = beacons;
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("test.producesu", null, null, null));
        } catch (RemoteException e) {   }
    }

    /**
     * Get Wifi AP's information
     * @param context
     * @return
     */
    //public String getCurrentSsid(Context context) {
    public List<ScanResult> getWifiAP(Context context) {
        //String ssid = null;
        //String etWifiList ="";
        //String textStatus="";
        List<ScanResult>  results = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
       /* IntentFilter i = new IntentFilter();
        i.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver(){
            public void onReceive(Context c, Intent i){
                // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
                WifiManager w = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
                w.getScanResults();
            }
        }, i );
        */
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            /*
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
            */
            // Get WiFi status
            //WifiInfo info = wifiManager.getConnectionInfo();
            //String textStatus = "";
            //textStatus += "\n\nWiFi Status: " + info.toString();
            // String BSSID = info.getBSSID();
            // String MAC = info.getMacAddress();

            wifiManager.startScan();
            results = wifiManager.getScanResults();


            //ScanResult bestSignal = null;
            //int count = 1;
            //String etWifiList = "";
            // for (ScanResult result : results) {
            //int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(),result.level);
            //etWifiList += count++ + ". " + result.SSID + " : " + result.level + "\n" +  result.BSSID + "\n" + result.capabilities + "\n" + "\n=======================\n";
            // etWifiList += result.SSID +","+level+ "\n";
            // }
            //Log.v(TAG, "from SO: \n"+etWifiList);

            /*
            // List stored networks
            List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : configs) {
                textStatus+= "\n\n" + config.toString();
            }
            //Log.v(TAG,"from marakana: \n"+textStatus);
            */
        }
        //return ssid;
        //return etWifiList;
        return results;
    }


    /**
     * timer class that counts down
     */
    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval){
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {
            timerText.setText("" +  millisUntilFinished/1000.0);//display to seconds and milliseconds

        }

        @Override
        public void onFinish() {
            timer.cancel();
            timerText.setText(DEFAULT_TIMER_TIME); //default 3 seconds
            timerButton.setBackgroundColor(Color.GREEN);
            timerButton.setText("Start");
            notFirstTime = false;
            timerTapped = false;
            rpsTimerCancelled = true;
        }
    }

    class MyView extends View{
        Paint paint = new Paint();
        Point point = new Point();

        Point predictedPoint = new Point();
        Paint predictedPaint = new Paint();
        ImageView map;



        public MyView(Context context, ImageView map, float soinnX, float soinnY) {
            super(context);
            //mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
            this.map  = map;

            predictedPaint.setColor(Color.RED);
            predictedPaint.setStrokeWidth(10);
            predictedPaint.setStyle(Paint.Style.STROKE);

            predictedPoint.x=soinnX/(36.65f/mainViewContReadMapWidth);
            predictedPoint.y=soinnY/(72.00f/mainViewContReadMapHeight);
            //this.bringToFront();


        }

        @Override
        protected void onDraw(Canvas canvas) {

            //X for the map if y on the phone bc phones default is portrait view
            mainViewfloorX = point.y * (72.00/mainViewContReadMapHeight);
            mainViewfloorY = point.x * (36.65/mainViewContReadMapWidth);

            //FOR SONY XPERIA Z3, xxhdpi
            //Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.edited_ibeacon_map_480);
            //canvas.drawBitmap(Bitmap.createScaledBitmap(mapBitmap, 1080, 1370, false),0,0,paint);

            if(mainViewfloorX <= 72.0) {
                canvas.drawCircle(point.x, point.y, 9, paint);
                canvas.drawCircle(predictedPoint.x,predictedPoint.y,9,predictedPaint);
            }

            // rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
            //rl_cont_read_map.bringToFront();
            //map.bringToFront();
            //saveBtn.bringToFront();
            //learnBtn.bringToFront();
            //learnXYBtn.bringToFront();
            //predictBtn.bringToFront();
            //doneBtn.bringToFront();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    point.x = event.getX();
                    point.y = event.getY();

                    //X for the map if y on the phone bc phones default is portrait view
                    predictedPoint.x=mainViewContViewSoinnPredictedY/(36.65f/mainViewContReadMapWidth);
                    predictedPoint.y=mainViewContViewSoinnPredictedX/(72.00f/mainViewContReadMapHeight);
            }
            //predictedPoint.x=soinnPredictedY/(36.65f/mapWidth);
            //predictedPoint.y=soinnPredictedX/(72.00f/mapHeight);

        /*
        #define FLOOR_PLAN_HEIGHT 73.05 //73.34 //82.34  //73.34 ; true dimensions : 72 m
        #define FLOOR_PLAN_WIDTH 37.58 //38.726 //+ (550+600+650 mm) ; true dimensions : 36.65 m
        xCGFloat = tappedPoint.y * (FLOOR_PLAN_HEIGHT/mapImageView.frame.size.height);
        yCGFloat = tappedPoint.x * (FLOOR_PLAN_WIDTH/mapImageView.frame.size.width);
        */

            mainViewContReadCoordLabel = (TextView)findViewById(R.id.mainViewContReadCoordLabel);

            if(mainViewfloorX < 72.0) {
                String coordinateStr ="predX: "+String.format("%.2f", mainViewContViewSoinnPredictedX)+ ", predY: "+String.format("%.2f", mainViewContViewSoinnPredictedY)+
                        ", learnX: "+ String.format("%.2f", mainViewfloorX) +", learnY: "+ String.format("%.2f", mainViewfloorY) +
                        ", error: "+ String.format("%.2f", calculateErrorSabun((float)mainViewfloorX,(float)mainViewfloorY,mainViewContViewSoinnPredictedX,mainViewContViewSoinnPredictedY));
                mainViewContReadCoordLabel.setText(coordinateStr);
            }

            //rl_cont_read_map = (RelativeLayout) findViewById(R.id.rl_cont_read_map);
            //rl_cont_read_map.bringToFront();
            //map.bringToFront();
            //saveBtn.bringToFront();
            //learnBtn.bringToFront();
            //learnXYBtn.bringToFront();
            //predictBtn.bringToFront();
            //doneBtn.bringToFront();
            invalidate();
            return true;

        }

    }

    class Point {
        float x, y;
    }



}


