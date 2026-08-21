/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.component;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigVector;
import java.util.Arrays;

public final class BigMatrix {
    private final BigFraction[] numbers;
    private final int rowCount;
    private final int columnCount;
    private final int underlyingColumnCount;
    private int startIndex = 0;

    public BigMatrix(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.underlyingColumnCount = columnCount;
        if (rowCount <= 0 || columnCount <= 0) {
            throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
        }
        this.numbers = new BigFraction[rowCount * columnCount];
        Arrays.fill(this.numbers, BigFraction.ZERO);
    }

    public BigMatrix(int rowCount, int columnCount, DataProvider gen) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.underlyingColumnCount = columnCount;
        if (rowCount <= 0 || columnCount <= 0) {
            throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
        }
        this.numbers = new BigFraction[rowCount * columnCount];
        for (int row = 0; row < rowCount; ++row) {
            for (int column = 0; column < columnCount; ++column) {
                this.numbers[column + columnCount * row] = gen.getValue(row, column);
            }
        }
    }

    private BigMatrix(int rowCount, int columnCount, BigFraction[] numbers, int startIndex, int underlyingColumnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.numbers = numbers;
        this.startIndex = startIndex;
        this.underlyingColumnCount = underlyingColumnCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public boolean isSquare() {
        return this.rowCount == this.columnCount;
    }

    public BigFraction get(int row, int col) {
        if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
        }
        return this.numbers[this.startIndex + col + this.underlyingColumnCount * row];
    }

    public void set(int row, int col, BigFraction value) {
        if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
        }
        this.numbers[this.startIndex + col + this.underlyingColumnCount * row] = value;
    }

    public BigVector getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.rowCount) {
            throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
        }
        return BigVector.createView(this.numbers, this.columnCount, this.startIndex + rowIndex * this.underlyingColumnCount, 1);
    }

    public BigVector getColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
        }
        return BigVector.createView(this.numbers, this.rowCount, this.startIndex + columnIndex, this.underlyingColumnCount);
    }

    public void setRow(int rowIndex, BigVector newRow) {
        if (newRow.getDimension() != this.columnCount) {
            throw new IllegalArgumentException("Invalid vector dimension, expected " + this.columnCount + ", got " + newRow.getDimension());
        }
        if (rowIndex < 0 || rowIndex >= this.rowCount) {
            throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
        }
        if (newRow.step == 1 && this.columnCount == this.underlyingColumnCount) {
            System.arraycopy(newRow.numbers, newRow.startPos, this.numbers, this.startIndex + rowIndex * this.columnCount, this.columnCount);
        } else {
            for (int i = 0; i < this.columnCount; ++i) {
                this.set(rowIndex, i, newRow.get(i));
            }
        }
    }

    public void setColumn(int columnIndex, BigVector newColumn) {
        if (newColumn.getDimension() != this.rowCount) {
            throw new IllegalArgumentException("Invalid vector dimension, expected " + this.rowCount + ", got " + newColumn.getDimension());
        }
        if (columnIndex < 0 || columnIndex >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
        }
        for (int i = 0; i < this.rowCount; ++i) {
            this.set(i, columnIndex, newColumn.get(i));
        }
    }

    public BigMatrix submatrix(int startRow, int startColumn, int rowCount, int columnCount) {
        if (startRow < 0 || startColumn < 0 || rowCount <= 0 || columnCount <= 0 || startRow + rowCount > this.rowCount || startColumn + columnCount > this.columnCount) {
            throw new IllegalArgumentException(String.format("Illegal submatrix start (%d, %d) with size (%d, %d), size of original matrix (%d, %d)", startRow, startColumn, rowCount, columnCount, this.rowCount, this.columnCount));
        }
        return new BigMatrix(rowCount, columnCount, this.numbers, this.startIndex + startColumn + this.underlyingColumnCount * startRow, this.underlyingColumnCount);
    }

    public BigMatrix add(BigMatrix m) {
        return this.copy().addAndSet(m);
    }

    public BigMatrix subtract(BigMatrix m) {
        return this.copy().subtractAndSet(m);
    }

    public BigMatrix multiply(BigFraction scalar) {
        return this.copy().multiplyAndSet(scalar);
    }

    public BigMatrix multiply(BigMatrix m) {
        if (this.columnCount != m.rowCount) {
            throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
        }
        BigMatrix dest = new BigMatrix(this.rowCount, m.columnCount);
        for (int row = 0; row < dest.rowCount; ++row) {
            for (int column = 0; column < dest.columnCount; ++column) {
                dest.set(row, column, this.getRow(row).dot(m.getColumn(column)));
            }
        }
        return dest;
    }

    public BigVector multiply(BigVector v) {
        if (this.columnCount != v.getDimension()) {
            throw new IllegalArgumentException("Vector length should equal the number of matrix columns");
        }
        BigVector dest = new BigVector(this.rowCount);
        for (int i = 0; i < this.rowCount; ++i) {
            dest.set(i, v.dot(this.getRow(i)));
        }
        return dest;
    }

    public BigMatrix divide(BigFraction scalar) {
        return this.multiply(scalar.reciprocal());
    }

    public BigMatrix swapRows(int row1, int row2) {
        return this.copy().swapRowsAndSet(row1, row2);
    }

    public BigMatrix swapElements(int row1, int col1, int row2, int col2) {
        return this.copy().swapElementsAndSet(row1, col1, row2, col2);
    }

    public BigMatrix transpose() {
        BigMatrix dest = new BigMatrix(this.getColumnCount(), this.getRowCount());
        for (int i = 0; i < this.columnCount; ++i) {
            dest.setRow(i, this.getColumn(i));
        }
        return dest;
    }

    public BigMatrix addAndSet(BigMatrix m) {
        if (this.getRowCount() != m.getRowCount() || this.getColumnCount() != m.getColumnCount()) {
            throw new IllegalArgumentException("Adding two matrices with different dimensions");
        }
        if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].add(m.numbers[m.startIndex + i]);
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col).add(m.get(row, col)));
                }
            }
        }
        return this;
    }

    public BigMatrix subtractAndSet(BigMatrix m) {
        if (this.getRowCount() != m.getRowCount() || this.getColumnCount() != m.getColumnCount()) {
            throw new IllegalArgumentException("Subtracting two matrices with different dimensions");
        }
        if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].subtract(m.numbers[m.startIndex + i]);
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col).subtract(m.get(row, col)));
                }
            }
        }
        return this;
    }

    public BigMatrix multiplyAndSet(BigFraction scalar) {
        if (this.columnCount == this.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                this.numbers[this.startIndex + i] = this.numbers[this.startIndex + i].multiply(scalar);
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col).multiply(scalar));
                }
            }
        }
        return this;
    }

    public BigMatrix multiplyAndSet(BigMatrix m) {
        if (this.rowCount != this.columnCount || m.rowCount != m.columnCount || this.rowCount != m.columnCount) {
            throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
        }
        BigMatrix result = this.multiply(m);
        for (int i = 0; i < this.getRowCount(); ++i) {
            this.setRow(i, result.getRow(i));
        }
        return this;
    }

    public BigMatrix divideAndSet(BigFraction scalar) {
        return this.multiplyAndSet(scalar.reciprocal());
    }

    public BigMatrix swapRowsAndSet(int row1, int row2) {
        BigVector temp = this.getRow(row1).copy();
        this.setRow(row1, this.getRow(row2));
        this.setRow(row2, temp);
        return this;
    }

    public BigMatrix swapElementsAndSet(int row1, int col1, int row2, int col2) {
        BigFraction temp = this.get(row1, col1);
        this.set(row1, col1, this.get(row2, col2));
        this.set(row2, col2, temp);
        return this;
    }

    public BigMatrix shiftRows(int startIndex, int endIndex) {
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("The ending index should be greater or equals to the starting one");
        }
        if (startIndex == endIndex) {
            return this;
        }
        for (int col = 0; col < this.getColumnCount(); ++col) {
            BigFraction last = this.get(endIndex, col);
            for (int row = endIndex; row > startIndex; --row) {
                this.set(row, col, this.get(row - 1, col));
            }
            this.set(startIndex, col, last);
        }
        return this;
    }

    public BigMatrix copy() {
        BigMatrix dest;
        if (this.columnCount == this.underlyingColumnCount) {
            dest = new BigMatrix(this.rowCount, this.columnCount);
            System.arraycopy(this.numbers, this.startIndex, dest.numbers, 0, dest.numbers.length);
        } else {
            dest = new BigMatrix(this.rowCount, this.columnCount, this::get);
        }
        return dest;
    }

    public int hashCode() {
        int h = 0;
        for (int row = 0; row < this.rowCount; ++row) {
            for (int col = 0; col < this.columnCount; ++col) {
                h = 31 * h + this.get(row, col).hashCode();
            }
        }
        h = 31 * h + this.columnCount;
        h = 31 * h + this.rowCount;
        return h;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != BigMatrix.class) {
            return false;
        }
        BigMatrix that = (BigMatrix)other;
        if (this.rowCount != that.rowCount || this.columnCount != that.columnCount) {
            return false;
        }
        if (this.columnCount == this.underlyingColumnCount && that.columnCount == that.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                if (that.numbers[that.startIndex + i].equals(this.numbers[this.startIndex + i])) continue;
                return false;
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    if (that.get(row, col).equals(this.get(row, col))) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < this.getRowCount(); ++i) {
            sb.append(this.getRow(i)).append(i == this.getRowCount() - 1 ? "" : ", ");
        }
        return sb.append("}").toString();
    }

    public static BigMatrix identityMatrix(int size) {
        BigMatrix m = new BigMatrix(size, size);
        for (int i = 0; i < size; ++i) {
            m.set(i, i, BigFraction.ONE);
        }
        return m;
    }

    @FunctionalInterface
    public static interface DataProvider {
        public BigFraction getValue(int var1, int var2);
    }
}

