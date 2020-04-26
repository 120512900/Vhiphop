package com.example.vhiphop.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vhiphop.R;

/*
 *作者：created by 影子 on 2020/4/22 14:37
 *邮箱：1723928492@qq.com
 */
public class PullLoadRecyclerView extends LinearLayout {
    private Context mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsRefresh = false;//是否刷新
    private boolean mIsLoadMore = false;//是否加载更多
    private RecyclerView mRecyclerView;
    private View mFootView;
    private AnimationDrawable mAnimationDrawable;
    private OnPullLoadMoreListener mOnPullLoadMoreListener;

    public PullLoadRecyclerView(Context context) {
        super(context);
        initView(context);
    }

    public PullLoadRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PullLoadRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
       View view =  LayoutInflater.from(mContext).inflate(R.layout.pull_loadmore_layout,null);
       mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
       //设置刷新时控件的颜色渐变
       mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark),getResources().getColor(android.R.color.holo_blue_dark),getResources().getColor(android.R.color.holo_orange_dark));
       mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayoutOnRefresh());

       //处理RecyclerView
       mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
       mRecyclerView.setHasFixedSize(true);//设置固定大小
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置默认动画
        mRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mIsRefresh || mIsLoadMore;
            }
        });
        mRecyclerView.setVerticalScrollBarEnabled(false);//设置滚动条隐藏
        mRecyclerView.addOnScrollListener(new RecyclerViewOnScroll());
        mFootView = view.findViewById(R.id.footer_view);
        ImageView imageView =(ImageView) mFootView.findViewById(R.id.iv_load_img);
        imageView.setBackgroundResource(R.drawable.vhiphop_loading);//震动化一帧一帧显示
        mAnimationDrawable = (AnimationDrawable) imageView.getBackground();

        TextView textView = (TextView)view.findViewById(R.id.tv_load_text);
        mFootView.setVisibility(View.GONE);
        this.addView(view);//view 包含swipeRefreshLayout, RecyclerView,FootView

    }

    //外部可以设置recyclerview的列数
    public void setGridLayout(int spanCount){
        GridLayoutManager manger = new GridLayoutManager(mContext,spanCount);
        manger.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manger);
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        if(adapter != null){
            mRecyclerView.setAdapter(adapter);
        }
    }
    class SwipeRefreshLayoutOnRefresh implements SwipeRefreshLayout.OnRefreshListener{

        @Override
        public void onRefresh() {
            if(!mIsRefresh){
                mIsRefresh = true;
                refreshData();
            }
        }
    }

    class RecyclerViewOnScroll extends RecyclerView.OnScrollListener{
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {//滑动过后
            super.onScrolled(recyclerView, dx, dy);
            int firstItem = 0;
            int lastItem = 0;
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            int totalCount = manager.getItemCount();
            if(manager instanceof GridLayoutManager){//是网格的
                GridLayoutManager gridLayoutManager = (GridLayoutManager)manager;
                //第一个完全可见的item
                firstItem = gridLayoutManager.findFirstCompletelyVisibleItemPosition();//完全你可以显示
                //最后一个可见的item
                lastItem = gridLayoutManager.findLastCompletelyVisibleItemPosition();
                if(firstItem == 0 || firstItem == RecyclerView.NO_POSITION){
                    lastItem = gridLayoutManager.findLastVisibleItemPosition();
                }
            }
            //什么时候触发上拉加载更多
            if(mSwipeRefreshLayout.isEnabled()){//可以下拉的时候
                mSwipeRefreshLayout.setEnabled(true);
            }else{
                mSwipeRefreshLayout.setEnabled(false);
            }
            //1.加载更多是false
            //2.totalCount - 1 == lastItem
            //3.mSwipeRefreshLayout可以用
            //4.不是处于下拉刷新状态
            //5.偏移量dx>0 或 dy>0
            if(!mIsLoadMore
               && totalCount == lastItem
               && mSwipeRefreshLayout.isEnabled()
               && !mIsRefresh
               && (dx > 0 || dy>0)){//dx dy偏移量
                mIsLoadMore = true;
                loadMoreData();
            }
        }
    }

    private void loadMoreData() {
        if(mOnPullLoadMoreListener != null){
            mFootView.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator())
            .setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mFootView.setVisibility(View.VISIBLE);
                    mAnimationDrawable.start();
                }
            }).start();
            invalidate();
            mOnPullLoadMoreListener.loadMore();
        }
    }

    private void refreshData() {
        if(mOnPullLoadMoreListener != null){
            mOnPullLoadMoreListener.reRresh();
        }
    }
   //设置刷新完毕
    public void setRefreshCompleted(){//是否完全刷新完
        mIsRefresh = false;
        setRefreshing(false);
    }
   //设置是否正在刷新数据
    private void setRefreshing(boolean isRefreshing) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(isRefreshing);
            }
        });
    }

    public void setLoadMoreCompleted(){
        mIsLoadMore = false;
        mIsRefresh = false;
        setRefreshing(false);
        mFootView.animate().translationY(mFootView.getHeight()).setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300).start();
    }
    public interface OnPullLoadMoreListener{//接口
        void reRresh();
        void loadMore();
    }

    public void setOnPullLoadMoreListener(OnPullLoadMoreListener listener){
        mOnPullLoadMoreListener = listener;
    }
}
