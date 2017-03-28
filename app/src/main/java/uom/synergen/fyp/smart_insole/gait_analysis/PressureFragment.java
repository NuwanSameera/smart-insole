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

import java.util.Arrays;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

public class PressureFragment extends Fragment {

    private static final String TAG = "Pressure";

    private ImageView footPressureView;
    private Bitmap bitmap;

    short [][] leftLegPressurePoints;

    short [][] rightLegPressurePonts;

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

    public void processMessage(String leg, short []pressureData) {

        if(leg.equals("0")) {
            colourFoot(leftLegPressurePoints, pressureData);
        } else {
            colourFoot(rightLegPressurePonts, pressureData);
        }

    }

    private void colourFoot(short [][] pressurePoints, short[] pressureValues) {

//        int forceDistanceProductSumX = 0;
//        int forceDistanceProductSumY = 0;

//        int forceSum = 0;

        final Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Create a canvas  for new bitmap
        Canvas c = new Canvas(newBmp);

        // Draw your old bitmap on it.
        c.drawBitmap(bitmap, 0, 0, new Paint());

        Paint paint = new Paint();                          //define paint and paint color

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        for(int k = 0 ; k < 16 ; k++) {
             for(int i = 0 ; i <= 20 ; i ++) {

                 if(pressureValues[k] > 25) {
                     paint.setColor(Color.rgb(255 - i , 0,0));
                     c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,i,paint);
                 } else {
                     paint.setColor(Color.rgb(0, 255 - i * 20,0));
                     c.drawCircle(pressurePoints[k][0] * 2 , pressurePoints[k][1] * 2 ,i,paint);
                 }

            }

//            forceDistanceProductSumX += pressureValues[k] * pressurePoints[k][0];
//            forceDistanceProductSumY += pressureValues[k] * pressurePoints[k][1];
//            forceSum += pressureValues[k];

        }

//        int centerOfPressureX = forceDistanceProductSumX / forceSum;
//        int centerOfPressureY = forceDistanceProductSumY / forceSum;
//
//        for (int i = -5; i <= 5; i++) {
//             for (int j = -5; j <= 5; j++) {
//                  newBmp.setPixel(2 * centerOfPressureX + i, 2 * centerOfPressureY + j, Color.GRAY);
//             }
//        }

        getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                  footPressureView.setImageBitmap(newBmp);
                  bitmap = newBmp;
             }
        });
    }

}
