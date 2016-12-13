package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.ble.BluetoothLeUart;
import uom.synergen.fyp.smart_insole.gait_analysis.device_list.BleDeviceAdapter;
import uom.synergen.fyp.smart_insole.gait_analysis.device_list.BleDeviceModel;

public class DevicesListActivity extends Activity implements BluetoothLeUart.Callback{

    private final String TAG = "Device Activity";

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private ListView listDevices;
    private Button connectButton;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private BluetoothLeUart uart;
    private ArrayList<BleDeviceModel> modelItems;
    private BleDeviceAdapter devicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_devices_list);
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();
        // Initialize UART.
        uart = BluetoothLeUart.getInstanse(getApplicationContext());

        setResult(Activity.RESULT_CANCELED);

        listDevices = (ListView) findViewById(R.id.listDevices);
        connectButton = (Button) findViewById(R.id.connectButton);
        modelItems = new ArrayList<>();

        connectButtonAction();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothAdapter = BluetoothLeUart.getBluetoothAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestLocationPermissionIfNeeded();

    }

    private void connectButtonAction() {

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> selectedAddress = new ArrayList<>();

                for(int i = 0 ; i < devicesAdapter.getCount() ; i++) {
                    BleDeviceModel bleDeviceModel = (BleDeviceModel) devicesAdapter.getItem(i);

                    if (bleDeviceModel.isSeleted()) {
                        String deviceName = bleDeviceModel.getName();
                        selectedAddress.add(deviceName.substring(deviceName.length() - 17));
                    }
                }

                if(selectedAddress.size() == 2) {
                    uart.connect(selectedAddress);
                } else {
                    Toast.makeText(DevicesListActivity.this,"Please Select two devices correctly.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                devicesAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        uart.registerCallback(this);

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        devicesAdapter = new BleDeviceAdapter(this,modelItems);
        listDevices.setAdapter(devicesAdapter);
        scanLeDevice(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        devicesAdapter.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        uart.unregisterCallback(this);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    uart.stopScan();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            uart.startScan();
        } else {
            mScanning = false;
            uart.stopScan();
        }
        invalidateOptionsMenu();
    }

    // region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for Bluetooth peripherals");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Bluetooth Scanning not available");
                    builder.setMessage("Since location access has not been granted, the app will not be able to scan for Bluetooth peripherals");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onConnected(BluetoothLeUart uart) {

        if (mScanning) {
            uart.stopScan();
            mScanning = false;
        }

        final Intent intent = new Intent(DevicesListActivity.this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onConnectFailed(BluetoothLeUart uart) {
        System.out.println("Connection Failed..");
    }

    @Override
    public void onDisconnected(BluetoothLeUart uart) {

    }

    @Override
    public void onReceiveLeg1(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {

    }

    @Override
    public void onReceiveLeg2(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {

    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        final String deviceName = device.getName();
        String deviceDetail = "";
        if(deviceName != null && deviceName.length() > 0) {
            deviceDetail = deviceDetail.concat(deviceName);
        }else {
            deviceDetail = deviceDetail.concat("Unknown Device");
        }

        deviceDetail = deviceDetail.concat("\n" + device.getAddress());
        BleDeviceModel bleDeviceModel = new BleDeviceModel(deviceDetail);

        if(!modelItems.contains(bleDeviceModel)) {
            modelItems.add(bleDeviceModel);
            listDevices.setAdapter(devicesAdapter);
        }
    }

}
