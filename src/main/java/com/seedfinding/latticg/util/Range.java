/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.util;

import java.util.Objects;

public final class Range<T> {
    private final T min;
    private final T max;
    private final boolean minInclusive;
    private final boolean maxInclusive;

    private Range(T min, T max, boolean minInclusive, boolean maxInclusive) {
        this.min = min;
        this.max = max;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public static <T extends Comparable<T>> Range<T> of(T minInclusive, T maxExclusive) {
        return new Range<T>(minInclusive, maxExclusive, true, false);
    }

    public static <T extends Comparable<T>> Range<T> ofInclusive(T min, T max) {
        return new Range<T>(min, max, true, true);
    }

    public static <T extends Comparable<T>> Range<T> of(T min, T max, boolean minInclusive, boolean maxInclusive) {
        return new Range<T>(min, max, minInclusive, maxInclusive);
    }

    public T min() {
        return this.min;
    }

    public T max() {
        return this.max;
    }

    public boolean minInclusive() {
        return this.minInclusive;
    }

    public boolean maxInclusive() {
        return this.maxInclusive;
    }

    public boolean contains(T value) {
        int minCmp = ((Comparable)this.min).compareTo(value);
        if (this.minInclusive ? minCmp < 0 : minCmp <= 0) {
            return false;
        }
        int maxCmp = ((Comparable)this.max).compareTo(value);
        return this.maxInclusive ? maxCmp <= 0 : maxCmp < 0;
    }

    public int hashCode() {
        return Objects.hash(this.min, this.max, this.minInclusive, this.maxInclusive);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Range)) {
            return false;
        }
        Range that = (Range)obj;
        return this.min.equals(that.min) && this.max.equals(that.max) && this.minInclusive == that.minInclusive && this.maxInclusive == that.maxInclusive;
    }

    public String toString() {
        return String.format("%s%s, %s%s", this.minInclusive ? "[" : "(", this.min, this.max, this.maxInclusive ? "]" : ")");
    }

    public static <T extends Comparable<T>> Class<Range<T>> type() {
        return (Class<Range<T>>) (Class<?>) Range.class;
    }
}

