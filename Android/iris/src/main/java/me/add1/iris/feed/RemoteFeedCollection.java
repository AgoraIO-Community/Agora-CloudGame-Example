// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.feed;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.add1.iris.ApiRequestException;
import me.add1.iris.ErrorCode;
import me.add1.iris.collection.DataCollection;
import me.add1.iris.model.FeedType;
import me.add1.iris.model.PageInfo;

@SuppressWarnings("rawtypes")
public abstract class RemoteFeedCollection extends DataCollection<FeedItem<?>> {
    public interface RequestCallback {
        void onSuccess(@NonNull List<FeedItem<?>> models);

        default void onError(@NonNull ApiRequestException e) {
        }
    }

    public interface ActionCallback {
        void onSuccess();

        default void onError(@NonNull ApiRequestException e) {
        }
    }

    public RemoteFeedCollection() {
        super();
    }

    public RemoteFeedCollection(List<FeedItem<?>> items) {
        super(items);
    }

    public void preload(@Nullable RequestCallback callback) {
        if (callback != null) {
            callback.onError(ApiRequestException.obtain(ErrorCode.CACHE_NOT_FOUND));
        }
    }

    public abstract void refresh(@Nullable RemoteFeedCollection.RequestCallback callback);

    public abstract void loadMore(@NonNull FeedItem<PageInfo> item,
            @Nullable RemoteFeedCollection.RequestCallback callback);

    public boolean hasData() {
        if (isEmpty()) return false;
        for (FeedItem item : mItems) {
            if ((item.getType() & FeedType.DATA_MASK) == FeedType.DATA_MASK) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public FeedItem<?> findItemById(@NonNull String id) {
        for (FeedItem<?> item : getItems()) {
            if (id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }
}
