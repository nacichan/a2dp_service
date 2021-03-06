package com.panocean.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * Created by chan on 1/3/17.
 */
public class ThreadRunnable implements Runnable {
    private static final String TAG = "A2DPService";
    private final boolean DBG = true;
    int count=1;
    int id;
    boolean stop = true;
    private Context mContext;
    private AudioManager mAudioManager;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private RecordPlay mRecordPlay;
    private int A2DP_SINK_PROFILE = 0;
    private int a2dp_state = 0;
    private int a2dp_state_pre = 0;

    public ThreadRunnable(Context context, int n) {
        id = n;
        System.out.println("创建线程 " + id);
        stop = true;

        Log.d(TAG," ThreadRunnable is starting!!!");
        mContext = context;
        if (Build.VERSION.SDK_INT<=22) A2DP_SINK_PROFILE = 10; // android5.1 / low
        else if (Build.VERSION.SDK_INT==23) A2DP_SINK_PROFILE = 11; // android 6.0
        else if (Build.VERSION.SDK_INT==24) A2DP_SINK_PROFILE = 11; // android 7.0

        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Log.d(TAG," ThreadRunnable has start!!!");
    }

    @Override
    public void run() {
        int bluetoothAdapterState;
        while(stop) {
            // TODO
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

            // heartbeat
            //Log.d(TAG, "线程 " + id + " : 计数 " + count);
            count++;

            bluetoothAdapterState = mBluetoothAdapter.getState();
            if (bluetoothAdapterState!=BluetoothAdapter.STATE_ON) {
                Log.d(TAG, "BluetoothAdapter is: " + bluetoothAdapterState + ", not on, so continue the loop");

                try {Thread.sleep(4000);} catch (InterruptedException e) {}
                continue;
            }

            a2dp_state = mBluetoothAdapter.getProfileConnectionState(A2DP_SINK_PROFILE);
            //Log.d(TAG, "a2dp state: " + a2dp_state + " a2dp profile: " + A2DP_SINK_PROFILE);

            if (a2dp_state==2) {
                if ((a2dp_state_pre==0)||(a2dp_state_pre==1)) {
                    playSound(mContext);
                }

                iconShowCtl(mContext,true);
            }

            a2dp_state_pre = a2dp_state;
        }

    }

    public void stopThreadRunnable() {
        stop = false;
    }


    public static void iconShowCtl(Context mContext, boolean show) {
        Object service = mContext.getSystemService("statusbar");
        Reflect.on(service).call("setIcon", "bluetooth", R.mipmap.stat_bluetooth_a2dp, 0, "bt connected");
        Reflect.on(service).call("setIconVisibility", "bluetooth", show);
    }

    public void playSound(Context ctx) {
        //Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        //alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(ctx, notification);
        r.play();
    }
}
