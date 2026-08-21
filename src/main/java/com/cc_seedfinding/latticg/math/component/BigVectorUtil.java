/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.math.component;

import java.util.ArrayList;

public final class BigVectorUtil {
    private BigVectorUtil() {
    }

    public static BigVector fromString(String str) {
        if (!(str = str.trim()).startsWith("{") || !str.endsWith("}")) {
            throw new IllegalArgumentException("Illegal BigVector format");
        }
        ArrayList<BigFraction> fractions = new ArrayList<BigFraction>();
        int fracStart = 1;
        int fracEnd = str.indexOf(44, fracStart);
        while (fracEnd >= 0) {
            fractions.add(BigFractionUtil.fromString(str.substring(fracStart, fracEnd)));
            fracStart = fracEnd + 1;
            fracEnd = str.indexOf(44, fracStart);
        }
        fractions.add(BigFractionUtil.fromString(str.substring(fracStart, str.length() - 1)));
        return new BigVector(fractions.toArray(new BigFraction[0]));
    }
}

