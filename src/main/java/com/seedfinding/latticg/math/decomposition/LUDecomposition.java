/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.decomposition;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigMatrixUtil;
import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.component.Matrix;
import com.seedfinding.latticg.math.component.Vector;
import java.util.regex.Pattern;

public class LUDecomposition {
    public static Result decompose(Matrix matrix) {
        int col;
        int row;
        int dcol;
        if (!matrix.isSquare()) {
            throw new UnsupportedOperationException("Matrix is not square");
        }
        Matrix m = matrix.copy();
        int size = m.getRowCount();
        Vector p = new Vector(size);
        Matrix inv = Matrix.identityMatrix(size);
        int swaps = 0;
        for (int i = 0; i < size; ++i) {
            int row2;
            int pivot = -1;
            double beegestNumbor = 0.0;
            for (row2 = i; row2 < size; ++row2) {
                double d = Math.abs(m.get(row2, i));
                if (!(d > beegestNumbor)) continue;
                beegestNumbor = d;
                pivot = row2;
            }
            if (pivot == -1) {
                throw new IllegalStateException("Matrix is singular");
            }
            p.set(i, pivot);
            inv.swapRowsAndSet(i, pivot);
            if (pivot != i) {
                m.swapRowsAndSet(i, pivot);
                ++swaps;
            }
            for (row2 = i + 1; row2 < size; ++row2) {
                m.set(row2, i, m.get(row2, i) / m.get(i, i));
            }
            for (row2 = i + 1; row2 < size; ++row2) {
                for (int col2 = i + 1; col2 < size; ++col2) {
                    m.set(row2, col2, m.get(row2, col2) - m.get(row2, i) * m.get(i, col2));
                }
            }
        }
        double det = 1.0;
        for (int i = 0; i < size; ++i) {
            det *= m.get(i, i);
        }
        det *= (swaps & 1) == 0 ? 1.0 : -1.0;
        for (dcol = 0; dcol < size; ++dcol) {
            for (row = 0; row < size; ++row) {
                for (col = 0; col < row; ++col) {
                    inv.set(row, dcol, inv.get(row, dcol) - m.get(row, col) * inv.get(col, dcol));
                }
            }
        }
        for (dcol = 0; dcol < size; ++dcol) {
            for (row = size - 1; row >= 0; --row) {
                for (col = size - 1; col > row; --col) {
                    inv.set(row, dcol, inv.get(row, dcol) - m.get(row, col) * inv.get(col, dcol));
                }
                inv.set(row, dcol, inv.get(row, dcol) / m.get(row, row));
            }
        }
        return new Result(m, p, det, inv);
    }

    public static BigResult decompose(BigMatrix matrix) {
        int col;
        int dcol;
        if (!matrix.isSquare()) {
            throw new UnsupportedOperationException("Matrix is not square");
        }
        BigMatrix m = matrix.copy();
        int size = m.getRowCount();
        BigVector p = new BigVector(size);
        BigMatrix inv = BigMatrix.identityMatrix(size);
        int swaps = 0;
        for (int i = 0; i < size; ++i) {
            int row;
            int pivot = -1;
            BigFraction beegestNumbor = BigFraction.ZERO;
            for (row = i; row < size; ++row) {
                BigFraction d = m.get(row, i).abs();
                if (d.compareTo(beegestNumbor) <= 0) continue;
                beegestNumbor = d;
                pivot = row;
            }
            if (pivot == -1) {
                throw new IllegalStateException("Matrix is singular");
            }
            p.set(i, new BigFraction(pivot));
            inv.swapRowsAndSet(i, pivot);
            if (pivot != i) {
                m.swapRowsAndSet(i, pivot);
                ++swaps;
            }
            for (row = i + 1; row < size; ++row) {
                m.set(row, i, m.get(row, i).divide(m.get(i, i)));
            }
            for (row = i + 1; row < size; ++row) {
                for (int col2 = i + 1; col2 < size; ++col2) {
                    m.set(row, col2, m.get(row, col2).subtract(m.get(row, i).multiply(m.get(i, col2))));
                }
            }
        }
        BigFraction det = BigFraction.ONE;
        for (int i = 0; i < size; ++i) {
            det = det.multiply(m.get(i, i));
        }
        if ((swaps & 1) != 0) {
            det.negate();
        }
        for (dcol = 0; dcol < size; ++dcol) {
            for (int row = 0; row < size; ++row) {
                for (col = 0; col < row; ++col) {
                    inv.set(row, dcol, inv.get(row, dcol).subtract(m.get(row, col).multiply(inv.get(col, dcol))));
                }
            }
        }
        for (dcol = 0; dcol < size; ++dcol) {
            for (int row = size - 1; row >= 0; --row) {
                for (col = size - 1; col > row; --col) {
                    inv.set(row, dcol, inv.get(row, dcol).subtract(m.get(row, col).multiply(inv.get(col, dcol))));
                }
                inv.set(row, dcol, inv.get(row, dcol).divide(m.get(row, row)));
            }
        }
        return new BigResult(m, p, det, inv);
    }

