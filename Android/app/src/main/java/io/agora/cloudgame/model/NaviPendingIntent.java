package io.agora.cloudgame.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.add1.iris.PageDelegate;

public class NaviPendingIntent {
    public enum Tab {
        HOME(0), FRIEND(1), PROFILE(2), GYM(3), SESSION(4);

        public final int val;

        Tab(int val) {
            this.val = val;
        }
    }

    @Nullable
    public final PageDelegate delegate;
    @NonNull
    public final Tab tab;
    public final boolean existTitle;

    public NaviPendingIntent(@NonNull Tab tab, @Nullable PageDelegate delegate,
                             boolean existTitle) {
        this.tab = tab;
        this.delegate = delegate;
        this.existTitle = existTitle;
    }
}
