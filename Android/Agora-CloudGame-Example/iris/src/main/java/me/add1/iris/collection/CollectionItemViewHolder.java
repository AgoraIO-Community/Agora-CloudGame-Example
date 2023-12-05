// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import me.add1.iris.ClickType;


public abstract class CollectionItemViewHolder<T extends ViewItem> extends RecyclerView.ViewHolder {
    T mItemModel;

    public CollectionItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public T getItemModel() {
        return mItemModel;
    }

    final void bind(@NonNull T t) {
        if (mItemModel != null) {
            if (mItemModel.getId().equals(t.getId())) {
                mItemModel = t;
                onBind(t, true);
            } else {
                onUnbind();
                mItemModel = t;
                onBind(t, false);
            }
        } else {
            mItemModel = t;
            onBind(t, false);
        }
    }

    @CallSuper
    public void onBind(@NonNull T t, boolean isUpdate) {
    }

    @CallSuper
    public void onUnbind() {
        mItemModel = null;
    }

    @CallSuper
    public void registerClickListener(final CollectionItemViewHolder
            .OnItemClickListener<T> listener) {
        itemView.setOnClickListener(view -> {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(CollectionItemViewHolder.this, view, getItemModel(),
                        ClickType.HOLDER);
            }
        });
    }

    @CallSuper
    public void registerLongClickListener(final CollectionItemViewHolder
            .OnItemLongClickListener<T> listener) {
        itemView.setOnLongClickListener(v -> {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(CollectionItemViewHolder.this, v, getItemModel(),
                        ClickType.HOLDER);
            }
            return false;
        });
    }

    protected interface Creator<K extends CollectionItemViewHolder<? extends ViewItem>> {
        @NonNull
        K create(LayoutInflater inflater, ViewGroup parent);
    }

    public interface OnItemClickListener<T extends ViewItem> {
        void onItemClick(@NonNull CollectionItemViewHolder<T> item, View itemView, T model,
                         @Nullable String type);
    }

    public interface OnItemLongClickListener<T extends ViewItem> {
        void onItemLongClick(@NonNull CollectionItemViewHolder<T> item, View itemView, T model,
                         @Nullable String type);
    }
}