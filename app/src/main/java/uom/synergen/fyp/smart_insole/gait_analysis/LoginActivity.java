package uom.synergen.fyp.smart_insole.gait_analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;
import uom.synergen.fyp.smart_insole.gait_analysis.wifi.WifiHandler;

public class LoginActivity extends Activity {

    private Button loginButton;
    private TextView userNameText;
    private TextView passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        userNameText = (TextView) findViewById(R.id.userNameText);
        passwordText = (TextView) findViewById(R.id.passwordText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonAction();
            }
        });

        requestPermissionIfNeeded();

    }

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
                        Intent homePage = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(homePage);
                    }
                }
            }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // region Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                String [] permissions = new String[]{Manifest.permission.SEND_SMS};

                requestPermissions(permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Login", "Permission Gained..");
                } else {
                }
                break;
            }
            default:
                break;
        }
    }

}
