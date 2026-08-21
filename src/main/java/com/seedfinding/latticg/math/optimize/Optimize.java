/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.optimize;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.component.GaussJordan;
import com.seedfinding.latticg.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Optimize {
    private final BigMatrix transform;
    private final BigMatrix table;
    private final int[] basics;
    private final int[] nonbasics;
    private final int rows;
    private final int cols;

    private Optimize(BigMatrix table, int[] basics, int[] nonbasics, BigMatrix transform) {
        this.table = table;
        this.basics = basics;
        this.nonbasics = nonbasics;
        this.transform = transform;
        this.rows = this.table.getRowCount();
        this.cols = this.table.getColumnCount();
    }

    private BigVector transformForTable(BigVector lhs, BigFraction rhs) {
        BigFraction x;
        int row;
        BigVector transformed = new BigVector(this.transform.getColumnCount());
        BigVector eliminated = new BigVector(this.cols);
        transformed.set(this.transform.getColumnCount() - 1, rhs);
        for (row = 0; row < this.transform.getRowCount(); ++row) {
            x = lhs.get(row);
            transformed.subtractAndSet(this.transform.getRow(row).multiply(x));
        }
        for (int col = 0; col < this.cols - 1; ++col) {
            eliminated.set(col, transformed.get(this.nonbasics[col]));
        }
        eliminated.set(this.cols - 1, transformed.get(this.transform.getColumnCount() - 1));
        for (row = 0; row < this.rows - 1; ++row) {
            x = transformed.get(this.basics[row]);
            eliminated.subtractAndSet(this.table.getRow(row).multiply(x));
        }
        return eliminated;
    }

    public Pair<BigVector, BigFraction> maximize(BigVector gradient) {
        Pair<BigVector, BigFraction> result = this.minimize(gradient.multiply(BigFraction.MINUS_ONE));
        return new Pair<BigVector, BigFraction>(result.getFirst(), result.getSecond().negate());
    }

    public Pair<BigVector, BigFraction> minimize(BigVector gradient) {
        if (gradient.getDimension() != this.transform.getRowCount()) {
            throw new IllegalArgumentException("invalid size of gradient");
        }
        this.table.setRow(this.rows - 1, new BigVector(this.cols));
        this.table.getRow(this.rows - 1).subtractAndSet(this.transformForTable(gradient, BigFraction.ZERO));
        this.solve();
        BigVector result = this.transform.getColumn(this.transform.getColumnCount() - 1).copy();
        for (int row = 0; row < this.rows - 1; ++row) {
            int v0 = this.basics[row];
            result.subtractAndSet(this.transform.getColumn(v0).multiply(this.table.get(row, this.cols - 1)));
        }
        return new Pair<BigVector, BigFraction>(result, this.table.get(this.rows - 1, this.cols - 1));
    }

    private void solve() {
        while (this.step()) {
        }
    }

    private boolean step() {
        BigFraction x;
        int row;
        boolean bland = false;
        int entering = -1;
        int exiting = -1;
        BigFraction candidate = BigFraction.ZERO;
        for (row = 0; row < this.rows - 1; ++row) {
            if (this.table.get(row, this.cols - 1).signum() != 0) continue;
            bland = true;
            break;
        }
        for (int col = 0; col < this.cols - 1; ++col) {
            x = this.table.get(this.rows - 1, col);
            if (x.signum() <= 0 || entering != -1 && x.compareTo(candidate) <= 0) continue;
            entering = col;
            candidate = x;
            if (bland) break;
        }
        if (entering == -1) {
            return false;
        }
        for (row = 0; row < this.rows - 1; ++row) {
            x = this.table.get(row, entering);
            if (x.signum() <= 0) continue;
            BigFraction y = this.table.get(row, this.cols - 1).divide(x);
            if (exiting != -1 && y.compareTo(candidate) >= 0) continue;
            exiting = row;
            candidate = y;
        }
        this.pivot(entering, exiting);
        return true;
    }

    private void pivot(int entering, int exiting) {
        int rows = this.table.getRowCount();
        int cols = this.table.getColumnCount();
        int constraints = rows - 1;
        int variables = cols - 1;
        assert (0 <= entering && entering < variables);
        assert (0 <= exiting && exiting < constraints);
        BigFraction pivot = this.table.get(exiting, entering);
        for (int col = 0; col < cols; ++col) {
            if (col == entering) continue;
            this.table.set(exiting, col, this.table.get(exiting, col).divide(pivot));
        }
        for (int row = 0; row < rows; ++row) {
            if (row == exiting) continue;
            BigFraction x = this.table.get(row, entering);
            for (int col = 0; col < cols; ++col) {
                if (col == entering) continue;
                BigFraction y = this.table.get(exiting, col);
                this.table.set(row, col, this.table.get(row, col).subtract(x.multiply(y)));
            }
            this.table.set(row, entering, x.divide(pivot).negate());
        }
        this.table.set(exiting, entering, pivot.reciprocal());
        int temp = this.nonbasics[entering];
        this.nonbasics[entering] = this.basics[exiting];
        this.basics[exiting] = temp;
    }

    public Optimize copy() {
        return new Optimize(this.table.copy(), Arrays.copyOf(this.basics, this.rows - 1), Arrays.copyOf(this.nonbasics, this.cols - 1), this.transform);
    }

    public Optimize withStrictBound(BigVector lhs, BigFraction rhs) {
        BigMatrix newTable = new BigMatrix(this.rows + 1, this.cols);
        for (int row = 0; row < this.rows - 1; ++row) {
            newTable.setRow(row, this.table.getRow(row));
        }
        newTable.setRow(this.rows - 1, this.transformForTable(lhs, rhs));
        if (newTable.get(this.rows - 1, this.cols - 1).signum() < 0) {
            newTable.getRow(this.rows - 1).multiplyAndSet(BigFraction.MINUS_ONE);
        }
        int[] newBasics = Arrays.copyOf(this.basics, this.rows);
        int[] newNonbasics = Arrays.copyOf(this.nonbasics, this.cols - 1);
        newBasics[this.rows - 1] = this.rows - 1 + (this.cols - 1);
        return Optimize.from(newTable, newBasics, newNonbasics, 1, this.transform);
    }

    private static Optimize from(BigMatrix table, int[] basics, int[] nonbasics, int artificials, BigMatrix transform) {
        int rows = table.getRowCount();
        int cols = table.getColumnCount();
        int realVariables = rows - 1 + (cols - 1) - artificials;
        for (int basicRow = 0; basicRow < rows - 1; ++basicRow) {
            if (basics[basicRow] < realVariables) continue;
            table.getRow(rows - 1).addAndSet(table.getRow(basicRow));
        }
        Optimize optimize = new Optimize(table, basics, nonbasics, null);
        optimize.solve();
        if (table.get(rows - 1, cols - 1).signum() != 0) {
            throw new IllegalArgumentException("table has no basic feasible solutions: " + table.get(rows - 1, cols - 1));
        }
        block1: for (int row = 0; row < rows - 1; ++row) {
            if (basics[row] < realVariables) continue;
            for (int col = 0; col < cols - 1; ++col) {
                if (nonbasics[col] >= realVariables || table.get(row, col).signum() == 0) continue;
                optimize.pivot(col, row);
                continue block1;
            }
        }
        int finalCols = cols - artificials;
        BigMatrix finalTable = new BigMatrix(rows, finalCols);
        int c0 = 0;
        int c1 = 0;
        while (c0 < finalCols - 1) {
            while (true) {
                if (nonbasics[c1] < realVariables) {
                    for (int row = 0; row < rows - 1; ++row) {
                        finalTable.set(row, c0, table.get(row, c1));
                    }
                    break;
                }
                ++c1;
            }
            nonbasics[c0] = nonbasics[c1];
            ++c0;
            ++c1;
        }
        for (int row = 0; row < rows - 1; ++row) {
            finalTable.set(row, finalCols - 1, table.get(row, cols - 1));
        }
        return new Optimize(finalTable, basics, nonbasics, transform);
    }

    private static Optimize from(BigMatrix innerTable, BigMatrix transform) {
        int row;
        int constraints = innerTable.getRowCount();
        int variables = innerTable.getColumnCount() - 1;
        int[] basics = new int[constraints];
        Arrays.fill(basics, -1);
        ArrayList<Integer> nonbasicList = new ArrayList<Integer>();
        for (int row2 = 0; row2 < constraints; ++row2) {
            if (innerTable.get(row2, variables).signum() >= 0) continue;
            innerTable.getRow(row2).multiplyAndSet(BigFraction.MINUS_ONE);
        }
        boolean i2 = false;
        for (int col = 0; col < variables; ++col) {
            int count = 0;
            int index = -1;
            for (row = 0; row < innerTable.getRowCount(); ++row) {
                if (innerTable.get(row, col).signum() == 0) continue;
                ++count;
                index = row;
            }
            if (count == 1 && basics[index] == -1 && innerTable.get(index, col).signum() > 0) {
                innerTable.getRow(index).divideAndSet(innerTable.get(index, col));
                basics[index] = col;
                continue;
            }
            nonbasicList.add(col);
        }
        int artificials = 0;
        for (int row3 = 0; row3 < constraints; ++row3) {
            if (basics[row3] != -1) continue;
            basics[row3] = variables + artificials;
            ++artificials;
        }
        int[] nonbasics = nonbasicList.stream().mapToInt(i -> i).toArray();
        int nonbasicCount = variables - constraints + artificials;
        BigMatrix table = new BigMatrix(constraints + 1, nonbasicCount + 1);
        for (row = 0; row < constraints; ++row) {
            for (int basicRow = 0; basicRow < constraints; ++basicRow) {
                if (row == basicRow || basics[basicRow] >= variables) continue;
                BigVector rowVector = innerTable.getRow(row);
                BigVector basicVector = innerTable.getRow(basicRow);
                rowVector.subtractAndSet(basicVector.multiply(rowVector.get(basics[basicRow])));
            }
            for (int col = 0; col < nonbasicCount; ++col) {
                table.set(row, col, innerTable.get(row, nonbasics[col]));
            }
            table.set(row, nonbasicCount, innerTable.get(row, variables));
        }
        return Optimize.from(table, basics, nonbasics, artificials, transform);
    }

    public static class Builder {
        private final int size;
        private final List<Integer> slacks;
        private final List<BigVector> lefts;
        private final List<BigFraction> rights;

        private Builder(int size) {
            this.size = size;
            this.slacks = new ArrayList<Integer>();
            this.lefts = new ArrayList<BigVector>();
            this.rights = new ArrayList<BigFraction>();
        }

        public static Builder ofSize(int size) {
            return new Builder(size);
        }

        private void checkLHS(BigVector lhs) {
            if (lhs.getDimension() != this.size) {
                throw new IllegalArgumentException("invalid size of lhs: " + lhs.getDimension());
            }
        }

        public Optimize build() {
            int col2;
            int row;
            int col3;
            int constraint;
            int variables = this.size + this.slacks.size();
            int slack = this.size;
            BigMatrix table = new BigMatrix(this.slacks.size() + this.size, variables + 2 * this.size + 1);
            for (constraint = 0; constraint < this.slacks.size(); ++constraint) {
                for (int col4 = 0; col4 < this.size; ++col4) {
                    table.set(constraint, col4, this.lefts.get(constraint).get(col4));
                }
                table.set(constraint, variables + 2 * this.size, this.rights.get(constraint));
                if (this.slacks.get(constraint) == 0) continue;
                table.set(constraint, slack, new BigFraction(this.slacks.get(constraint).intValue()));
                ++slack;
            }
            int[] pivotRows = GaussJordan.reduce(table, (col, rows) -> col < this.size);
            for (col3 = 0; col3 < this.size; ++col3) {
                if (pivotRows[col3] != -1) continue;
                table.getRow(constraint).set(col3, BigFraction.ONE);
                table.getRow(constraint).set(slack, BigFraction.ONE);
                table.getRow(constraint).set(slack + 1, BigFraction.MINUS_ONE);
                ++constraint;
                slack += 2;
            }
            pivotRows = GaussJordan.reduce(table);
            for (col3 = 0; col3 < this.size; ++col3) {
                if (pivotRows[col3] != -1) continue;
                throw new IllegalStateException("something went wrong? couldn't remove column from table");
            }
            constraint = 1 + Arrays.stream(pivotRows).max().orElse(-1);
            BigMatrix transform = new BigMatrix(this.size, slack - this.size + 1);
            BigMatrix innerTable = new BigMatrix(constraint - this.size, slack - this.size + 1);
            for (row = 0; row < this.size; ++row) {
                for (col2 = 0; col2 < slack - this.size; ++col2) {
                    transform.set(row, col2, table.get(row, this.size + col2));
                }
                transform.set(row, slack - this.size, table.get(row, variables + 2 * this.size));
            }
            for (row = 0; row < constraint - this.size; ++row) {
                for (col2 = 0; col2 < slack - this.size; ++col2) {
                    innerTable.set(row, col2, table.get(this.size + row, this.size + col2));
                }
                innerTable.set(row, slack - this.size, table.get(this.size + row, variables + 2 * this.size));
            }
            return Optimize.from(innerTable, transform);
        }

        private void checkLHS(int lhs) {
            if (0 > lhs || lhs >= this.size) {
                throw new IllegalArgumentException("invalid index of lhs: " + lhs);
            }
        }

        private void add(int slack, BigVector lhs, BigFraction rhs) {
            this.slacks.add(slack);
            this.lefts.add(lhs);
            this.rights.add(rhs);
        }

        public Builder withLowerBound(BigVector lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(-1, lhs.copy(), rhs);
            return this;
        }

        public Builder withUpperBound(BigVector lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(1, lhs.copy(), rhs);
            return this;
        }

        public Builder withStrictBound(BigVector lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(0, lhs.copy(), rhs);
            return this;
        }

        public Builder withLowerBound(int lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(-1, BigVector.basis(this.size, lhs), rhs);
            return this;
        }

        public Builder withUpperBound(int lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(1, BigVector.basis(this.size, lhs), rhs);
            return this;
        }

        public Builder withLowerBound(int lhs, long rhs) {
            return this.withLowerBound(lhs, new BigFraction(rhs));
        }

        public Builder withUpperBound(int lhs, long rhs) {
            return this.withUpperBound(lhs, new BigFraction(rhs));
        }

        public Builder withStrictBound(int lhs, BigFraction rhs) {
            this.checkLHS(lhs);
            this.add(0, BigVector.basis(this.size, lhs), rhs);
            return this;
        }
    }
}