    public static final class Result {
        private final int size;
        private final Matrix P;
        private final Matrix L;
        private final Matrix U;
        private final double det;
        private final Matrix inv;

        private Result(Matrix lu, Vector p, double det, Matrix inv) {
            this.size = lu.getRowCount();
            this.L = new Matrix(this.size, this.size, (row, col) -> {
                if (row > col) {
                    return lu.get(row, col);
                }
                if (row == col) {
                    return 1.0;
                }
                return 0.0;
            });
            this.U = new Matrix(this.size, this.size, (row, col) -> {
                if (row <= col) {
                    return lu.get(row, col);
                }
                return 0.0;
            });
            this.P = Matrix.identityMatrix(this.size);
            for (int i = 0; i < this.size; ++i) {
                this.P.swapRowsAndSet(i, (int)p.get(i));
            }
            this.det = det;
            this.inv = inv;
        }

        public int getMatrixSize() {
            return this.size;
        }

        public Matrix getP() {
            return this.P;
        }

        public Matrix getL() {
            return this.L;
        }

        public Matrix getU() {
            return this.U;
        }

        public double getDet() {
            return this.det;
        }

        public Matrix inverse() {
            return this.inv;
        }

        public String toPrettyString() {
            StringBuilder sb = new StringBuilder();
            String[] uStuff = this.U.toPrettyString().split(Pattern.quote("\n"));
            String[] lStuff = this.L.toPrettyString().split(Pattern.quote("\n"));
            String[] pStuff = this.P.toPrettyString().split(Pattern.quote("\n"));
            for (int i = 0; i < lStuff.length; ++i) {
                sb.append(lStuff[i]).append("  ").append(uStuff[i]).append("  ").append(pStuff[i]);
                if (i == lStuff.length - 1) continue;
                sb.append("\n");
            }
            return sb.toString();
        }

        public String toString() {
            return this.P + " | " + this.L + " | " + this.U;
        }
    }

    public static final class BigResult {
        private final int size;
        private final BigMatrix P;
        private final BigMatrix L;
        private final BigMatrix U;
        private final BigFraction det;
        private final BigMatrix inv;

        private BigResult(BigMatrix lu, BigVector p, BigFraction det, BigMatrix inv) {
            this.size = lu.getRowCount();
            this.L = new BigMatrix(this.size, this.size, (row, col) -> {
                if (row > col) {
                    return lu.get(row, col);
                }
                if (row == col) {
                    return BigFraction.ONE;
                }
                return BigFraction.ZERO;
            });
            this.U = new BigMatrix(this.size, this.size, (row, col) -> {
                if (row <= col) {
                    return lu.get(row, col);
                }
                return BigFraction.ZERO;
            });
            this.P = BigMatrix.identityMatrix(this.size);
            for (int i = 0; i < this.size; ++i) {
                this.P.swapRowsAndSet(i, p.get(i).getNumerator().intValue());
            }
            this.det = det;
            this.inv = inv;
        }

        public int getMatrixSize() {
            return this.size;
        }

        public BigMatrix getP() {
            return this.P;
        }

        public BigMatrix getL() {
            return this.L;
        }

        public BigMatrix getU() {
            return this.U;
        }

        public BigFraction getDet() {
            return this.det;
        }

        public BigMatrix inverse() {
            return this.inv;
        }

        public String toPrettyString() {
            StringBuilder sb = new StringBuilder();
            String[] uStuff = BigMatrixUtil.toPrettyString(this.U).split(Pattern.quote("\n"));
            String[] lStuff = BigMatrixUtil.toPrettyString(this.L).split(Pattern.quote("\n"));
            String[] pStuff = BigMatrixUtil.toPrettyString(this.P).split(Pattern.quote("\n"));
            for (int i = 0; i < lStuff.length; ++i) {
                sb.append(lStuff[i]).append("  ").append(uStuff[i]).append("  ").append(pStuff[i]);
                if (i == lStuff.length - 1) continue;
                sb.append("\n");
            }
            return sb.toString();
        }

        public String toString() {
            return this.P + " | " + this.L + " | " + this.U;
        }
    }
}

