package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.myapplication.api.Api;
import com.example.myapplication.bean.Bean;
import com.example.myapplication.helper.CommonLGVAdapter;
import com.example.myapplication.helper.LGViewHolder;
import com.example.myapplication.helper.LoadMoreListView;
import com.example.myapplication.utils.StatusBarUtil;
import com.example.myapplication.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private long firstTime = 0;

    @BindView(R.id.listView)
    LoadMoreListView listView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private List<Bean.ResultsBean> results;
    private int index = 1;
    private SoundPool mSoundPool;
    private int mSoundId;
    private CommonLGVAdapter<Bean.ResultsBean> mAdapter;

    private float mFirstY, mCurrentY, mLastDeltaY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        //        mSoundPool = new SoundPool.Builder().build();   Api 21
        mSoundId = mSoundPool.load(this, R.raw.click, 1);

        StatusBarUtil.setColorNoTranslucent(this, Color.parseColor("#454545"));
        setSupportActionBar(mToolbar);
        initView();
        setListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

       /* listView.setOnTouchListener((v, event) -> {
            final float y = event.getY();
            float translationY = mToolbar.getTranslationY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mFirstY = y;
                    mCurrentY = mFirstY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float mDeltaY = y - mCurrentY;
                    float newTansY = translationY + mDeltaY;
                    if (newTansY <= 0 && newTansY >= -mToolbar.getHeight()) {
                        mToolbar.setTranslationY(newTansY);
                    }
                    mCurrentY = y;
                    mLastDeltaY = mDeltaY;
                    break;
                case MotionEvent.ACTION_UP:
                    ObjectAnimator animator = null;
                    if (mLastDeltaY < 0) {
                        animator = ObjectAnimator.ofFloat(mToolbar, "translationY", mToolbar.getTranslationY(), -mToolbar
                        .getHeight());
                    } else {
                        animator = ObjectAnimator.ofFloat(mToolbar, "translationY", mToolbar.getTranslationY(), 0);
                    }
                    animator.setDuration(100);
                    animator.setInterpolator(AnimationUtils.loadInterpolator(MainActivity.this, android.R.interpolator.linear));
                    animator.start();
                    break;
            }
            return false;
        });*/

        listView.setOnLoadMoreListener(this::loadMore);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ViewPagerActivity.class);
            intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) results);
            intent.putExtra("position", position);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    private void initView() {
        /*View header = new View(this);
        header.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mToolbar.getHeight()));
        listView.addHeaderView(header);*/

        Api.getInstance().service.getMsg(10, index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(bean -> results = bean.getResults())
                .subscribe(this::setAdapter
                        , throwable -> ToastUtil.showToast(MainActivity.this, "网络错误"));
    }

    private void setAdapter(List<Bean.ResultsBean> results) {
        mAdapter = new CommonLGVAdapter<Bean.ResultsBean>(this, results, R.layout.listview_adapter) {
            @Override
            public void convert(final LGViewHolder helper, Bean.ResultsBean item) {
                helper.setImageByUrl(R.id.item_image, item.getUrl());
                helper.setText(R.id.item_text, item.getCreatedAt());
            }
        };
        listView.setAdapter(mAdapter);
    }

    private void loadMore() {
        Api.getInstance().service.getMsg(10, ++index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bean -> {
                    results.addAll(bean.getResults());
                    mAdapter.notifyDataSetChanged();
                    listView.setLoadCompleted();
                });
    }

    @OnClick({R.id.fab})
    public void loadPic(View view) {
        switch (view.getId()) {
            case R.id.fab:
                mSoundPool.play(mSoundId, 1, 1, 1, 0, 0.5f);
                listView.setSelection(0);
                break;
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

        mSoundPool.release();
        ButterKnife.bind(this).unbind();
    }
}
