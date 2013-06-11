package com.Silencer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.Calendar;

/**
 * User: Serge Shibaev
 * Date: 11.06.13
 * Time: 18:53
 */
public class Validator {
    private static final String[] blackList = { "^\\+372(.*)", "^\\+282(.)", "^01177(.*)" };
    private Context context;

    public Validator(Context baseContext) {
        this.context = baseContext;
    }

    public void RunTests() {
        String goodNumber = "+79223685980";
        String evilNumber = "+37212345678";
        String fromСanada = "011773333";
        boolean isValid = true;
        isValid = isValid && !isValidNumber(null);  // check undefined numbers
        isValid = isValid &&  isValidNumber(goodNumber);
        isValid = isValid && !isValidNumber(evilNumber);
        isValid = isValid && !isValidNumber(fromСanada);
    }

    private boolean isBlack(String incomingNumber) {
        for (String number : blackList ) {
            if (incomingNumber.matches(number)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKnown(String incomingNumber) {
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
        int curHour = calendar.get(Calendar.HOUR);

        return (curHour >= 0 && curHour <= 9);
    }

    public boolean isValidNumber(String incomingNumber) {
        return (incomingNumber != null && !isBlack(incomingNumber) && (!isSilenceTime() || isKnown(incomingNumber)));
    }
}
