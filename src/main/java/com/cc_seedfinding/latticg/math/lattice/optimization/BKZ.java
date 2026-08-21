/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.math.lattice.optimization;

import com.cc_seedfinding.latticg.math.component.BigFraction;
import com.cc_seedfinding.latticg.math.component.BigMatrix;
import com.cc_seedfinding.latticg.math.component.BigVector;
import com.cc_seedfinding.latticg.math.lattice.LLL.LLL;
import com.cc_seedfinding.latticg.math.lattice.LLL.Params;
import com.cc_seedfinding.latticg.math.lattice.LLL.Result;
import java.math.BigInteger;

public class BKZ {
    private final BigFraction redFudgeFactor;
    private final Params params;
    private final LLL lll;
    BigFraction[] cT;
    BigFraction[] y;
    BigInteger[] v;
    BigInteger[] delta;
    BigInteger[] d;
    BigVector u;
    BigInteger[] uT;
    private BigMatrix basis;
    private BigMatrix baseGSO;
    private BigMatrix mu;
    private BigVector norms;
    private int nbRows;
    private int nbCols;
    private BigVector BKZConstant = null;
    private BigVector BKZTresh = null;

    public BKZ(BigMatrix lattice, Params params) {
        this.params = params;
        this.basis = lattice.copy();
        this.nbRows = lattice.getRowCount();
        this.nbCols = lattice.getColumnCount();
        this.lll = new LLL(lattice, params);
        this.baseGSO = new BigMatrix(this.nbRows, this.nbCols);
        this.mu = new BigMatrix(this.nbRows, this.nbRows);
        this.norms = new BigVector(this.nbRows);
        this.redFudgeFactor = BKZ.calulateFudge(51);
        this.cT = new BigFraction[this.nbRows + 2];
        this.v = new BigInteger[this.nbRows + 2];
        this.y = new BigFraction[this.nbRows + 2];
        this.u = new BigVector(this.nbRows + 2);
        this.uT = new BigInteger[this.nbRows + 2];
        this.delta = new BigInteger[this.nbRows + 2];
        this.d = new BigInteger[this.nbRows + 2];
    }

    public static Result reduce(BigMatrix lattice, int blockSize, Params params) {
        if (blockSize < 2 || blockSize > lattice.getRowCount()) {
            throw new IllegalArgumentException("Invalid blocksize: " + blockSize + " for range 2-" + lattice.getRowCount());
        }
        if (blockSize > 100) {
            throw new IllegalArgumentException("BlockSize not supported: " + blockSize);
        }
        return new BKZ(lattice, params).reduceBKZ(lattice, blockSize);
    }

    public static Result reduce(BigMatrix lattice, int blockSize) {
        if (blockSize < 2 || blockSize > lattice.getRowCount()) {
            throw new IllegalArgumentException("Invalid blocksize: " + blockSize + " for range 2-" + lattice.getRowCount());
        }
        if (blockSize > 100) {
            throw new IllegalArgumentException("BlockSize not supported: " + blockSize);
        }
        return new BKZ(lattice, new Params()).reduceBKZ(lattice, blockSize);
    }

    private boolean passvec(BigVector v, int index, int dim) {
        if (!v.get(index).equals(BigFraction.ONE)) {
            return false;
        }
        for (int i = 0; i < dim; ++i) {
            if (i == index || v.get(i).equals(BigFraction.ZERO)) continue;
            return false;
        }
        return true;
    }

