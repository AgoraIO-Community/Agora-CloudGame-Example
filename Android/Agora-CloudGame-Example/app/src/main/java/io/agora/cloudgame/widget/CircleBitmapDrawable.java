package io.agora.cloudgame.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * A drawable for displaying a circular bitmap. This is a wrapper over RoundedBitmapDrawable,
 * since that implementation doesn't behave quite as desired.
 * <p>
 * Note that not all drawable functionality is passed to the RoundedBitmapDrawable at this
 * time. Feel free to add more as necessary.
 */
public class CircleBitmapDrawable extends Drawable {
    private final Resources mResources;
    private Bitmap mBitmap;
    private RoundedBitmapDrawable mDrawable;
    private int mAlpha = -1;
    private ColorFilter mCf = null;

    public CircleBitmapDrawable(@NonNull Resources res, @NonNull Bitmap bitmap) {
        mBitmap = bitmap;
        mResources = res;
    }

    @Override
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;
        mDrawable = RoundedBitmapDrawableFactory.create(mResources, mBitmap);
        mDrawable.setBounds(bounds);
        mDrawable.setAntiAlias(true);
        mDrawable.setCornerRadius(Math.min(width, height) / 2f);
        if (mAlpha != -1) {
            mDrawable.setAlpha(mAlpha);
        }
        if (mCf != null) {
            mDrawable.setColorFilter(mCf);
        }
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public int getOpacity() {
        return mDrawable != null ? mDrawable.getOpacity() : PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha == mAlpha) return;
        mAlpha = alpha;
        if (mDrawable != null) {
            mDrawable.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mCf) {
            return;
        }
        mCf = cf;
        if (mDrawable != null) {
            mDrawable.setColorFilter(cf);
            invalidateSelf();
        }
    }

    /**
     * Convert the drawable to a bitmap.
     *
     * @param size The target size of the bitmap in pixels.
     * @return A bitmap representation of the drawable.
     */
    public Bitmap toBitmap(int size) {
        Bitmap largeIcon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(largeIcon);
        Rect bounds = getBounds();
        setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        draw(canvas);
        setBounds(bounds);
        return largeIcon;
    }
}
