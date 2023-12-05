// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.collection;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CollectionAdapter<T extends ViewItem>
        extends RecyclerView.Adapter<CollectionItemViewHolder<T>> {
    @SuppressWarnings("rawtypes")
    private final Map<Integer, CollectionItemViewHolder.Creator> mViewHolderMap;
    private final Map<ViewItem, CollectionItemViewHolder<T>> mBindingHolderMap;
    private DataCollection<? extends ViewItem> mCollection;
    private CollectionItemViewHolder.OnItemClickListener<T> mClickListener;
    private CollectionItemViewHolder.OnItemLongClickListener<T> mLongClickListener;

    public CollectionAdapter() {
        mViewHolderMap = new HashMap<>();
        mBindingHolderMap = new HashMap<>();
    }

    public CollectionAdapter(DataCollection<? extends ViewItem> collection) {
        mCollection = collection;
        mViewHolderMap = new HashMap<>();
        mBindingHolderMap = new HashMap<>();
    }

    @NonNull
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CollectionItemViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        CollectionItemViewHolder.Creator creator = mViewHolderMap.get(viewType);
        CollectionItemViewHolder<T> holder;
        if (creator == null) {
            holder = new InvalidViewHolder<>(parent.getContext());
        } else {
            holder = creator.create(LayoutInflater.from(parent.getContext()), parent);
        }
        if (mClickListener != null) holder.registerClickListener(mClickListener);
        return holder;
    }

    @SuppressWarnings("rawtypes")
    public void registerViewHolder(int viewType, CollectionItemViewHolder.Creator creator) {
        mViewHolderMap.put(viewType, creator);
    }

    public void cleanRegisterViewHolder() {
        mViewHolderMap.clear();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onBindViewHolder(CollectionItemViewHolder holder, int position) {
        final ViewItem t = mCollection.get(position);
        if (t == null) return;
        if (mBindingHolderMap.containsKey(t)) {
            if (mBindingHolderMap.get(t) != holder) {
                mBindingHolderMap.remove(t).onUnbind();
            }
        }
        mBindingHolderMap.put(t, holder);
        holder.bind(t);
    }

    public void setCollection(DataCollection<? extends ViewItem> collection) {
        mCollection = collection;
    }

    public DataCollection<? extends ViewItem> getCollection() {
        return mCollection;
    }

    public CollectionItemViewHolder.OnItemClickListener<? extends ViewItem> getClickListener() {
        return mClickListener;
    }

    public void setClickListener(CollectionItemViewHolder.OnItemClickListener<T> clickListener) {
        this.mClickListener = clickListener;
    }

    public void setLongClickListener(CollectionItemViewHolder.OnItemLongClickListener<T> clickListener) {
        this.mLongClickListener = clickListener;
    }

    @Override
    public void onViewRecycled(CollectionItemViewHolder<T> holder) {
        if (holder.getItemModel() == null) return;
        mBindingHolderMap.remove(holder.getItemModel());
        holder.onUnbind();
    }

    @Override
    public int getItemCount() {
        if (mCollection == null) return 0;
        return mCollection.size();
    }

    public ViewItem getItem(int position) {
        if (mCollection == null) return null;
        return mCollection.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mCollection.size() || mCollection.get(position) == null) return 0;
        int type = mCollection.get(position).getType();
        if (mViewHolderMap.containsKey(type)) {
            return type;
        }
        return 0;
    }
}
