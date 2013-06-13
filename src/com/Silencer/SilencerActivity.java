package com.Silencer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SilencerActivity extends Activity {
    public static Logger logger;
    public static Context context;

    TextView title;
    Button action;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getBaseContext();
        setContentView(R.layout.main);
        logger = new Logger(context, (ListView) findViewById(R.id.lvLog), R.layout.loglist);
        logger.ShowLog();

        title = (TextView) findViewById(R.id.txtTitle);
        action = (Button) findViewById(R.id.btnAction);
        if (Silencer.isActive) {
            action.setText("Stop Silencer");
            title.setText("Silencer is active. You can hide this window");
        } else {
            action.setText("Start Silencer");
            title.setText("To start Silencer press the button below");
        }
    }

    public void onClickAction(View v) {

        if (Silencer.isActive) {
            stopService(new Intent(this, Silencer.class));
            title.setText("You should close this window to stop Silencer");
            action.setText("Start Silencer");
        } else {
            startService(new Intent(this, Silencer.class));
            title.setText("Silencer is active. You can hide this window");
            action.setText("Stop Silencer");
        }
    }
}