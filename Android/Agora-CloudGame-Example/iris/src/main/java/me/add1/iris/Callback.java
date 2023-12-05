package me.add1.iris;

import androidx.annotation.NonNull;

public interface Callback<T> {
    @NonNull
    Callback<Object> EMPTY = t -> { };

    @SuppressWarnings("unchecked")
    @NonNull
    static <V> Callback<V> empty() {
        return (Callback<V>) EMPTY;
    }

    void callback(T t);
}