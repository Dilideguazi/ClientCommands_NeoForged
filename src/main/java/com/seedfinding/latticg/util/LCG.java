/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.util;

import java.util.Objects;

public class LCG {
    public static final LCG JAVA = new LCG(25214903917L, 11L, 0x1000000000000L);
    public final long multiplier;
    public final long addend;
    public final long modulus;
    private final boolean canMask;

    public LCG(long multiplier, long addend, long modulus) {
        this.multiplier = multiplier;
        this.addend = addend;
        this.modulus = modulus;
        this.canMask = (this.modulus & -this.modulus) == this.modulus;
    }

    public long nextSeed(long seed) {
        return this.mod(seed * this.multiplier + this.addend);
    }

    public LCG combine(LCG other) {
        if (this.modulus != other.modulus) {
            throw new IllegalArgumentException("Combining with LCG of different modulus");
        }
        return new LCG(this.mod(this.multiplier * other.multiplier), this.mod(this.multiplier * other.addend + this.addend), this.modulus);
    }

    public LCG combine(long steps) {
        long multiplier = 1L;
        long addend = 0L;
        long intermediateMultiplier = this.multiplier;
        long intermediateAddend = this.addend;
        for (long k = steps; k != 0L; k >>>= 1) {
            if ((k & 1L) != 0L) {
                multiplier *= intermediateMultiplier;
                addend = intermediateMultiplier * addend + intermediateAddend;
            }
            intermediateAddend = (intermediateMultiplier + 1L) * intermediateAddend;
            intermediateMultiplier *= intermediateMultiplier;
        }
        multiplier = this.mod(multiplier);
        addend = this.mod(addend);
        return new LCG(multiplier, addend, this.modulus);
    }

    public LCG invert() {
        return this.combine(-1L);
    }

    public long mod(long n) {
        if (this.canMask) {
            return n & this.modulus - 1L;
        }
        return Long.remainderUnsigned(n, this.modulus);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LCG)) {
            return false;
        }
        LCG lcg = (LCG)obj;
        return this.multiplier == lcg.multiplier && this.addend == lcg.addend && this.modulus == lcg.modulus;
    }

    public int hashCode() {
        return Objects.hash(this.multiplier, this.addend, this.modulus);
    }

    public String toString() {
        return "LCG{multiplier=" + this.multiplier + ", addend=" + this.addend + ", modulo=" + this.modulus + '}';
    }
}

