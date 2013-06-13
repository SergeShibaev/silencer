package com.Silencer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class SilencerActivity extends Activity {
    private static final String logFileName = "calls.txt";
    public static Logger logger;

    public static String getLogFileName() {
        return logFileName;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        logger = new Logger(this.getBaseContext(), (ListView) findViewById(R.id.lvLog), R.layout.loglist);
        logger.ShowLog();
    }

    public void onClickStart(View v) {
        startService(new Intent(this, Silencer.class));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, Silencer.class));
    }
}