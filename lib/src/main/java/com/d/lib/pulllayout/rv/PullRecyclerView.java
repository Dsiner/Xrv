package com.d.lib.pulllayout.rv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.d.lib.pulllayout.Pullable;
import com.d.lib.pulllayout.Refreshable;
import com.d.lib.pulllayout.edge.IEdgeView;
import com.d.lib.pulllayout.edge.IExtendEdgeView;
import com.d.lib.pulllayout.edge.IState;
import com.d.lib.pulllayout.edge.arrow.ExtendFooterView;
import com.d.lib.pulllayout.edge.arrow.ExtendHeaderView;
import com.d.lib.pulllayout.rv.adapter.WrapAdapter;
import com.d.lib.pulllayout.rv.adapter.WrapAdapterDataObserver;
import com.d.lib.pulllayout.util.AppBarHelper;
import com.d.lib.pulllayout.util.NestedScrollHelper;
import com.d.lib.pulllayout.util.RecyclerScrollHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * PullRecyclerView
 * Created by D on 2020/3/21.
 */
public class PullRecyclerView extends RecyclerView implements Pullable, Refreshable {
    private static final int INVALID_POINTER = -1;

    @NonNull
    private final IExtendEdgeView mHeaderView;
    @NonNull
    private final IExtendEdgeView mFooterView;
    @NonNull
    private final HeaderList mHeaderList;

    private boolean mCanPullDown = true;
    private boolean mCanPullUp = true;
    private boolean mAutoLoadMore = true;
    private int mPullPointerId = INVALID_POINTER;
    private int mLastY = -1;
    private int mPullState = Pullable.PULL_STATE_IDLE;
    private AppBarHelper mAppBarHelper;
    private WrapAdapter mWrapAdapter;
    private List<OnPullListener> mOnPullListeners;
    private OnRefreshListener mOnRefreshListener;

    public PullRecyclerView(Context context) {
        this(context, null);
    }

