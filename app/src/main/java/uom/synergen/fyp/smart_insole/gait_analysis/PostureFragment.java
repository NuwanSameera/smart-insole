package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

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
    private TextView strideLengthText;
    private Button stepCountResetButton;
    private TextView calaryBurnedText;
    private Button calaryBurnedResetButton;

    private byte i;
    private byte count[];
    private short pressureSum;
    private double fallTime[];
    private double fallDetectionTime[];

    private static final int REQUEST_PERMISSIONS = 1;

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
        stepCountResetButton = (Button) rootView.findViewById(R.id.stepCountResetButton);
        speedText = (TextView) rootView.findViewById(R.id.speedText);
        strideLengthText = (TextView) rootView.findViewById(R.id.strideLengthText);
        calaryBurnedText = (TextView) rootView.findViewById(R.id.calaryBurnedText);
        calaryBurnedResetButton = (Button) rootView.findViewById(R.id.calaryBurnedButton);

        stepCountResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCountResetButtonAction();
            }
        });

        calaryBurnedResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calaryBurnedButtonAction();
            }
        });

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

        return rootView;

    }

    private void stepCountResetButtonAction() {
        stepCount[0] = 0;
        stepCount[1] = 0;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stepCountText.setText(String.valueOf(stepCount[0] + stepCount[1]));
                strideLengthText.setText(String.valueOf(stepCount[1]));
                speedText.setText(String.valueOf(stepCount[0]));
            }
        });
    }

    private void calaryBurnedButtonAction() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calaryBurnedText.setText("");
            }
        });
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

        if (status[legInt] == 1) {
            status[legInt] = 10;
        }

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

        if (status[legInt] == 7) {
            status[legInt] = 0;
            firstMaxPeak[legInt] = 0;
            firstMinPeak[legInt] = 0;
            secondMinPeak[legInt] = 0;
            stepCount[legInt]++;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stepCountText.setText(String.valueOf(stepCount[0] + stepCount[1]));
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
                        calaryBurnedText.setText("Person Fall");

                        new AlertDialog.Builder(getActivity()).setMessage("Person Fall.")
                                .show();

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
                        MediaPlayer mp = MediaPlayer.create(getActivity().getApplicationContext(), notification);
                        mp.start();

                        //Send Location to Server
                        LocationManager locationManager = (LocationManager) getActivity().
                                getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String provider = locationManager.getBestProvider(criteria, false);

                        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED &&
                                getActivity().checkSelfPermission(Manifest.permission.SEND_SMS)
                                        != PackageManager.PERMISSION_GRANTED) {

                            Log.i(TAG, "No Permission");

                            String [] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};

                            getActivity().requestPermissions(permissions, REQUEST_PERMISSIONS);
                        }

                        Location location = locationManager.getLastKnownLocation(provider);

                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String locationUrl = "https://www.google.com/maps/preview/@"+latitude+","+longitude+",8z";
                        Log.i("Login", locationUrl);


                        //Send SMS to care giver
                        PendingIntent pi = PendingIntent.getActivity
                                (getActivity(), 0,new Intent(getActivity(), PostureFragment.class), 0);
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(SmartInsoleConstants.CARE_GIVER_PHONE_NUMBER, null, "Person Fall", pi, null);

                        count[legInt] = 0;

                     }
                 });
            } else {
                count[legInt] = 0;
            }

        }

        preGxValue[legInt] = gyroscopeData[0];

    }

}
