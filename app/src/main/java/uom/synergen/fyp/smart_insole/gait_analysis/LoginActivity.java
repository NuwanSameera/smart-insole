package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.wifi.WifiHandler;

public class LoginActivity extends Activity {

    private final String TAG = "Login";

    private Button loginButton;
    private TextView userNameText;
    private TextView passwordText;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        userNameText = (TextView) findViewById(R.id.userNameText);
        passwordText = (TextView) findViewById(R.id.passwordText);
        title = (TextView) findViewById(R.id.titleText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonAction();
            }
        });

    }


    @TargetApi(Build.VERSION_CODES.M)
    void loginButtonAction() {

        try {

            final WifiHandler wifiHandler = WifiHandler.getInstance();

            new Thread() {
                @Override
                public void run() {

                    boolean twoClientConnected = false;
                    
                    try {
                        twoClientConnected = wifiHandler.clientConnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(twoClientConnected) {
                        Intent homePage = new Intent(LoginActivity.this, SetupActivity.class);
                        startActivity(homePage);
                    }
                }
            }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
