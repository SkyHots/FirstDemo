package com.example.myapplication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.api.Api;
import com.example.myapplication.bean.Bean;
import com.example.myapplication.helper.CommonLGVAdapter;
import com.example.myapplication.helper.LGViewHolder;
import com.example.myapplication.helper.LoadMoreListView;
import com.example.myapplication.utils.NetUtil;
import com.example.myapplication.utils.StatusBarUtil;
import com.example.myapplication.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.example.myapplication.R.menu.main;

public class MainActivity extends AppCompatActivity {

    private long firstTime = 0;

    @BindView(R.id.listView)
    LoadMoreListView listView;
    @BindView(R.id.btn)
    Button btn;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private List<Bean.ResultsBean> results;
    private int index = 1;
    private SoundPool soundPool;
    private int load;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPool = new SoundPool.Builder().build();
        load = soundPool.load(this, R.raw.click, 1);

        StatusBarUtil.setColorNoTranslucent(this, Color.parseColor("#FF4081"));
        ActionBar supportActionBar = getSupportActionBar();
        //资源文件转换为Drawable  new ColorDrawable(id) 或者getResources.getDrawable(id)
        supportActionBar.setBackgroundDrawable(getResources().getDrawable(R.color.colorAccent));
        supportActionBar.setTitle("Surprise");
        supportActionBar.show();
        ButterKnife.bind(this);

        initView();

        setListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.go:
                startActivity(new Intent(this, NextActivity.class));
                overridePendingTransition(R.anim.enter, R.anim.exit);
                break;
        }
        return true;
    }

    private void setListener() {
        fab.setOnClickListener(v -> {
            soundPool.play(load, 1, 1, 1, 0, 0.5f);
            listView.setSelection(0);
        });

        listView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                loadMore();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ViewPagerActivity.class);
                intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>)
                        results);
                intent.putExtra("position", position);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initView() {
        Api.getInstance().service.getMsg(10, index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bean -> results = bean.getResults())
                .subscribe(results -> setAdapter(results)
                        , throwable -> ToastUtil.showToast(MainActivity.this, "网络错误"));
        Api.getInstance().service.getMsg(10, index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bean -> results = bean.getResults())
                .subscribe(results -> setAdapter(results)
                        , throwable -> ToastUtil.showToast(MainActivity.this, "网络错误"));
    }

    private void setAdapter(List<Bean.ResultsBean> results) {

        listView.setAdapter(new CommonLGVAdapter<Bean.ResultsBean>(MainActivity.this, results,
                R.layout.listview_adapter) {
            @Override
            public void convert(final LGViewHolder helper, Bean.ResultsBean item) {
                helper.setImageByUrl(R.id.item_image, item.getUrl());
                helper.setText(R.id.item_text, item.getCreatedAt());
            }
        });
    }

    private void loadMore() {
        new Handler().postDelayed(() ->
                Api.getInstance().service.getMsg(10, ++index)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bean -> {
                            results.addAll(bean.getResults());
                            listView.setLoadCompleted();
                        }), 1500);
    }

    @OnClick(R.id.btn)
    public void loadPic() {
        soundPool.play(load, 1, 1, 1, 0, 1);
        if (NetUtil.isNetConnected(this)) {
            if (listView.getAdapter() != null) {
                listView.setSelection(0);
            } else {
                initView();
            }
        } else {
            Toast.makeText(this, "当前无网络连接", Toast.LENGTH_SHORT).show();
            //            listView.setAdapter(null);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - firstTime > 2000) {
                ToastUtil.showToast(this, "再按一次退出程序");
                firstTime = System.currentTimeMillis();
            } else {
                super.onKeyDown(keyCode, event);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.bind(this).unbind();
    }
}
