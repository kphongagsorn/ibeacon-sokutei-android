package producesu.ntt.com.ibeaconsokutei;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kphongagsorn on 11/6/14.
 */


public class MyWifiAP implements Parcelable {
    String timeStamp;
    String ssid;
    String bssid;
    int rssi;
    String os;
    String device;
    String deviceId;
    long timeInMilli;

    MyWifiAP(String timeStamp, String os, String device,String deviceId,String ssid, String bssid, int rssi){
        this.timeStamp=timeStamp;
        this.ssid = ssid;
        this.rssi =rssi;
        this.bssid =bssid;
        this.os = os;
        this.device = device;
        this.deviceId = deviceId;


    }

    MyWifiAP(String timeStamp, long timeInMilli, String os, String device,String deviceId,String ssid, String bssid, int rssi){
        this.timeStamp=timeStamp;
        this.ssid = ssid;
        this.rssi =rssi;
        this.bssid =bssid;
        this.os = os;
        this.device = device;
        this.deviceId = deviceId;
        this.timeInMilli = timeInMilli;


    }

    public String toString(){
        return timeStamp+","+os+","+device+","+deviceId+","+rssi+","+bssid;
    }

    protected MyWifiAP(Parcel in) {
        timeStamp = in.readString();
        ssid = in.readString();
        bssid = in.readString();
        rssi = in.readInt();
        os = in.readString();
        device = in.readString();
        deviceId = in.readString();
        timeInMilli = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timeStamp);
        dest.writeString(ssid);
        dest.writeString(bssid);
        dest.writeInt(rssi);
        dest.writeString(os);
        dest.writeString(device);
        dest.writeString(deviceId);
        dest.writeLong(timeInMilli);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyWifiAP> CREATOR = new Parcelable.Creator<MyWifiAP>() {
        @Override
        public MyWifiAP createFromParcel(Parcel in) {
            return new MyWifiAP(in);
        }

        @Override
        public MyWifiAP[] newArray(int size) {
            return new MyWifiAP[size];
        }
    };
}