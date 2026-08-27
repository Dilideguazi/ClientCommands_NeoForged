/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.util;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class DeserializeRt {
    private DeserializeRt() {
    }

    public static BigMatrix mat(String str) {
        return DeserializeRt.readBigMatrix(DeserializeRt.bufFromString(str));
    }

    private static BigMatrix readBigMatrix(ByteBuffer buf) {
        int numRows = DeserializeRt.readVarInt(buf);
        if (numRows == 0) {
            return new BigMatrix(0, 0);
        }
        int numCols = DeserializeRt.readVarInt(buf);
        BigVector[] rows = new BigVector[numRows];
        for (int i = 0; i < numRows; ++i) {
            rows[i] = DeserializeRt.readBigVector(numCols, buf);
        }
        BigMatrix matrix = new BigMatrix(numRows, numCols);
        for (int i = 0; i < numRows; ++i) {
            matrix.setRow(i, rows[i]);
        }
        return matrix;
    }

    public static BigVector vec(String str) {
        return DeserializeRt.readBigVector(DeserializeRt.bufFromString(str));
    }

    private static BigVector readBigVector(ByteBuffer buf) {
        return DeserializeRt.readBigVector(DeserializeRt.readVarInt(buf), buf);
    }

    private static BigVector readBigVector(int len, ByteBuffer buf) {
        BigFraction[] fractions = new BigFraction[len];
        for (int i = 0; i < len; ++i) {
            fractions[i] = DeserializeRt.readBigFraction(buf);
        }
        return new BigVector(fractions);
    }

    public static BigFraction frac(String str) {
        return DeserializeRt.readBigFraction(DeserializeRt.bufFromString(str));
    }

    private static BigFraction readBigFraction(ByteBuffer buf) {
        return new BigFraction(DeserializeRt.readBigInt(buf), DeserializeRt.readBigInt(buf));
    }

    public static BigInteger scalar(String str) {
        return DeserializeRt.readBigInt(DeserializeRt.bufFromString(str));
    }

    private static BigInteger readBigInt(ByteBuffer buf) {
        byte b;
        BigInteger result = BigInteger.ZERO;
        int shift = 0;
        do {
            b = buf.get();
            result = result.or(BigInteger.valueOf(b & 0x7F).shiftLeft(shift));
            shift += 7;
        } while ((b & 0x80) != 0);
        result = result.testBit(0) ? result.shiftRight(1).negate() : result.shiftRight(1);
        return result;
    }

    private static int readVarInt(ByteBuffer buf) {
        byte b;
        int result = 0;
        int shift = 0;
        do {
            if (shift >= 35) {
                throw new IllegalArgumentException("Varint is too long");
            }
            b = buf.get();
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    private static ByteBuffer bufFromString(String str) {
        ByteBuffer result = ByteBuffer.allocate(str.length() * 2);
        CharBuffer chars = result.asCharBuffer();
        for (int i = 0; i < str.length(); ++i) {
            chars.put(str.charAt(i));
        }
        result.limit(chars.position() * 2);
        return result;
    }
}

