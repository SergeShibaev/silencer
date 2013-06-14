package com.Silencer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by
 * User: Serge Shibaev
 * Date: 13.06.13
 * Time: 14:33
 */
public class AutorunManager extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, Silencer.class));
    }
}
