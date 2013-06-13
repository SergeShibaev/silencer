package com.Silencer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Serge Shibaev
 * Date: 11.06.13
 * Time: 18:53
 */
public class Validator {
    private static final String[] blackList = { "^\\+372(.*)"/*, "^\\+282(.*)", "^01177(.*)"*/ };
    private static Context context = SilencerActivity.context;
    private String reason = "";
    private Map<String, Integer> silenceTime = new HashMap<String, Integer>();

    public Validator() {
        setSilenceTime(0, 9);
        isTestsPassed();
    }

    public String getReason() {
        return reason;
    }

    private boolean isTestsPassed() {
        String goodNumber = "+79223456789";
        String evilNumber = "+37212345678";
        String textName = "Home";
        boolean passed = true;
        passed = passed && !isValidNumber(null);  // check undefined numbers
        passed = passed &&  isValidNumber(goodNumber);
        passed = passed && !isValidNumber(evilNumber);
        passed = passed && isValidNumber(textName);

        return passed;
    }

    private boolean isBlack(String incomingNumber) {
        for (String number : blackList ) {
            if (incomingNumber.matches(number)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isKnown(String incomingNumber) {
        boolean result = false;

        ContentResolver cr = context.getContentResolver();
        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (pCur.moveToNext()) {
            String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
            if (phone.equals(incomingNumber)) {
                result = true;
                break;
            }
        }
        pCur.close();

        return result;
    }

    private boolean isSilenceTime() {
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);

        return (curHour >= silenceTime.get("start") && curHour <= silenceTime.get("finish"));
    }

    public void setSilenceTime(int start, int finish) {
        silenceTime.put("start", start);
        silenceTime.put("finish", finish);
    }

    public boolean isValidNumber(String incomingNumber) {
        if (incomingNumber == null) {
            reason = "Undefined caller";
            return false;
        }
        if (isBlack(incomingNumber)) {
            reason = "Number from blacklist";
            return false;
        }
        if (isSilenceTime() && !isKnown(incomingNumber)) {
            reason = "Unknown caller refused in Silence time";
            return false;
        }

        reason = "";
        return true;
    }
}
