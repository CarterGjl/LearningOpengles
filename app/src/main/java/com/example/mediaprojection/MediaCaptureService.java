package com.example.mediaprojection;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MediaCaptureService extends Service {


    private static final String TAG = "MediaCaptureService";

    public static final String ACTION_ALL = "ALL";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    public static final String EXTRA_ACTION_NAME = "ACTION_NAME";

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    NotificationCompat.Builder _notificationBuilder;
    NotificationManager _notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "ChannelId";
    private static final String NOTIFICATION_CHANNEL_NAME = "Channel";
    private static final String NOTIFICATION_CHANNEL_DESC = "ChannelDescription";
    private static final int NOTIFICATION_ID = 1000;
    private static final String ONGING_NOTIFICATION_TICKER = "RecorderApp";

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    AudioRecord recorder;
    AudioRecord recorderMic;
    private boolean isRecording = false;

    private boolean isMicRecording = false;
    private MediaProjectionManager _mediaProjectionManager;
    private MediaProjection mediaProjection;

    Intent _callingIntent;

    public MediaCaptureService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Call Start foreground with notification
            Intent notificationIntent = new Intent(this, MediaCaptureService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            _notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Starting Service")
                    .setContentText("Starting monitoring service")
                    .setTicker(ONGING_NOTIFICATION_TICKER)
                    .setContentIntent(pendingIntent);
            Notification notification = _notificationBuilder.build();
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            _notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            _notificationManager.createNotificationChannel(channel);
            startForeground(NOTIFICATION_ID, notification);
        }

        _mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _callingIntent = intent;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ALL);
        registerReceiver(_actionReceiver, filter);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecording(Intent intent) {
        //final int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        mediaProjection = _mediaProjectionManager.getMediaProjection(-1, intent);
        startRecording(mediaProjection);
    }

    @TargetApi(29)
    private void startRecording(MediaProjection mediaProjection) {
        AudioPlaybackCaptureConfiguration config =
                new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .build();
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(RECORDER_AUDIO_ENCODING)
                .setSampleRate(RECORDER_SAMPLERATE)
                .setChannelMask(RECORDER_CHANNELS)
                .build();
        recorder = new AudioRecord.Builder()
//                不可以同时设置 AudioPlaybackCaptureConfiguration MediaRecorder.AudioSource.MIC
//              .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BufferElements2Rec * BytesPerElement)
                .setAudioPlaybackCaptureConfig(config)
                .build();


        recorderMic = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BufferElements2Rec * BytesPerElement)
                .build();

        isRecording = true;
        isMicRecording = true;
        recorder.startRecording();
        if (recorderMic != null) {
            recorderMic.startRecording();
        }
        new Thread(() -> {
            if (recorder != null) {
                writeAudioDataToFile();
            }
        }).start();
        new Thread(() -> {
            if (recorderMic != null) {
                writeAudioDataToFileMic();
            }
        }).start();

    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        if (recorder == null) return;
        // Write the output audio in byte
        Log.i(TAG, "Recording started. Computing output file name");
        File sampleDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "/TestRecordingDasa1");
        if (!sampleDir.exists()) {
            boolean mkdirs = sampleDir.mkdirs();
            Log.d(TAG, "writeAudioDataToFile: " + mkdirs);
        }
        String format = SimpleDateFormat.getDateTimeInstance().format(new Date());
        String fileName = "Record-" + format + ".pcm";
        String filePath = sampleDir.getAbsolutePath() + "/" + fileName;
        //String filePath = "/sdcard/voice8K16bitmono.pcm";
        short[] sData = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(sData, 0, BufferElements2Rec);
            Log.i(TAG, "Short wirting to file" + Arrays.toString(sData));
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte[] bData = short2byte(sData);
                if (os != null) {
                    os.write(bData, 0, BufferElements2Rec * BytesPerElement);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "record error:" + e.getMessage());
            }
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, String.format("Recording finished. File saved to '%s'", filePath));
    }

    private void writeAudioDataToFileMic() {
        if (recorderMic == null) return;
        // Write the output audio in byte
        Log.i(TAG, "Recording started. Computing output file name");
        File sampleDir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "/TestRecordingDasa1Mic");
        if (!sampleDir.exists()) {
            boolean mkdirs = sampleDir.mkdirs();
            Log.d(TAG, "writeAudioDataToFileMic: " + mkdirs);
        }
        String format = SimpleDateFormat.getDateTimeInstance().format(new Date());
        String fileName = "Record-" + format + ".pcm";
        String filePath = sampleDir.getAbsolutePath() + "/" + fileName;
        //String filePath = "/sdcard/voice8K16bitmono.pcm";
        short[] sData = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isMicRecording) {
            // gets the voice output from microphone to byte format
            recorderMic.read(sData, 0, BufferElements2Rec);
            Log.i(TAG, "Short wirting to file" + Arrays.toString(sData));
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte[] bData = short2byte(sData);
                if (os != null) {
                    os.write(bData, 0, BufferElements2Rec * BytesPerElement);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "record error:" + e.getMessage());
            }
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, String.format("Recording finished. File saved to '%s'", filePath));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        if (null != recorderMic) {
            isMicRecording = false;
            recorderMic.stop();
            recorderMic.release();
            recorderMic = null;
        }

        if (mediaProjection != null) {
            mediaProjection.stop();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_actionReceiver);
    }

    BroadcastReceiver _actionReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(ACTION_ALL)) {
                String actionName = intent.getStringExtra(EXTRA_ACTION_NAME);
                if (actionName != null && !actionName.isEmpty()) {
                    if (actionName.equalsIgnoreCase(ACTION_START)) {
                        startRecording(_callingIntent);
                    } else if (actionName.equalsIgnoreCase(ACTION_STOP)) {
                        stopRecording();
                    }
                }

            }
        }
    };
}