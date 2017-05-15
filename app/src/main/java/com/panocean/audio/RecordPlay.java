package com.panocean.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chan on 1/3/17.
 */
public class RecordPlay {

    private final static String TAG = "A2DPService";
    private Context mContext;
    private int recBufSize = 0;
    private int playBufSize = 0;
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private int a2dp_state = 0;

    private boolean blnInstantPlay = false;

    public RecordPlay(Context context) {

        mContext = context;

        mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public void instantplay(){

            recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);
            playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz, channelConfig, encodingBitrate, recBufSize);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                    channelConfig, encodingBitrate, playBufSize, AudioTrack.MODE_STREAM);
            blnInstantPlay = true;
            new ThreadInstantPlay().start();


    }
    public void stopInstantplay(){

        blnInstantPlay = false;
    }

    public boolean getWorkingState(){
        return blnInstantPlay;
    }

    private void setMuteEnabled(boolean enabled){
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, enabled);
    }


    /*class ThreadInstantPlay extends Thread
    {
        @Override
        public void run()
        {
            byte[] bsBuffer = new byte[recBufSize];
            if(!(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
                Log.d(TAG, "audioRecord start recording");
                audioRecord.startRecording();
            }
            if(!(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
                Log.d(TAG, "audioTrack start playing");
                audioTrack.play();
            }

            Log.d(TAG, "ThreadInstantPlay enter ...");
            while(blnInstantPlay)
            {
                int line = audioRecord.read(bsBuffer, 0, recBufSize);
                byte[] tmpBuf = new byte[line];
                System.arraycopy(bsBuffer, 0, tmpBuf, 0, line);
                if (blnInstantPlay) audioTrack.write(tmpBuf, 0, tmpBuf.length);
            }


            // mute
            //setMuteEnabled(true);
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            //int vol_bak = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.ADJUST_MUTE);
            //try {Thread.sleep(1000);}catch (InterruptedException e) {}
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            //try {Thread.sleep(1000);}catch (InterruptedException e) {}



            //audioTrack.stop();
            audioTrack.pause();
            Log.d(TAG, "audioTrack pause");
            audioTrack.flush();
            Log.d(TAG, "audioTrack flush");
            audioTrack.release();
            Log.d(TAG, "audioTrack release");
            blnInstantPlay = false;
            //Log.d(TAG, "audioTrack.getPlayState() = " + audioTrack.getPlayState());
            audioTrack = null;


            //audioRecord.stop();
            audioRecord.release();
            //Log.d(TAG, "audioRecord.getRecordingState() = " + audioRecord.getRecordingState());
            audioRecord = null;

            //try {Thread.sleep(1000);}catch (InterruptedException e) {}

            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol_bak, AudioManager.ADJUST_MUTE);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            //setMuteEnabled(false);


            Log.d(TAG, "audioRecord release");
            Log.d(TAG, "... ThreadInstantPlay exit");
        }
    }*/
    class ThreadInstantPlay extends Thread
    {
        @Override
        public void run()
        {
            byte[] bsBuffer = new byte[recBufSize];
            Producer p = new Producer();
            int readCnt;

            if(!(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
                Log.d(TAG, "audioRecord start recording");
                audioRecord.startRecording();
            }
            if(!(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
                Log.d(TAG, "audioTrack start playing");
                audioTrack.play();
            }

            Log.d(TAG, "ThreadInstantPlay enter ......");
            p.Producer_start();
            while(blnInstantPlay)
            {
                //readCnt = audioRecord.read(bsBuffer, 0, recBufSize);
                //Log.d(TAG, " ********** read " + readCnt);
                //audioTrack.write(tmpBuf, 0, line);
                //p.cr.addToMsgBuf(bsBuffer);

                p.cr.produceToBuf();
            }
            p.Producer_stop();

            // mute
            //setMuteEnabled(true);
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            //int vol_bak = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, AudioManager.ADJUST_MUTE);
            //try {Thread.sleep(1000);}catch (InterruptedException e) {}
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            //try {Thread.sleep(100);}catch (InterruptedException e) {}

            //audioTrack.stop();
            audioTrack.pause();
            Log.d(TAG, "audioTrack pause");
            audioTrack.flush();
            Log.d(TAG, "audioTrack flush");
            audioTrack.release();
            Log.d(TAG, "audioTrack release");
            blnInstantPlay = false;
            //Log.d(TAG, "audioTrack.getPlayState() = " + audioTrack.getPlayState());
            audioTrack = null;


            //audioRecord.stop();
            audioRecord.release();
            //Log.d(TAG, "audioRecord.getRecordingState() = " + audioRecord.getRecordingState());
            audioRecord = null;

            //try {Thread.sleep(1000);}catch (InterruptedException e) {}

            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol_bak, AudioManager.ADJUST_MUTE);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            //setMuteEnabled(false);


            Log.d(TAG, "audioRecord release");
            Log.d(TAG, "... ThreadInstantPlay exit");
        }

        public class Producer {
            public Consumer cr;
            public static final int UNIT = 8192;

            public Producer() {

                cr = new Consumer();
            }

            public void Producer_start() {
                cr.start();
            }

            public void Producer_stop() {
                cr.stop_pc = true;
                try { Thread.sleep(100); } catch (InterruptedException e) { }
            }

            class Consumer extends Thread {
                int bufferSize = UNIT*200;
                private byte[] buffer = new byte[bufferSize];
                public int producer_index;
                public boolean producer_overflow;
                public int consumer_index;
                public boolean stop_pc;
                byte[] cBuf;

                public Consumer() {

                    producer_index = 0;
                    producer_overflow = false;
                    consumer_index = 0;
                    stop_pc = false;
                    cBuf = new byte[UNIT];
                }

                public void run() {
                    try { Thread.sleep(200); } catch (InterruptedException e) { }
                    while (!stop_pc) {
                        if ((producer_overflow) || (consumer_index < producer_index)) {
                            System.arraycopy(buffer, consumer_index * UNIT, cBuf, 0, UNIT);
                            //Log.d(TAG, "consumer index ---------- " + consumer_index);
                            consumer_index++;
                            if (consumer_index > 99) {
                                consumer_index = 0;
                                producer_overflow = false;
                            }

                            a2dp_state = mBluetoothAdapter.getProfileConnectionState(11);
                            //Log.d(TAG, " a2dp state: " + a2dp_state);
                            if (2==a2dp_state) audioTrack.write(cBuf, 0, UNIT);
                        } else {
                            try { Thread.sleep(200); } catch (InterruptedException e) { }
                        }
                    }
                }

                public void addToMsgBuf(byte[] buf) {
                    System.arraycopy(buf, 0, buffer, producer_index * UNIT, UNIT);
                    producer_index++;
                    if (producer_index > 99) {
                        producer_overflow = true;
                        producer_index = 0;
                    }

                    //Log.d(TAG, "producer index ++++++++++ " + producer_index);
                }

                public void produceToBuf() {
                    byte[] buf = new byte[recBufSize];
                    int readCnt = audioRecord.read(buf, 0, recBufSize);
                    //Log.d(TAG, " ********** read " + readCnt);
                    addToMsgBuf(buf);
                }
            }
        }
    }
}
