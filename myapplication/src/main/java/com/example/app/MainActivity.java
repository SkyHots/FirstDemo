package com.example.app;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.app.api.Api;
import com.example.app.bean.Bean;
import com.example.app.utils.StatusBarUtil;
import com.example.app.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private long firstTime = 0;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private List<Bean.ResultsBean> mResultsBeans = new ArrayList<>();
    private int pageNum = 1;
    private static final int PAGE_SIZE = 10;

    private SoundPool mSoundPool;
    private int mSoundId;
    private BaseQuickAdapter mAdapter;

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
        initData();
        setListener();
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go) {
            startActivity(new Intent(this, NextActivity.class));
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
        return true;
    }*/

    private void setListener() {
        mAdapter.setOnLoadMoreListener(this::loadMore, mRecyclerView);

        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(MainActivity.this, ViewPagerActivity.class);
            intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) mResultsBeans);
            intent.putExtra("position", position);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    private void initView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        mAdapter = new BaseQuickAdapter<Bean.ResultsBean, BaseViewHolder>(R.layout.item_rv_main, mResultsBeans) {

            @Override
            protected void convert(BaseViewHolder helper, Bean.ResultsBean item) {
                Glide.with(MainActivity.this).load(item.getUrl()).apply(new RequestOptions().centerCrop()).into(((ImageView) helper.getView(R.id.item_image)));
                helper.setText(R.id.item_text, item.getCreatedAt());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {
        Api.getInstance().service.getMsg(PAGE_SIZE, pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bean -> {
                            mResultsBeans.addAll(bean.getResults());
                            mAdapter.notifyDataSetChanged();
                        }
                        , throwable -> ToastUtil.showToast(MainActivity.this, "网络错误"));
    }

    private void loadMore() {
        Api.getInstance().service.getMsg(PAGE_SIZE, ++pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bean -> {
                    if (bean.getResults() == null || bean.getResults().size() == 0) {
                        mAdapter.loadMoreEnd();
                        return;
                    }
                    mResultsBeans.addAll(bean.getResults());
                    mAdapter.notifyDataSetChanged();
                    mAdapter.loadMoreComplete();
                });
    }

    @OnClick({R.id.fab})
    public void loadPic(View view) {
        if (view.getId() == R.id.fab) {
            mSoundPool.play(mSoundId, 1, 1, 1, 0, 0.5f);
            mRecyclerView.scrollToPosition(0);
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
