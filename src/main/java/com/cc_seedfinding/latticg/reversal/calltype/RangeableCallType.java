/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 */
package com.cc_seedfinding.latticg.reversal.calltype;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class RangeableCallType<T extends Comparable<T>>
extends CallType<T> {
    public RangeableCallType(Class<T> type, int numCalls) {
        super(type, numCalls);
    }

    @Override
    public CallType<Boolean> betweenII(T min, T max) {
        return this.createRangeCallType(min, max, false, false, false);
    }

    @Override
    public CallType<Boolean> betweenIE(T min, T max) {
        return this.createRangeCallType(min, max, false, true, false);
    }

    @Override
    public CallType<Boolean> betweenEI(T min, T max) {
        return this.createRangeCallType(min, max, true, false, false);
    }

    @Override
    public CallType<Boolean> betweenEE(T min, T max) {
        return this.createRangeCallType(min, max, true, true, false);
    }

    @Override
    public CallType<Boolean> equalTo(T value) {
        return this.betweenII(value, value);
    }

    @Override
    public CallType<Boolean> lessThan(T value) {
        return this.createRangeCallType(this.getAbsoluteMin(), value, this.isAbsoluteMinStrict(), true, false);
    }

    @Override
    public CallType<Boolean> lessThanEqual(T value) {
        return this.createRangeCallType(this.getAbsoluteMin(), value, this.isAbsoluteMinStrict(), false, false);
    }

    @Override
    public CallType<Boolean> greaterThan(T value) {
        return this.createRangeCallType(value, this.getAbsoluteMax(), true, this.isAbsoluteMaxStrict(), false);
    }

    @Override
    public CallType<Boolean> greaterThanEqual(T value) {
        return this.createRangeCallType(value, this.getAbsoluteMax(), false, this.isAbsoluteMaxStrict(), false);
    }

    protected abstract RangeCallType<T> createRangeCallType(T var1, T var2, boolean var3, boolean var4, boolean var5);

    protected abstract T getAbsoluteMin();

    protected abstract T getAbsoluteMax();

    protected boolean isAbsoluteMinStrict() {
        return false;
    }

    protected boolean isAbsoluteMaxStrict() {
        return true;
    }
}

