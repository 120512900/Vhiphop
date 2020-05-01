package com.example.vhiphop;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vhiphop.api.SiteApi;
import com.example.vhiphop.api.onGetChannelAlbumListener;
import com.example.vhiphop.base.BaseFragment;
import com.example.vhiphop.model.Album;
import com.example.vhiphop.model.AlbumList;
import com.example.vhiphop.model.Channel;
import com.example.vhiphop.model.EorrorInfo;
import com.example.vhiphop.model.Site;
import com.example.vhiphop.widget.PullLoadRecyclerView;

/*
 *作者：created by 影子 on 2020/4/20 22:16
 *邮箱：1723928492@qq.com
 */
public class DetailListFragment extends BaseFragment {
    private static final String TAG = DetailListFragment.class.getSimpleName();
    private  int mSiteId;
    private  int mChannelId;
    private static final String CHANNEL_ID = "channelid";
    private static final String SITE_ID = "siteid";
    private PullLoadRecyclerView mRecyclerView;
    private TextView mEmptyView;
    private int mColumns;
    private DetailListAdapter mAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());//在主线程
    private static final int REFRESH_DURATION = 1500;//刷数据1500毫秒
    private static final int LOADMORE_DURATION = 3000;//加载一页或者几十个相关数据
    private int pageNo;
    private int pageSize = 30;
    //private Handler mHandler = new Handler(Looper.getMainLooper());//正在加载数据是子线程 UI刷新是主线程

    public DetailListFragment(){//需要空的构造 才不会出错
    }

    public static Fragment newInstance(int siteId,int channId){
        DetailListFragment fragment = new DetailListFragment();
        Bundle bundle = new Bundle();//把值传入fragment里面去 bundle intent传值
        bundle.putInt(SITE_ID,siteId);//把值放入bundle
        bundle.putInt(CHANNEL_ID,channId);
        fragment.setArguments(bundle);//如： 乐视，电视剧页面，fragment需要知道
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mSiteId = getArguments().getInt(SITE_ID);
            mChannelId = getArguments().getInt(CHANNEL_ID);
        }
        pageNo = 0;
        mAdapter = new DetailListAdapter(getActivity(),new Channel(mChannelId,getActivity()));
        loadData();
        if(mSiteId == Site.LETV){//乐视下相关频道2列
            mColumns = 2;
            mAdapter.setColumns(mColumns);
        }
    }

    @Override
    protected void initView() {
        mEmptyView = bindViewId(R.id.tv_empty);
        mEmptyView.setText(getActivity().getResources().getString(R.string.load_more_text));
        mRecyclerView = bindViewId(R.id.pullloadRecyclerView);//拥有刷新和上拉加载更多的特点
        mRecyclerView.setGridLayout(3);//设置3列
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnPullLoadMoreListener(new PullLoadMoreListener());
    }
    private void reRreshData(){
        //TODO 请求接口，加载数据
}
    private void loadData(){
        // 请求接口，加载更多数据
        pageNo ++;
        SiteApi.onGetChannelAlbums(getActivity(), pageNo, pageSize, mSiteId, mChannelId, new onGetChannelAlbumListener() {
            @Override
            public void onGetChannelAlbumSuccess(AlbumList albumList) {
                /*for(Album album : albumList){//打印数据
                    Log.d(TAG,">> album"+album.toString());
                }*/
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyView.setVisibility(View.GONE);//注释掉正在加载数据
                    }
                });
                for(Album album : albumList) {
                    mAdapter.setData(album);
                }
                mHandler.post(new Runnable() {//刷新
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onGetChannelAlbumFailed(EorrorInfo info) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyView.setText(getActivity().getResources().getString(R.string.data_failed_tip));
                    }
                });
            }
        });
    }
    class PullLoadMoreListener implements PullLoadRecyclerView.OnPullLoadMoreListener {

        @Override
        public void reRresh() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reRreshData();
                    mRecyclerView.setRefreshCompleted();
                }
            },REFRESH_DURATION);
        }

        @Override
        public void loadMore() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData();
                    mRecyclerView.setLoadMoreCompleted();
                }
            },LOADMORE_DURATION);
        }
    }

    class DetailListAdapter extends RecyclerView.Adapter{
        private Context mContext;
        private Channel mChannel;
        public DetailListAdapter(Context context,Channel channel){
            mContext = context;
            mChannel = channel;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
        public void setColumns(int columns){
            //TODO
        }
        public void setData(Album album){
            //TODO
        }
    }
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_detaillist;
    }

    @Override
    protected void initData() {

    }
}
