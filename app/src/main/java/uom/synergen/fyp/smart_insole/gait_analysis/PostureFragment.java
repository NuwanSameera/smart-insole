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

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.ble.BluetoothLeUart;

public class PostureFragment extends Fragment {

    private static final String TAG = "Posture";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private BluetoothLeUart uart;

    private ImageView postureView;
    private Bitmap bitmap;
    private Bitmap newBmp;

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
        postureView = (ImageView) rootView.findViewById(R.id.posture);
        bitmap = ((BitmapDrawable)postureView.getDrawable()).getBitmap();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // Create a canvas  for new bitmap
        Canvas c = new Canvas(newBmp);

        // Draw your old bitmap on it.
        c.drawBitmap(bitmap, 0, 0, new Paint());
        c.drawLine(width/2,0,width/2,height, new Paint());

        c.drawLine(width/4 , height - 10, 20 , 100 , new Paint());
        c.drawLine(3 * width/4 , height - 10, 200 , 100 , new Paint());

        postureView.setImageBitmap(newBmp);
        bitmap = newBmp;

        return rootView;

    }

    public void processMessage(String receivedMessage) {
        Log.i(TAG,receivedMessage);
    }

}
