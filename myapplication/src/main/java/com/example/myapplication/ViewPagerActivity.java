package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.myapplication.bean.Bean;
import com.example.myapplication.utils.AlertDialogUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        ButterKnife.bind(this);

        initData();
        initView();
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
                mHolder.imageView = (ImageView) convertView.findViewById(R.id.larageImage);
                mHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialogUtil.showAlertDialog(ViewPagerActivity.this, R.mipmap.save,
                                "保存图片", "你要保存照片吗？", "确定", "取消", true, new AlertDialogUtil
                                        .DialogClickListener() {
                                    @Override
                                    public void clickPositive() {
                                        saveToSDCard(position);
                                    }
                                    @Override
                                    public void clickNegative() {

                                    }
                                });
                        return true;
                    }
                });
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
     * @param pos
     */
    private void saveToSDCard(int pos) {
        String url = imagePaths.get(pos);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + System
                        .currentTimeMillis() + ".jpg");
                if (!file.exists()) {
                    file.createNewFile();
                }
                InputStream in = response.body().byteStream();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buf = new byte[1024 * 8];
                int len = 0;
                while ((len = in.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                    bos.flush();
                }
                if (in != null) {
                    in.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }

        });
    }


}
