package uom.synergen.fyp.smart_insole.gait_analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;

public class SetupActivity extends Activity {

    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        continueButton = (Button) findViewById(R.id.continueButton);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homePage = new Intent(SetupActivity.this, MainActivity.class);
                startActivity(homePage);
            }
        });

    }

}
