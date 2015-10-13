package producesu.ntt.com.ibeaconsokutei;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

/**
 * Created by kphongagsorn on 4/15/15.
 */
class ContinuousReadView extends Activity implements BeaconConsumer{
    Timer wifiDT;
    private ArrayList <ScanResult>wifiCRList =null;
    private Collection<Beacon>beaconCRList =null;
    static final String APPLIX_BEACON = "00000000-8e5b-1001-b000-001c4db3db2c";
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_cont_read_view);

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
        if(wifiDT!=null){
           wifiDT.cancel();
           wifiDT =null;
        }

        wifiDT = new Timer();
        wifiDT.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wifiCRList.clear();
               // runOnUiThread(new Runnable() {
                //    public void run() {
                        List<ScanResult>tempWholeWifiList =getWifiAP(getApplicationContext());
                        if (tempWholeWifiList != null) {
                            for (ScanResult r : tempWholeWifiList) {
                                if (r.SSID.contentEquals("CLOC_40") || r.SSID.contentEquals("iac-sphone-ac") || r.SSID.contentEquals("iac-sphone-n")) {
                                    wifiCRList.add(r);
                                    //Log.e("wifilist from onCreate",tempWifiStr.toString());
                                }
                            }
                            //wifiCRList = (ArrayList<ScanResult>) tempWholeWifiList;
                        }
                   // }
                //});
            }
        }, 1000, 1000);



        //   wifiAPDectorTimerObjNum++;
        //}



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


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // beaconList= beacons;
                StringBuilder tempBeaconStr = new StringBuilder();
                int beaconCounter = 0;
               // foundBeaconsNum = 0;


                for (Beacon b : beacons) {
                    //only show our beacons
                    if (b.getId1().toString().equals(APPLIX_BEACON)) {
                        beaconCounter++;
                        tempBeaconStr.append(b.getId2() + "/" + b.getId3() + ", " + b.getRssi() + ", " + String.format("%.2f", b.getDistance()) + "m\n");
                        //beaconList.add(b);
                        //beaconList= beacons;
                      //  foundBeaconsNum = beaconCounter;
                    }
                }

                if (beaconCounter > 0) {
                    // Toast.makeText(getApplicationContext(), "No Beacons Found", Toast.LENGTH_SHORT).show();
                    //logToBeaconDisplay(tempBeaconStr.toString());
                }


               // beaconList = beacons;
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



    class MyView extends View {
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

            //predictedPoint.x=soinnX/(36.65f/mainViewContReadMapWidth);
            //predictedPoint.y=soinnY/(72.00f/mainViewContReadMapHeight);
            //this.bringToFront();


        }

        @Override
        protected void onDraw(Canvas canvas) {

            //X for the map if y on the phone bc phones default is portrait view
            //mainViewfloorX = point.y * (72.00/mainViewContReadMapHeight);
            //mainViewfloorY = point.x * (36.65/mainViewContReadMapWidth);

            //FOR SONY XPERIA Z3, xxhdpi
            //Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.edited_ibeacon_map_480);
            //canvas.drawBitmap(Bitmap.createScaledBitmap(mapBitmap, 1080, 1370, false),0,0,paint);

           // if(mainViewfloorX <= 72.0) {
             //   canvas.drawCircle(point.x, point.y, 9, paint);
              //  canvas.drawCircle(predictedPoint.x,predictedPoint.y,9,predictedPaint);
           // }

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
                 //   predictedPoint.x=mainViewContViewSoinnPredictedY/(36.65f/mainViewContReadMapWidth);
                  //  predictedPoint.y=mainViewContViewSoinnPredictedX/(72.00f/mainViewContReadMapHeight);
            }
            //predictedPoint.x=soinnPredictedY/(36.65f/mapWidth);
            //predictedPoint.y=soinnPredictedX/(72.00f/mapHeight);

        /*
        #define FLOOR_PLAN_HEIGHT 73.05 //73.34 //82.34  //73.34 ; true dimensions : 72 m
        #define FLOOR_PLAN_WIDTH 37.58 //38.726 //+ (550+600+650 mm) ; true dimensions : 36.65 m
        xCGFloat = tappedPoint.y * (FLOOR_PLAN_HEIGHT/mapImageView.frame.size.height);
        yCGFloat = tappedPoint.x * (FLOOR_PLAN_WIDTH/mapImageView.frame.size.width);
        */

          //  mainViewContReadCoordLabel = (TextView)findViewById(R.id.mainViewContReadCoordLabel);

            /*
            if(mainViewfloorX < 72.0) {
                String coordinateStr ="predX: "+String.format("%.2f", mainViewContViewSoinnPredictedX)+ ", predY: "+String.format("%.2f", mainViewContViewSoinnPredictedY)+
                        ", learnX: "+ String.format("%.2f", mainViewfloorX) +", learnY: "+ String.format("%.2f", mainViewfloorY) +
                        ", error: "+ String.format("%.2f", calculateErrorSabun((float)mainViewfloorX,(float)mainViewfloorY,mainViewContViewSoinnPredictedX,mainViewContViewSoinnPredictedY));
                mainViewContReadCoordLabel.setText(coordinateStr);
            }
            */

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
