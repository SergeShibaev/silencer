package com.Silencer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Silencer extends Service {

    private TelephonyManager tm;
    //private NotificationManager nm;
    //private Notifyer notifyer = new Notifyer();
    private Validator validator = new Validator();
    private Logger logger = SilencerActivity.logger;
    public static boolean isActive = false;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        isActive = true;
        super.onCreate();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        registerReceiver(mSMSReceiver, filter);

        //nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        validator.setSilenceTime(0, 9);
    }

    public void onDestroy() {
        isActive = false;
        super.onDestroy();
        unregisterReceiver(mSMSReceiver);
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

    /*private void SendNotification(String message) {
        notifyer.add(message);

        Notification notification = new Notification(R.drawable.ic_menu_delete, message, System.currentTimeMillis());

        Intent intent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notification.setLatestEventInfo(this, "Silencer says", notifyer.getMessage(), pIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.number = notifyer.getUnreaded();

        nm.notify(1, notification);
    }*/

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

                        if (isActive && !validator.isValidNumber(incomingNumber)) {
                            endCall();
                            if (incomingNumber == null) {
                                incomingNumber = "undefined";
                            }
                            String message = "Caller [" + incomingNumber + "] was blocked. Reason: " + validator.getReason();
                            //SendNotification(message);

                            logger.AddLog(message);
                            logger.ShowLog();
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

    private BroadcastReceiver mSMSReceiver = new BroadcastReceiver() {

        private Validator validator = new Validator();

        @Override
        public void onReceive(Context context, Intent intent) {
            validator.setSilenceTime(100, 100);     // no Silence Time for SMS

            if (intent == null || intent.getExtras() == null) {
                return;
            }
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            if (pduArray.length == 0) {
                return;
            }

            List<SmsMessage> messages = new ArrayList<SmsMessage>();
            for (int i = 0; i < pduArray.length; ++i) {
                messages.add(SmsMessage.createFromPdu((byte[]) pduArray[i]));
            }

            String sender = messages.get(0).getDisplayOriginatingAddress();
            if (!validator.isValidNumber(sender)) {
                this.abortBroadcast();
                logger.AddLog("SMS from [" + sender + "] was blocked. Reason: " + validator.getReason());
                logger.ShowLog();
            }
        }
    };
}
