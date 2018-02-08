package com.example.myapplication.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by DVO on 2017/7/21 0021.
 */

public abstract class CommonLGVAdapter<T> extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private List<T> mDatas;
    private final int mItemLayoutId;

    protected CommonLGVAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LGViewHolder LGViewHolder = getViewHolder(position, convertView,
                parent);
        convert(LGViewHolder, getItem(position));
        return LGViewHolder.getConvertView();

    }

    public abstract void convert(LGViewHolder helper, T item);

    private LGViewHolder getViewHolder(int position, View convertView,
                                       ViewGroup parent) {
        return LGViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }
}
