/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package com.seedfinding.latticg.reversal.calltype;

import com.seedfinding.latticg.reversal.calltype.CallType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class RangeCallType<T extends Comparable<T>>
extends CallType<Boolean> {
    private final T min;
    private final T max;
    private final boolean minStrict;
    private final boolean maxStrict;
    private final boolean inverted;

    public RangeCallType(T min, T max, boolean minStrict, boolean maxStrict, boolean inverted, int numCalls) {
        super(Boolean.class, numCalls);
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException(String.format("min (%s) > max (%s)", min, max));
        }
        if ((minStrict || maxStrict) && min.equals(max)) {
            throw new IllegalArgumentException(String.format("min (%s) == max (%s)", min, max));
        }
        this.min = min;
        this.max = max;
        this.minStrict = minStrict;
        this.maxStrict = maxStrict;
        this.inverted = inverted;
    }

    protected abstract RangeCallType<T> createNew(T var1, T var2, boolean var3, boolean var4, boolean var5);

    @Override
    public CallType<Boolean> not() {
        return this.createNew(this.min, this.max, this.minStrict, this.maxStrict, !this.inverted);
    }

    @Override
    public CallType<Boolean> equalTo(Boolean value) {
        return value == this.inverted ? this.not() : this;
    }

    public T getMin() {
        return this.min;
    }

    public T getMax() {
        return this.max;
    }

    public boolean isMinStrict() {
        return this.minStrict;
    }

    public boolean isMaxStrict() {
        return this.maxStrict;
    }

    public boolean isInverted() {
        return this.inverted;
    }
}

