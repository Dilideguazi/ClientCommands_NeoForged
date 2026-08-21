/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.reversal.calltype;

import com.cc_seedfinding.latticg.util.Range;

public abstract class CallType<T> {
    private final Class<T> type;
    private final int numCalls;

    public CallType(Class<T> type, int numCalls) {
        this.type = type;
        this.numCalls = numCalls;
    }

    public int getNumCalls() {
        return this.numCalls;
    }

    public final Class<T> getType() {
        return this.type;
    }

    public CallType<T> not() {
        throw this.unsupported("not");
    }

    public CallType<Boolean> betweenII(T min, T max) {
        throw this.unsupported("betweenII");
    }

    public CallType<Boolean> betweenIE(T min, T max) {
        throw this.unsupported("betweenIE");
    }

    public CallType<Boolean> betweenEI(T min, T max) {
        throw this.unsupported("betweenEI");
    }

    public CallType<Boolean> betweenEE(T min, T max) {
        throw this.unsupported("betweenEE");
    }

    public CallType<Boolean> equalTo(T value) {
        throw this.unsupported("equalTo");
    }

    public CallType<Boolean> notEqualTo(T value) {
        return this.equalTo(value).not();
    }

    public CallType<Boolean> lessThan(T value) {
        throw this.unsupported("lessThan");
    }

    public CallType<Boolean> lessThanEqual(T value) {
        throw this.unsupported("lessThanEqual");
    }

    public CallType<Boolean> greaterThan(T value) {
        return this.lessThanEqual(value).not();
    }

    public CallType<Boolean> greaterThanEqual(T value) {
        return this.lessThan(value).not();
    }

    public CallType<Range<T>> ranged() {
        throw this.unsupported("range");
    }

    public CallType<Range<T>> ranged(T expectedSize) {
        throw this.unsupported("range");
    }

    private UnsupportedOperationException unsupported(String methodName) {
        return new UnsupportedOperationException("Method \"" + methodName + "\" is not supported by " + this.getClass().getName());
    }
}

