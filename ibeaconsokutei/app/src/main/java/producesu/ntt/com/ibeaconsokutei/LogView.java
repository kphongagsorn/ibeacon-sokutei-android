package producesu.ntt.com.ibeaconsokutei;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by kphongagsorn on 10/27/14.
 */
public class LogView extends Activity{
    Button doneButton;
    ArrayList<Beacon>recordedBeaconListFromMain=null;
    ArrayList<MyBeacons>blist=null;
    ArrayList<MyWifiAP>wlist=null;
   // String wifiListFromMain="";
    ArrayList<ScanResult> wifiListFromMain=null;

    StringBuilder recordedBeaconListFromMainStr =null;
    StringBuilder wifiListFromMainStr =null;

    TextView rangingTextTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);

        doneButton = (Button) findViewById(R.id.doneBtn);
        rangingTextTV = (TextView) findViewById(R.id.rangingText);
        rangingTextTV.setMovementMethod(new ScrollingMovementMethod());

        recordedBeaconListFromMainStr = new StringBuilder(getIntent().getStringExtra("recordedBeaconList"));
        wifiListFromMainStr = new StringBuilder(getIntent().getStringExtra("wifiList"));

        if(recordedBeaconListFromMainStr==null || wifiListFromMainStr == null){
            rangingTextTV.setText("No recorded data");
        } else {

        /*
        if(recordedBeaconListFromMainStr!=null) {
            blist = new ArrayList<MyBeacons>();
        }
        if(wifiListFromMainStr !=null) {
            wlist = new ArrayList<MyWifiAP>();
        }
        */

            wlist = new ArrayList<MyWifiAP>();
            blist = new ArrayList<MyBeacons>();
            //Time now = new Time();
            //now.setToNow();
            //String timeStamp = now.format("%Y-%m-%d,%H:%M:%S");

            String[] tempBeacons = recordedBeaconListFromMainStr.toString().split("\n");
            if (tempBeacons[0]!="") {
                for (int i = 0; i < tempBeacons.length; i++) {
                    String[] aBeacon = tempBeacons[i].split(",");
                    String timeStmp = aBeacon[0];
                    String uuid = aBeacon[1];
                    int major = Integer.parseInt(aBeacon[2]);
                    int minor = Integer.parseInt(aBeacon[3]);
                    int rssi = Integer.parseInt(aBeacon[4]);
                    double distance = Double.parseDouble(aBeacon[5]);

                    TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    String deviceUuid = tManager.getDeviceId();

                    MyBeacons b = new MyBeacons(timeStmp, Build.VERSION.RELEASE, Build.MODEL, deviceUuid,uuid, major, minor, rssi, distance);
                    blist.add(b);
                }
            }

            String[] tempWAP = wifiListFromMainStr.toString().split("\n");
            if(tempWAP[0]!="") {
                for (int i = 0; i < tempWAP.length; i++) {
                    String[] aWAP = tempWAP[i].split(",");
                    String timeStmp = aWAP[0];
                    String ssid = aWAP[1];
                    String bssid =aWAP[2];
                    int rssi = Integer.parseInt(aWAP[3]);

                    TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    String deviceUuid = tManager.getDeviceId();

                    //MyWifiAP w = new MyWifiAP(timeStmp, Build.VERSION.RELEASE, Build.MODEL,deviceUuid,ssid, bssid, rssi);
                    //wlist.add(w);

                }
            }

            //recordedBeaconListFromMain = (ArrayList<Beacon>) getIntent().getExtras().getSerializable("recordedBeaconList");
            //wifiListFromMain = (ArrayList<ScanResult>) getIntent().getExtras().getSerializable("wifiList");


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
                //rangingTextTV.append(blist.toString()+"\n");
                rangingTextTV.append(blist.toString()+"\n");

            }

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
                rangingTextTV.append(wlist.toString()+"\n");
            }


        }
    }

    /**
     * return to MainView
     * @param view
     */
    public void onDoneButtonClick(View view){
        //recordedBeaconListFromMain = null;
        //wifiListFromMain = null;
        rangingTextTV.setText("");
        recordedBeaconListFromMainStr=null;
        wifiListFromMainStr=null;
        blist=null;
        wlist=null;
        this.finish();
    }

    /**
     * return to MainView
     */
    @Override
    public void onBackPressed() {
        //recordedBeaconListFromMain = null;
        //wifiListFromMain = null;
        recordedBeaconListFromMainStr=null;
        wifiListFromMainStr=null;
        rangingTextTV.setText("");
        blist=null;
        wlist=null;
        super.onBackPressed();
    }

    /**
     * show recorded beacons only
     * @param view
     */
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
     * show beacons and wifi ap's
     * @param view
     */
    public void onShowAllClick(View view){
        rangingTextTV.setText("");
        if (blist !=null) {/*
            for (MyBeacons b : blist) {
                rangingTextTV.append(b.toString());
            }
            */
            rangingTextTV.append(blist.toString()+"\n");
        }

        if(wlist !=null){
            /*
            for(MyWifiAP w: wlist){
                rangingTextTV.append(w.toString());
            }
            */
            rangingTextTV.append(wlist.toString()+"\n");
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


            if (blist !=null){
                /*
                for(MyBeacons b: blist){
                    tempOutputString+=timeStamp + "," + Build.SERIAL + "," + Build.DISPLAY + "," + Build.FINGERPRINT  + "," + Build.MODEL + "," + Build.DEVICE + ","
                            + b.toString();
                }
                */
                tempOutputString+=blist.toString();
            }
            /*
            if(recordedBeaconListFromMain!=null) {
                for (Beacon b : recordedBeaconListFromMain) {
                    //osw.write(timeStamp + "," + Build.SERIAL + "," + Build.DISPLAY + "," + Build.BOOTLOADER + "," + Build.BRAND + "," + Build.BOARD + "," + Build.FINGERPRINT + "," + Build.PRODUCT + "," + Build.MODEL + "," + Build.DEVICE + "," + b.getId1().toString() + "," + b.getId2() + "," + b.getId3() + "," + b.getRssi() + "\n");
                     tempOutputString+=timeStamp + "," + Build.SERIAL + "," + Build.DISPLAY + "," + Build.FINGERPRINT  + "," + Build.MODEL + "," + Build.DEVICE + "," + b.getId1().toString() + "," + b.getId2() + "," + b.getId3() + "," + b.getRssi() + "\n";
                }
                //osw.write(wifiListFromMain);
            }
            */


            if(wlist !=null){
                /*
                for(MyWifiAP w: wlist){
                    tempOutputString+=timeStamp + ","+w.toString();
                }
                */
                tempOutputString+=wlist.toString();
            }
            /*
            if(wifiListFromMain!=null) {
                for(ScanResult r: wifiListFromMain){
                    //osw.write(timeStamp + ","+r.SSID+","+r.level+"\n");
                    tempOutputString+=timeStamp + ","+r.SSID+","+r.level+"\n";
                }
            }
            */
            Log.i("logFile output:",tempOutputString);
            osw.write(tempOutputString);
            osw.flush();
            osw.close();
            Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
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

}


