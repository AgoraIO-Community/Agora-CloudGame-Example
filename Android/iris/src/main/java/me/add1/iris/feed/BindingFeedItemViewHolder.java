package me.add1.iris.feed;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import me.add1.iris.feed.FeedItemViewHolder;

public class BindingFeedItemViewHolder<T extends ViewDataBinding, K> extends FeedItemViewHolder<K> {
    protected T mBinding;

    public BindingFeedItemViewHolder(@NonNull T binding, int divider, int color) {
        super(binding.getRoot(), divider, color);
        mBinding = binding;
    }
}
