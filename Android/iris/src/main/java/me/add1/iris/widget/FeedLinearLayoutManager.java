package me.add1.iris.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class FeedLinearLayoutManager extends LinearLayoutManager {
    private int mSmoothScrollerOffsetY;


    public FeedLinearLayoutManager(Context context) {
        super(context);
    }

    public FeedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public FeedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
            int position) {
        LinearSmoothTopScroller scroller =
                new LinearSmoothTopScroller(recyclerView.getContext(), mSmoothScrollerOffsetY,
                        LinearSmoothScroller.SNAP_TO_START);
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, int position, int snapTo) {
        LinearSmoothTopScroller scroller = new LinearSmoothTopScroller(recyclerView.getContext(),
                mSmoothScrollerOffsetY, snapTo);
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    public void setSmoothScrollerOffsetY(int offsetY) {
        mSmoothScrollerOffsetY = offsetY;
    }

    static class LinearSmoothTopScroller extends LinearSmoothScroller {
        public final int scrollOffsetY;
        public final int snapTo;

        public LinearSmoothTopScroller(Context context, int scrollOffsetY, int snapTo) {
            super(context);
            this.scrollOffsetY = scrollOffsetY;
            this.snapTo = snapTo;
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return snapTo;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return snapTo;
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int
                snapPreference) {
            switch (snapPreference) {
                case SNAP_TO_START:
                    return boxStart - viewStart + scrollOffsetY;
                case SNAP_TO_END:
                    return boxEnd - viewEnd;
                case SNAP_TO_ANY:
                    final int dtStart = boxStart - viewStart;
                    if (dtStart > 0) {
                        return dtStart;
                    }
                    final int dtEnd = boxEnd - viewEnd;
                    if (dtEnd < 0) {
                        return dtEnd;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("snap preference should be one of the"
                            + " constants defined in SmoothScroller, starting with SNAP_");
            }
            return 0;
        }
    }
}

