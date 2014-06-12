package nl.uva.netcentric.murt.protocol;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;
import java.util.Map;


public class MurtActivity extends Activity {

    private static final String TAG = "protocolservice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_murt);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("murt"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");
        }
    };


    // Returns a map containing <domain, device>
    public Map<String, String> getMurtDevices() {
        return null;
    }


    public void startProtocolService(View v) {
        Log.i(TAG, "callService");
        Intent i = new Intent(MurtActivity.this, ProtocolService.class);
        i.putExtra("murt", "murt");
        startService(i);
    }

    public void stopProtocolService(View v) {
        Log.i(TAG, "stop");
        Intent i = new Intent(MurtActivity.this, ProtocolService.class);
        MurtActivity.this.stopService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.murt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
