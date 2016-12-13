package uom.synergen.fyp.smart_insole.gait_analysis;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.location.Location;
import android.location.LocationListener;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.ble.BluetoothLeUart;

public class MainActivity extends Activity implements BluetoothLeUart.Callback, LocationListener {

    private static final String TAG = "Main Activity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private PressureFragment pressureFragment;
    private PostureFragment postureFragment;

    private static StringBuilder leg1Received;
    private static StringBuilder leg2Received;

    private BluetoothLeUart uart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        uart = BluetoothLeUart.getInstanse(this);
        uart.registerCallback(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        uart.registerCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        uart.unregisterCallback(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //GPS coordinates listener starts
    @Override
    public void onLocationChanged(Location location) {

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        Log.i(TAG, "Longitude : " + longitude + " Latitude : " + latitude);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //GPS coordinates listener ends

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0 :pressureFragment = PressureFragment.newInstance(0);
                        return pressureFragment;

                case 1 :postureFragment = PostureFragment.newInstance(1);
                        return postureFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }


    @Override
    public void onConnected(BluetoothLeUart uart) {

    }

    @Override
    public void onConnectFailed(BluetoothLeUart uart) {

    }

    @Override
    public void onDisconnected(BluetoothLeUart uart) {

    }

    @Override
    public void onReceiveLeg1(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {

        try{

            if(leg1Received == null) {
                leg1Received = new StringBuilder();
            }

            String received = rx.getStringValue(0);

            if(!received.endsWith("}")) {
                leg1Received.append(received);
            }else {
                leg1Received.append(received);
                String message = leg1Received.toString();
                message = message.substring(0,message.length()-2);
                pressureFragment.processMessage(message);
                postureFragment.processMessage(message);
                leg1Received = null;
            }
        }catch (Exception ex) {
            Log.e(TAG,"Error.");
            leg1Received = null;
        }
    }

    @Override
    public void onReceiveLeg2(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {
        try{
            if(leg2Received == null) {
                leg2Received = new StringBuilder();
            }

            String received = rx.getStringValue(0);

            if(!received.endsWith("}")) {
                leg2Received.append(received);
            }else {
                leg2Received.append(received);
                String message = leg2Received.toString();
                message = message.substring(0,message.length()-2);
                pressureFragment.processMessage(message);
                postureFragment.processMessage(message);
                leg2Received = null;
            }
        }catch (Exception ex) {
            Log.e(TAG, "Error. " + ex);
            leg2Received = null;
        }
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {

    }

}
