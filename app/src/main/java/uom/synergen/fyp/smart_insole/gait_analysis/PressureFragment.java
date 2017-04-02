package uom.synergen.fyp.smart_insole.gait_analysis;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Arrays;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;
import uom.synergen.fyp.smart_insole.gait_analysis.mqtt.MqttConnector;

public class PressureFragment extends Fragment {

    private static final String TAG = "Pressure";

    private ImageView footPressureView;
    private Bitmap bitmap;

    short [][] leftLegPressurePoints;

    short [][] rightLegPressurePonts;

    private static final String ARG_SECTION_NUMBER = "section_number";

    private int []pressureSum;
    private byte i;
    private byte []count;

    private byte []overpronation;
    private byte []supination;

    private short pressureSum1;
    private short pressureSum2;

    private float unitPressureSum1;
    private float unitPressureSum2;

    private MqttConnector mqttConnector;

    private TextView []indecators;

    public PressureFragment() {

    }

    public static PressureFragment newInstance(int sectionNumber) {
        PressureFragment pressureFragment = new PressureFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        pressureFragment.setArguments(args);
        return pressureFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        leftLegPressurePoints = SmartInsoleConstants.LEFT_LEG_PRESSURE_POINTS;
        rightLegPressurePonts = SmartInsoleConstants.RIGHT_LEG_PRESSURE_POINTS;

        View rootView = inflater.inflate(R.layout.fragment_pressure, container, false);
        footPressureView = (ImageView) rootView.findViewById(R.id.footPressure);
        bitmap = ((BitmapDrawable)footPressureView.getDrawable()).getBitmap();

        indecators = new TextView[2];

        indecators[0] = (TextView) rootView.findViewById(R.id.leftIndicatorTextView);
        indecators[1] = (TextView) rootView.findViewById(R.id.rightIndicatorTextView);

        mqttConnector = MqttConnector.getInstance(getActivity());

        pressureSum = new int[2];
        count = new byte[2];
        overpronation = new byte[2];
        supination = new byte[2];

        return rootView;
    }

    public void processMessage(String leg, short []pressureData) {

        final byte legInt = Byte.parseByte(leg);

        if(count[legInt] == 0) {
            footPressureView.setImageResource(R.drawable.wait);
        }

        if(count[legInt] < 100) {

            for (i = 0 ; i < 16; i++) {
                pressureSum[legInt] += pressureData[i];
            }

            count[legInt] ++;

        } else if(count[legInt] == 100) {

            pressureSum[legInt] = pressureSum[legInt] / 100;

            if(pressureSum[legInt] > 2000) {
                pressureSum[legInt] = 2000;
            } else if(pressureSum[legInt] < 1000) {
                pressureSum[legInt] = 1000;
            }

            Log.i(TAG, pressureSum[legInt] + "");
            count[legInt]++;

        }else if (count[legInt] == 101 && count[1-legInt] == 101) {

            count[0] ++;
            count[1] ++;

        }else if(count[legInt] == 102 && count[1-legInt] == 102){

            if(leg.equals("0")) {
                colourFoot(leftLegPressurePoints, pressureData);
            } else {
                colourFoot(rightLegPressurePonts, pressureData);
            }

            //Balance Detecting Algorithm

            pressureSum1 = (short) (pressureData[0] + pressureData[5] + pressureData[13] + pressureData[15]);
            unitPressureSum1 = (float) pressureSum1 / pressureSum[legInt];

            pressureSum2 = (short) (pressureData[9] + pressureData[10] + pressureData[11]);
            unitPressureSum2 = (float) pressureSum2 / pressureSum[legInt];

            if(unitPressureSum1 > SmartInsoleConstants.UPPER_LEVEL &&
                    unitPressureSum2 < SmartInsoleConstants.LOWER_LEVEL) {
                overpronation[legInt] = (byte)(overpronation[legInt] + 1);
                Log.i(TAG, "A1");
            }

            if(unitPressureSum2 > SmartInsoleConstants.LOWER_LEVEL) {
                overpronation[legInt] = (byte)(overpronation[legInt] - 1);
                Log.i(TAG, "A2");
            }

            if(unitPressureSum2 > SmartInsoleConstants.UPPER_LEVEL &&
                    unitPressureSum1 < SmartInsoleConstants.LOWER_LEVEL) {
                supination[legInt] = (byte)(supination[legInt] + 1);
                Log.i(TAG, "A3");
            }

            if(unitPressureSum1 > SmartInsoleConstants.LOWER_LEVEL) {
                supination[legInt] = (byte) (supination[legInt] - 1);
                Log.i(TAG, "A4");
            }

            if (overpronation[legInt] > 10 && supination[legInt] <= 0) {

                Log.i(TAG, leg + " Overpronation");
                overpronation[legInt] = 0;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        indecators[legInt].setBackgroundColor(Color.rgb(255, 0, 0));
                    }
                });


            } else if(overpronation[legInt] <= 0 && supination[legInt] > 10) {

                Log.i(TAG, leg + " Supination");
                supination[legInt] = 0;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        indecators[legInt].setBackgroundColor(Color.rgb(0, 0, 255));
                    }
                });

            }

            //Send pressure data to server

            String payload = "P&" + leg + Arrays.toString(pressureData);
            payload = payload.replace("[", ",");
            payload = payload.replace("]", "");
            payload = payload.replace(" ", "");

            try {
                mqttConnector.publish(SmartInsoleConstants.MQTT_SERVER_TOPIC, payload);
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }

    }

    private void colourFoot(short [][] pressurePoints, short[] pressureValues) {

        final Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Create a canvas  for new bitmap
        Canvas c = new Canvas(newBmp);

        // Draw your old bitmap on it.
        c.drawBitmap(bitmap, 0, 0, new Paint());

        Paint paint = new Paint();                          //define paint and paint color
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        for(int k = 0 ; k < 16 ; k++) {

            if(pressureValues[k] < 80) {
                 paint.setColor(Color.GRAY);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            } else if(pressureValues[k] >= 80 && pressureValues[k] < 160) {
                 paint.setColor(Color.BLUE);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            } else if(pressureValues[k] >= 160 && pressureValues[k] < 240) {
                 paint.setColor(Color.GREEN);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            } else if(pressureValues[k] >= 240 && pressureValues[k] < 320) {
                 paint.setColor(Color.WHITE);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            } else if(pressureValues[k] >= 320 && pressureValues[k] < 400) {
                 paint.setColor(Color.YELLOW);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            } else {
                 paint.setColor(Color.RED);
                 c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,20,paint);
            }

        }

        getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
              footPressureView.setImageBitmap(newBmp);
              bitmap = newBmp;
             }
        });
    }

}
