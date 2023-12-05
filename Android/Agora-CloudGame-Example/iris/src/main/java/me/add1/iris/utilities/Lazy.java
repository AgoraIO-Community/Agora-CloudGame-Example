package me.add1.iris.utilities;

import androidx.core.util.Supplier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class Lazy<T> {

    private static final Object NONE = new Object();

    private static class LazyFromSupplier<T> extends Lazy<T> {
        @NonNull
        private final Supplier<T> mSupplier;

        LazyFromSupplier(@NonNull Supplier<T> supplier) {
            mSupplier = new OneShotSupplier(supplier);
        }

        @Override
        protected final T make() {
            return mSupplier.get();
        }
    }

    @SuppressWarnings("unchecked")
    private volatile T mValue = (T) NONE;

    @NonNull
    public static <T> Lazy<T> from(@NonNull Supplier<T> supplier) {
        return new LazyFromSupplier<>(supplier);
    }

    /**
     * The same object is guaranteed to be returned by this method.
     * @return initialized object
     */
    public T get() {
        if (mValue != NONE) {
            return mValue;
        }
        synchronized (this) {
            if (mValue == NONE) {
                mValue = make();
            }
            return mValue;
        }
    }

    /**
     * @return true if the object has already been created
     */
    public boolean has() {
        return mValue != NONE;
    }

    /**
     * This method is guaranteed to be called only once.
     * @return initialized object
     */
    protected abstract T make();

    /**
     * Asynchronously initializes this {@link Lazy} object.
     * @see #async(Lazy)
     */
    @NonNull
    public Lazy<T> async() {
        return async(this);
    }

    @NonNull
    public static <L extends Lazy<?>> L async(@NonNull final L lazy) {
        return async(lazy, null);
    }

    /**
     * Asynchronously initializes passed {@link Lazy} object.
     * This flavor of {@link #async()} should be used in order to preserve implementor's type.
     *
     * @param onDone must be called on main thread, if any
     */
    @NonNull
    public static <L extends Lazy<?>> L async(@NonNull final L lazy, @Nullable Runnable onDone) {
        if (lazy.has()) {
            if (onDone != null) {
                ThreadUtils.postOnUiThread(onDone);
            }
        } else {
            ThreadUtils.postOnBackgroundThread(() -> {
                lazy.get();
                if (onDone != null) {
                    ThreadUtils.postOnUiThread(onDone);
                }
            });
        }
        return lazy;
    }
}