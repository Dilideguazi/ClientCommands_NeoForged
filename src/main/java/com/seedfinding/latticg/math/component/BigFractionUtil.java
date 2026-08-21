/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.component;

import com.seedfinding.latticg.math.component.BigFraction;
import java.math.BigInteger;

public final class BigFractionUtil {
    private BigFractionUtil() {
    }

    public static BigFraction fromString(String str) {
        int slashIndex = (str = str.trim()).indexOf(47);
        if (slashIndex == -1) {
            return new BigFraction(new BigInteger(str));
        }
        String numerator = str.substring(0, slashIndex).trim();
        String denominator = str.substring(slashIndex + 1).trim();
        return new BigFraction(new BigInteger(numerator), new BigInteger(denominator));
    }
}

