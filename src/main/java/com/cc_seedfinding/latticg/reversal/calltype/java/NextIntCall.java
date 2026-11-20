/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.cc_seedfinding.latticg.reversal.calltype.java;

import com.cc_seedfinding.latticg.reversal.calltype.CallType;
import com.cc_seedfinding.latticg.reversal.calltype.RangeCallType;
import com.cc_seedfinding.latticg.reversal.calltype.RangeableCallType;
import com.cc_seedfinding.latticg.util.Range;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class NextIntCall
extends RangeableCallType<Integer> {
    private final int bound;
    private static final Integer ABS_MIN = 0;
    private final Integer ABS_MAX;

    @ApiStatus.Internal
    protected NextIntCall(int bound) {
        super(Integer.class, 1);
        this.bound = bound;
        this.ABS_MAX = bound;
    }

    @Override
    protected RangeCallType<Integer> createRangeCallType(Integer min, Integer max, boolean minStrict, boolean maxStrict, boolean inverted) {
        return new IntRange(this.bound, min, max, minStrict, maxStrict, inverted);
    }

    @Override
    protected Integer getAbsoluteMin() {
        return ABS_MIN;
    }

    @Override
    protected Integer getAbsoluteMax() {
        return this.ABS_MAX;
    }

    public int getBound() {
        return this.bound;
    }

    @Override
    public CallType<Range<Integer>> ranged() {
        return new Ranged(this.bound, this.bound / 2);
    }

    @Override
    public CallType<Range<Integer>> ranged(Integer expectedSize) {
        return new Ranged(this.bound, expectedSize);
    }

    @ApiStatus.Internal
    public static class IntRange
    extends RangeCallType<Integer> {
        private final int bound;

        public IntRange(int bound, Integer min, Integer max, boolean minStrict, boolean maxStrict, boolean inverted) {
            super(min, max, minStrict, maxStrict, inverted, 1);
            this.bound = bound;
        }

        @Override
        protected RangeCallType<Integer> createNew(Integer min, Integer max, boolean lowerStrict, boolean upperStrict, boolean inverted) {
            return new IntRange(this.bound, min, max, lowerStrict, upperStrict, inverted);
        }

        public int getBound() {
            return this.bound;
        }
    }

    @ApiStatus.Internal
    public static final class Ranged
    extends CallType<Range<Integer>> {
        private final int bound;
        private final int expectedSize;

        private Ranged(int bound, int expectedSize) {
            super(Range.type(), 1);
            this.bound = bound;
            this.expectedSize = expectedSize;
        }

        public int getBound() {
            return this.bound;
        }

        public int getExpectedSize() {
            return this.expectedSize;
        }

        @Override
        public CallType<Range<Integer>> not() {
            return new RangedInverted(this.bound, this.expectedSize);
        }
    }

    @ApiStatus.Internal
    public static final class RangedInverted
    extends CallType<Range<Integer>> {
        private final int bound;
        private final int expectedSize;

        private RangedInverted(int bound, int expectedSize) {
            super(Range.type(), 1);
            this.bound = bound;
            this.expectedSize = expectedSize;
        }

        public int getBound() {
            return this.bound;
        }

        public int getExpectedSize() {
            return this.expectedSize;
        }

        @Override
        public CallType<Range<Integer>> not() {
            return new Ranged(this.bound, this.expectedSize);
        }
    }
}

