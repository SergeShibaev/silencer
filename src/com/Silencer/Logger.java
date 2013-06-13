package com.Silencer;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by
 * User: Serge Shibaev
 * Date: 11.06.13
 * Time: 19:19
 */
public class Logger {
    private static final String logFileName = "calls.txt";
    private Context context;
    private ListView lv;
    private int resourceId;

    public Logger(Context baseContext, ListView logWindow, int resId) {
        this.context = baseContext;
        this.lv = logWindow;
        this.resourceId = resId;
    }

    private static String NowAsStr() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%2d/%02d/%4d   %02d:%02d:%02d",
                calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    public void AddLog(String logString) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(SilencerActivity.getLogFileName(), context.MODE_APPEND)));
            writer.write(String.format("[%s] %s\n", NowAsStr(), logString));
            writer.close();
        } catch (Exception e) {
            AddLog("Can't save message to the log file because of: " + e.getMessage());
        }
    }

    public void ShowLog() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(logFileName)));
            String str;
            ArrayList<String> text = new ArrayList<String>();
            while ((str = reader.readLine()) != null) {
                text.add(str);
            }
            Collections.reverse(text);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, resourceId, text);
            lv.setAdapter(adapter);
        } catch (Exception e) {

        }
    }
}
