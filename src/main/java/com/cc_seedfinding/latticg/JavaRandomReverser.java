/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.cc_seedfinding.latticg;

import com.cc_seedfinding.latticg.reversal.calltype.FilteredSkip;
import com.cc_seedfinding.latticg.util.LCG;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class JavaRandomReverser
extends RandomReverser {
    @ApiStatus.Internal
    public JavaRandomReverser(List<FilteredSkip> filteredSkips) {
        super(LCG.JAVA, filteredSkips);
    }

    public void addNextIntCall(int n, int min, int max) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.format("Bad bound for nextInt call can only be positive : %d", n));
        }
        if ((n & -n) == n) {
            int log = Long.numberOfTrailingZeros(n);
            this.addMeasuredSeed((long)min * (1L << 48 - log), (long)max * (1L << 48 - log) + (1L << 48 - log) - 1L);
        } else {
            this.addModuloMeasuredSeed((long)min * 131072L, (long)max * 131072L | 0x1FFFFL, (long)n * 131072L);
        }
    }

    public void addNextIntCall(int min, int max) {
        this.addMeasuredSeed((long)min * 65536L, (long)max * 65536L + 65536L - 1L);
    }

    public void consumeNextIntCalls(int numCalls, int bound) {
        long residue = 0x1000000000000L % (131072L * (long)bound);
        if (residue != 0L) {
            this.successChance *= Math.pow(1.0 - (double)residue / 2.81474976710656E14, numCalls);
        }
        this.addUnmeasuredSeeds(numCalls);
    }

    public void addNextBooleanCall(boolean value) {
        if (value) {
            this.addNextIntCall(2, 1, 1);
        } else {
            this.addNextIntCall(2, 0, 0);
        }
    }

    public void consumeNextBooleanCalls(int numCalls) {
        this.addUnmeasuredSeeds(numCalls);
    }

    public void addNextFloatCall(float min, float max, boolean minInclusive, boolean maxInclusive) {
        if (min < 0.0f || max < 0.0f || min > 1.0f || max > 1.0f) {
            throw new IllegalArgumentException(String.format("Bounds should have 0 <= min, max <=1 but were min: %f max: %f with min included : %s and max included %s", Float.valueOf(min), Float.valueOf(max), minInclusive, maxInclusive));
        }
        float minInc = min;
        float maxInc = max;
        if (!minInclusive) {
            minInc = Math.nextUp(min);
        }
        if (maxInclusive) {
            maxInc = Math.nextUp(max);
        }
        long minLong = (long)StrictMath.ceil(minInc * 1.6777216E7f);
        long maxLong = (long)StrictMath.ceil(maxInc * 1.6777216E7f) - 1L;
        long minSeed = minLong << 24;
        long maxSeed = maxLong << 24 | 0xFFFFFFL;
        this.addMeasuredSeed(minSeed, maxSeed);
    }

    public void addNextFloatCall(float min, float max) {
        this.addNextFloatCall(min, max, true, false);
    }

    public void consumeNextFloatCalls(int numCalls) {
        this.addUnmeasuredSeeds(numCalls);
    }

    public void addNextLongCall(long min, long max) {
        if (max + 1L == min) {
            throw new IllegalArgumentException("nextLong bounds give no actual constraint");
        }
        boolean minSignBit = (min & 0x80000000L) != 0L;
        boolean maxSignBit = (max & 0x80000000L) != 0L;
        long minFirstSeed = minSignBit ? (min >>> 32) + 1L << 16 : min >>> 32 << 16;
        long maxFirstSeed = maxSignBit ? ((max >>> 32) + 2L << 16) - 1L : ((max >>> 32) + 1L << 16) - 1L;
        this.addMeasuredSeed(minFirstSeed, maxFirstSeed);
        if (max - min < 0x100000000L && 0L <= max - min) {
            this.addMeasuredSeed((min & 0xFFFFFFFFL) << 16, ((max & 0xFFFFFFFFL) + 1L << 16) - 1L);
        } else {
            this.addUnmeasuredSeeds(1L);
        }
    }

    public void consumeNextLongCalls(int numCalls) {
        this.addUnmeasuredSeeds(2L * (long)numCalls);
    }

    public void addNextDoubleCall(double min, double max, boolean minInclusive, boolean maxInclusive) {
        if (min < 0.0 || max < 0.0 || min > 1.0 || max > 1.0) {
            throw new IllegalArgumentException(String.format("Bounds should have 0 <= min, max <=1 but were min: %f max: %f with min included : %s and max included %s", min, max, minInclusive, maxInclusive));
        }
        double minInc = min;
        double maxInc = max;
        if (!minInclusive) {
            minInc = Math.nextUp(min);
        }
        if (maxInclusive) {
            maxInc = Math.nextUp(max);
        }
        long minLong = (long)StrictMath.ceil(minInc * 9.007199254740992E15);
        long maxLong = (long)StrictMath.ceil(maxInc * 9.007199254740992E15) - 1L;
        long minSeed1 = minLong >> 27 << 22;
        long maxSeed1 = maxLong >> 27 << 22 | 0x3FFFFFL;
        this.addMeasuredSeed(minSeed1, maxSeed1);
        if ((maxLong - minLong) % 0x20000000000000L < 0x8000000L) {
            long minSeed2 = (minLong & 0x7FFFFFFL) << 21;
            long maxSeed2 = (maxLong & 0x7FFFFFFL) << 21 | 0x1FFFFFL;
            this.addMeasuredSeed(minSeed2, maxSeed2);
        } else {
            this.addUnmeasuredSeeds(1L);
        }
    }

    public void addNextDoubleCall(double min, double max) {
        this.addNextDoubleCall(min, max, true, false);
    }

    public void consumeNextDoubleCalls(int numCalls) {
        this.addUnmeasuredSeeds(2L * (long)numCalls);
    }

    public void addModConstraint(long min, long max, long newMod) {
        this.addModuloMeasuredSeed(min, max, newMod);
    }
}

