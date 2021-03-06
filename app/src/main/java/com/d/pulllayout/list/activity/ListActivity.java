package com.d.pulllayout.list.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.d.lib.common.component.mvp.MvpBasePresenter;
import com.d.lib.common.component.mvp.app.v4.BaseFragmentActivity;
import com.d.lib.common.util.ViewHelper;
import com.d.lib.common.widget.TitleLayout;
import com.d.lib.common.widget.popup.MenuPopup;
import com.d.lib.common.widget.popup.PopupWindowFactory;
import com.d.pulllayout.R;
import com.d.pulllayout.list.fragment.CoordinatorLayoutFragment;
import com.d.pulllayout.list.fragment.ItemTouchFragment;
import com.d.pulllayout.list.fragment.MultipleFragment;
import com.d.pulllayout.list.fragment.SimpleFragment;
import com.d.pulllayout.list.model.EdgeType;
import com.d.pulllayout.list.model.ListType;

/**
 * ListActivity
 * Created by D on 2017/4/26.
 */
public class ListActivity extends BaseFragmentActivity<MvpBasePresenter>
        implements View.OnClickListener {
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    public static final String EXTRA_LIST_TYPE = "EXTRA_LIST_TYPE";
    public static final String EXTRA_EDGE_TYPE = "EXTRA_EDGE_TYPE";

    public static final int TYPE_SIMPLE = 0;
    public static final int TYPE_MULTIPLE = 1;
    public static final int TYPE_COORDINATOR_LIST = 2;
    public static final int TYPE_ITEM_TOUCH = 3;

    protected TitleLayout tl_title;
    protected int mType = TYPE_SIMPLE;
    protected int mListType = ListType.PULLRECYCLERLAYOUT_PULLRECYCLERVIEW;
    protected int mEdgeType = EdgeType.TYPE_CLASSIC;
    protected Fragment mCurFragment;

    public static void openActivity(Context context, int type) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, ListActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static int getExtrasListType(Fragment fragment) {
        return fragment.getArguments() != null
                ? fragment.getArguments().getInt(ListActivity.EXTRA_LIST_TYPE, ListType.PULLRECYCLERLAYOUT_RECYCLERVIEW)
                : ListType.PULLRECYCLERLAYOUT_RECYCLERVIEW;
    }

    public static int getExtrasEdgeType(Fragment fragment) {
        return fragment.getArguments() != null
                ? fragment.getArguments().getInt(ListActivity.EXTRA_EDGE_TYPE, EdgeType.TYPE_CLASSIC)
                : EdgeType.TYPE_CLASSIC;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_left:
                finish();
                break;

            case R.id.iv_title_right:
                onMore(mListType);
                break;
        }
    }

    protected void onMore(final int listType) {
        MenuPopup menuPopup = PopupWindowFactory.createFactory(this)
                .getMenuPopup(ListType.getMenus(mListType),
                        new MenuPopup.OnMenuListener() {
                            @Override
                            public void onClick(PopupWindow popup, int position, String item) {
                                if (listType == position) {
                                    return;
                                }
                                mListType = position;
                                replace(mType, mListType, mEdgeType);
                            }
                        });
        menuPopup.showAsDropDown((View) ViewHelper.findViewById(this, R.id.iv_title_right));
    }

    @Override
    protected int getLayoutRes() {
        return mType == TYPE_COORDINATOR_LIST
                ? R.layout.fragment_list_coordinatorlayout
                : R.layout.lib_pub_activity_loader_content;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_SIMPLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void bindView() {
        super.bindView();
        tl_title = ViewHelper.findViewById(this, R.id.tl_title);
        tl_title.setVisibility(R.id.iv_title_right, View.VISIBLE);

        ViewHelper.setOnClickListener(this, this, R.id.iv_title_left,
                R.id.iv_title_right);
    }

    @Override
    protected void init() {
        ImageView iv_title_right = ViewHelper.findViewById(tl_title, R.id.iv_title_right);
        iv_title_right.setImageResource(R.drawable.lib_pub_ic_title_more);

        replace(mType, mListType, mEdgeType);
    }

    public void replace(int type, int listType, int edgeType) {
        final String title;
        final Fragment fragment;
        final Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_LIST_TYPE, listType);
        bundle.putInt(EXTRA_EDGE_TYPE, edgeType);
        if (type == TYPE_SIMPLE) {
            title = "Simple";
            fragment = new SimpleFragment();

        } else if (type == TYPE_MULTIPLE) {
            title = "Multiple";
            fragment = new MultipleFragment();

        } else if (type == TYPE_COORDINATOR_LIST) {
            title = "CoordinatorLayout";
            fragment = new CoordinatorLayoutFragment();

        } else if (type == TYPE_ITEM_TOUCH) {
            title = "Item Touch";
            fragment = new ItemTouchFragment();

        } else {
            title = "Invalid";
            fragment = new Fragment();
        }
        fragment.setArguments(bundle);
        tl_title.setText(R.id.tv_title_title, title);
        tl_title.setVisibility(mType == TYPE_ITEM_TOUCH ? View.GONE : View.VISIBLE);
        mCurFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
    }
}
