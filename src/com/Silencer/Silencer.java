package com.Silencer;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class Silencer extends Service {

    private TelephonyManager tm;
    private NotificationManager nm;
    private Notifyer notifyer = new Notifyer();
    public static Context context;
    private Validator validator;
    public static Logger logger;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Silencer was born", Toast.LENGTH_SHORT).show();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        context = getBaseContext();
        logger  = new Logger(this.context);
        validator = new Validator(this.context);
        logger.AddLog("Silencer was born");
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
                logger.AddLog("EndCallException: " + e.getMessage());
            }
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (cntRings == 0) {
                            logger.AddLog("Incoming: " + incomingNumber);
                        }
                        ++cntRings;

                        if (!validator.isValidNumber(incomingNumber)) {
                            endCall();
                            if (incomingNumber == null) {
                                incomingNumber = "undefined";
                            }
                            SendNotification("Caller was blocked: " + incomingNumber);

                            String message = "Number [" + incomingNumber + "] was blocked";
                            logger.AddLog(message);
                        }

                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        logger.AddLog("OFFHOOK: " + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                    default:
                        logger.AddLog("Unknown phone state: " + state + " called: " + incomingNumber);
                }
            }
            catch (Exception e) {
                logger.AddLog("PhoneStateListener: " + e);
            }
        }
    };
}
