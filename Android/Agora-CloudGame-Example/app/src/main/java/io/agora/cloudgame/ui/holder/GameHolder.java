package io.agora.cloudgame.ui.holder;


import androidx.annotation.NonNull;

import io.agora.cloudgame.example.R;
import io.agora.cloudgame.example.databinding.HolderTrainCampBinding;
import io.agora.cloudgame.network.model.GameEntity;

import me.add1.iris.feed.BindingFeedItemViewHolder;
import me.add1.iris.feed.FeedItem;

public class GameHolder extends BindingFeedItemViewHolder<HolderTrainCampBinding,
        GameEntity> {

    public static final String CLICK_ITEM = "item_click";

    public static final Creator<GameHolder> CREATOR =
            (inflater, parent) -> new GameHolder(HolderTrainCampBinding.inflate(inflater,
                    parent, false), R.dimen.divider_gym
                    , R.color.colorDivider);

    @NonNull
    private final HolderTrainCampBinding mBinding;

    public GameHolder(@NonNull
                      HolderTrainCampBinding binding, int divider, int color) {
        super(binding, divider, color);
        mBinding = binding;
    }

    @Override
    public void onBind(@NonNull FeedItem<GameEntity> feedItem,
                       boolean isUpdate) {
        super.onBind(feedItem, isUpdate);
        mBinding.setData(feedItem.model);
//        mBinding.asyncView.load(feedItem.model.images.get(0),null);
    }

    @Override
    public void registerClickListener(OnItemClickListener<FeedItem<GameEntity>> listener) {
        super.registerClickListener(listener);

        listener.onItemClick(GameHolder.this, itemView, getItemModel(), CLICK_ITEM);
    }
}
