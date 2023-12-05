package me.add1.iris.model;

import androidx.annotation.Nullable;

public class PageInfo {
    public final boolean hasMore;
    @Nullable
    public final String nextId;
    public final int totalCount;

    public PageInfo(boolean hasMore, @Nullable String nextId, int totalCount) {
        this.hasMore = hasMore;
        this.nextId = nextId;
        this.totalCount = totalCount;
    }
}
