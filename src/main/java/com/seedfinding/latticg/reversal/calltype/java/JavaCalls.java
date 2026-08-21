/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.reversal.calltype.java;

import com.seedfinding.latticg.reversal.calltype.CallType;
import com.seedfinding.latticg.reversal.calltype.java.NextBooleanCall;
import com.seedfinding.latticg.reversal.calltype.java.NextDoubleCall;
import com.seedfinding.latticg.reversal.calltype.java.NextFloatCall;
import com.seedfinding.latticg.reversal.calltype.java.NextIntCall;
import com.seedfinding.latticg.reversal.calltype.java.NextLongCall;
import com.seedfinding.latticg.reversal.calltype.java.UnboundedNextIntCall;

public final class JavaCalls {
    private JavaCalls() {
    }

    public static CallType<Boolean> nextBoolean() {
        return NextBooleanCall.EQUAL_TO_TRUE;
    }

    public static CallType<Float> nextFloat() {
        return NextFloatCall.INSTANCE;
    }

    public static CallType<Double> nextDouble() {
        return NextDoubleCall.INSTANCE;
    }

    public static CallType<Integer> nextInt(int bound) {
        return new NextIntCall(bound);
    }

    public static CallType<Integer> nextInt() {
        return UnboundedNextIntCall.INSTANCE;
    }

    public static CallType<Long> nextLong() {
        return NextLongCall.INSTANCE;
    }
}

