/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {
    void accept(T var1) throws E;

    @SuppressWarnings("unused")
    default CheckedConsumer<T, E> andThen(CheckedConsumer<? super T, E> after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}

