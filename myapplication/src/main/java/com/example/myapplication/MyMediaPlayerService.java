package com.example.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.myapplication.bean.MusicObject;

import java.io.File;
import java.io.IOException;

public class MyMediaPlayerService extends Service {

    private File[] musics;
    //设置标志音乐是否停了
    private boolean isStop = true;
    public MediaPlayer mediaPlayer;
    private int position = 0;
    private MyReceiver receiver;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    position++;
                    if (position == musics.length) {
                        position = 0;
                    }

                    onPrepare();
                    isStop = false;
                }
            });
        }
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("value");
        registerReceiver(receiver, intentFilter);
    }

    private void onPrepare() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musics[position].getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMusic() {
        if (isStop) {
            onPrepare();
            isStop = false;
        } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isStop = true;
        }
    }

    public void lastSong() {
        if (position == 0) {
            position = musics.length;
        }
        position--;
        onPrepare();
        isStop = false;
    }

    public void nextSong() {
        if (position == musics.length - 1) {
            position = -1;
        }
        position++;
        onPrepare();
        isStop = false;
    }

    public int getCurrentDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        MusicObject musicObject = (MusicObject) intent.getSerializableExtra("musics");
        musics = musicObject.getMusics();
        return new MyMediaPlayerServiceInner();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    class MyMediaPlayerServiceInner extends Binder {

        MyMediaPlayerService getService() {
            return MyMediaPlayerService.this;
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("position", -1) != -1) {
                position = intent.getIntExtra("position", 0);
                onPrepare();
                isStop = false;
            }
            if (intent.getIntExtra("progress", -2) != -1 && mediaPlayer != null) {
                mediaPlayer.seekTo(intent.getIntExtra("progress", -2));
            }
        }
    }
}
