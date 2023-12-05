package io.agora.cloudgame.utilities;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.cloudgame.AppContext;
import io.agora.cloudgame.widget.RoundedCornersTransformation;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import me.add1.iris.Callback;
import me.add1.iris.utilities.Lazy;

public class ImageUtils {
    private static final int CACHE_SIZE = 200 * 1024 * 1024;

    private static Lazy<Picasso> sPicasso = new Lazy<Picasso>() {
        @Override
        protected Picasso make() {
            AppContext context = AppContext.Companion.get();
            return new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(FileUtils.getCacheDir(context, "resource"),
                            CACHE_SIZE))
                    .build();
        }
    };

    public static void renderImage(@NonNull String url, @NonNull ImageView view,
                                   @DrawableRes int loadPlaceholder,
                                   @DrawableRes int errorPlaceholder) {
        ImageUtils.renderImage(url, view, loadPlaceholder, errorPlaceholder, 0);
    }

    public static void renderImage(@NonNull String url, @NonNull ImageView view,
                                   @DrawableRes int loadPlaceholder,
                                   @DrawableRes int errorPlaceholder, int radius) {
        RequestCreator creator = sPicasso.get().load(url);
        if (loadPlaceholder != 0) creator.placeholder(loadPlaceholder);
        if (errorPlaceholder != 0) creator.error(errorPlaceholder);
        if (radius > 0) {
            creator.transform(new RoundedCornersTransformation(radius, 0,
                    RoundedCornersTransformation.CornerType.ALL));
        }
        creator.into(view);
    }

    public static void renderImage(@NonNull String url, @NonNull ImageView view,
                                   @Nullable Drawable loadPlaceholder,
                                   @Nullable Drawable errorPlaceholder) {
        ImageUtils.renderImage(url, view, loadPlaceholder, errorPlaceholder, 0);
    }

    public static void renderImage(@NonNull String url, @NonNull ImageView view,
                                   @Nullable Drawable loadPlaceholder,
                                   @Nullable Drawable errorPlaceholder, int radius) {
        RequestCreator creator = sPicasso.get().load(url);
        if (loadPlaceholder != null) creator.placeholder(loadPlaceholder);
        if (errorPlaceholder != null) creator.error(errorPlaceholder);
        if (radius > 0) {
            creator.transform(new RoundedCornersTransformation(radius, 0,
                    RoundedCornersTransformation.CornerType.ALL));
        }

        switch (view.getScaleType()) {
            case CENTER_CROP:
                creator.centerCrop();
                creator.fit();
                break;
            case CENTER_INSIDE:
                creator.centerInside();
                creator.fit();
                break;
        }

        creator.into(view);
    }

    @MainThread
    public static void getBitmap(@NonNull String url, @NonNull Callback<Bitmap> callback) {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callback.callback(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                callback.callback(null);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        sPicasso.get().load(url).into(target);
    }

    public static void cancel(@NonNull ImageView view) {
        sPicasso.get().cancelRequest(view);
    }


}
