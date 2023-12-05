package io.agora.cloudgame;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.agora.cloudgame.delegate.GameGoDelegate;
import io.agora.cloudgame.example.BuildConfig;
import io.agora.cloudgame.example.R;
import io.agora.cloudgame.fragment.TitleFragment;
import io.agora.cloudgame.holder.GameHolder;
import io.agora.cloudgame.network.model.ApiResult;
import io.agora.cloudgame.network.model.GameEntity;
import io.agora.cloudgame.network.model.GameResult;
import io.agora.cloudgame.utilities.NavigationUtils;
import me.add1.iris.ApiRequestException;
import me.add1.iris.collection.CollectionAdapter;
import me.add1.iris.collection.CollectionItemViewHolder;
import me.add1.iris.feed.FeedItem;
import me.add1.iris.feed.RefreshableFeedsDelegate;
import me.add1.iris.feed.RemoteFeedCollection;
import me.add1.iris.model.FeedType;
import me.add1.iris.model.PageInfo;

public class MainTabsDelegate extends RefreshableFeedsDelegate {


    private static final int FEED_TYPE_ITEM = 0x1001;

    private long mLastBackPressedTime;

    protected boolean onStatusBarDarkFont() {
        return true;
    }

    protected int onStatusBarColor() {
        return R.color.white;
    }

    protected boolean immerse() {
        return true;
    }

    private GameListRemoteCollection mCollection;

    @Override
    protected String getOrigin() {
        return "main-tab";
    }


    private void setImmerse() {
        if (!immerse()) {
            return;
        }

        ImmersionBar.with(Objects.requireNonNull(getActivity()))
                .statusBarColor(onStatusBarColor())
                .statusBarDarkFont(onStatusBarDarkFont(), 0.2f)
                .init();
    }

    @Override
    protected void registerViewHolder(CollectionAdapter<FeedItem<?>> adapter) {
        adapter.registerViewHolder(FEED_TYPE_ITEM, GameHolder.CREATOR);
    }

    @Override
    protected void onViewHolderClick(@NonNull CollectionItemViewHolder<FeedItem<?>> holder,
                                     View itemView, FeedItem<?> model, @Nullable String type) {
        super.onViewHolderClick(holder, itemView, model, type);

        GameEntity entity = (GameEntity) model.model;

        NavigationUtils.enterNewFragment(Objects.requireNonNull(getRootFragmentManager()), TitleFragment.newInstance(new GameGoDelegate(entity)), R.id.content);

    }

    @Override
    protected void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != getActivity()) {
            getActivity().findViewById(R.id.version).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected RemoteFeedCollection getCollection() {
        if (mCollection == null) {
            mCollection = new GameListRemoteCollection();
        }

        return mCollection;
    }

    @NonNull
    protected RareBackend.ApiRequestCallback<GameResult> obtainRequestCallback(
            @Nullable FeedItem<PageInfo> pageInfoFeedItem,
            @Nullable RemoteFeedCollection.RequestCallback callback) {
        return new RareBackend.ApiRequestCallback<GameResult>() {
            @Override
            public void onSucceed(ApiResult<GameResult> result) {
                List<FeedItem<?>> items = new ArrayList<>();

                for (GameEntity entity : result.data.list) {
                    items.add(new FeedItem<>(FEED_TYPE_ITEM, "", entity));
                }

                if (pageInfoFeedItem == null) {
                    getCollection().clear();
                    getCollection().addAll(items);
                    if (!getCollection().hasData()) {
                        getCollection().add(new FeedItem<>(FeedType.EMPTY,
                                UUID.randomUUID().toString(), null));
                    }
                } else {
                    int index = getCollection().indexOf(pageInfoFeedItem);
                    if (index >= 0) {
                        getCollection().replaceAll(index, items);
                    }
                }

                if (callback != null) {
                    callback.onSuccess(items);
                }
            }

            @Override
            public void onFailure(ApiRequestException e) {
                if (isAlive() && !TextUtils.isEmpty(e.message)) {
                    showToast(e.code + " " + e.message);
                }
                if (!getCollection().hasData()) {
                    getCollection()
                            .add(new FeedItem<>(FeedType.EMPTY, UUID.randomUUID().toString(), ""));
                }
                if (callback != null) {
                    callback.onError(e);
                }
            }
        };
    }

    private class GameListRemoteCollection extends RemoteFeedCollection {

        @Override
        public void refresh(@Nullable RequestCallback callback) {
            RareBackend.getInstance().getGames(BuildConfig.APP_ID, obtainRequestCallback(null, callback));
        }

        @Override
        public void loadMore(@NonNull FeedItem<PageInfo> item, @Nullable RequestCallback callback) {

        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }

    @Override
    protected void onActive() {
        super.onActive();
        setImmerse();
    }

    @Override
    public boolean onBackPressed() {
        if (System.currentTimeMillis() - mLastBackPressedTime > 2000) {
            mLastBackPressedTime = System.currentTimeMillis();
            showToast(R.string.home_dou_notice);
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        return true;
    }

}
