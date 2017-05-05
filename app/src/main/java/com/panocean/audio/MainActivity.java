package com.panocean.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "A2DPService";
    private Audio mAudio;
    private TextView mTextViewStatus;
    private IA2DPService mIA2DPService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudio=new Audio();

        mTextViewStatus = (TextView) findViewById(R.id.textView_status);
        mTextViewStatus.setText("按键点击状态");


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String str = telephonyManager.getDeviceId();
        //String str = Integer.toString(Build.VERSION.SDK_INT);
        mTextViewStatus.setText(str);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                mAudio.startRecord();
                mTextViewStatus.setText("录音键已按");
                break;
            case R.id.btn_stop_record:
                mAudio.stopRecord();
                mTextViewStatus.setText("停止录音键已按");
                break;
            case R.id.btn_play:
                mAudio.play();
                mTextViewStatus.setText("播放键已按");
                break;
            case R.id.btn_stop_play:
                mAudio.stopPlay();
                mTextViewStatus.setText("停止播放键已按");
                break;
            case R.id.btn_instantplay:
                mAudio.instantplay();
                mTextViewStatus.setText("即时播放键已按");
                break;
            case R.id.btn_stop_instantplay:
                mAudio.stopInstantplay();
                mTextViewStatus.setText("停止即时播放键已按");
                break;
            case R.id.start_service:
                Log.d(TAG," MainActivity start service");
                Intent intent = new Intent();
                //intent.setAction("com.panocean.audio.IA2DPService");
                //intent.setPackage("com.panocean.audio");
                intent.setClass(this,A2DPService.class);

                bindService(intent, serConn, Context.BIND_AUTO_CREATE);
                mTextViewStatus.setText("启动A2DP服务键已按");
                break;
            case R.id.stop_service:
                Log.d(TAG,"MainActivity stop service");
                unbindService(serConn);
                mTextViewStatus.setText("停止A2DP服务键已按");
                break;

        }
    }

    private ServiceConnection serConn = new ServiceConnection() {
        // 此方法在系统建立服务连接时调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected() called");
            mIA2DPService = IA2DPService.Stub.asInterface(service);
            try {
                Log.d(TAG, "A2DPService version: " + mIA2DPService.getVersion());
            }catch (RemoteException e){
                Log.e(TAG, e.getMessage(), e);
            }
        }

        // 此方法在销毁服务连接时调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected() called");
            mIA2DPService = null;
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy");

        //android.os.Process.killProcess(android.os.Process.myPid());
    }
}


