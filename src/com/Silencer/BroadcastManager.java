package com.Silencer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Serge Shibaev
 * Date: 11.06.13
 * Time: 18:25
 */
public class BroadcastManager extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private final Validator validator = new Validator(Silencer.context);
    private Logger logger = SilencerActivity.logger;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getExtras() == null/* || ACTION.compareToIgnoreCase(intent.getAction()) == 0*/) {
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
}
