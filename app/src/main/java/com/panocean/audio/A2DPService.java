package com.panocean.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class A2DPService extends Service {
    private final static String TAG = "A2DPService";

    private Context mContext;
    private Thread mThread;
    private ThreadRunnable mTR;


    public A2DPService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate called");

        mContext = getApplicationContext();


        mTR = new ThreadRunnable(mContext, 110);
        mThread = new Thread(mTR);
        mThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestory() called");
        //MyThread.stopThread();
        mTR.stopThreadRunnable();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.v(TAG, "onStart() called");
    }


    public class A2DPServiceImpl extends IA2DPService.Stub {

        public String getVersion() throws RemoteException {
            //Log.v(TAG, "getVersion()");
            return "V1.23";
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.v(TAG, "onBind() called");
        return (IBinder) new A2DPServiceImpl();
    }
}
