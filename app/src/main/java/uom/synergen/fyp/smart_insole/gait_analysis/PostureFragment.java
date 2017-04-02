package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Calendar;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;
import uom.synergen.fyp.smart_insole.gait_analysis.mqtt.MqttConnector;

public class PostureFragment extends Fragment {

    private static final String TAG = "Posture";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private byte[] status;

    private short[] firstMaxPeak;
    private short[] firstMinPeak;
    private short[] secondMinPeak;

    private short[] preGxValue;
    private int[] stepCount;

    private double thetaX;
    private double thetaY;
    private double[][] theta;
    private double[] previousTime;

    private TextView stepCountText;
    private TextView speedText;


    private byte i;
    private byte count[];
    private short pressureSum;
    private double fallTime[];
    private double fallDetectionTime[];

    private static final int REQUEST_PERMISSIONS = 1;
    private LocationManager locationManager;
    private String locationProvider;

    private MqttConnector mqttConnector;

    private Calendar calendar;

    private ImageView postureImageView;

    public PostureFragment() {
        // Required empty public constructor
    }

    public static PostureFragment newInstance(int sectionNumber) {
        PostureFragment fragment = new PostureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_posture, container, false);

        stepCountText = (TextView) rootView.findViewById(R.id.stepCountText);
        postureImageView = (ImageView) rootView.findViewById(R.id.postureImageView);

        status = new byte[2];

        firstMaxPeak = new short[2];
        firstMinPeak = new short[2];
        secondMinPeak = new short[2];

        preGxValue = new short[]{0, 0};

        stepCount = new int[2];

        theta = new double[2][15];
        previousTime = new double[2];

        count = new byte[2];
        fallTime = new double[2];
        fallDetectionTime = new double[2];

        //Location Begin

        requestLocationPermissionIfNeeded();
        locationBegin();

        mqttConnector = MqttConnector.getInstance(rootView.getContext());

