package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;
import uom.synergen.fyp.smart_insole.gait_analysis.mqtt.MqttConnector;
import uom.synergen.fyp.smart_insole.gait_analysis.wifi.WifiHandler;

public class MainActivity extends Activity {

    private static final String TAG = "Main Activity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private PressureFragment pressureFragment;
    private PostureFragment postureFragment;

    private BufferedReader[] clientReaders;

    private File externalDirectory;
    private File leftDataFile;
    private File rightDataFile;

    private FileOutputStream leftOutputStream;
    private OutputStreamWriter leftWriter;

    private FileOutputStream rightOutputStream;
    private OutputStreamWriter rightWriter;

    private byte i;

    private static final int REQUEST_PERMISSIONS = 1;

    boolean connected = false;

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

        //Create Wifi connections
        try {

            final Socket[] clients = WifiHandler.getInstance().getClients();
            clientReaders = new BufferedReader[2];

            for (int i = 0 ; i < 2 ; i++) {
                clientReaders[i] = new BufferedReader(new InputStreamReader
                        (clients[i].getInputStream()));
            }

            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String receivedMessage = clientReaders[0].readLine();
                            processMessage(receivedMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            new Thread() {

                @Override

                public void run() {
                    while (true) {
                        try {
                            String receivedMessage = clientReaders[1].readLine();
                            processMessage(receivedMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

        requestPermissionIfNeeded();

        externalDirectory = Environment.getExternalStorageDirectory();

        Log.i(TAG, externalDirectory.getAbsolutePath());

        leftDataFile = new File(externalDirectory,"Left.txt");

        if(!leftDataFile.exists()) {
            try {
                leftDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        try {
            leftOutputStream = new FileOutputStream(leftDataFile);
            leftWriter = new OutputStreamWriter(leftOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        rightDataFile = new File(externalDirectory,"Right.txt");

        if(!rightDataFile.exists()) {
            try {
                rightDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            rightOutputStream = new FileOutputStream(rightDataFile);
            rightWriter = new OutputStreamWriter(rightOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //MQTT Connection
        MqttConnector mqttConnector = MqttConnector.getInstance(this);
        try {
            mqttConnector.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

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

    private void processMessage (String message) {

        try{

            if(message.startsWith("{") && message.endsWith("{")) {
                getReadings(message.substring(1, message.length()-1));
            } else {

                String[] dataMap = message.split("\\{");

                if(dataMap.length > 1) {
                    for (String data : dataMap) {
                        getReadings(data);
                    }
                }
            }

        }catch (Exception e){

        }

    }

    private void getReadings(String message) {

        String[] receivedData = message.split(",");

        if (receivedData.length > 1) {
            String leg = receivedData[0];

            double time = Double.parseDouble(receivedData[17]);

            short[] pressureData = new short[16];

            for (i = 0; i < 16; i++) {
                short x = (short)(Short.parseShort(receivedData[i + 1]) * 2 - 30);
                pressureData[i] = (x < 0) ? 0 : x;
            }

            double[] accelerometerData = new double[3];

            for (i = 0; i < 3; i++) {
                accelerometerData[i] = Double.parseDouble(receivedData[i + 18]) / 10;
            }

            short[] gyroscopeData = new short[3];

            for (i = 0; i < 3; i++) {
                gyroscopeData[i] = Short.parseShort(receivedData[i + 21]);
            }

            String outputString = time + Arrays.toString(pressureData) +
                    Arrays.toString(accelerometerData) + Arrays.toString(gyroscopeData) + "\n";

            outputString = outputString.replace("[",",");
            outputString = outputString.replace("]", "");
            outputString = outputString.replace(" ", "");

            if (leg.equals("0")) {
                try {
                    leftWriter.append(outputString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    rightWriter.append(outputString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            pressureFragment.processMessage(leg,pressureData);
            postureFragment.processData(leg, time, accelerometerData, gyroscopeData , pressureData);

        }
    }

    // region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                    this.checkSelfPermission(Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "No Permission");

                String [] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

                requestPermissions(permissions, REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission Gained..");
                } else {
                }
                break;
            }
            default:
                break;
        }
    }
}
