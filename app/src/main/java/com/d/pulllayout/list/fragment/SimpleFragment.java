package com.d.pulllayout.list.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.d.lib.common.component.loader.v4.AbsFragment;
import com.d.lib.common.component.mvp.MvpView;
import com.d.lib.pulllayout.Pullable;
import com.d.lib.pulllayout.loader.RecyclerAdapter;
import com.d.pulllayout.R;
import com.d.pulllayout.list.activity.ListActivity;
import com.d.pulllayout.list.adapter.rv.SimpleAdapter;
import com.d.pulllayout.list.model.Bean;
import com.d.pulllayout.list.model.ListType;
import com.d.pulllayout.list.presenter.LoadPresenter;

import java.util.ArrayList;

/**
 * Simple Type
 * Created by D on 2017/4/26.
 */
public class SimpleFragment extends AbsFragment<Bean, LoadPresenter> {
    private int mListType;

    @Override
    protected int getLayoutRes() {
        return ListType.S_RES_IDS[mListType];
    }

    @Override
    public LoadPresenter getPresenter() {
        return new LoadPresenter(getActivity().getApplicationContext());
    }

    @Override
    protected MvpView getMvpView() {
        return this;
    }

    @Override
    protected RecyclerAdapter<Bean> getAdapter() {
        return mListType == ListType.PULLRECYCLERLAYOUT_LISTVIEW ?
                new com.d.pulllayout.list.adapter.lv.
                        SimpleAdapter(mContext, new ArrayList<Bean>(), R.layout.adapter_item_text)
                : new SimpleAdapter(mContext, new ArrayList<Bean>(), R.layout.adapter_item_text);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mListType = ListActivity.getListType(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initList() {
        super.initList();
    }

    @Override
    protected void init() {
        super.init();
        ((Pullable) mPullList).addOnPullListener(new Pullable.OnPullListener() {
            @Override
            public void onPullStateChanged(Pullable pullable, int newState) {
                Log.d("dsiner", "onPullStateChanged newState: " + newState);
            }

            @Override
            public void onPulled(Pullable pullable, int dx, int dy) {
                Log.d("dsiner", "onPulled dx: " + dx + " dy: " + dy);
            }
        });
    }

    @Override
    protected void onLoad(int page) {
        mPresenter.get(page);
    }
}