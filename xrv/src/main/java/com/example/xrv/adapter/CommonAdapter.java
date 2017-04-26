package com.example.xrv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonAdapter-recyclerview
 * Created by hehuajia on 15/4/7.
 */
public abstract class CommonAdapter<T> extends RecyclerView.Adapter<CommonHolder> {
    protected Context mContext;
    protected List<T> mDatas;
    protected int mLayoutId;
    protected MultiItemTypeSupport<T> multiItemTypeSupport;

    public CommonAdapter(Context context, List<T> datas, int layoutId) {
        mContext = context;
        mDatas = datas == null ? new ArrayList<T>() : datas;
        mLayoutId = layoutId;
    }

    public CommonAdapter(Context context, List<T> datas, MultiItemTypeSupport<T> multiItemTypeSupport) {
        mContext = context;
        mDatas = datas == null ? new ArrayList<T>() : datas;
        this.multiItemTypeSupport = multiItemTypeSupport;
    }

    @Override
    public int getItemViewType(int position) {
        if (multiItemTypeSupport != null) {
            return multiItemTypeSupport.getItemViewType(position, position < mDatas.size() ? mDatas.get(position) : null);
        }
        return super.getItemViewType(position);
    }

    @Override
    public CommonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = mLayoutId;
        if (multiItemTypeSupport != null) {
            //multiType
            if (mDatas != null && mDatas.size() > 0) {
                layoutId = multiItemTypeSupport.getLayoutId(viewType);
            }
        }
        CommonHolder holder = CommonHolder.createViewHolder(mContext, parent, layoutId);
        onViewHolderCreated(holder, holder.getConvertView());
        return holder;
    }

    @Override
    public void onBindViewHolder(CommonHolder holder, int position) {
        convert(position, holder, mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void onViewHolderCreated(CommonHolder holder, View itemView) {
    }

    public List<T> getDatas() {
        return mDatas;
    }

    /**
     * @param position
     * @param holder
     * @param item     position对应的数据Bean
     */
    public abstract void convert(int position, CommonHolder holder, T item);
}
