/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.math.lattice.LLL;

import com.cc_seedfinding.latticg.math.component.BigFraction;

public class Params {
    public BigFraction delta = new BigFraction(75L, 100L);
    public boolean debug = false;
    public int maxStage = -1;
    public int pruneFactor = 0;
    public static BigFraction recommendedDelta = new BigFraction(99L, 100L);

    public Params setPruneFactor(int pruneFactor) {
        this.pruneFactor = pruneFactor;
        return this;
    }

    public Params setMaxStage(int maxStage) {
        this.maxStage = maxStage;
        return this;
    }

    public Params setDelta(BigFraction delta) {
        this.delta = delta;
        return this;
    }

    public Params setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
}

