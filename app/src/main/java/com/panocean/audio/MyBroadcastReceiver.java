package com.panocean.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by chan on 7/19/16.
 */
public class MyBroadcastReceiver  extends BroadcastReceiver {
    private static final String TAG = "A2DPService";

    @Override
    public void onReceive(Context context, Intent intent) {
         String act = intent.getAction();

        if (act.equals( Intent.ACTION_BOOT_COMPLETED )) {
            Log.i(TAG, "BOOT_COMPLETED received");
            Intent it = new Intent();
            it.setClass(context,A2DPService.class);
            context.startService(it);
        } else if (act.equals( Intent.ACTION_SCREEN_OFF )) {
            Log.i(TAG, "ACTION_SCREEN_OFF received");
        } else if (act.equals( Intent.ACTION_SCREEN_ON )) {
            Log.i(TAG, "ACTION_SCREEN_ON received");
        } else if (act.equals( Intent.ACTION_BATTERY_LOW )) {
            Log.i(TAG, "ACTION_BATTERY_LOW received");
        } else if (act.equals( Intent.ACTION_BATTERY_OKAY )) {
            Log.i(TAG, "ACTION_BATTERY_OKAY received");
        } else if (act.equals( Intent.ACTION_REBOOT )) {
            Log.i(TAG, "ACTION_REBOOT received");
        }

    }
}
