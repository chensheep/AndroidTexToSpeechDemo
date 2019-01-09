package com.fan.ttsdemo.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fan.ttsdemo.utils.HttpUtils;

public class TtsService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "TtsService";


    public static final String ACTION_TTS_SERVICE = "com.fan.ttsdemo.tts";

    public static final String EXTRA_PLAY_MESSAGE = "play-message";

    private HandlerThread mJobThread;
    private Handler mJober;

    MediaPlayer mMediaPlayer = null;

    private static Map<Integer, String> convertMsgToStringMap = Collections.unmodifiableMap(new HashMap<Integer, String>(10, 1.0f) {
        {
            put(MSG_C_ACTION_PLAY_VOICE_WITH_TEXT, "MSG_C_ACTION_PLAY_VOICE_WITH_TEXT");
        }
    });

    private static final int MSG_C_ACTION_PLAY_VOICE_WITH_TEXT = 1;

    private static String convertMsgToString(int msg) {
        return convertMsgToStringMap.get(msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreat() ");
        mJobThread = new HandlerThread("DemoServiceJobThread");
        mJobThread.start();
        mJober = new Handler(mJobThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "Got " + convertMsgToString(msg.what));
                switch (msg.what) {
                    case MSG_C_ACTION_PLAY_VOICE_WITH_TEXT:
                        String text = (String) msg.obj;
                        playVoiceWithText_l(text);
                        break;
                    default:
                        break;
                }
            }
        };

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if (ACTION_TTS_SERVICE.equals(intent.getAction())) {
            Log.v(TAG, "got action=" + ACTION_TTS_SERVICE);
            try {
                if (intent.hasExtra(EXTRA_PLAY_MESSAGE)) {
                    Log.v(TAG, "got extra play-message=" + intent.getStringExtra(EXTRA_PLAY_MESSAGE));
                    sendPlayVoiceWithTextMessage(intent.getStringExtra(EXTRA_PLAY_MESSAGE));
                }
            } catch (Exception e) {
                Log.e(TAG, "[OSC] got exception ", e);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestory()");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mJober.removeCallbacksAndMessages(null);
        if (mJobThread.quit()) {
            Log.d(TAG, "mJobThread quit success");
        }
        mJober = null;
        mJobThread = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendPlayVoiceWithTextMessage(String text) {
        mJober.obtainMessage(MSG_C_ACTION_PLAY_VOICE_WITH_TEXT, text).sendToTarget();
    }

    private void playVoiceWithText_l(String text) {
        String url = HttpUtils.post("https://iot.cht.com.tw/api/tts/ch/synthesis", "inputText=" + text);
        if (url == null) {
            url = HttpUtils.postWithSkipHttpsCertAndCheckHostname("https://iot.cht.com.tw/api/tts/ch/synthesis", "inputText=" + text);
        }
        try {
            JSONObject jsonObject = new JSONObject(url);
            playVoiceWithUrl_l(jsonObject.getString("file"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void playVoiceWithUrl_l(String url) {
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }
}
