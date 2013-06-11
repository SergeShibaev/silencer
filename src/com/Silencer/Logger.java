package com.Silencer;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Created by
 * User: Serge Shibaev
 * Date: 11.06.13
 * Time: 19:19
 */
public class Logger {
    private Context context;

    public Logger(Context baseContext) {
        this.context = baseContext;
    }

    private static String NowAsStr() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%2d/%02d/%4d   %02d:%02d:%02d",
                calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
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
}
