package com.example.myapplication.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.example.myapplication.R;

/**
 * Created by fupp on 2017/7/22.
 */

public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {

    private View mFootView;
    private int mTotalItemCount;
    private OnLoadMoreListener mLoadMoreListener;
    private boolean mIsLoading = false;

    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mFootView = LayoutInflater.from(context).inflate(R.layout.footview, null);
        setOnScrollListener(this);
    }


    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        // 滑到底部后自动加载，判断listview已经停止滚动并且最后可视的条目等于adapter的条目
        int lastVisibleIndex = listView.getLastVisiblePosition();
        if (!mIsLoading && scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && lastVisibleIndex == mTotalItemCount - 1) {
            mIsLoading = true;
            addFooterView(mFootView);
            if (mLoadMoreListener != null) {
                mLoadMoreListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    public void setLoadCompleted() {
        mIsLoading = false;
        removeFooterView(mFootView);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /*@Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int
            maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, *//*maxOverScrollY*//*400,
                isTouchEvent);
    }*/
}