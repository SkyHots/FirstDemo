package com.example.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.app.bean.Bean;
import com.example.app.utils.ToastUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewPagerActivity extends AppCompatActivity {


    @BindView(R.id.vp)
    ViewPager vp;

    private int index;
    private ArrayList<Bean.ResultsBean> data;
    private List<String> imagePaths;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        ButterKnife.bind(this);

        initData();
        initView();

        int i = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
    }

    private void initData() {
        imagePaths = new ArrayList<>();
        data = getIntent().getParcelableArrayListExtra("data");
        for (Bean.ResultsBean bean : data) {
            imagePaths.add(bean.getUrl());
        }
        //位置
        index = getIntent().getIntExtra("position", 0);
    }

    private void initView() {
        MyAdapter myAdapter = new MyAdapter(this);
        vp.setAdapter(myAdapter);
        vp.setPageMargin(80);
        //设置当前图片位置
        vp.setCurrentItem(index);
    }

    class MyAdapter extends PagerAdapter {

        private LinkedList<View> mViewCache;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public MyAdapter(Context context) {
            super();
            this.mContext = context;
            this.mLayoutInflater = LayoutInflater.from(mContext);
            mViewCache = new LinkedList<>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final ViewHolder mHolder;
            View convertView;
            if (mViewCache.size() == 0) {
                mHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item, null, false);
                mHolder.imageView = convertView.findViewById(R.id.larageImage);
                mHolder.imageView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(ViewPagerActivity.this)
                            .setTitle("保存图片")
                            .setMessage("你要保存照片吗？")
                            .setPositiveButton("确定", (dialog, which) -> saveToSDCard(position))
                            .setNegativeButton("取消", null)
                            .create().show();
                    return true;
                });
                mHolder.imageView.setOnClickListener(v -> ViewPagerActivity.this.onBackPressed());
                convertView.setTag(mHolder);
            } else {
                convertView = mViewCache.removeFirst();
                mHolder = (ViewHolder) convertView.getTag();
            }
            Glide.with(mContext)
                    .load(imagePaths.get(position))
                    .thumbnail(0.2f)
                    .into(mHolder.imageView);
            container.addView(convertView);
            return convertView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View contentView = (View) object;
            container.removeView(contentView);
            mViewCache.add(contentView);
        }

        @Override
        public int getCount() {
            return imagePaths == null ? 0 : imagePaths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        class ViewHolder {
            ImageView imageView;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activiyt_out_enter, R.anim.activity_out_exit);
    }

    /**
     * 保存到手机更目录
     *
     * @param position
     */
    private void saveToSDCard(int position) {
        String url = imagePaths.get(position);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {

                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + url.substring(url.length() - 8));
                if (!file.exists()) {
                    boolean newFile = file.createNewFile();
                    InputStream in = response.body().byteStream();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] buf = new byte[1024 * 8];
                    int len = 0;
                    while ((len = in.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                        bos.flush();
                    }
                    in.close();
                    bos.close();
                    // 最后通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    runOnUiThread(() -> ToastUtil.showToast(ViewPagerActivity.this, "保存成功"));
                } else {
                    runOnUiThread(() -> ToastUtil.showToast(ViewPagerActivity.this, "文件已存在"));
                }
            }
        });
    }


}
