package com.d.lib.pulllayout.lv.adapter;

/**
 * CommonHolder for ListView
 * Created by D on 2017/4/25.
 */
public interface MultiItemTypeSupport<T> {
    int getViewTypeCount();

    int getItemViewType(int position, T t);

    int getLayoutId(int viewType);
}
