package com.Silencer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SilencerActivity extends Activity {
    private static final String logFileName = "calls.txt";

    public static String getLogFileName() {
        return logFileName;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onClickStart(View v) {
        startService(new Intent(this, Silencer.class));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, Silencer.class));
    }

    public void onShowLog(View v) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(logFileName)));
            String str;
            ArrayList<String> text = new ArrayList<String>();
            while ((str = reader.readLine()) != null) {
                text.add(str);
            }
            ListView lv = (ListView) findViewById(R.id.lvLog);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.loglist, text);
            lv.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error while opening the log file: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }
}