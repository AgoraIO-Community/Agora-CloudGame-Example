package io.agora.cloudgame.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import io.agora.cloudgame.example.R;
import io.agora.cloudgame.utilities.ImageUtils;

public class AsyncImageView extends AppCompatImageView {

    @DrawableRes
    private int mPlaceHolderRes;
    @DrawableRes
    private int mErrorRes;

    public int mRadius;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        mPlaceHolderRes = array.getResourceId(R.styleable.AsyncImageView_placeholder, 0);
        mErrorRes = array.getResourceId(R.styleable.AsyncImageView_error, 0);
        mRadius = array.getDimensionPixelSize(R.styleable.AsyncImageView_radius, 0);
        array.recycle();
    }

    @Override
    public View findFocus() {
        return super.findFocus();
    }

    public void load(@NonNull String url, @Nullable Drawable loadPlaceholder) {
        load(url, loadPlaceholder, null);
    }

    public void load(@NonNull String url, @Nullable Drawable loadPlaceholder,
                     @Nullable Drawable errorPlaceholder) {
        if (TextUtils.isEmpty(url)) {
            clear(null);
            if (mErrorRes != 0) setImageResource(mErrorRes);
            return;
        }
        ImageUtils.renderImage(url, this,
                loadPlaceholder != null ? loadPlaceholder : mPlaceHolderRes == 0 ? null :
                        ResourcesCompat.getDrawable(getResources(), mPlaceHolderRes, null),
                errorPlaceholder != null ? errorPlaceholder : mErrorRes == 0 ? null :
                        ResourcesCompat.getDrawable(getResources(), mErrorRes, null), mRadius);
    }

    public void load(@NonNull String url, @DrawableRes int loadPlaceholder,
                     @DrawableRes int errorPlaceholder) {
        if (TextUtils.isEmpty(url)) {
            clear(null);
            return;
        }

        ImageUtils.renderImage(url, this, loadPlaceholder == 0 ? mPlaceHolderRes : loadPlaceholder,
                errorPlaceholder == 0 ? mErrorRes : errorPlaceholder);
    }

    public void clear(@Nullable Drawable drawable) {
        ImageUtils.cancel(this);
        setImageDrawable(drawable);
    }
}