    private Result reduceBKZ(BigMatrix lattice, int beta) {
        int z = 0;
        int j = 0;
        this.basis = lattice.copy();
        Result result = LLL.reduce(this.basis, this.params);
        this.updateWithResult(result);
        if (this.params.pruneFactor > 0) {
            this.BKZConstant = this.calculateBKZConstant(beta, this.params.pruneFactor);
        }
        boolean clean = true;
        while (z < this.nbRows - 1) {
            int k = Math.min(++j + beta - 1, this.nbRows);
            if (j == this.nbRows) {
                j = 1;
                k = beta;
                clean = true;
            }
            if (this.params.pruneFactor > 0) {
                this.BKZTresh = this.computeBKZThresh(j, k - j + 1);
            }
            Object[] objects = this.enumerateBKZ(j, k, this.nbRows, this.norms, this.mu);
            BigVector uvec = (BigVector)objects[1];
            BigFraction cbar = (BigFraction)objects[0];
            int h = Math.min(k + 1, this.nbRows);
            if (this.params.delta.subtract(this.redFudgeFactor.multiply(8L)).multiply(this.norms.get(j - 1)).compareTo(cbar) > 0) {
                clean = false;
                int s = 0;
                for (int i = j + 1; i <= k; ++i) {
                    if (uvec.get(i).equals(BigFraction.ZERO)) continue;
                    s = s == 0 ? i : -1;
                }
                if (s == 0) {
                    System.err.println("Huge error, impossible case for s");
                }
                if (s > 0) {
                    this.basis.shiftRows(j - 1, s - 1);
                    result = LLL.reduce(this.basis, this.params);
                    this.updateWithResult(result);
                } else {
                    int row;
                    int i;
                    BigVector newVec = new BigVector(this.nbCols);
                    for (i = 0; i < this.nbCols; ++i) {
                        newVec.set(i, BigFraction.ZERO);
                    }
                    for (i = j; i <= k; ++i) {
                        if (uvec.get(i).equals(BigFraction.ZERO)) continue;
                        for (int l = 0; l < this.nbCols; ++l) {
                            newVec.set(l, newVec.get(l).add(uvec.get(i).multiply(this.basis.get(i - 1, l))));
                        }
                    }
                    BigMatrix newBlock = new BigMatrix(this.nbRows + 1, this.nbCols);
                    for (row = 0; row <= j - 2; ++row) {
                        newBlock.setRow(row, this.basis.getRow(row));
                    }
                    newBlock.setRow(j - 1, newVec);
                    for (row = j - 1; row < this.nbRows; ++row) {
                        newBlock.setRow(row + 1, this.basis.getRow(row));
                    }
                    result = LLL.reduce(newBlock, this.params);
                    this.updateWithResult(result);
                }
                z = 0;
                continue;
            }
            ++z;
            if (clean) continue;
            result = LLL.reduce(this.basis, this.params);
            this.updateWithResult(result);
        }
        return result;
    }

    private void updateWithResult(Result result) {
        this.nbRows = result.getReducedBasis().getRowCount();
        this.nbCols = result.getReducedBasis().getColumnCount();
        this.baseGSO = result.getGramSchmidtBasis().copy();
        this.mu = result.getGramSchmidtCoefficients().copy();
        this.norms = result.getGramSchmidtSizes().copy();
        this.basis = result.getReducedBasis().copy();
    }

