package producesu.ntt.com.ibeaconsokutei;

/**
 * Created by kphongagsorn on 10/28/14.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by kphongagsorn on 10/27/14.
 */
public class WifiLogView extends Activity {
    Button doneButton;
    //Collection<Beacon> recordedBeaconListFromMain;
    // TextView rangingTextTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_log_view);

        doneButton = (Button) findViewById(R.id.doneBtn);

    }
    public void onDoneButtonClick(View view){
        this.finish();
    }

    @Override
    public void onBackPressed() {
        // do something on back.
        super.onBackPressed();
        //return;
    }
}
