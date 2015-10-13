package producesu.ntt.com.ibeaconsokutei;

/**
 * Created by kphongagsorn on 1/5/15.
 */
public class SignalsArr {
    String dateTime;
    Signal[] sigArray;

    SignalsArr(String dateTime, Signal... signals){
        this.dateTime = dateTime;
        int i = 0;
        for (Signal sig : signals) {
            this.sigArray[i]=sig;
            i++;
        }

    }

    @Override
    public String toString() {
        return super.toString();
    }
}
