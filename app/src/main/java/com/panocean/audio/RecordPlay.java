package com.panocean.audio;

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
    private int recBufSize = 0;
    private int playBufSize = 0;
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private boolean blnInstantPlay = false;



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

    class ThreadInstantPlay extends Thread
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
                audioTrack.write(tmpBuf, 0, tmpBuf.length);
            }


            //audioTrack.stop();
            audioTrack.pause();
            Log.d(TAG, "audioTrack pause");
            /*try {
                Thread.sleep(100);
            }catch (InterruptedException e) {
            }*/
            audioTrack.flush();
            Log.d(TAG, "audioTrack flush");
            /*try {
                Thread.sleep(100);
            }catch (InterruptedException e) {
            }*/
            audioTrack.release();
            Log.d(TAG, "audioTrack release");
            /*try {
                Thread.sleep(100);
            }catch (InterruptedException e) {
            }*/
            blnInstantPlay = false;
            //Log.d(TAG, "audioTrack.getPlayState() = " + audioTrack.getPlayState());
            audioTrack = null;


            //audioRecord.stop();
            audioRecord.release();
            //Log.d(TAG, "audioRecord.getRecordingState() = " + audioRecord.getRecordingState());
            audioRecord = null;


            Log.d(TAG, "audioRecord release");
            Log.d(TAG, "... ThreadInstantPlay exit");
        }
    }
}