    private Object[] enumerateBKZ(int jj, int k, int dim, BigVector norms, BigMatrix blockMu) {
        int s = jj;
        int t = jj;
        BigFraction cbar = norms.get(jj - 1);
        this.d[jj] = this.uT[jj] = BigInteger.ONE;
        this.u.set(jj, BigFraction.ONE);
        this.delta[jj] = this.v[jj] = BigInteger.ZERO;
        this.y[jj] = BigFraction.ZERO;
        for (int i = jj + 1; i <= k + 1; ++i) {
            this.delta[i] = this.v[i] = BigInteger.ZERO;
            this.uT[i] = this.v[i];
            this.u.set(i, BigFraction.ZERO);
            this.cT[i] = this.y[i] = BigFraction.ZERO;
            this.d[i] = BigInteger.ONE;
        }
        while (t <= k) {
            BigFraction auxY = this.y[t].multiply(this.y[t]);
            BigInteger auxUT = this.uT[t].multiply(this.uT[t]);
            this.cT[t] = this.cT[t + 1].add(auxY.add(this.y[t].multiply(this.uT[t]).multiply(BigFraction.TWO)).add(auxUT).multiply(norms.get(t - 1)));
            BigFraction eta = this.params.pruneFactor > 0 && t > jj ? this.BKZTresh.get(t - jj - 1) : BigFraction.ZERO;
            if (this.cT[t].compareTo(cbar.subtract(eta)) < 0) {
                if (t > jj) {
                    this.y[--t] = BigFraction.ZERO;
                    for (int i = t + 1; i <= s; ++i) {
                        this.y[t] = this.y[t].add(blockMu.get(i - 1, t - 1).multiply(this.uT[i]));
                    }
                    this.uT[t] = this.v[t] = this.y[t].round().negate();
                    this.delta[t] = BigInteger.ZERO;
                    if (this.y[t].negate().compareTo(this.uT[t]) < 0) {
                        this.d[t] = BigInteger.ONE.negate();
                        continue;
                    }
                    this.d[t] = BigInteger.ONE;
                    continue;
                }
                cbar = this.cT[jj];
                for (int j = jj; j <= k; ++j) {
                    this.u.set(j, new BigFraction(this.uT[j]));
                }
                continue;
            }
            if (++t < (s = Math.max(s, t))) {
                this.delta[t] = this.delta[t].negate();
            }
            if (this.delta[t].multiply(this.d[t]).compareTo(BigInteger.ZERO) >= 0) {
                this.delta[t] = this.delta[t].add(this.d[t]);
            }
            this.uT[t] = this.v[t].add(this.delta[t]);
        }
        return new Object[]{cbar, this.u};
    }

    private void printVec(BigFraction[] vec, String msg) {
        System.out.print("vec " + msg + " =");
        for (BigFraction bigFraction : vec) {
            if (bigFraction == null) {
                System.out.print("0 ");
                continue;
            }
            System.out.print(bigFraction.toDouble() + " ");
        }
        System.out.println();
    }

    private static BigFraction calulateFudge(int precision) {
        BigFraction fudge = BigFraction.ONE;
        for (int i = precision; i > 0; --i) {
            fudge = fudge.multiply(BigFraction.HALF);
        }
        return fudge;
    }

    private BigVector calculateBKZConstant(int beta, int p) {
        BigVector res = new BigVector(beta - 1);
        BigVector log = new BigVector(beta);
        for (int j = 1; j <= beta; ++j) {
            log.set(j, BigVector.LOG_TABLE.get(j));
        }
        for (int i = 1; i <= beta - 1; ++i) {
            int j;
            BigFraction x;
            BigFraction k = new BigFraction(i).divide(BigFraction.TWO);
            if ((i & 1) == 0) {
                x = BigFraction.ZERO;
                j = 1;
                while ((double)j <= k.toDouble()) {
                    x = x.add(log.get(j));
                    ++j;
                }
                x = x.multiply(BigFraction.ONE.divide(k));
                x = x.exp();
            } else {
                x = BigFraction.ZERO;
                j = k.round().intValue() + 2;
                while ((double)j <= 2.0 * k.toDouble() + 2.0) {
                    x = x.add(log.get(j));
                    ++j;
                }
                x = BigFraction.HALF.multiply(BigFraction.LOG_PI).add(x).subtract(k.add(BigInteger.ONE).multiply(log.get(2)).multiply(BigFraction.TWO));
                x = x.multiply(new BigFraction(2L, i));
                x = x.exp();
            }
            BigFraction y = new BigFraction(-2L, i).multiply(log.get(2)).multiply(p);
            y = y.exp();
            res.set(i - 1, x.multiply(y).divide(BigFraction.PI));
        }
        return res;
    }

    private BigVector computeBKZThresh(int j, int beta) {
        BigVector res = new BigVector(beta - 1);
        BigFraction x = BigFraction.ZERO;
        for (int i = 0; i < beta - 1; ++i) {
            x.add(this.norms.get(j + i).log());
            res.set(i, x.divide(new BigFraction(i + 1)).exp().multiply(this.BKZConstant.get(i)));
        }
        return res;
    }
}

