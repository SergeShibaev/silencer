package com.Silencer;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
  * User: Serge
 * Date: 27.01.13
 * Time: 13:54
  */
public class Notifyer extends Activity {
    //public static final int SILENCER_NOTIFYER = 1;
    private ArrayList<String> messages = new ArrayList<String>();
    private Map<String, String> blocked = new HashMap<String, String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        messages.clear();
        blocked.clear();
    }

    public int getUnreaded() {
        return messages.size();
    }

    public void add(String text) {
        messages.add(text);
    }

    public String getMessage() {
        String result = "";
        for (String message : messages) {
            result += message + "\n";
        }
        return result;
    }
}
