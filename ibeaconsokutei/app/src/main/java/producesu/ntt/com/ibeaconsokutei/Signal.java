package producesu.ntt.com.ibeaconsokutei;

import android.text.format.Time;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by kphongagsorn on 1/5/15.
 */

public class Signal implements Parcelable {
    int rssi;
    String uuid;
    int major;
    int minor;
    long timeRecordedInMilli;
    String timeStamp;
    boolean isABeaconSignal;

    Signal(String timeStamp, long timeRecordedInMilli, int rssi, String uuid, int major, int minor,  boolean isABeaconSignal){
        this.timeStamp = timeStamp;
        this.timeRecordedInMilli = timeRecordedInMilli;
        this.rssi = rssi;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.isABeaconSignal = isABeaconSignal;

    }

    Signal(String timeStamp, long timeRecordedInMilli, int rssi, String uuid, boolean isABeaconSignal){
        this.timeStamp = timeStamp;
        this.timeRecordedInMilli=timeRecordedInMilli;
        this.rssi = rssi;
        this.uuid = uuid;
        this.isABeaconSignal = isABeaconSignal;

    }

    String getBeaconJsonAsString(){
        return "{ \"rssi\":"  + rssi + ", "
                + "\"uuid\":" +  "\"" +uuid  + "\"" + ","
                + "\"major\":" + major + ","
                + "\"minor\":" + minor +
                "}";
    }

    String getWifiJsonAsString(){
        return "{ \"rssi\":"  + rssi + ", "
                + "\"uuid\":" + "\"" + uuid + "\"" +
                "}";
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected Signal(Parcel in) {
        rssi = in.readInt();
        uuid = in.readString();
        major = in.readInt();
        minor = in.readInt();
        timeRecordedInMilli = in.readLong();
        timeStamp = in.readString();
        isABeaconSignal = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rssi);
        dest.writeString(uuid);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeLong(timeRecordedInMilli);
        dest.writeString(timeStamp);
        dest.writeByte((byte) (isABeaconSignal ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Signal> CREATOR = new Parcelable.Creator<Signal>() {
        @Override
        public Signal createFromParcel(Parcel in) {
            return new Signal(in);
        }

        @Override
        public Signal[] newArray(int size) {
            return new Signal[size];
        }
    };
}
