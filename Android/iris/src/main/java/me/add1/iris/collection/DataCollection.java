// -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
//
// Copyright (C) 2018 Opera Software AS. All rights reserved.
//
// This file is an original work developed by Opera Software AS

package me.add1.iris.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;

public class DataCollection<T extends ViewItem> implements List<T> {

    public interface OnDataSetChangedObserver<T> {

        default void onItemAdded(int position, T item) {
        }

        default void onItemChanged(int position, T item) {
        }

        default void onItemMoved(int src, int dest) {
        }

        default void onItemRemoved(int position) {
        }

        default void onItemReplaced(int position, Collection<? extends T> items) {
        }

        default void onItemsAllReplaced(Collection<? extends T> items) {
        }

        default void onItemsAdded(int position, Collection<? extends T> items) {
        }

        default void onItemsRemoved(int start, int end) {
        }

        default void onItemsRemoved() {
        }
    }

    protected List<T> mItems;
    private List<OnDataSetChangedObserver<T>> mObservers;

    public DataCollection() {
        this(new ArrayList<>());
    }

    public DataCollection(List<T> items) {
        mItems = items;
        mObservers = new ArrayList<>();
    }

    public void addOnDataSetChangedObserver(OnDataSetChangedObserver<T> observer) {
        mObservers.add(observer);
    }

    public void removeOnDataSetChangedObserver() {
        mObservers.clear();
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public boolean isEmpty() {
        return mItems == null || mItems.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return mItems.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return mItems.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mItems.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return mItems.toArray(a);
    }

    @Override
    public boolean add(final T t) {
        boolean result = mItems.add(t);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemAdded(size() - 1, t);
            }
        }
        return true;
    }

    public T get(@NonNull final String id) {
        for (T i : mItems) {
            if (id.equals(i.getId())) {
                return i;
            }
        }
        return null;
    }

    @Override
    public boolean remove(final Object o) {
        int index = mItems.indexOf(o);
        boolean result = mItems.remove(o);
        if (index >= 0 && result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemRemoved(index);
            }
        }
        return true;
    }

    public boolean remove(@NonNull final String id) {
        if (id == null) return false;
        T o = get(id);
        if (o == null) return false;
        return remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return mItems.containsAll(c);
    }

    public boolean updateAllItems() {
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemsAllReplaced(mItems);
        }
        return true;
    }

    public boolean replaceAll(@NonNull final Collection<? extends T> c) {
        mItems.clear();
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemsRemoved();
        }
        boolean result = mItems.addAll(c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemsAllReplaced(mItems);
            }
        }
        return true;
    }

    public boolean replaceItem(int index, T t) {
        mItems.remove(index);
        mItems.add(index, t);
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemChanged(index, t);
        }
        return true;
    }

    public boolean updateValue(T t) {
        if (mItems.indexOf(t) > -1) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemChanged(mItems.indexOf(t), t);
            }
            return true;
        }
        return false;
    }

    public boolean replaceBelowIndex(int index, @NonNull final Collection<? extends T> c) {
        for (int i = index + 1, length = mItems.size(); i < length; ++i) {
            mItems.remove(i);
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemRemoved(i);
            }
            --length;
            --i;
        }
        addAll(index + 1, c);
        return true;
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends T> c) {
        int size = mItems.size();
        boolean result = mItems.addAll(c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemsAdded(size, c);
            }
        }
        return true;
    }

    public boolean replaceAll(final int index, @NonNull final Collection<? extends T> c) {
        T t = mItems.remove(index);
        boolean result = mItems.addAll(index, c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemReplaced(index, c);
            }
        } else {
            if (t != null) {
                for (OnDataSetChangedObserver<T> item : mObservers) {
                    item.onItemRemoved(index);
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(final int index, @NonNull final Collection<? extends T> c) {
        boolean result = mItems.addAll(index, c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemsAdded(index, c);
                if (index != 0) {
                    item.onItemChanged(index - 1, get(index - 1));
                }
            }
        }
        return true;
    }


    @Override
    public boolean removeAll(@NonNull final Collection<?> c) {
        boolean result = mItems.removeAll(c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemsRemoved();
            }
        }
        return result;
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {
        boolean result = mItems.retainAll(c);
        if (result) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemsAllReplaced(mItems);
            }
        }
        return result;
    }

    @Override
    public void clear() {
        mItems.clear();
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemsRemoved();
        }
    }

    @Override
    public boolean equals(Object o) {
        return mItems.equals(o);
    }

    @Override
    public T get(int index) {
        if (index >= mItems.size()) return null;
        return mItems.get(index);
    }

    @Override
    public T set(final int index, final T element) {
        T t = mItems.set(index, element);
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemChanged(index, t);
        }
        return t;
    }

    public void swap(final int src, final int dest) {
        Collections.swap(mItems, src, dest);
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemMoved(src, dest);
        }
    }

    @Override
    public void add(final int index, final T element) {
        mItems.add(index, element);
        for (OnDataSetChangedObserver<T> item : mObservers) {
            item.onItemAdded(index, element);
        }
    }

    @Override
    public T remove(final int index) {
        if (index >= mItems.size() || index < 0) {
            return null;
        }
        T t = mItems.remove(index);
        if (t != null) {
            for (OnDataSetChangedObserver<T> item : mObservers) {
                item.onItemRemoved(index);
                if (index != 0) {
                    item.onItemChanged(index - 1, get(index - 1));
                }
            }
        }
        return t;
    }

    @Override
    public int indexOf(Object o) {
        synchronized (mItems) {
            return mItems.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (mItems) {
            return mItems.lastIndexOf(o);
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        synchronized (mItems) {
            return mItems.listIterator();
        }
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int index) {
        synchronized (mItems) {
            return mItems.listIterator(index);
        }
    }

    @NonNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        synchronized (mItems) {
            return mItems.subList(fromIndex, toIndex);
        }
    }

    public ArrayList<T> getItems() {
        return (ArrayList<T>) mItems;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
