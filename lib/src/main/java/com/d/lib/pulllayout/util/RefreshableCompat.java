package com.d.lib.pulllayout.util;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.d.lib.pulllayout.PullRecyclerLayout;
import com.d.lib.pulllayout.Refreshable;
import com.d.lib.pulllayout.loader.RecyclerAdapter;
import com.d.lib.pulllayout.rv.PullRecyclerView;

public class RefreshableCompat {

    public static void addHeaderView(Refreshable refreshable, @LayoutRes int resource) {
        Refreshable root = getSupport(refreshable);
        if (root == null) {
            return;
        }
        View header = LayoutInflater.from(((ViewGroup) root).getContext())
                .inflate(resource, (ViewGroup) root, false);
        addHeaderView(root, header);
    }

    public static void addHeaderView(Refreshable refreshable, @NonNull View view) {
        if (refreshable instanceof PullRecyclerLayout) {
            View nestedChild = ((PullRecyclerLayout) refreshable).getNestedChild();
            if (nestedChild instanceof PullRecyclerView) {
                ((PullRecyclerView) nestedChild).addHeaderView(view);
            }
        } else if (refreshable instanceof PullRecyclerView) {
            ((PullRecyclerView) refreshable).addHeaderView(view);
        }
    }

    public static void addFooterView(Refreshable refreshable, @LayoutRes int resource) {
        Refreshable root = getSupport(refreshable);
        if (root == null) {
            return;
        }
        View header = LayoutInflater.from(((ViewGroup) root).getContext())
                .inflate(resource, (ViewGroup) root, false);
        addFooterView(root, header);
    }

    public static void addFooterView(Refreshable refreshable, @NonNull View view) {
        if (refreshable instanceof PullRecyclerLayout) {
            View nestedChild = ((PullRecyclerLayout) refreshable).getNestedChild();
            if (nestedChild instanceof PullRecyclerView) {
                ((PullRecyclerView) nestedChild).addFooterView(view);
            }
        } else if (refreshable instanceof PullRecyclerView) {
            ((PullRecyclerView) refreshable).addFooterView(view);
        }
    }

    @Nullable
    private static Refreshable getSupport(Refreshable refreshable) {
        Refreshable root = null;
        if (refreshable instanceof PullRecyclerLayout) {
            View nestedChild = ((PullRecyclerLayout) refreshable).getNestedChild();
            if (nestedChild instanceof PullRecyclerView) {
                root = (PullRecyclerView) nestedChild;
            }
        } else if (refreshable instanceof PullRecyclerView) {
            root = refreshable;
        }
        return root;
    }

    public static void setAdapter(Refreshable refreshable, RecyclerAdapter adapter) {
        if (refreshable instanceof PullRecyclerLayout) {
            ((PullRecyclerLayout) refreshable).setAdapter(adapter);
        } else if (refreshable instanceof PullRecyclerView) {
            ((PullRecyclerView) refreshable).setAdapter((RecyclerView.Adapter) adapter);
        }
    }

    public static void setNestedScrollingEnabled(Refreshable refreshable, boolean enabled) {
        if (refreshable instanceof PullRecyclerLayout) {
            View nestedChild = ((PullRecyclerLayout) refreshable).getNestedChild();
            if (nestedChild instanceof RecyclerView) {
                ((RecyclerView) nestedChild).setNestedScrollingEnabled(enabled);
            } else if (nestedChild instanceof ListView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((ListView) nestedChild).setNestedScrollingEnabled(enabled);
                }
            }
        } else if (refreshable instanceof PullRecyclerView) {
            ((PullRecyclerView) refreshable).setNestedScrollingEnabled(enabled);
        }
    }

    public static void notifyItemChanged(AbsListView listView, int position) {
        if (listView == null || listView.getAdapter() == null) {
            return;
        }
        final int firstVisiblePosition = listView.getFirstVisiblePosition();
        final int lastVisiblePosition = listView.getLastVisiblePosition();
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            View view = listView.getChildAt(position - firstVisiblePosition);
            listView.getAdapter().getView(position, view, listView);
        }
    }
}
