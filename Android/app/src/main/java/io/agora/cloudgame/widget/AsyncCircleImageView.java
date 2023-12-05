package io.agora.cloudgame.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class AsyncCircleImageView extends AsyncImageView {
    public AsyncCircleImageView(Context context) {
        super(context);
    }

    public AsyncCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AsyncCircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable == null) {
            super.setImageDrawable(null);
        } else if (drawable instanceof BitmapDrawable) {
            this.setImageBitmap(((BitmapDrawable) drawable).getBitmap());
        } else {
            super.setImageDrawable(drawable);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            super.setImageDrawable(null);
        } else {
            super.setImageDrawable(new CircleBitmapDrawable(getResources(), bm));
        }
    }
}
