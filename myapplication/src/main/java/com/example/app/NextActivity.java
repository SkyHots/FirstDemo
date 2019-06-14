package com.example.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.bean.MusicObject;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NextActivity extends AppCompatActivity implements ServiceConnection {

    private ListView mListView;
    private File[] musics;
    private List<String> musicName = new ArrayList<>();
    private MyMediaPlayerService myMediaPlayService;
    private boolean canStart = true;
    private ImageButton mImageButton;
    private TextView mTV_current, mTV_total;

    private int duration = 0;
    public SeekBar mSeekBar;
    private MyArrayAdapter myArrayAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mListView = (ListView) findViewById(R.id.lv_listTable);
        mImageButton = (ImageButton) findViewById(R.id.pause);
        mTV_current = (TextView) findViewById(R.id.txt_current);
        mTV_total = (TextView) findViewById(R.id.txt_total);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar_musicProgress);
        initData();
        setData();
        setListener();
        //绑定服务
        Intent intent = new Intent(this, MyMediaPlayerService.class);
        intent.putExtra("musics", new MusicObject(musics));
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        overridePendingTransition(R.anim.activiyt_out_enter, R.anim.activity_out_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    private void setListener() {
        //设置点击列表播放音乐功能
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                myArrayAdapter.changeSelected(position);
                //发送广播给Service提示选中的歌曲的位置
                Intent intent = new Intent("value");
                intent.putExtra("position", position);
                sendBroadcast(intent);
                mImageButton.setImageResource(R.mipmap.pause);
                canStart = false;
                //每隔一秒获取本首音乐时长并设置在右边位置
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDuration();
                        getCurrentDuration();
                    }
                },1000);
            }
        });
    }

    private void setData() {
        myArrayAdapter = new MyArrayAdapter();
        mListView.setAdapter(myArrayAdapter);
    }

    private void initData() {
        //获取SDCard根目录中以.mp3为后缀的文件
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Music");
        musics = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith(".mp3")) {
                    String[] split = file.getName().split("-");
                    musicName.add(split[0] + " - " + split[1]);
                    return true;
                }
                return false;
            }
        });
    }

    //获取当前音乐播放的时长添加到左边textview
    private void getCurrentDuration() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int currentDuration = myMediaPlayService.getCurrentDuration();
                    mSeekBar.setProgress(currentDuration);
                    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUse) {
                            if (fromUse) {
                                Intent intent = new Intent("value");
                                intent.putExtra("progress", progress);
                                sendBroadcast(intent);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            myMediaPlayService.pauseMusic();
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            myMediaPlayService.mediaPlayer.seekTo(seekBar.getProgress());
                            mSeekBar.setProgress(seekBar.getProgress());
                            myMediaPlayService.startMusic();
                        }
                    });
                    final String currentTime = new SimpleDateFormat("mm:ss").format(currentDuration);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTV_current.setText(currentTime);
                        }
                    });
                }
            }
        }).start();
    }

    //获取当前音乐总时长添加到右边textview
    private void getDuration() {
        duration = myMediaPlayService.getDuration();
        mSeekBar.setMax(duration);
        mTV_total.setText(new SimpleDateFormat("mm:ss").format(duration));
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                myMediaPlayService.lastSong();
                getDuration();
                getCurrentDuration();
                mImageButton.setImageResource(R.mipmap.pause);
                break;
            case R.id.pause:
                if (canStart) {
                    myMediaPlayService.startMusic();
                    getDuration();
                    getCurrentDuration();
                    canStart = false;
                    mImageButton.setImageResource(R.mipmap.pause);
                } else {
                    myMediaPlayService.pauseMusic();
                    getDuration();
                    getCurrentDuration();
                    canStart = true;
                    mImageButton.setImageResource(R.mipmap.begin);
                }
                break;
            case R.id.stop:
                myMediaPlayService.stopMusic();
                getDuration();
                getCurrentDuration();
                mImageButton.setImageResource(R.mipmap.begin);
                canStart = true;
                break;
            case R.id.next:
                myMediaPlayService.nextSong();
                getDuration();
                getCurrentDuration();
                mImageButton.setImageResource(R.mipmap.pause);
                break;
            case R.id.title_bar_menu_btn:
                Toast.makeText(this, "点毛点！点毛点！点毛点！点毛点！", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //当服连接成功之后获取服务的一个实例
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MyMediaPlayerService.MyMediaPlayerServiceInner inner = (MyMediaPlayerService.MyMediaPlayerServiceInner) iBinder;
        myMediaPlayService = inner.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    class MyArrayAdapter extends BaseAdapter {

        private int mSelect = -1;

        public void changeSelected(int positon) { //刷新方法
            if (positon != mSelect) {
                mSelect = positon;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return musicName.size() == 0 ? 0 : musicName.size();
        }

        @Override
        public Object getItem(int position) {
            return musicName.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(NextActivity.this).inflate(R.layout.item_music,
                        null);
                holder = new ViewHolder();
                holder.textView = ((TextView) convertView.findViewById(R.id.tv_musicName));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(musicName.get(position));
            if (mSelect == position) {
                convertView.setBackgroundResource(R.color.colorGray);  //选中项背景
            } else {
                convertView.setBackgroundResource(android.R.color.transparent);  //其他项背景
            }
            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }
    }
}