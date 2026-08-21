/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.util;

import java.math.BigInteger;

public class Mth {
    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, min), max);
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(Math.min(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, min), max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, min), max);
    }

    public static double gcd(double a, double b) {
        while (b != 0.0) {
            double temp = a %= b;
            a = b;
            b = temp;
        }
        return a;
    }

    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b.divide(a.gcd(b)));
    }

    public static long modInverse(long x, int mod) {
        if ((x & 1L) == 0L) {
            throw new IllegalArgumentException("x is not coprime with the modulus");
        }
        long inv = 0L;
        long b = 1L;
        for (int i = 0; i < mod; ++i) {
            if ((b & 1L) == 1L) {
                inv |= 1L << i;
                b = b - x >> 1;
                continue;
            }
            b >>= 1;
        }
        return inv;
    }
}

