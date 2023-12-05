package me.add1.iris.widget;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class ViewUtils {
    /** Handles layout_sort_latest_tab_item state change for a view. */
    public interface LayoutListener {
        void onLayout();
    }

    public static void addOneShotLayoutListener(final View view, final LayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("WrongCall")
            @Override
            public void onGlobalLayout() {
                listener.onLayout();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }


    /**
     * ClickableSpan which adds support for defining a custom link color.
     */
    public abstract static class LinkSpan extends ClickableSpan {
        private final int mLinkColor;
        private final int mHighlightColor;
        private boolean mHighlighted;

        public LinkSpan(int linkColor, int highlightColor) {
            mLinkColor = linkColor;
            mHighlightColor = highlightColor;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.bgColor = mHighlighted ? mHighlightColor : Color.TRANSPARENT;
            ds.setColor(mLinkColor);
        }

        private void setHighlighted(boolean highlighted) {
            mHighlighted = highlighted;
        }
    }

    public static class LinkMovementMethod extends android.text.method.LinkMovementMethod {
        private static LinkMovementMethod sInstance;
        private LinkSpan mHighlightedLink;

        public static LinkMovementMethod getInstance() {
            if (sInstance == null) {
                sInstance = new LinkMovementMethod();
            }
            return sInstance;
        }

        private void setHighlightedLink(TextView widget, LinkSpan link) {
            if (link == mHighlightedLink) {
                return;
            }
            widget.invalidate();
            if (mHighlightedLink != null) {
                mHighlightedLink.setHighlighted(false);
            }
            mHighlightedLink = link;
            if (mHighlightedLink != null) {
                mHighlightedLink.setHighlighted(true);
            }
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN ||
                    action == MotionEvent.ACTION_MOVE) {

                int x = ((int) event.getX()) + widget.getScrollX() - widget.getTotalPaddingLeft();
                int y = ((int) event.getY()) + widget.getScrollY() - widget.getTotalPaddingTop();

                LinkSpan[] link = null;
                if (y >= 0 && y < widget.getHeight()) {
                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int lineLeft = (int) layout.getLineLeft(line);
                    int lineRight = (int) layout.getLineRight(line);
                    if (x >= lineLeft && x <= lineRight) {
                        int off = layout.getOffsetForHorizontal(line, x);
                        link = buffer.getSpans(off, off, LinkSpan.class);
                    }
                }

                if (link != null && link.length != 0) {
                    setHighlightedLink(widget, link[0]);
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                        setHighlightedLink(widget, null);
                    }
                    return true;
                } else {
                    setHighlightedLink(widget, null);
                }
            }
            // Don't let super handle any touch event since it may click links even when touch is
            // moved far away. Note: This breaks scrolling.
            return false;
        }
    }

}
