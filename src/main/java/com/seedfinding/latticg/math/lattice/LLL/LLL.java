/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.lattice.LLL;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.lattice.LLL.Params;
import com.seedfinding.latticg.math.lattice.LLL.Result;
import java.math.BigInteger;

public class LLL {
    private final int nbRows;
    private final int nbCols;
    private final BigMatrix coordinates;
    private BigMatrix baseGSO;
    private BigMatrix mu;
    private BigVector norms;
    private BigMatrix basis;
    private Params params;

    public LLL(BigMatrix lattice, Params params) {
        this.basis = lattice.copy();
        this.nbRows = lattice.getRowCount();
        this.nbCols = lattice.getColumnCount();
        this.baseGSO = new BigMatrix(this.nbRows, this.nbCols);
        this.mu = new BigMatrix(this.nbRows, this.nbRows);
        this.norms = new BigVector(this.nbRows);
        this.coordinates = BigMatrix.identityMatrix(this.nbRows);
        this.params = params;
    }

    public static Result reduce(BigMatrix lattice, Params params) {
        return new LLL(lattice, params).reduceLLL(lattice);
    }

    public static Result reduce(BigMatrix lattice) {
        return new LLL(lattice, new Params()).reduceLLL(lattice);
    }

    public void setParams(Params params) {
        this.params = params;
    }

    private boolean testCondition(int k, BigFraction delta) {
        BigFraction muTemp = this.mu.get(k, k - 1);
        BigFraction factor = delta.subtract(muTemp.multiply(muTemp));
        return this.norms.get(k).compareTo(this.norms.get(k - 1).multiply(factor)) < 0;
    }

    private void updateGSO(int k) {
        BigVector newRow = this.basis.getRow(k).copy();
        for (int j = 0; j <= k - 1; ++j) {
            if (!this.norms.get(j).equals(BigFraction.ZERO)) {
                this.mu.set(k, j, this.basis.getRow(k).dot(this.baseGSO.getRow(j)).divide(this.norms.get(j)));
            } else {
                this.mu.set(k, j, BigFraction.ZERO);
            }
            newRow.subtractAndSet(this.baseGSO.getRow(j).multiply(this.mu.get(k, j)));
        }
        this.baseGSO.setRow(k, newRow);
        this.norms.set(k, newRow.magnitudeSq());
    }

    private void red(int i, int j) {
        BigInteger r = this.mu.get(i, j).round();
        if (r.equals(BigInteger.ZERO)) {
            return;
        }
        this.basis.getRow(i).subtractAndSet(this.basis.getRow(j).multiply(r));
        this.coordinates.getRow(i).subtractAndSet(this.coordinates.getRow(j).multiply(r));
        this.mu.set(i, j, this.mu.get(i, j).subtract(r));
        for (int col = 0; col <= j - 1; ++col) {
            this.mu.set(i, col, this.mu.get(i, col).subtract(this.mu.get(j, col).multiply(r)));
        }
    }

    private void swapg(int k, int kmax) {
        this.basis.swapRowsAndSet(k, k - 1);
        this.coordinates.swapRowsAndSet(k, k - 1);
        if (k > 1) {
            for (int j = 0; j <= k - 2; ++j) {
                this.mu.swapElementsAndSet(k, j, k - 1, j);
            }
        }
        BigFraction tmu = this.mu.get(k, k - 1);
        BigFraction tB = this.norms.get(k).add(tmu.multiply(tmu).multiply(this.norms.get(k - 1)));
        if (tB.equals(BigFraction.ZERO)) {
            this.norms.set(k, this.norms.get(k - 1));
            this.norms.set(k - 1, BigFraction.ZERO);
            this.baseGSO.swapRowsAndSet(k, k - 1);
            for (int i = k + 1; i <= kmax; ++i) {
                this.mu.set(i, k, this.mu.get(i, k - 1));
                this.mu.set(i, k - 1, BigFraction.ZERO);
            }
        } else if (this.norms.get(k).equals(BigFraction.ZERO) && !tmu.equals(BigFraction.ZERO)) {
            this.norms.set(k - 1, tB);
            this.baseGSO.getRow(k - 1).multiplyAndSet(tmu);
            this.mu.set(k, k - 1, BigFraction.ONE.divide(tmu));
            for (int i = k + 1; i <= kmax; ++i) {
                this.mu.set(i, k - 1, this.mu.get(i, k - 1).divide(tmu));
            }
        } else {
            BigFraction t = this.norms.get(k - 1).divide(tB);
            this.mu.set(k, k - 1, tmu.multiply(t));
            BigVector b = this.baseGSO.getRow(k - 1).copy();
            this.baseGSO.setRow(k - 1, this.baseGSO.getRow(k).add(b.multiply(tmu)));
            this.baseGSO.setRow(k, b.multiply(this.norms.get(k).divide(tB)).subtract(this.baseGSO.getRow(k).multiply(this.mu.get(k, k - 1))));
            this.norms.set(k, this.norms.get(k).multiply(t));
            this.norms.set(k - 1, tB);
            for (int i = k + 1; i <= kmax; ++i) {
                t = this.mu.get(i, k);
                this.mu.set(i, k, this.mu.get(i, k - 1).subtract(tmu.multiply(t)));
                this.mu.set(i, k - 1, t.add(this.mu.get(k, k - 1).multiply(this.mu.get(i, k))));
            }
        }
    }

    private int removeZeroes() {
        int p = 0;
        for (int i = 0; i < this.nbRows; ++i) {
            if (!this.basis.getRow(i).isZero()) continue;
            ++p;
        }
        this.basis = this.basis.submatrix(p, 0, this.nbRows - p, this.nbCols);
        this.baseGSO = this.baseGSO.submatrix(p, 0, this.nbRows - p, this.nbCols);
        this.mu = this.mu.submatrix(p, p, this.nbRows - p, this.nbRows - p);
        BigVector nonZeroQNorms = new BigVector(this.nbRows - p);
        for (int i = 0; i < this.nbRows - p; ++i) {
            nonZeroQNorms.set(i, this.norms.get(i + p));
        }
        this.norms = nonZeroQNorms;
        return p;
    }

    public Result reduceLLL(BigMatrix lattice) {
        this.basis = lattice.copy();
        int k = 1;
        int kmax = 0;
        int n = this.params.maxStage == -1 ? this.nbRows : this.params.maxStage;
        this.baseGSO.setRow(0, this.basis.getRow(0));
        boolean updateGSO = true;
        this.norms.set(0, this.basis.getRow(0).magnitudeSq());
        while (k < n) {
            if (k > kmax && updateGSO) {
                kmax = k;
                this.updateGSO(k);
            }
            this.red(k, k - 1);
            if (this.testCondition(k, this.params.delta)) {
                this.swapg(k, kmax);
                k = Math.max(1, k - 1);
                updateGSO = false;
                continue;
            }
            for (int l = k - 2; l >= 0; --l) {
                this.red(k, l);
            }
            ++k;
            updateGSO = true;
        }
        int p = this.removeZeroes();
        return new Result(p, this.basis, this.coordinates).setGramSchmidtInfo(this.baseGSO, this.mu, this.norms);
    }
}