    public PullRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHeaderView = getHeader();
        mFooterView = getFooter();
        mHeaderList = new HeaderList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
        new RecyclerScrollHelper(this).addOnScrollListener(new RecyclerScrollHelper.OnBottomScrollListener() {
            @Override
            public void onBottom() {
                if (!autoLoadMore() || mFooterView.getState() == IState.STATE_ERROR) {
                    return;
                }
                loadMore();
            }
        });
        mAppBarHelper = new AppBarHelper(this);
    }

    @NonNull
    protected IExtendEdgeView getHeader() {
        return new ExtendHeaderView(getContext());
    }

    @NonNull
    protected IExtendEdgeView getFooter() {
        ExtendFooterView view = new ExtendFooterView(getContext());
        view.setOnFooterClickListener(new IEdgeView.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
        return view;
    }

    @Override
    public boolean autoLoadMore() {
        return mAutoLoadMore;
    }

    @Override
    public void setAutoLoadMore(boolean enable) {
        mAutoLoadMore = enable;
    }

    @Override
    public void reset() {
        loadMoreSuccess();
        refreshSuccess();
    }

    @Override
    public void refresh() {
        if (isLoading()) {
            return;
        }
        mHeaderView.setState(IState.STATE_LOADING);
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }

    @Override
    public void loadMore() {
        if (isLoading() || mFooterView.getState() == IState.STATE_NO_MORE) {
            return;
        }
        mFooterView.setState(IState.STATE_LOADING);
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onLoadMore();
        }
    }

    @Override
    public void refreshSuccess() {
        mHeaderView.setState(IState.STATE_SUCCESS);
        mFooterView.setState(IState.STATE_NONE);
    }

    @Override
    public void refreshError() {
        mHeaderView.setState(IState.STATE_SUCCESS);
        mFooterView.setState(IState.STATE_NONE);
    }

    @Override
    public void loadMoreSuccess() {
        mFooterView.setState(IState.STATE_SUCCESS);
    }

    @Override
    public void loadMoreError() {
        mFooterView.setState(IState.STATE_ERROR);
    }

    @Override
    public void loadMoreNoMore() {
        mFooterView.setState(IState.STATE_NO_MORE);
    }

    public boolean isLoading() {
        return mHeaderView.getState() == IState.STATE_LOADING
                || mFooterView.getState() == IState.STATE_LOADING;
    }

    public void addHeaderView(@NonNull View view) {
        mHeaderList.add(view);
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    public boolean removeHeaderView(View v) {
        if (mHeaderList.remove(v)) {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter, mHeaderView, mFooterView, mHeaderList);
        mWrapAdapter.setCanPullDown(mCanPullDown);
        mWrapAdapter.setCanPullUp(mCanPullUp);
        WrapAdapterDataObserver adapterDataObserver = new WrapAdapterDataObserver(mWrapAdapter);
        adapter.registerAdapterDataObserver(adapterDataObserver);
        super.setAdapter(mWrapAdapter);
    }

    /**
     * Retrieves the previously set wrap adapter or null if no adapter is set.
     */
    @Override
    public Adapter getAdapter() {
        if (mWrapAdapter != null) {
            return mWrapAdapter.getOriginalAdapter();
        } else {
            return null;
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (mWrapAdapter != null) {
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (mWrapAdapter.isHeaderList(position)
                                || mWrapAdapter.isFooter(position)
                                || mWrapAdapter.isHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!canPullDown() && !canPullUp()) {
            return super.onInterceptTouchEvent(ev);
        }
        final int action = ev.getAction();
        final int actionIndex = ev.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPullPointerId = ev.getPointerId(0);
                mLastY = (int) (ev.getY() + 0.5f);
                setPullState(Pullable.PULL_STATE_IDLE);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!canPullDown() && !canPullUp()) {
            return super.onTouchEvent(ev);
        }
        final int action = ev.getAction();
        final int actionIndex = ev.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPullPointerId = ev.getPointerId(0);
                mLastY = (int) (ev.getY() + 0.5f);
                setPullState(Pullable.PULL_STATE_IDLE);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mPullPointerId = ev.getPointerId(actionIndex);
                mLastY = (int) (ev.getY(actionIndex) + 0.5f);
                break;

            case MotionEvent.ACTION_MOVE:
                final int index = ev.findPointerIndex(mPullPointerId);
                if (index < 0) {
                    Log.e("PullLayout", "Error processing scroll; pointer index for id "
                            + mPullPointerId + " not found. Did any MotionEvents get skipped?");
                    super.onTouchEvent(ev);
                    return true;
                }

                final int y = (int) (ev.getY(index) + 0.5f);
                final int dy = mLastY - y;

                if (canPullDown() && NestedScrollHelper.isOnTop((View) mHeaderView)) {
                    if (mAppBarHelper.isExpanded() || mPullState == Pullable.PULL_STATE_DRAGGING) {
                        mHeaderView.onDispatchPulled(0, -dy);
                        int offset = ((View) mHeaderView).getBottom();
                        if (offset > 0) {
                            setPullState(Pullable.PULL_STATE_DRAGGING);
                            dispatchOnPullScrolled(0, -offset);
                            mLastY = y;
                            return false;
                        } else {
                            setPullState(Pullable.PULL_STATE_IDLE);
                        }
                    }
                } else if (canPullUp() && NestedScrollHelper.isOnBottom(PullRecyclerView.this, (View) mFooterView)) {
                    mFooterView.onDispatchPulled(0, dy);
                    int offset = getHeight() - ((View) mFooterView).getTop();
                    if (offset > 0) {
                        setPullState(Pullable.PULL_STATE_DRAGGING);
                        dispatchOnPullScrolled(0, offset);
                    } else {
                        setPullState(Pullable.PULL_STATE_IDLE);
                    }
                } else {
                    setPullState(Pullable.PULL_STATE_IDLE);
                }
                mLastY = y;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (canPullDown() && NestedScrollHelper.isOnTop((View) mHeaderView)
                        && ((View) mHeaderView).getBottom() > mHeaderView.getExpandedOffset()
                        && mAppBarHelper.isExpanded()) {
                    refresh();
                    mHeaderView.onExtendTo(0, mHeaderView.getState() == IState.STATE_LOADING
                            ? mHeaderView.getExpandedOffset() : 0);
                } else if (canPullUp() && NestedScrollHelper.isOnBottom(PullRecyclerView.this, (View) mFooterView)
                        && getHeight() - ((View) mFooterView).getTop() > mFooterView.getExpandedOffset()) {
                    loadMore();
                    mFooterView.onExtendTo(0, mFooterView.getState() == IState.STATE_LOADING
                            ? mFooterView.getExpandedOffset() : 0);
                } else {
                    mHeaderView.onExtendTo(0, 0);
                    mFooterView.onExtendTo(0, 0);
                }
                setPullState(Pullable.PULL_STATE_IDLE);
                mLastY = -1;
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == mPullPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mPullPointerId = e.getPointerId(newIndex);
            mLastY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Solve the conflict with CollapsingToolbarLayout
        mAppBarHelper.setOnOffsetChangedListener();
    }

    @Override
    public int getPullState() {
        return mPullState;
    }

    @Override
    public void setPullState(int state) {
        if (state == mPullState) {
            return;
        }
        mPullState = state;
        dispatchOnPullStateChanged(state);
    }

    @Override
    public boolean canPullDown() {
        return mCanPullDown;
    }

    @Override
    public void setCanPullDown(boolean enable) {
        this.mCanPullDown = enable;
        if (!enable) {
            mHeaderView.setState(IState.STATE_NONE);
        }
        if (mWrapAdapter != null) {
            mWrapAdapter.setCanPullDown(enable);
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean canPullUp() {
        return mCanPullUp;
    }

    @Override
    public void setCanPullUp(boolean enable) {
        this.mCanPullUp = enable;
        if (!enable) {
            mFooterView.setState(IState.STATE_NONE);
        }
        if (mWrapAdapter != null) {
            mWrapAdapter.setCanPullUp(enable);
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    private void dispatchOnPullStateChanged(int state) {
        // Listeners go last. All other internal state is consistent by this point.
        if (mOnPullListeners != null) {
            for (int i = mOnPullListeners.size() - 1; i >= 0; i--) {
                mOnPullListeners.get(i).onPullStateChanged(this, state);
            }
        }
    }

    private void dispatchOnPullScrolled(int hresult, int vresult) {
        // Pass the real deltas to onScrolled
        if (-vresult >= 0) {
            mHeaderView.onPulled(hresult, Math.max(0, -vresult));
        }
        if (vresult >= 0) {
            mFooterView.onPulled(hresult, Math.max(0, vresult));
        }
        if (mOnPullListeners != null) {
            for (int i = mOnPullListeners.size() - 1; i >= 0; i--) {
                mOnPullListeners.get(i).onPulled(this, hresult, vresult);
            }
        }
    }

    @Override
    public void addOnPullScrollListener(OnPullListener listener) {
        if (mOnPullListeners == null) {
            mOnPullListeners = new ArrayList<>();
        }
        mOnPullListeners.add(listener);
    }

    @Override
    public void removeOnPullScrollListener(OnPullListener listener) {
        if (mOnPullListeners != null) {
            mOnPullListeners.remove(listener);
        }
    }

    @Override
    public void clearOnPullScrollListeners() {
        if (mOnPullListeners != null) {
            mOnPullListeners.clear();
        }
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }
}