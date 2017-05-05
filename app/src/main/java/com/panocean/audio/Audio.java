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


public class Audio {
    private final static String TAG = "A2DPService";
    private int recBufSize = 0;
    private int playBufSize = 0;
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean blnRecord = false;
    private boolean blnPlay = false;
    private boolean blnInstantPlay = false;
    private Thread threadRecord;
    private ThreadAudioTrack threadAudioTrack;
    private static final String AUDIO_RECORDER_FOLDER = "audioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final String AUDIO_RECORDER_FILE = "session.wav";
    private static final int RECORDER_BPP = 16;

    public void startRecord()
    {
        if(blnRecord==false&&blnInstantPlay==false) {
            recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz, channelConfig, encodingBitrate, recBufSize);
            audioRecord.startRecording();
            blnRecord = true;
            threadRecord = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeAudioDataToFile();
                }
            });
            threadRecord.start();
        }
    }
    public void stopRecord()
    {
        if(audioRecord!=null )
        {
            blnRecord = false;
            audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            threadRecord = null;
        }
        copyWaveFile(getTempFilename(),getFileName());
        deleteTempFile();
    }

    public void play(){
        threadAudioTrack = new ThreadAudioTrack();
        threadAudioTrack.init();
        threadAudioTrack.start();
    }

    public void stopPlay(){
        if(threadAudioTrack!=null)
        threadAudioTrack.free();
        threadAudioTrack = null;
    }
    public void instantplay(){
        if(blnRecord==false&&blnInstantPlay==false){
            recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);
            playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                    channelConfig, encodingBitrate);

            try {
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRateInHz, channelConfig, encodingBitrate, recBufSize);
            }catch (Exception e) {
                e.printStackTrace();
            }

            try {
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                        channelConfig, encodingBitrate, playBufSize, AudioTrack.MODE_STREAM);
            }catch (Exception e){
                e.printStackTrace();
            }
            blnInstantPlay = true;
            new ThreadInstantPlay().start();
        }
    }
    public void stopInstantplay(){
        blnInstantPlay = false;
    }

    private void deleteTempFile()
    {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void writeAudioDataToFile()
    {
        byte[] bs = new byte[recBufSize];
        String fileName = getTempFilename();
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(fileName);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        int line = 0;
        if(fos!=null)
        {
            while(blnRecord)
            {
                line = audioRecord.read(bs, 0, recBufSize);
                if(line!=AudioRecord.ERROR_INVALID_OPERATION)
                {
                    try
                    {
                        fos.write(bs);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getTempFilename()
    {
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath, AUDIO_RECORDER_FOLDER);
        if(!file.exists())
        {
            file.mkdirs();
        }
        File tmpFile = new File(filePath, AUDIO_RECORDER_TEMP_FILE);
        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
        return (file.getPath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private String getFileName()
    {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(filePath, AUDIO_RECORDER_FOLDER);
        if(file.exists())
        {
            file.delete();
        }
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_FILE);
    }

    private void copyWaveFile(String inFilename,String outFilename)
    {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long sampleRate = sampleRateInHz;
        int channels = 2;
        long byteRate = RECORDER_BPP * sampleRateInHz * channels/8;
        byte[] data = new byte[recBufSize];
        try
        {
            fis = new FileInputStream(inFilename);
            fos = new FileOutputStream(outFilename);
            totalAudioLen = fis.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(fos, totalAudioLen, totalDataLen, sampleRate,
                    channels, byteRate);
            while(fis.read(data)!=-1)
            {
                fos.write(data);
            }
            fis.close();
            fos.close();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * @param fos
     * @param totalAudioLen
     * @param totalDataLen
    //   * @param longSampleRate
     * @param channels
     * @param byteRate
     * @throws IOException
     */
    private void writeWaveFileHeader(FileOutputStream fos, long totalAudioLen,
                                     long totalDataLen, long sampleRate, int channels,
                                     long byteRate) throws IOException
    {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        fos.write(header, 0, 44);
    }

    class ThreadAudioTrack extends Thread
    {
        byte[] bs;

        File file;
        FileInputStream fis;

        /**
         * 锟斤拷始锟斤拷AudioTrack
         */
        public void init()
        {
            file = new File("/sdcard/" + AUDIO_RECORDER_FOLDER +"/",
                    AUDIO_RECORDER_FILE);
            try
            {
                file.createNewFile();
                fis = new FileInputStream(file);

                blnPlay = true;

                playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                        channelConfig, encodingBitrate);

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                        channelConfig, encodingBitrate, playBufSize, AudioTrack.MODE_STREAM);

                bs = new byte[playBufSize];
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void free()
        {
            blnPlay = false;
        }

        @Override
        public void run() {
            audioTrack.play();
            Log.d(TAG, "ThreadAudioTrack enter ...");
            while(blnPlay)
            {
                try
                {

                    int line = fis.read(bs, 0, recBufSize);

                    if(line==-1)
                    {
                        blnPlay = false;
                  //      handler.sendMessage(new Message());
                        return;
                    }
                    byte[] tmpBuf = new byte[line];
                    System.arraycopy(bs, 0, tmpBuf, 0, line);
                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            audioTrack.stop();
            audioTrack = null;
            try
            {
                fis.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Log.d(TAG, "... ThreadAudioTrack exit");
        }
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
            Log.d(TAG, "audioTrack.getPlayState() = " + audioTrack.getPlayState());
            audioTrack = null;


            //audioRecord.stop();
            audioRecord.release();
            Log.d(TAG, "audioRecord.getRecordingState() = " + audioRecord.getRecordingState());
            audioRecord = null;


            Log.d(TAG, "audioRecord release");
            Log.d(TAG, "... ThreadInstantPlay exit");
        }
    }
}
