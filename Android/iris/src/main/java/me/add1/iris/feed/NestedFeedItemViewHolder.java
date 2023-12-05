package me.add1.iris.feed;

import android.view.View;

import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.add1.iris.collection.CollectionAdapter;
import me.add1.iris.collection.CollectionItemViewHolder;
import me.add1.iris.collection.DataCollection;
import me.add1.iris.collection.ViewItem;
import me.add1.iris.databinding.HolderNestedBinding;
import me.add1.iris.widget.FeedItemDecoration;

public abstract class NestedFeedItemViewHolder
        extends BindingFeedItemViewHolder<HolderNestedBinding, List<FeedItem<?>>>
        implements CollectionItemViewHolder.OnItemClickListener {

    public static abstract class NestedViewPanel extends NestedFeedItemViewHolder {
        DataCollection<FeedItem<?>> collection;

        public NestedViewPanel(@NonNull HolderNestedBinding binding) {
            super(binding, 0, 0);
        }

        @Override
        protected void resetCollection() {
            getCollection().clear();
        }

        @Override
        public DataCollection<FeedItem<?>> getCollection() {
            if (collection == null) collection = new DataCollection<>();
            return collection;
        }

        @Override
        public void onItemClick(@NonNull CollectionItemViewHolder item, View itemView,
                                ViewItem model, @Nullable String type) {
        }
    }

    protected class NestedCollectionChangedObserver
            implements DataCollection.OnDataSetChangedObserver<FeedItem<?>> {
        protected CollectionAdapter<FeedItem<?>> adapter;

        protected NestedCollectionChangedObserver(CollectionAdapter<FeedItem<?>> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemAdded(int position, FeedItem<?> item) {
            adapter.notifyItemInserted(position);
        }

        @Override
        public void onItemChanged(int position, FeedItem<?> item) {
            adapter.notifyItemChanged(position);
        }

        @Override
        public void onItemMoved(int src, int dest) {
            adapter.notifyItemMoved(src, dest);
        }

        @Override
        public void onItemRemoved(int position) {
            adapter.notifyItemRemoved(position);
        }

        @Override
        public void onItemReplaced(int position, Collection<? extends FeedItem<?>> items) {
            adapter.notifyItemRangeChanged(position, items.size());
        }

        @Override
        public void onItemsAllReplaced(Collection<? extends FeedItem<?>> items) {
            adapter.notifyItemRangeChanged(0, items.size());
        }

        @Override
        public void onItemsAdded(int position, Collection<? extends FeedItem<?>> items) {
            adapter.notifyItemRangeInserted(position, items.size());
        }

        @Override
        public void onItemsRemoved(int start, int end) {
            adapter.notifyItemRangeRemoved(start, end);
        }

        @Override
        public void onItemsRemoved() {
            adapter.notifyDataSetChanged();
        }
    }

    public NestedFeedItemViewHolder(@NonNull HolderNestedBinding binding, int divider, int color) {
        super(binding, divider, color);
        FeedItemDecoration decoration = new FeedItemDecoration();
        decoration.setOrientation(FeedItemDecoration.HORIZONTAL);
        mBinding.nestedRecycler.addItemDecoration(decoration);
        CollectionAdapter<FeedItem<?>> adapter = new CollectionAdapter();
        mBinding.nestedRecycler.setAdapter(adapter);
        adapter.setCollection(getCollection());
        getCollection().addOnDataSetChangedObserver(new NestedCollectionChangedObserver(adapter));
        registerViewHolder(adapter);
        adapter.setClickListener(this);
    }

    public void onBind(@NonNull List<FeedItem<?>> model) {
        getCollection().clear();
        getCollection().addAll(model);
        mBinding.nestedRecycler.setLayoutManager(createLayoutManager());
    }

    @Override
    public void onBind(@NonNull FeedItem<List<FeedItem<?>>> model, boolean isUpdate) {
        super.onBind(model, isUpdate);
        getCollection().clear();
        getCollection().addAll(model.model);
        mBinding.nestedRecycler.setLayoutManager(createLayoutManager());
    }

    @Override
    public void onUnbind() {
        super.onUnbind();
        resetCollection();
        mBinding.nestedRecycler.setLayoutManager(null);
    }

    protected abstract void registerViewHolder(CollectionAdapter<FeedItem<?>> adapter);

    protected abstract void resetCollection();

    protected abstract DataCollection<FeedItem<?>> getCollection();

    protected RecyclerView.LayoutManager createLayoutManager() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL,
                        false);
        return layoutManager;
    }
}
