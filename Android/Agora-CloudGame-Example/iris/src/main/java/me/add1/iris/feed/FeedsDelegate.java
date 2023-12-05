// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.Objects;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.add1.iris.Check;
import me.add1.iris.ClickType;
import me.add1.iris.PageDelegate;
import me.add1.iris.R;
import me.add1.iris.collection.CollectionAdapter;
import me.add1.iris.collection.CollectionItemViewHolder;
import me.add1.iris.collection.DataCollection;
import me.add1.iris.databinding.DelegateFeedsBinding;
import me.add1.iris.widget.FeedItemDecoration;
import me.add1.iris.widget.FeedRecyclerView;
import me.add1.iris.widget.ViewUtils;

/**
 * Base fragment for social collection related fragments
 */
public abstract class FeedsDelegate extends PageDelegate
        implements FeedRecyclerView.OnItemVisibilityListener {


    protected CollectionAdapter<FeedItem<?>> mAdapter;
    protected DelegateFeedsBinding mBinding;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = obtainCollectionAdapter();
        addCollectionObserver();
    }

    protected CollectionAdapter<FeedItem<?>> getAdapter() {
        return mAdapter;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return mBinding.refresh;
    }

    protected CollectionAdapter<FeedItem<?>> obtainCollectionAdapter() {
        return new CollectionAdapter<>(getCollection());
    }

    protected View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        mBinding = DelegateFeedsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    protected void setFeedsBinding(@NonNull DelegateFeedsBinding binding) {
        mBinding = binding;
    }

    public boolean isRefreshing() {
        if (mBinding == null) return false;
        return mBinding.refresh.isRefreshing();
    }

    @CallSuper
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.recycler.setLayoutManager(getLayoutManager());
        if (mBinding.recycler.getLayoutManager() instanceof LinearLayoutManager) {
            FeedItemDecoration itemDecoration = new FeedItemDecoration();
            itemDecoration.setOrientation(FeedItemDecoration.VERTICAL);
            mBinding.recycler.addItemDecoration(itemDecoration);
        }

        mBinding.recycler.setAdapter(mAdapter);
        registerViewHolder(mAdapter);
        mAdapter.setClickListener((item, itemView, model, type) -> {
            if (!isAlive() && !isActive()) return;
            if (getRecycleView() == null) return;
            if (item.getItemModel() == null) return;
            onViewHolderClick(item, itemView, model, type);
        });
        mBinding.recycler.setOnItemVisibilityListener(this);
        mBinding.refresh.setEnabled(false);
        if (getContext() != null) {
            mBinding.refresh.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary));
        }
    }

    @CallSuper
    protected void onViewHolderClick(@NonNull CollectionItemViewHolder<FeedItem<?>> holder,
                                     View itemView, FeedItem<?> model, @Nullable String type) {
        if (type == ClickType.FORCE_ACTIVE) {
            if (!(holder instanceof FeedItemViewHolder)) return;
            getRecycleView().setActiveEnable(true);
            getRecycleView().forceChangeActiveHolder((FeedItemViewHolder) holder);
        } else if (type == ClickType.REQUEST_ACTIVE) {
            if (!(holder instanceof FeedItemViewHolder)) return;
            FeedItemViewHolder<?> lastActiveHolder = getRecycleView().getLastActiveItemViewHolder();
            FeedItemViewHolder<?> requestActiveHolder = (FeedItemViewHolder) holder;
            if (lastActiveHolder == null) {
                if (getRecycleView().getCurrentVisibilityHolders() != null
                        && getRecycleView().getCurrentVisibilityHolders()
                        .contains(requestActiveHolder)) {
                    getRecycleView().setActiveEnable(true);
                    getRecycleView().forceChangeActiveHolder(requestActiveHolder);
                } else {
                    getRecycleView().smoothChangeActiveHolder(requestActiveHolder);
                    getRecycleView().setActiveEnable(true);
                }
            } else if (lastActiveHolder != null) {
                getRecycleView().smoothChangeActiveHolder(requestActiveHolder);
            }
        }
    }

    protected abstract void registerViewHolder(CollectionAdapter<FeedItem<?>> adapter);

    public FeedRecyclerView getRecycleView() {
        return mBinding.recycler;
    }


    @SuppressWarnings("rawtypes")
    protected void addCollectionObserver() {
        getCollection().addOnDataSetChangedObserver(new DataCollection
                .OnDataSetChangedObserver<FeedItem<?>>() {
            @Override
            public void onItemAdded(final int position, FeedItem<?> item) {
                mAdapter.notifyItemInserted(position);
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemChanged(final int position, final FeedItem item) {
                mAdapter.notifyItemChanged(position, item);
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemMoved(int src, int dest) {
                mAdapter.notifyItemMoved(src, dest);
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemRemoved(final int position) {
                mAdapter.notifyItemRemoved(position);
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemReplaced(final int position,
                                       final Collection<? extends FeedItem<?>> items) {
                mAdapter.notifyItemRangeChanged(position, items.size());
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemsAllReplaced(final Collection<? extends FeedItem<?>> items) {
                mAdapter.notifyItemRangeChanged(0, items.size());
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemsAdded(final int position,
                                     final Collection<? extends FeedItem<?>> items) {
                mAdapter.notifyItemRangeInserted(position, items.size());
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemsRemoved(final int position, final int size) {
                mAdapter.notifyItemRangeRemoved(position, size);
                notifyRecyclerViewLayoutChanged();
            }

            @Override
            public void onItemsRemoved() {
                mAdapter.notifyDataSetChanged();
                notifyRecyclerViewLayoutChanged();
            }
        });
    }

    private void notifyRecyclerViewLayoutChanged() {
        if (!isAlive() || mBinding.recycler == null) return;
        ViewUtils.addOneShotLayoutListener(mBinding.recycler, () -> {
            if (mBinding == null) return;
            mBinding.recycler.post(() -> {
                if (!isActive() || mBinding.recycler == null) return;
                mBinding.recycler.refreshScrollState();
            });
        });
    }

    protected abstract DataCollection<FeedItem<?>> getCollection();

    public void resetCollection() {
        if (mAdapter == null) return;
        if (mAdapter.getCollection() == null || getCollection() == null) {
            Check.shouldNeverHappen();
            return;
        }
        if (mAdapter.getCollection() == getCollection()) {
            getCollection().clear();
        } else {
            mAdapter.getCollection().removeOnDataSetChangedObserver();
            mAdapter.setCollection(getCollection());
            addCollectionObserver();
            mAdapter.notifyDataSetChanged();
        }
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        layoutManager.setRecycleChildrenOnDetach(true);
        return layoutManager;
    }

    @Override
    public void onItemShow(FeedItemViewHolder<?> holder) {
    }

    @Override
    public void onItemHidden(FeedItemViewHolder<?> holder) {
    }

    protected void onDestroyView() {
        mBinding.unbind();
        mBinding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (getCollection() != null) getCollection().removeOnDataSetChangedObserver();
        mHost = null;
        super.onDestroy();
    }
}