        return rootView;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void processData(String leg, double time, double[] accelerometerData, short[] gyroscopeData,
                            short[] pressureData) {

        final int legInt = Integer.parseInt(leg);
        pressureSum = 0;

        if (status[legInt] == 0 && (Math.abs(preGxValue[legInt] - gyroscopeData[0]) >
                SmartInsoleConstants.DELTA_GX) && (gyroscopeData[0] > SmartInsoleConstants.P1_REF_LOW) &&
                (gyroscopeData[0] < SmartInsoleConstants.P1_REF_HIGH)) {
            status[legInt] = 1;
        }

        if (status[legInt] == 10) {
            if (firstMinPeak[legInt] > gyroscopeData[0]) {
                firstMinPeak[legInt] = gyroscopeData[0];
            } else {
                status[legInt] = 2;
            }
        }

        if (status[legInt] == 2 && (preGxValue[legInt] < 0 && gyroscopeData[0] >= 0)) {
            status[legInt] = 3;
        }

        if (status[legInt] == 3) {
            if (firstMaxPeak[legInt] < gyroscopeData[0]) {
                firstMaxPeak[legInt] = gyroscopeData[0];
            } else {
                status[legInt] = 4;
            }
        }

        if (status[legInt] == 4 && (preGxValue[legInt] > 0 && gyroscopeData[0] <= 0)) {
            status[legInt] = 5;
        }

        if (status[legInt] == 5) {
            if (secondMinPeak[legInt] > gyroscopeData[0]) {
                secondMinPeak[legInt] = gyroscopeData[0];
            } else {
                status[legInt] = 6;
            }
        }

        if (status[legInt] == 6 && (Math.abs(preGxValue[legInt] - gyroscopeData[0])
                < SmartInsoleConstants.DELTA_GX) && (gyroscopeData[0] < SmartInsoleConstants.P7_REF_HIGH)
                && (gyroscopeData[0] > SmartInsoleConstants.P7_REF_LOW)) {

            status[legInt] = 7;

        }

        //Start of cycle
        if (status[legInt] == 1) {
            status[legInt] = 10;
            postureImageView.setImageResource(R.drawable.walikng);
        }

        //Integration
        if (status[legInt] > 0) {
            if ((time - previousTime[legInt]) > 500) {
                count[legInt]++;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stepCountText.setText(String.valueOf(stepCount[0] + stepCount[1]));
                    }
                });
            }
        }

        //End of Cycle
        if (status[legInt] == 7) {
            status[legInt] = 0;
            firstMaxPeak[legInt] = 0;
            firstMinPeak[legInt] = 0;
            secondMinPeak[legInt] = 0;
            stepCount[legInt]++;

            postureImageView.setImageResource(R.drawable.standstill);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stepCountText.setText(String.valueOf(stepCount[0] + stepCount[1]));
                    try {

                        calendar = Calendar.getInstance();
                        String payload = "S&" + (stepCount[0] + stepCount[1]) + "&" + calendar.get(Calendar.YEAR) + "&" +
                                (calendar.get(Calendar.MONTH) + 1) + "&" + calendar.get(Calendar.DAY_OF_MONTH) + "&" +
                                calendar.get(Calendar.HOUR_OF_DAY);

                        mqttConnector.publish(SmartInsoleConstants.MQTT_SERVER_TOPIC , payload);

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        //Fall Detection
        thetaX += gyroscopeData[0] * (time - previousTime[legInt]) * 0.001;
        thetaY += gyroscopeData[1] * (time - previousTime[legInt]) * 0.001;


        for (i = 0; i < 14; i++) {
            theta[legInt][14 - i] = theta[legInt][13 - i];
        }

        theta[legInt][0] = Math.sqrt(thetaX * thetaX + thetaY * thetaY);
        previousTime[legInt] = time;

        for (i = 0; i < 5; i++) {
            if (Math.abs(theta[legInt][0] - theta[legInt][14 - i]) > 80.0) {
                count[legInt] = 1;
                fallTime[legInt] = time;
                break;
            }
        }

        if (count[legInt] > 0 && count[legInt] < 6) {

            if ((time - fallTime[legInt]) < 2000.0) {
                for (i = 0; i < 16; i++) {
                    pressureSum += pressureData[i];
                }
                if (pressureSum < 600) {
                    count[legInt]++;
                }

            } else {
                count[legInt] = 0;
            }

        } else if (count[legInt] == 6) {

            fallDetectionTime[legInt] = time;
            count[legInt] = 7;

        } else if (count[legInt] == 7) {

            if ((time - fallDetectionTime[legInt] < 500) && (count[1 - legInt] == 7)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        postureImageView.setImageResource(R.drawable.falling);

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
                        MediaPlayer mp = MediaPlayer.create(getActivity().getApplicationContext(), notification);
                        mp.start();

                        //Send Location to Server
                        sendLocation();

                        //Send SMS to care giver
                        //sendSms();

                        
                        count[legInt] = 0;

                     }
                 });
            } else {
                count[legInt] = 0;
            }

        }

        preGxValue[legInt] = gyroscopeData[0];

    }


    // region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access to get GPS Location");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                });
                builder.show();
            }

            if(getActivity().checkSelfPermission(Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("This app needs send SMS permission");
                builder.setMessage("Please grant send SMS permission to send falling message");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                    }
                });
                builder.show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Location access has not been granted");
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

            case  2: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("SMS permission has not been granted");
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

    @TargetApi(Build.VERSION_CODES.M)
    private void locationBegin() {
        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            String [] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, 1);

        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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
        };

        locationManager.requestLocationUpdates(locationProvider, 0, 0, listener);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void sendLocation() {

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            String [] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, 1);
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        //String locationUrl = "https://www.google.com/maps/preview/@" + latitude + "," + longitude +
        //        ",18z/data=!3m1!4b1!4m5!3m4!1s0x0:0x0!8m2!3d"+latitude+"!4d" + longitude;
        //Log.i("Login", locationUrl);

        calendar = Calendar.getInstance();

        String payload = "F&" + latitude + "&" + longitude +"&" + calendar.get(Calendar.YEAR) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "&" +
                calendar.get(Calendar.HOUR_OF_DAY) +":" + calendar.get(Calendar.MINUTE);

        try {
            mqttConnector.publish(SmartInsoleConstants.MQTT_SERVER_TOPIC, payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendSms() {
        PendingIntent pi = PendingIntent.getActivity
                (getActivity(), 0,new Intent(getActivity(), PostureFragment.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(SmartInsoleConstants.CARE_GIVER_PHONE_NUMBER, null, "Person Fall", pi, null);
    }

}
