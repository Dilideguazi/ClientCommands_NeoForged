/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedBiConsumer<T, U, E extends Exception> {
    void accept(T var1, U var2) throws E;

    @SuppressWarnings("unused")
    default CheckedBiConsumer<T, U, E> andThen(CheckedBiConsumer<? super T, ? super U, E> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            this.accept(l, r);
            after.accept(l, r);
        };
    }
}

