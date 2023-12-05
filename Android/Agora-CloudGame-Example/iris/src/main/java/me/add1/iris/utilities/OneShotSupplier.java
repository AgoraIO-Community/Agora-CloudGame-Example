package me.add1.iris.utilities;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import me.add1.iris.Check;

public class OneShotSupplier<T> implements Supplier<T> {
    // The supplier may be called on any thread so make the reference atomic to ensure "one shot".
    @Nullable
    private volatile AtomicReference<Supplier<T>> mSupplierRef;

    public OneShotSupplier(@NonNull Supplier<T> supplier) {
        mSupplierRef = new AtomicReference<>(supplier);
    }

    @Override
    public final T get() {
        final AtomicReference<Supplier<T>> supplierRef = mSupplierRef;
        mSupplierRef = null;
        if (supplierRef != null) {
            // Reset the original supplier to help GC.
            final Supplier<T> supplier = supplierRef.getAndSet(null);
            if (supplier != null) {
                return supplier.get();
            }
        }
        if (Check.ON) Check.shouldNeverHappen();
        return null;
    }
}