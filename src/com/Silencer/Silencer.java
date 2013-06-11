package com.Silencer;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.*;

public class Silencer extends Service {

    private TelephonyManager tm;
    private NotificationManager nm;
    private Notifyer notifyer = new Notifyer();
    private Map<String, ArrayList<String>> blocked = new HashMap<String, ArrayList<String>>();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Silencer was born", Toast.LENGTH_SHORT).show();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Silencer is OFF", Toast.LENGTH_SHORT).show();
    }

    public int onStartCommand(Intent intent, int flags, int startid) {
        if (startid == 1) {
            Toast.makeText(this, "Silencer is ON", Toast.LENGTH_SHORT).show();
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        else {
            Toast.makeText(this, "Silencer is already running", Toast.LENGTH_LONG).show();
        }
        return super.onStartCommand(intent, flags,  startid);
    }

    private String NowAsStr() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%2d/%02d/%4d   %02d:%02d:%02d",
            calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
    }

    private void AddLog(String logString) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(SilencerActivity.getLogFileName(), MODE_APPEND)));
            writer.write(String.format("[%s] %s\n", NowAsStr(), logString));
            writer.close();
        } catch (Exception e) {
            AddLog("Can't save message to the log file because of: " + e.getMessage());
        }
    }

    private void AddBlockedNumber(String number) {
        if (!blocked.containsKey(number)) {
            blocked.put(number, new ArrayList<String>());
        }
        blocked.get(number).add(NowAsStr());
    }

    private void SendNotification(String message) {
        notifyer.add(message);

        Notification notification = new Notification(R.drawable.ic_menu_delete, message, System.currentTimeMillis());

        Intent intent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notification.setLatestEventInfo(this, "Silencer says", notifyer.getMessage(), pIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.number = notifyer.getUnreaded();

        nm.notify(1, notification);
    }

    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        private int cntRings = 0;
        private String[] blackList = { "^\\+372(.*)", "^\\+282(.)", "^01177(.*)" };

        private void RunTests() {
            String goodNumber = "+79223685980";
            String evilNumber = "+37212345678";
            String fromСanada = "011773333";
            boolean isValid = true;
            isValid = isValid && !isValidNumber(null);  // check undefined numbers
            isValid = isValid &&  isValidNumber(goodNumber);
            isValid = isValid && !isValidNumber(evilNumber);
            isValid = isValid && !isValidNumber(fromСanada);
        }

        private void endCall() {
            Class<TelephonyManager> c = TelephonyManager.class;
            Method getITelephonyMethod = null;
            try {
                getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[])null);
                getITelephonyMethod.setAccessible(true);
                ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(tm, (Object[])null);
                iTelephony.endCall();
            }
            catch (Exception e) {
                AddLog("EndCallException: " + e.getMessage());
            }
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

            ContentResolver cr = getContentResolver();
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

        private boolean isValidNumber(String incomingNumber) {
            return (incomingNumber != null && !isBlack(incomingNumber) && (!isSilenceTime() || isKnown(incomingNumber)));
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                //RunTests();
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (cntRings == 0) {
                            AddLog("Incoming: " + incomingNumber);
                        }
                        ++cntRings;

                        if (!isValidNumber(incomingNumber)) {
                            endCall();
                            if (incomingNumber == null) {
                                incomingNumber = "undefined";
                            }
                            SendNotification("Caller was blocked: " + incomingNumber);
                            AddBlockedNumber(incomingNumber);

                            String message = "Number [" + incomingNumber + "] was blocked";
                            AddLog(message);
                        }

                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        AddLog("OFFHOOK: " + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                    default:
                        AddLog("Unknown phone state: " + state + " called: " + incomingNumber);
                }
            }
            catch (Exception e) {
                AddLog("PhoneStateListener: " + e);
            }
        }
    };
}
