package producesu.ntt.com.ibeaconsokutei;

import android.text.format.Time;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kphongagsorn on 11/6/14.
 */

public class MyBeacons extends Time implements Parcelable {
    long timeInMilli;
    String uuid;
    String os;
    String device;
    String timeStamp;
    String deviceId;
    int major;
    int minor;
    int rssi;
    double distance;


    MyBeacons(String timeStamp, String os, String device, String uuid, int maj, int min, int rssi){
        this.timeStamp = timeStamp;
        this.os = os;
        this.device = device;
        this.uuid = uuid;
        this.major = maj;
        this.minor = min;
        this.rssi = rssi;

    }
    MyBeacons(String timeStamp, String os, String device, String deviceId, String uuid, int maj, int min, int rssi, double dist){
        this.timeStamp = timeStamp;
        this.os = os;
        this.device = device;
        this.uuid = uuid;
        this.major = maj;
        this.minor = min;
        this.rssi = rssi;
        this.distance = dist;
        this.deviceId = deviceId;

    }

    MyBeacons(String timeStamp,long timeInMilli, String os, String device, String deviceId, String uuid, int maj, int min, int rssi, double dist){
        this.timeInMilli = timeInMilli;
        this.timeStamp = timeStamp;
        this.os = os;
        this.device = device;
        this.uuid = uuid;
        this.major = maj;
        this.minor = min;
        this.rssi = rssi;
        this.distance = dist;
        this.deviceId = deviceId;

    }


    public String toString(){
        return timeStamp+","+os+","+device+","+deviceId+","+rssi+","+ uuid+","+major+","+minor;
    }


    protected MyBeacons(Parcel in) {
        timeInMilli = in.readLong();
        uuid = in.readString();
        os = in.readString();
        device = in.readString();
        timeStamp = in.readString();
        deviceId = in.readString();
        major = in.readInt();
        minor = in.readInt();
        rssi = in.readInt();
        distance = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timeInMilli);
        dest.writeString(uuid);
        dest.writeString(os);
        dest.writeString(device);
        dest.writeString(timeStamp);
        dest.writeString(deviceId);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeInt(rssi);
        dest.writeDouble(distance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyBeacons> CREATOR = new Parcelable.Creator<MyBeacons>() {
        @Override
        public MyBeacons createFromParcel(Parcel in) {
            return new MyBeacons(in);
        }

        @Override
        public MyBeacons[] newArray(int size) {
            return new MyBeacons[size];
        }
    };
}
