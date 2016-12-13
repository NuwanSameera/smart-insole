package uom.synergen.fyp.smart_insole.gait_analysis;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

public class PressureFragment extends Fragment {

    private static final String TAG = "Pressure";

    private ImageView footPressureView;
    private Bitmap bitmap;

    int [][] leftLegPressurePoints;

    int [][] rightLegPressurePonts;

    private static final String ARG_SECTION_NUMBER = "section_number";

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
        return rootView;
    }

    public void processMessage(String receivedMessage) {

        String []message = receivedMessage.split(":");
        String leg = message[0];
        int[] sensorData = new int[16];

        for (int i = 0; i < 15 ; i++) {
            sensorData[i] = Integer.parseInt(message[i + 1]);
        }

        final Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
        // Create a canvas  for new bitmap
        Canvas c = new Canvas(newBmp);

        // Draw your old bitmap on it.
        c.drawBitmap(bitmap, 0, 0, new Paint());

        if(leg.equals("0")) {
            for(int k = 0 ; k < 15 ; k++) {

                for(int i = -2 ; i <= 2 ; i ++) {
                    for(int j = -2 ; j <= 2 ; j++) {

                        if(sensorData[k] > 50) {
                            newBmp.setPixel(2 * leftLegPressurePoints[k][0] + i, 2 * leftLegPressurePoints[k][1] + j, Color.RED);
                        } else {
                            newBmp.setPixel(2 * leftLegPressurePoints[k][0] + i, 2 * leftLegPressurePoints[k][1] + j, Color.BLUE);
                        }
                    }
                }
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    footPressureView.setImageBitmap(newBmp);
                    bitmap = newBmp;
                }
            });

        }else if (leg.equals("1")) {

            for(int k = 0 ; k < 15 ; k++) {

                for(int i = -2 ; i <= 2 ; i ++) {
                    for(int j = -2 ; j <= 2 ; j++) {

                        if(sensorData[k] > 50) {
                            newBmp.setPixel(2 * rightLegPressurePonts[k][0] + i, 2 * rightLegPressurePonts[k][1] + j , Color.RED);
                        } else {
                            newBmp.setPixel(2 * rightLegPressurePonts[k][0] + i, 2 * rightLegPressurePonts[k][1] + j , Color.BLUE);
                        }
                    }
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

}
