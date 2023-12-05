// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.feed;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import me.add1.iris.collection.CollectionItemViewHolder;

public class FeedItemViewHolder<T> extends CollectionItemViewHolder<FeedItem<T>> {

    public Paint mPaint;

    private boolean mVisibility;
    protected int mDivider;
    protected int mColor;

    public FeedItemViewHolder(@NonNull View itemView, @DimenRes int divider, @ColorRes int color) {
        super(itemView);
        mPaint = new Paint();
        if (divider != 0) {
            mDivider = itemView.getContext().getResources().getDimensionPixelSize(divider);
        }
        if (color != 0) {
            mColor = ContextCompat.getColor(itemView.getContext(), color);
        } else {
            mColor = Color.TRANSPARENT;
        }
        mPaint.setColor(mColor);
    }

    public void onItemShown() {
        if (getItemModel() == null) return;
        if (!getItemModel().isStatus(FeedItem.Status.IMPRESSION)) {
            getItemModel().setStatus(FeedItem.Status.IMPRESSION);
            onItemFirstShown();
        }
    }

    public void onItemFirstShown() {

    }

    public void onItemFirstHidden() {
    }

    public void onItemHidden() {
        if (getItemModel() == null) return;
        if (getItemModel().isStatus(FeedItem.Status.IMPRESSION)
                && !getItemModel().isStatus(FeedItem.Status.END_IMPRESSION)) {
            getItemModel().setStatus(FeedItem.Status.END_IMPRESSION);
            onItemFirstHidden();
        }
    }

    @Override
    public void onUnbind() {
        if (getItemModel().isStatus(FeedItem.Status.IMPRESSION)
                && !getItemModel().isStatus(FeedItem.Status.END_IMPRESSION)) {
            getItemModel().setStatus(FeedItem.Status.END_IMPRESSION);
            onItemFirstHidden();
        }
        super.onUnbind();
    }

    public void onItemIdle() {
    }

    public void onItemScroll() {
    }

    protected boolean onItemActive() {
        return false;
    }

    protected boolean onItemInactive() {
        return false;
    }

    public boolean invokeOnItemActive() {
        if (getItemModel() == null) return false;
        if (onItemActive()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean invokeOnItemInactive() {
        if (getItemModel() == null) return false;
        if (onItemInactive()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public FeedItem<T> getItemModel() {
        return super.getItemModel();
    }

    public boolean syncVisibility(boolean visibility) {
        if (getItemModel() == null) return false;
        if (visibility == mVisibility) return false;
        mVisibility = visibility;
        if (visibility) {
            onItemShown();
        } else {
            onItemHidden();
        }

        if (getItemModel().isStatus(FeedItem.Status.VISIBILITY) == visibility) {
            return false;
        }
        if (visibility) {
            getItemModel().setStatus(FeedItem.Status.VISIBILITY);
        } else {
            getItemModel().removeStatus(FeedItem.Status.VISIBILITY);
        }
        return true;
    }

    public void onDrawOver(Rect rect, Canvas canvas, RecyclerView parent, int position,
            int preViewType, int nextViewType, int orientation) {
    }

    public void onDrawDivider(Rect rect, Canvas canvas, RecyclerView parent, int position,
            int preViewType, int nextViewType) {
        if (mDivider == 0) return;
        if (nextViewType == 0) return;
        if (mColor == Color.TRANSPARENT) return;
        rect.top = rect.bottom - mDivider;
        canvas.drawRect(rect, mPaint);
    }

    public void onDrawDivider(Rect rect, Canvas canvas, RecyclerView parent, int position,
            int preViewType, int nextViewType, int orientation) {
        onDrawDivider(rect, canvas, parent, position, preViewType, nextViewType);
    }

    public void getDividerOffset(Rect outRect, RecyclerView parent, RecyclerView.State state,
            int position, int preViewType, int nextViewType) {
        if (mDivider == 0) return;
        if (nextViewType == 0) return;
        outRect.set(0, 0, 0, mDivider);
    }
}
