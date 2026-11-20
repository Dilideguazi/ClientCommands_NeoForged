/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.math.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public final class BigFraction
implements Comparable<BigFraction> {
    public static final BigFraction ZERO = new BigFraction(0L);
    public static final BigFraction ONE = new BigFraction(1L);
    public static final BigFraction HALF = new BigFraction(1L, 2L);
    public static final BigFraction MINUS_ONE = new BigFraction(-1L);
    public static final BigFraction PI = new BigFraction(30246273033735921L, 9627687726852338L);
    public static final BigFraction LOG_PI = new BigFraction(14405300475444212L, 12584017114880639L);
    public static final BigFraction LOG_10 = new BigFraction(152469287047331902L, 66216570024379193L);
    public static final BigFraction EXP = new BigFraction(47813267563899719L, 17589518151988078L);
    public static final BigInteger TWO = new BigInteger("2");
    private static final MathContext TO_DOUBLE_CONTEXT = MathContext.DECIMAL64;
    private BigInteger ntor;
    private BigInteger dtor;

    public BigFraction(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException("/ by zero");
        }
        this.ntor = numerator;
        this.dtor = denominator;
        this.simplify();
    }

    public BigFraction(long numerator, long denominator) {
        this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public BigFraction(BigInteger numerator) {
        this(numerator, BigInteger.ONE);
    }

    public BigFraction(long numerator) {
        this(numerator, 1L);
    }

    private void simplify() {
        if (this.ntor.signum() == 0) {
            this.dtor = BigInteger.ONE;
            return;
        }
        if (this.dtor.signum() == -1) {
            this.ntor = this.ntor.negate();
            this.dtor = this.dtor.negate();
        }
        BigInteger commonFactor = this.ntor.gcd(this.dtor);
        this.ntor = this.ntor.divide(commonFactor);
        this.dtor = this.dtor.divide(commonFactor);
    }

    public BigInteger getNumerator() {
        return this.ntor;
    }

    public BigInteger getDenominator() {
        return this.dtor;
    }

    public BigDecimal toBigDecimal(MathContext mc) {
        return new BigDecimal(this.ntor).divide(new BigDecimal(this.dtor), mc);
    }

    public double toDouble() {
        return this.toBigDecimal(TO_DOUBLE_CONTEXT).doubleValue();
    }

    public BigFraction add(BigFraction other) {
        return new BigFraction(this.ntor.multiply(other.dtor).add(other.ntor.multiply(this.dtor)), this.dtor.multiply(other.dtor));
    }

    public BigFraction add(BigInteger other) {
        return new BigFraction(this.ntor.add(other.multiply(this.dtor)), this.dtor);
    }

    public BigFraction add(long other) {
        return this.add(BigInteger.valueOf(other));
    }

    public BigFraction subtract(BigFraction other) {
        return new BigFraction(this.ntor.multiply(other.dtor).subtract(other.ntor.multiply(this.dtor)), this.dtor.multiply(other.dtor));
    }

    public BigFraction subtract(BigInteger other) {
        return new BigFraction(this.ntor.subtract(other.multiply(this.dtor)), this.dtor);
    }

    public BigFraction subtract(long other) {
        return this.subtract(BigInteger.valueOf(other));
    }

    public BigFraction multiply(BigFraction other) {
        return new BigFraction(this.ntor.multiply(other.ntor), this.dtor.multiply(other.dtor));
    }

    public BigFraction multiply(BigInteger other) {
        return new BigFraction(this.ntor.multiply(other), this.dtor);
    }

    public BigFraction multiply(long other) {
        return this.multiply(BigInteger.valueOf(other));
    }

    public BigFraction divide(BigFraction other) {
        return new BigFraction(this.ntor.multiply(other.dtor), this.dtor.multiply(other.ntor));
    }

    public BigFraction divide(BigInteger other) {
        return new BigFraction(this.ntor, this.dtor.multiply(other));
    }

    public BigFraction divide(long other) {
        return this.divide(BigInteger.valueOf(other));
    }

    public BigFraction negate() {
        return new BigFraction(this.ntor.negate(), this.dtor);
    }

    public BigFraction reciprocal() {
        return new BigFraction(this.dtor, this.ntor);
    }

    public BigInteger floor() {
        if (this.dtor.equals(BigInteger.ONE)) {
            return this.ntor;
        }
        if (this.ntor.signum() == -1) {
            return this.ntor.divide(this.dtor).subtract(BigInteger.ONE);
        }
        return this.ntor.divide(this.dtor);
    }

    public BigInteger ceil() {
        if (this.dtor.equals(BigInteger.ONE)) {
            return this.ntor;
        }
        if (this.ntor.signum() == 1) {
            return this.ntor.divide(this.dtor).add(BigInteger.ONE);
        }
        return this.ntor.divide(this.dtor);
    }

    public BigInteger round() {
        return this.add(HALF).floor();
    }

    public int signum() {
        return this.ntor.signum();
    }

    public BigFraction abs() {
        return this.ntor.signum() == -1 ? this.negate() : this;
    }

    public BigFraction exp() {
        BigInteger dtor = BigInteger.ONE;
        BigFraction result = ONE;
        BigFraction ntor = this;
        for (int i = 1; i < 10; ++i) {
            dtor = dtor.multiply(new BigInteger(String.valueOf(i)));
            result = result.add(ntor.divide(dtor));
            ntor = ntor.multiply(this);
        }
        return result;
    }

    public BigFraction log() {
        if (this.ntor.compareTo(BigInteger.ONE) == 0 && this.dtor.compareTo(BigInteger.ONE) == 0) {
            return ZERO;
        }
        String digits = Double.toString(this.toDouble()).split("\\.")[0];
        int lenght = digits.charAt(0) == '1' || digits.charAt(0) == '0' ? digits.length() - 1 : digits.length();
        BigFraction y = this.divide(BigInteger.TEN.pow(lenght));
        if (y.compareTo(BigInteger.ZERO) <= 0) {
            throw new ArithmeticException("Domain error " + this.toDouble());
        }
        if (y.compareTo(TWO) <= 0) {
            BigFraction x;
            BigFraction result = ZERO;
            BigFraction ntor = x = y.subtract(BigInteger.ONE);
            BigInteger dtor = BigInteger.ONE;
            BigInteger sign = BigInteger.ONE;
            for (int i = 1; i < 200; ++i) {
                BigFraction temp = ntor.divide(dtor).multiply(sign);
                result = result.add(temp);
                ntor = ntor.multiply(x);
                dtor = dtor.add(BigInteger.ONE);
                sign = sign.negate();
            }
            return result.add(LOG_10.multiply(lenght));
        }
        throw new ArithmeticException("Unexpected division by largest power of 10");
    }

    @Override
    public int compareTo(BigFraction other) {
        return this.ntor.multiply(other.dtor).compareTo(other.ntor.multiply(this.dtor));
    }

    public int compareTo(BigInteger other) {
        BigFraction other_frac = new BigFraction(other);
        return this.compareTo(other_frac);
    }

    public int hashCode() {
        return this.ntor.hashCode() + 31 * this.dtor.hashCode();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != BigFraction.class) {
            return false;
        }
        BigFraction that = (BigFraction)other;
        return this.ntor.equals(that.ntor) && this.dtor.equals(that.dtor);
    }

    public String toString() {
        if (this.dtor.equals(BigInteger.ONE)) {
            return this.ntor.toString();
        }
        return this.ntor + "/" + this.dtor;
    }
}

