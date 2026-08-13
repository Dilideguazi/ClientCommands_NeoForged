/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, E extends Exception> {
    R apply(T var1, U var2) throws E;

    @SuppressWarnings("unused")
    default <V> CheckedBiFunction<T, U, V, E> andThen(CheckedFunction<? super R, ? extends V, E> after) {
        Objects.requireNonNull(after);
        return (t, u) -> after.apply((R)this.apply(t, u));
    }
}

