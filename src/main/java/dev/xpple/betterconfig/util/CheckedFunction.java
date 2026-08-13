/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
@SuppressWarnings("unused")
public interface CheckedFunction<T, R, E extends Exception> {
    R apply(T var1) throws E;

    default <V> CheckedFunction<V, R, E> compose(CheckedFunction<? super V, ? extends T, E> before) {
        Objects.requireNonNull(before);
        return v -> this.apply(before.apply(v));
    }

    default <V> CheckedFunction<T, V, E> andThen(CheckedFunction<? super R, ? extends V, E> after) {
        Objects.requireNonNull(after);
        return t -> after.apply((R)this.apply(t));
    }

    static <T, E extends Exception> CheckedFunction<T, T, E> identity() {
        return t -> t;
    }
}

