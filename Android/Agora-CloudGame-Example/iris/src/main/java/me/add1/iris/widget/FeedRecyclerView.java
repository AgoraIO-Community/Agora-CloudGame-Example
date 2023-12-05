package me.add1.iris.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import me.add1.iris.Check;
import me.add1.iris.feed.FeedItemViewHolder;

public class FeedRecyclerView extends RecyclerView
        implements ViewTreeObserver.OnGlobalLayoutListener,
        ViewTreeObserver.OnScrollChangedListener {

    private static final float SHOWN_PERCENT_THRESHOLD = 0.5f;
    private static final int SCROLL_INVALID = -1;
    @NonNull
    private Rect mLocalVisibleRect;
    @NonNull
    private List<FeedItemViewHolder<?>> mLastVisibleChildren;
    @Nullable
    private FeedItemViewHolder<?> mLastActiveItemViewHolder;
    @Nullable
    private OnItemVisibilityListener mItemVisibilityListener;
    private int mLastScrollState = SCROLL_INVALID;
    private boolean mActiveEnable;

    public FeedRecyclerView(Context context) {
        super(context);
        initialize(context);
    }

    public FeedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public FeedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mLastVisibleChildren = new ArrayList<>();
        mLocalVisibleRect = new Rect();
    }

    public interface OnItemVisibilityListener {
        void onItemShow(FeedItemViewHolder<?> holder);

        void onItemHidden(FeedItemViewHolder<?> holder);
    }

    public void setOnItemVisibilityListener(@Nullable OnItemVisibilityListener listener) {
        mItemVisibilityListener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnScrollChangedListener(this);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        syncChildVisibilityChange(getCurrentVisibilityHolders());
        onScrollChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        getViewTreeObserver().removeOnScrollChangedListener(this);
        clearChildVisibility();
        if (mLastActiveItemViewHolder != null) {
            mLastActiveItemViewHolder.invokeOnItemInactive();
            mLastActiveItemViewHolder = null;
        }
        mLastScrollState = SCROLL_INVALID;
        onScrollChanged();
        super.onDetachedFromWindow();
    }

    @Override
    public void onScrollChanged() {
        if (getLocalVisibleRect(mLocalVisibleRect)) {
            syncChildVisibilityChange(getCurrentVisibilityHolders());
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (mLastScrollState == state) return;
        mLastScrollState = state == SCROLL_INVALID ? SCROLL_STATE_IDLE : state;
        List<FeedItemViewHolder<?>> holders = getCurrentVisibilityHolders();
        syncChildScrollStateChanged(state, holders);
        if (state == SCROLL_STATE_IDLE) {
            syncChildActiveChanged(holders);
        }
    }

    public void refreshScrollState() {
        mLastScrollState = SCROLL_INVALID;
        post(() -> onScrollStateChanged(getScrollState()));
    }

    @Override
    public void onGlobalLayout() {
        if (mActiveEnable) syncChildVisibilityChange(getCurrentVisibilityHolders());
    }

    @Override
    public void onChildDetachedFromWindow(View child) {
        super.onChildDetachedFromWindow(child);
        if (mLastActiveItemViewHolder != null && mLastActiveItemViewHolder.itemView == child) {
            mLastActiveItemViewHolder.invokeOnItemInactive();
            mLastActiveItemViewHolder = null;
        }
    }

    public void setActiveEnable(boolean enable) {
        mActiveEnable = enable;
        syncChildVisibilityChange(getCurrentVisibilityHolders());
        syncChildActiveChanged(getCurrentVisibilityHolders());
    }

    public boolean isActiveEnable() {
        return mActiveEnable;
    }

    public void forceChangeActiveHolder(@NonNull FeedItemViewHolder<?> holder) {
        if (mLastActiveItemViewHolder != holder) {
            if (mLastActiveItemViewHolder != null) mLastActiveItemViewHolder.invokeOnItemInactive();
            mLastActiveItemViewHolder = holder;
            mLastActiveItemViewHolder.invokeOnItemActive();
        }
    }

    public void smoothScrollToPosition(int position, int snapTo) {
        if (getLayoutManager() == null) {
            if (Check.ON) Check.shouldNeverHappen();
            return;
        }
        if (getLayoutManager() instanceof FeedLinearLayoutManager) {
            FeedLinearLayoutManager layoutManager =
                    (FeedLinearLayoutManager) getLayoutManager();
            layoutManager.smoothScrollToPosition(this, position, snapTo);
        }
    }

    public void smoothChangeActiveHolder(@NonNull FeedItemViewHolder<?> holder) {
        if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
            smoothScrollToPosition(holder.getAdapterPosition());
            if (!isActiveEnable()) setActiveEnable(true);
        }
    }

    @Nullable
    public FeedItemViewHolder<?> getLastActiveItemViewHolder() {
        return mLastActiveItemViewHolder;
    }

    public void syncChildActiveChanged() {
        syncChildActiveChanged(getCurrentVisibilityHolders());
    }

    public void syncChildActiveChanged(@Nullable List<FeedItemViewHolder<?>> holders) {
        if (!mActiveEnable) {
            if (mLastActiveItemViewHolder != null) {
                mLastActiveItemViewHolder.invokeOnItemInactive();
                mLastActiveItemViewHolder = null;
            }
            return;
        }
        if (holders != null) {
            for (FeedItemViewHolder<?> holder : holders) {
                if (mLastActiveItemViewHolder == holder) break;

                if (holder.invokeOnItemActive()) {
                    if (mLastActiveItemViewHolder != null) {
                        mLastActiveItemViewHolder.invokeOnItemInactive();
                    }
                    mLastActiveItemViewHolder = holder;
                    break;
                }
            }
        } else if (mLastActiveItemViewHolder != null) {
            mLastActiveItemViewHolder.invokeOnItemInactive();
            mLastActiveItemViewHolder = null;
        }
    }

    public void syncChildScrollStateChanged(int state,
            @Nullable List<FeedItemViewHolder<?>> holders) {
        if (holders == null) return;
        for (FeedItemViewHolder<?> holder : holders) {
            if (state == SCROLL_STATE_IDLE) {
                holder.onItemIdle();
            } else {
                holder.onItemScroll();
            }
        }
    }

    public void syncChildVisibilityChange(@Nullable List<FeedItemViewHolder<?>> holders) {
        if (holders == null) {
            for (FeedItemViewHolder<?> holder : mLastVisibleChildren) {
                if (holder.syncVisibility(false)) {
                    if (mItemVisibilityListener != null) {
                        mItemVisibilityListener.onItemHidden(holder);
                    }
                }
            }
            if (mLastActiveItemViewHolder != null) {
                mLastActiveItemViewHolder.invokeOnItemInactive();
                mLastActiveItemViewHolder = null;
            }
            mLastVisibleChildren.clear();
        } else {
            for (FeedItemViewHolder<?> holder : holders) {
                if (holder.syncVisibility(true)) {
                    if (mItemVisibilityListener != null) mItemVisibilityListener.onItemShow(holder);
                }
            }
            mLastVisibleChildren.removeAll(holders);

            for (FeedItemViewHolder<?> holder : mLastVisibleChildren) {
                if (holder.syncVisibility(false)) {
                    if (mItemVisibilityListener != null) {
                        mItemVisibilityListener.onItemHidden(holder);
                    }
                    if (mLastActiveItemViewHolder == holder) {
                        mLastActiveItemViewHolder.invokeOnItemInactive();
                        mLastActiveItemViewHolder = null;
                    }
                }
            }
            mLastVisibleChildren = holders;
        }
    }

    @Nullable
    public List<FeedItemViewHolder<?>> getCurrentVisibilityHolders() {
        if (!getLocalVisibleRect(mLocalVisibleRect)) return null;

        List<FeedItemViewHolder<?>> visibleChildren = new ArrayList<>();
        if (getLayoutManager() == null) return visibleChildren;
        int count = getLayoutManager().getChildCount();
        int index = 0;
        while (index < count) {
            View view = getLayoutManager().getChildAt(index);
            if (isShown(view)) {
                ViewHolder holder = getChildViewHolder(view);
                if (holder == null) continue;
                if (holder instanceof FeedItemViewHolder) {
                    visibleChildren.add((FeedItemViewHolder) holder);
                }
            }
            index++;
        }
        return visibleChildren;
    }

    private void clearChildVisibility() {
        for (FeedItemViewHolder<?> holder : mLastVisibleChildren) {
            if (holder.syncVisibility(false) && mItemVisibilityListener != null) {
                mItemVisibilityListener.onItemHidden(holder);
            }
        }

        mLastVisibleChildren.clear();
    }

    private boolean isShown(View itemView) {
        if (itemView.getLocalVisibleRect(mLocalVisibleRect)) {
            return (itemView.getWidth() != 0
                    && mLocalVisibleRect.width() * 1.0f / itemView.getWidth()
                    > SHOWN_PERCENT_THRESHOLD)
                    && (itemView.getHeight() != 0
                    && mLocalVisibleRect.height() * 1.0f / itemView.getHeight()
                    > SHOWN_PERCENT_THRESHOLD);
        }

        return false;
    }
}