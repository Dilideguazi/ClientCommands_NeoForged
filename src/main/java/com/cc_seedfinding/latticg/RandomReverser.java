/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.cc_seedfinding.latticg;

import com.cc_seedfinding.latticg.math.component.BigFraction;
import com.cc_seedfinding.latticg.math.component.BigMatrix;
import com.cc_seedfinding.latticg.math.component.BigMatrixUtil;
import com.cc_seedfinding.latticg.math.component.BigVector;
import com.cc_seedfinding.latticg.math.lattice.LLL.LLL;
import com.cc_seedfinding.latticg.math.lattice.LLL.Params;
import com.cc_seedfinding.latticg.math.lattice.LLL.Result;
import com.cc_seedfinding.latticg.math.lattice.enumeration.Enumerate;
import com.cc_seedfinding.latticg.reversal.calltype.FilteredSkip;
import com.cc_seedfinding.latticg.util.LCG;
import com.cc_seedfinding.latticg.util.Mth;
import com.cc_seedfinding.latticg.util.Rand;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class RandomReverser {
    protected final BigInteger MOD;
    protected final BigInteger MULT;
    protected final LCG lcg;
    protected final ArrayList<BigInteger> mins;
    protected final ArrayList<BigInteger> maxes;
    protected final ArrayList<Long> callIndices;
    protected final List<FilteredSkip> filteredSkips;
    protected BigMatrix lattice;
    protected long currentCallIndex;
    protected int dimensions;
    protected boolean verbose;
    protected double successChance;

    @ApiStatus.Internal
    public RandomReverser(LCG lcg, List<FilteredSkip> filteredSkips) {
        this.MOD = lcg.modulus > 0L ? BigInteger.valueOf(lcg.modulus) : BigInteger.valueOf(lcg.modulus).add(BigInteger.valueOf(2L).pow(64));
        this.MULT = BigInteger.valueOf(lcg.multiplier).mod(this.MOD);
        this.lcg = lcg;
        this.verbose = false;
        this.dimensions = 0;
        this.mins = new ArrayList();
        this.maxes = new ArrayList();
        this.callIndices = new ArrayList();
        this.currentCallIndex = 0L;
        this.successChance = 1.0;
        this.filteredSkips = filteredSkips;
    }

    public LongStream findAllValidSeeds() {
        if (this.dimensions == 0) {
            return LongStream.range(0L, this.lcg.modulus);
        }
        this.createLattice();
        BigVector lower = new BigVector(this.dimensions);
        BigVector upper = new BigVector(this.dimensions);
        BigVector offset = new BigVector(this.dimensions);
        Rand rand = Rand.ofInternalSeed(this.lcg, 0L);
        for (int i = 0; i < this.dimensions; ++i) {
            lower.set(i, new BigFraction(this.mins.get(i)));
            upper.set(i, new BigFraction(this.maxes.get(i)));
            offset.set(i, new BigFraction(rand.getSeed()));
            if (i == this.dimensions - 1) continue;
            rand.advance(this.callIndices.get(i + 1) - this.callIndices.get(i));
        }
        if (this.verbose) {
            System.out.println("Mins: " + lower);
            System.out.println("Maxes: " + upper);
            System.out.println("Offsets: " + offset);
        }
        LCG r = this.lcg.combine(-this.callIndices.get(0).longValue());
        if (this.successChance != 1.0) {
            System.err.printf("Ignored approximately %.2e of all seeds %n", 1.0 - this.successChance);
        }
        return Enumerate.enumerate(this.lattice.transpose(), lower, upper, offset).map(vec -> vec.get(0)).map(BigFraction::getNumerator).mapToLong(BigInteger::longValue).map(r::nextSeed).filter(seed -> {
            for (FilteredSkip call : this.filteredSkips) {
                Rand rr;
                if (call.checkState(rr = Rand.ofInternalSeed(this.lcg, seed))) continue;
                return false;
            }
            return true;
        });
    }

    private void createLattice() {
        if (this.verbose) {
            System.out.println("Call Indices: " + this.callIndices);
        }
        if (this.mins.size() != this.dimensions || this.maxes.size() != this.dimensions || this.callIndices.size() != this.dimensions) {
            return;
        }
        BigInteger[] sideLengths = new BigInteger[this.dimensions];
        for (int i = 0; i < this.dimensions; ++i) {
            sideLengths[i] = this.maxes.get(i).subtract(this.mins.get(i)).add(BigInteger.ONE);
        }
        BigInteger lcm = BigInteger.ONE;
        for (int i = 0; i < this.dimensions; ++i) {
            lcm = Mth.lcm(lcm, sideLengths[i]);
        }
        BigMatrix scales = new BigMatrix(this.dimensions, this.dimensions);
        for (int i = 0; i < this.dimensions; ++i) {
            for (int j = 0; j < this.dimensions; ++j) {
                scales.set(i, j, BigFraction.ZERO);
            }
            scales.set(i, i, new BigFraction(lcm.divide(sideLengths[i])));
        }
        BigMatrix unscaledLattice = this.lattice;
        if (this.verbose) {
            System.out.println("Looking for points on:\n" + BigMatrixUtil.toPrettyString(unscaledLattice));
        }
        BigMatrix scaledLattice = unscaledLattice.multiply(scales);
        Params params = new Params().setDelta(Params.recommendedDelta).setDebug(false);
        if (this.verbose) {
            System.out.println("Reducing:\n" + BigMatrixUtil.toPrettyString(scaledLattice));
        }
        Result result = LLL.reduce(scaledLattice, params);
        if (this.verbose) {
            System.out.println("Found Reduced Scaled Basis:\n" + BigMatrixUtil.toPrettyString(result.getReducedBasis()));
            System.out.println("Found Reduced Basis:\n" + BigMatrixUtil.toPrettyString(result.getReducedBasis().multiply(BigMatrixUtil.inverse(scales))));
        }
        this.lattice = result.getReducedBasis().multiply(BigMatrixUtil.inverse(scales));
    }

    public void addMeasuredSeed(long min, long max) {
        this.addMeasuredSeed(BigInteger.valueOf(min), BigInteger.valueOf(max));
    }

    public void addMeasuredSeed(BigInteger min, BigInteger max) {
        min = min.mod(this.MOD);
        if ((max = max.mod(this.MOD)).compareTo(min) < 0) {
            max = max.add(this.MOD);
        }
        this.mins.add(min);
        this.maxes.add(max);
        ++this.dimensions;
        ++this.currentCallIndex;
        this.callIndices.add(this.currentCallIndex);
        BigMatrix newLattice = new BigMatrix(this.dimensions + 1, this.dimensions);
        if (this.dimensions != 1) {
            for (int row = 0; row < this.dimensions; ++row) {
                for (int col = 0; col < this.dimensions - 1; ++col) {
                    newLattice.set(row, col, this.lattice.get(row, col));
                }
            }
        }
        BigInteger tempMult = this.MULT.modPow(BigInteger.valueOf(this.callIndices.get(this.dimensions - 1) - this.callIndices.get(0)), this.MOD);
        newLattice.set(0, this.dimensions - 1, new BigFraction(tempMult));
        newLattice.set(this.dimensions, this.dimensions - 1, new BigFraction(this.MOD));
        this.lattice = newLattice;
    }

    public void addModuloMeasuredSeed(long min, long max, long mod) {
        this.addModuloMeasuredSeed(BigInteger.valueOf(min), BigInteger.valueOf(max), BigInteger.valueOf(mod));
    }

    public void addModuloMeasuredSeed(BigInteger min, BigInteger max, BigInteger measured_mod) {
        BigInteger residue;
        min = min.mod(measured_mod);
        if ((max = max.mod(measured_mod)).compareTo(min) < 0) {
            max = max.add(measured_mod);
        }
        if (!(residue = this.MOD.mod(measured_mod)).equals(BigInteger.ZERO)) {
            this.successChance *= 1.0 - residue.doubleValue() / (double)this.lcg.modulus;
            this.mins.add(BigInteger.ZERO);
            this.maxes.add(this.MOD.subtract(residue));
            ++this.currentCallIndex;
            this.callIndices.add(this.currentCallIndex);
            this.mins.add(min);
            this.maxes.add(max);
            this.callIndices.add(this.currentCallIndex);
            this.dimensions += 2;
            BigMatrix newLattice = new BigMatrix(this.dimensions + 1, this.dimensions);
            if (this.dimensions != 2) {
                for (int row = 0; row < this.dimensions - 1; ++row) {
                    for (int col = 0; col < this.dimensions - 2; ++col) {
                        newLattice.set(row, col, this.lattice.get(row, col));
                    }
                }
            }
            BigInteger tempMult = this.MULT.modPow(BigInteger.valueOf(this.callIndices.get(this.dimensions - 1) - this.callIndices.get(0)), this.MOD);
            newLattice.set(0, this.dimensions - 2, new BigFraction(tempMult));
            newLattice.set(0, this.dimensions - 1, new BigFraction(tempMult));
            newLattice.set(this.dimensions - 1, this.dimensions - 1, new BigFraction(this.MOD));
            newLattice.set(this.dimensions - 1, this.dimensions - 2, new BigFraction(this.MOD));
            newLattice.set(this.dimensions, this.dimensions - 1, new BigFraction(measured_mod));
            this.lattice = newLattice;
        } else {
            this.mins.add(min);
            this.maxes.add(max);
            ++this.dimensions;
            ++this.currentCallIndex;
            this.callIndices.add(this.currentCallIndex);
            BigMatrix newLattice = new BigMatrix(this.dimensions + 1, this.dimensions);
            if (this.dimensions != 1) {
                for (int row = 0; row < this.dimensions; ++row) {
                    for (int col = 0; col < this.dimensions - 1; ++col) {
                        newLattice.set(row, col, this.lattice.get(row, col));
                    }
                }
            } else if (!this.MOD.equals(measured_mod)) {
                System.err.println("First call not a bound on a seed. Junk output may be produced.");
            }
            BigInteger tempMult = this.MULT.modPow(BigInteger.valueOf(this.callIndices.get(this.dimensions - 1) - this.callIndices.get(0)), this.MOD);
            newLattice.set(0, this.dimensions - 1, new BigFraction(tempMult));
            newLattice.set(this.dimensions, this.dimensions - 1, new BigFraction(measured_mod));
            this.lattice = newLattice;
        }
    }

    public void addUnmeasuredSeeds(long numSeeds) {
        this.currentCallIndex += numSeeds;
    }

    public GenerationInfo createGenerationInfo() {
        if (this.dimensions == 0) {
            return new GenerationInfo(0, new BigMatrix(0, 0), new BigVector(new BigFraction[0]), this.lcg, 1.0);
        }
        this.createLattice();
        BigVector offset = new BigVector(this.dimensions);
        Rand rand = Rand.ofInternalSeed(this.lcg, 0L);
        for (int i = 0; i < this.dimensions; ++i) {
            offset.set(i, new BigFraction(rand.getSeed()));
            if (i == this.dimensions - 1) continue;
            rand.advance(this.callIndices.get(i + 1) - this.callIndices.get(i));
        }
        LCG r = this.lcg.combine(-this.callIndices.get(0).longValue());
        return new GenerationInfo(this.dimensions, this.lattice.transpose(), offset, r, this.successChance);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public static final class GenerationInfo {
        public final int dimensions;
        public final BigMatrix basis;
        public final BigVector offset;
        public final LCG r;
        public final double successChance;

        private GenerationInfo(int dimensions, BigMatrix basis, BigVector offset, LCG r, double successChance) {
            this.dimensions = dimensions;
            this.basis = basis;
            this.offset = offset;
            this.r = r;
            this.successChance = successChance;
        }
    }
}

