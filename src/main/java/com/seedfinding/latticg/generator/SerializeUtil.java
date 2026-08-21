/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.generator;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;

public final class SerializeUtil {
    private SerializeUtil() {
    }

    public static String matrixToStringLiteral(String indent, BigMatrix matrix) {
        ByteVector buf = new ByteVector();
        SerializeUtil.writeBigMatrix(buf, matrix);
        return SerializeUtil.bufToStringLiteral(indent, buf);
    }

    private static void writeBigMatrix(ByteVector buf, BigMatrix matrix) {
        SerializeUtil.writeVarInt(buf, matrix.getRowCount());
        if (matrix.getRowCount() == 0) {
            return;
        }
        SerializeUtil.writeVarInt(buf, matrix.getColumnCount());
        for (int i = 0; i < matrix.getRowCount(); ++i) {
            SerializeUtil.writeBigVector(buf, matrix.getRow(i), false);
        }
    }

    public static String vectorToStringLiteral(String indent, BigVector vector) {
        ByteVector buf = new ByteVector();
        SerializeUtil.writeBigVector(buf, vector, true);
        return SerializeUtil.bufToStringLiteral(indent, buf);
    }

    private static void writeBigVector(ByteVector buf, BigVector vector, boolean includeLength) {
        if (includeLength) {
            SerializeUtil.writeVarInt(buf, vector.getDimension());
        }
        for (int i = 0; i < vector.getDimension(); ++i) {
            SerializeUtil.writeBigFraction(buf, vector.get(i));
        }
    }

    public static String fractionToStringLiteral(String indent, BigFraction fraction) {
        ByteVector buf = new ByteVector();
        SerializeUtil.writeBigFraction(buf, fraction);
        return SerializeUtil.bufToStringLiteral(indent, buf);
    }

    private static void writeBigFraction(ByteVector buf, BigFraction fraction) {
        SerializeUtil.writeBigInt(buf, fraction.getNumerator());
        SerializeUtil.writeBigInt(buf, fraction.getDenominator());
    }

    public static String bigIntToStringLiteral(String indent, BigInteger value) {
        ByteVector buf = new ByteVector();
        SerializeUtil.writeBigInt(buf, value);
        return SerializeUtil.bufToStringLiteral(indent, buf);
    }

    private static void writeBigInt(ByteVector buf, BigInteger value) {
        value = value.signum() == -1 ? value.negate().shiftLeft(1).setBit(0) : value.shiftLeft(1);
        do {
            byte b = (byte)(value.intValue() & 0x7F);
            if ((value = value.shiftRight(7)).signum() != 0) {
                b = (byte)(b | 0xFFFFFF80);
            }
            buf.add(b);
        } while (value.signum() != 0);
    }

    private static void writeVarInt(ByteVector buf, int value) {
        do {
            byte b = (byte)(value & 0x7F);
            if ((value >>>= 7) != 0) {
                b = (byte)(b | 0xFFFFFF80);
            }
            buf.add(b);
        } while (value != 0);
    }

    private static String bufToStringLiteral(String indent, ByteVector buf) {
        StringBuilder result = new StringBuilder(indent).append("\"");
        int lineStartIndex = indent.length();
        CharBuffer chars = ByteBuffer.wrap(buf.toEvenLengthArray()).asCharBuffer();
        boolean justPrintedOctal = false;
        block9: while (chars.hasRemaining()) {
            if (result.length() - lineStartIndex >= 126) {
                result.append("\" +\n").append(indent);
                lineStartIndex = result.length();
                result.append("\"");
            }
            char ch = chars.get();
            switch (ch) {
                case '\n': {
                    result.append("\\n");
                    break;
                }
                case '\r': {
                    result.append("\\r");
                    break;
                }
                case '\f': {
                    result.append("\\f");
                    break;
                }
                case '\t': {
                    result.append("\\t");
                    break;
                }
                case '\"': {
                    result.append("\\\"");
                    break;
                }
                case '\\': {
                    result.append("\\\\");
                    break;
                }
                case '0': 
                case '1': 
                case '2': 
                case '3': 
                case '4': 
                case '5': 
                case '6': 
                case '7': {
                    if (justPrintedOctal) {
                        result.append('\\').append(Integer.toOctalString(ch));
                        continue block9;
                    }
                    result.append(ch);
                    break;
                }
                default: {
                    if (Character.isISOControl(ch)) {
                        result.append('\\').append(Integer.toOctalString(ch));
                        justPrintedOctal = true;
                        continue block9;
                    }
                    if (SerializeUtil.isPrintable(ch)) {
                        result.append(ch);
                        break;
                    }
                    result.append(String.format("\\u%04x", ch));
                }
            }
            justPrintedOctal = false;
        }
        return result.append("\"").toString();
    }

    private static boolean isPrintable(char ch) {
        switch (Character.getType(ch)) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: 
            case 30: {
                return true;
            }
        }
        return false;
    }

    private static final class ByteVector {
        private byte[] array = new byte[16];
        private int size = 0;

        private ByteVector() {
        }

        public void add(byte b) {
            if (this.size >= this.array.length) {
                this.array = Arrays.copyOf(this.array, this.array.length * 2);
            }
            this.array[this.size++] = b;
        }

        public byte[] toEvenLengthArray() {
            return Arrays.copyOf(this.array, this.size + 1 & 0xFFFFFFFE);
        }
    }
}

