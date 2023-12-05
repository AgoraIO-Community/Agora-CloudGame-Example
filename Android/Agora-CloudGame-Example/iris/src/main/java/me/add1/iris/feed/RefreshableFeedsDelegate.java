package me.add1.iris.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import me.add1.iris.ApiRequestException;
import me.add1.iris.ObserverList;
import me.add1.iris.collection.CollectionItemViewHolder;
import me.add1.iris.collection.DataCollection;
import me.add1.iris.model.FeedType;
import me.add1.iris.model.PageInfo;

public abstract class RefreshableFeedsDelegate extends FeedsDelegate {
    private static final int LOAD_MORE_CHECK_LIMIT = 5;
    private int mLoadMoreCheckThreshold;

    @NonNull
    private final ObserverList<FeedsPageRefreshListener> mPageRefreshListeners =
            new ObserverList<>();

    public interface FeedsPageRefreshListener {
        void onRefreshStarted(FeedsDelegate delegate);

        void onRefreshEnd(FeedsDelegate delegate, boolean success);
    }

    public boolean addPageRefreshListener(@NonNull FeedsPageRefreshListener listener) {
        return mPageRefreshListeners.addObserver(listener);
    }

    public boolean removePageRefreshListener(@NonNull FeedsPageRefreshListener listener) {
        return mPageRefreshListeners.removeObserver(listener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadMoreCheckThreshold = LOAD_MORE_CHECK_LIMIT;
    }

    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getCollection().size() == 0) requestFeeds(null);
        mBinding.refresh.setEnabled(true);
        mBinding.refresh.setOnRefreshListener(() -> refreshFeeds(null));
        mBinding.recycler.setItemAnimator(new RecyclerViewFeedItemAnimator());
    }

    public void forceRefreshing(@Nullable RemoteFeedCollection.RequestCallback callback) {
        if (mBinding == null) return;
        if (mBinding.refresh.isShown()) {
            mBinding.refresh.setRefreshing(true);
            refreshFeeds(callback);
        }
    }

    protected abstract RemoteFeedCollection getCollection();

    @Override
    @SuppressWarnings("unchecked")
    public void onItemShow(FeedItemViewHolder<?> holder) {
        int index = holder.getAdapterPosition();
        if (index == RecyclerView.NO_POSITION) return;
        int i;
        for (i = 0; index < mAdapter.getItemCount() && i < mLoadMoreCheckThreshold; i++) {
            if (allowLoading(getCollection().get(index))) {
                loadMore((FeedItem<PageInfo>) getCollection().get(index));
                break;
            }
            index++;
        }
    }

    public void requestFeeds(@Nullable RemoteFeedCollection.RequestCallback callback) {
        getCollection().preload(new RemoteFeedCollection.RequestCallback() {
            @Override
            public void onSuccess(@NonNull List<FeedItem<?>> models) {
                getCollection().clear();
                getCollection().addAll(models);
                if (!getCollection().hasData()) {
                    getCollection().add(new FeedItem<>(FeedType.EMPTY,
                            UUID.randomUUID().toString(), null));
                }
                if (isAlive()) refreshFeeds(null);
                if (callback != null) {
                    callback.onSuccess(models);
                }

                if (isAlive() && mBinding != null) mBinding.refresh.setRefreshing(false);
            }

            @Override
            public void onError(@NonNull ApiRequestException e) {
                getCollection().add(new FeedItem<>(FeedType.PRELOAD,
                        UUID.randomUUID().toString(), null));
                if (callback != null) {
                    callback.onError(e);
                }
                if (isAlive() && mBinding != null) mBinding.refresh.setRefreshing(false);
                if (isAlive()) refreshFeeds(null);
            }
        });
    }

    protected void loadMore(FeedItem<PageInfo> feedItem) {
        feedItem.setStatus(FeedItem.Status.STATUS_PROCESS);
        getCollection().loadMore(feedItem, new RemoteFeedCollection.RequestCallback() {
            @Override
            public void onSuccess(@NonNull List<FeedItem<?>> models) {
                feedItem.removeStatus(FeedItem.Status.STATUS_PROCESS);

                int index = getCollection().indexOf(feedItem);
                if (index >= 0) {
                    getCollection().replaceAll(index, models);
                }
            }

            @Override
            public void onError(@NonNull ApiRequestException e) {
                feedItem.removeStatus(FeedItem.Status.STATUS_PROCESS);
            }
        });
    }

    private boolean allowLoading(FeedItem<?> feedItem) {
        return feedItem.type == FeedType.LOAD_MORE
                && !feedItem.isStatus(FeedItem.Status.STATUS_PROCESS);
    }

    public void refreshFeeds(@Nullable RemoteFeedCollection.RequestCallback callback) {
        if (mBinding != null && !mBinding.refresh.isRefreshing()) {
            mBinding.refresh.setRefreshing(true);
        }

        for (FeedsPageRefreshListener listener : mPageRefreshListeners) {
            listener.onRefreshStarted(this);
        }
        //when refresh, first scroll to top.
        if (mBinding != null) mBinding.recycler.scrollToPosition(0);

        getCollection().refresh(new RemoteFeedCollection.RequestCallback() {

            @Override
            public void onSuccess(@NonNull List<FeedItem<?>> models) {
                if (callback != null) {
                    callback.onSuccess(models);
                }

                for (FeedsPageRefreshListener listener : mPageRefreshListeners) {
                    listener.onRefreshEnd(RefreshableFeedsDelegate.this, true);
                }

                if (isAlive() && mBinding != null) mBinding.refresh.setRefreshing(false);
            }

            @Override
            public void onError(@NonNull ApiRequestException e) {
                if (callback != null) {
                    callback.onError(e);
                }
                for (FeedsPageRefreshListener listener : mPageRefreshListeners) {
                    listener.onRefreshEnd(RefreshableFeedsDelegate.this, false);
                }

                if (isAlive() && mBinding != null) mBinding.refresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onViewHolderClick(@NonNull CollectionItemViewHolder<FeedItem<?>> holder,
            View itemView, FeedItem<?> model, @Nullable String type) {
        if (model.type == FeedType.EMPTY) {
            refreshFeeds(null);
        }
        super.onViewHolderClick(holder, itemView, model, type);
    }

    protected void refreshEnable(boolean enable) {
        if (mBinding == null) return;
        mBinding.refresh.setRefreshing(enable);
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        mPageRefreshListeners.clear();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        for (FeedsPageRefreshListener listener : mPageRefreshListeners) {
            listener.onRefreshEnd(this, false);
        }
    }

}
