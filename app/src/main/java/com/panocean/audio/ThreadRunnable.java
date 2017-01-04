package com.panocean.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.media.AudioManager;
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
    private final int A2DP_SINK_PROFILE = 10;
    private int a2dp_state = 0;

    public ThreadRunnable(Context context, int n) {
        id = n;
        System.out.println("创建线程 " + id);
        stop = true;

        Log.d(TAG," ThreadRunnable is starting!!!");
        mContext = context;
        mRecordPlay = new RecordPlay();
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Log.d(TAG," ThreadRunnable has start!!!");
    }

    @Override
    public void run() {
        while(stop) {
            // TODO
            //try {
            //    Thread.sleep(1);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}

            // heartbeat
            //Log.d(TAG, "线程 " + id + " : 计数 " + count);
            count++;

            a2dp_state = mBluetoothAdapter.getProfileConnectionState(A2DP_SINK_PROFILE);
            //Log.d(TAG, "a2dp state: " + a2dp_state);

            if (a2dp_state==2) {
                if (mRecordPlay.getWorkingState()==false) {
                    mRecordPlay.instantplay();
                    //try {Thread.sleep(200);} catch (InterruptedException e) {}
                    //mRecordPlay.stopInstantplay();
                    //try {Thread.sleep(200);} catch (InterruptedException e) {}
                    //mRecordPlay.instantplay();
                }
            }else{
                if (mRecordPlay.getWorkingState()==true) {
                    mRecordPlay.stopInstantplay();
                }
            }
        }

        if (mRecordPlay.getWorkingState()==true) {
            mRecordPlay.stopInstantplay();
        }
    }

    public void stopThreadRunnable() {
        stop = false;
    }
}
