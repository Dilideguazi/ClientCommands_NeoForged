/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.component;

import com.seedfinding.latticg.math.decomposition.LUDecomposition;
import com.seedfinding.latticg.util.StringUtils;
import java.util.ArrayList;

public final class Matrix {
    private final double[] numbers;
    private final int rowCount;
    private final int columnCount;
    private final int underlyingColumnCount;
    private int startIndex = 0;

    public Matrix(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.underlyingColumnCount = columnCount;
        if (rowCount <= 0 || columnCount <= 0) {
            throw new IllegalArgumentException("Matrix dimensions cannot be less or equal to 0");
        }
        this.numbers = new double[rowCount * columnCount];
    }

    public Matrix(int rowCount, int columnCount, DataProvider gen) {
        this(rowCount, columnCount);
        for (int row = 0; row < this.rowCount; ++row) {
            for (int col = 0; col < this.columnCount; ++col) {
                this.set(row, col, gen.getValue(row, col));
            }
        }
    }

    private Matrix(int rowCount, int columnCount, double[] numbers, int startIndex, int underlyingColumnCount) {
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

    public double get(int row, int col) {
        if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
        }
        return this.numbers[this.startIndex + col + this.underlyingColumnCount * row];
    }

    public void set(int row, int col, double value) {
        if (row < 0 || row >= this.rowCount || col < 0 || col >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index (" + row + ", " + col + "), size (" + this.rowCount + ", " + this.columnCount + ")");
        }
        this.numbers[this.startIndex + col + this.underlyingColumnCount * row] = value;
    }

    public Vector getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.rowCount) {
            throw new IndexOutOfBoundsException("Index " + rowIndex + ", size " + this.rowCount);
        }
        return Vector.createView(this.numbers, this.columnCount, this.startIndex + rowIndex * this.underlyingColumnCount, 1);
    }

    public Vector getColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= this.columnCount) {
            throw new IndexOutOfBoundsException("Index " + columnIndex + ", size " + this.columnCount);
        }
        return Vector.createView(this.numbers, this.rowCount, this.startIndex + columnIndex, this.underlyingColumnCount);
    }

    public void setRow(int rowIndex, Vector newRow) {
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

    public void setColumn(int columnIndex, Vector newColumn) {
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

    public Matrix submatrix(int startRow, int startColumn, int rowCount, int columnCount) {
        if (startRow < 0 || startColumn < 0 || rowCount <= 0 || columnCount <= 0 || startRow + rowCount > this.rowCount || startColumn + columnCount > this.columnCount) {
            throw new IllegalArgumentException(String.format("Illegal submatrix start (%d, %d) with size (%d, %d), size of original matrix (%d, %d)", startRow, startColumn, rowCount, columnCount, this.rowCount, this.columnCount));
        }
        return new Matrix(rowCount, columnCount, this.numbers, this.startIndex + startColumn + this.underlyingColumnCount * startRow, this.underlyingColumnCount);
    }

    public Matrix add(Matrix m) {
        return this.copy().addAndSet(m);
    }

    public Matrix subtract(Matrix m) {
        return this.copy().subtractAndSet(m);
    }

    public Matrix multiply(double scalar) {
        return this.copy().multiplyAndSet(scalar);
    }

    public Matrix multiply(Matrix m) {
        if (this.columnCount != m.rowCount) {
            throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
        }
        Matrix dest = new Matrix(this.rowCount, m.columnCount);
        for (int row = 0; row < dest.rowCount; ++row) {
            for (int column = 0; column < dest.columnCount; ++column) {
                dest.set(row, column, this.getRow(row).dot(m.getColumn(column)));
            }
        }
        return dest;
    }

    public Vector multiply(Vector v) {
        if (this.columnCount != v.getDimension()) {
            throw new IllegalArgumentException("Vector length should equal the number of matrix columns");
        }
        Vector dest = new Vector(this.rowCount);
        for (int i = 0; i < this.rowCount; ++i) {
            dest.set(i, v.dot(this.getRow(i)));
        }
        return dest;
    }

    public Matrix divide(double scalar) {
        return this.copy().divideAndSet(scalar);
    }

    public Matrix inverse() {
        return LUDecomposition.decompose(this).inverse();
    }

    public Matrix swapRows(int row1, int row2) {
        return this.copy().swapRowsAndSet(row1, row2);
    }

    public Matrix transpose() {
        Matrix dest = new Matrix(this.columnCount, this.rowCount);
        for (int i = 0; i < this.columnCount; ++i) {
            dest.setRow(i, this.getColumn(i));
        }
        return dest;
    }

    public Matrix addAndSet(Matrix m) {
        if (this.rowCount != m.rowCount || this.columnCount != m.columnCount) {
            throw new IllegalArgumentException("Adding two matrices with different dimensions");
        }
        if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                int n = this.startIndex + i;
                this.numbers[n] = this.numbers[n] + m.numbers[m.startIndex + i];
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col) + m.get(row, col));
                }
            }
        }
        return this;
    }

    public Matrix subtractAndSet(Matrix m) {
        if (this.rowCount != m.rowCount || this.columnCount != m.columnCount) {
            throw new IllegalArgumentException("Subtracting two matrices with different dimensions");
        }
        if (this.columnCount == this.underlyingColumnCount && m.columnCount == m.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                int n = this.startIndex + i;
                this.numbers[n] = this.numbers[n] - m.numbers[m.startIndex + i];
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col) - m.get(row, col));
                }
            }
        }
        return this;
    }

    public Matrix multiplyAndSet(double scalar) {
        if (this.columnCount == this.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                int n = this.startIndex + i;
                this.numbers[n] = this.numbers[n] * scalar;
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    this.set(row, col, this.get(row, col) * scalar);
                }
            }
        }
        return this;
    }

    public Matrix multiplyAndSet(Matrix m) {
        if (this.rowCount != this.columnCount || m.rowCount != m.columnCount || this.rowCount != m.columnCount) {
            throw new IllegalArgumentException("Multiplying two matrices with disallowed dimensions");
        }
        Matrix result = this.multiply(m);
        for (int i = 0; i < this.rowCount; ++i) {
            this.setRow(i, result.getRow(i));
        }
        return this;
    }

    public Matrix divideAndSet(double scalar) {
        return this.multiplyAndSet(1.0 / scalar);
    }

    public Matrix swapRowsAndSet(int row1, int row2) {
        Vector temp = this.getRow(row1).copy();
        this.setRow(row1, this.getRow(row2));
        this.setRow(row2, temp);
        return this;
    }

    public Matrix copy() {
        Matrix dest;
        if (this.columnCount == this.underlyingColumnCount) {
            dest = new Matrix(this.rowCount, this.columnCount);
            System.arraycopy(this.numbers, this.startIndex, dest.numbers, 0, dest.numbers.length);
        } else {
            dest = new Matrix(this.rowCount, this.columnCount, this::get);
        }
        return dest;
    }

    public String toPrettyString() {
        return StringUtils.tableToString(this.rowCount, this.columnCount, (row, column) -> String.valueOf(this.get(row, column)));
    }

    public boolean equals(Matrix other, double tolerance) {
        if (this.rowCount != other.rowCount || this.columnCount != other.columnCount) {
            return false;
        }
        if (this.columnCount == this.underlyingColumnCount && other.columnCount == other.underlyingColumnCount) {
            int size = this.rowCount * this.columnCount;
            for (int i = 0; i < size; ++i) {
                if (!(Math.abs(other.numbers[other.startIndex + i] - this.numbers[this.startIndex + i]) > tolerance)) continue;
                return false;
            }
        } else {
            for (int row = 0; row < this.rowCount; ++row) {
                for (int col = 0; col < this.columnCount; ++col) {
                    if (!(Math.abs(other.get(row, col) - this.get(row, col)) > tolerance)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int h = 0;
        for (int row = 0; row < this.rowCount; ++row) {
            for (int col = 0; col < this.columnCount; ++col) {
                h = 31 * h + Double.hashCode(this.get(row, col));
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
        if (other == null || other.getClass() != Matrix.class) {
            return false;
        }
        Matrix that = (Matrix)other;
        return this.equals(that, 0.0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < this.rowCount; ++i) {
            sb.append(this.getRow(i)).append(i == this.rowCount - 1 ? "" : ", ");
        }
        return sb.append("}").toString();
    }

    public static Matrix fromString(String str) {
        if (!(str = str.trim()).startsWith("{") || !str.endsWith("}")) {
            throw new IllegalArgumentException("Illegal Matrix format");
        }
        ArrayList<Vector> rows = new ArrayList<Vector>();
        int vectorStart = str.indexOf(123, 1);
        while (vectorStart >= 0) {
            int vectorEnd = str.indexOf(125, vectorStart + 1);
            rows.add(Vector.fromString(str.substring(vectorStart, vectorEnd + 1)));
            vectorStart = str.indexOf(123, vectorEnd + 1);
        }
        if (rows.isEmpty()) {
            return new Matrix(0, 0);
        }
        Matrix matrix = new Matrix(rows.size(), ((Vector)rows.get(0)).getDimension());
        for (int i = 0; i < rows.size(); ++i) {
            matrix.setRow(i, (Vector)rows.get(i));
        }
        return matrix;
    }

    public static Matrix fromBigMatrix(BigMatrix m) {
        Matrix p = new Matrix(m.getRowCount(), m.getColumnCount());
        for (int i = 0; i < p.rowCount; ++i) {
            p.setRow(i, Vector.fromBigVector(m.getRow(i)));
        }
        return p;
    }

    public static Matrix identityMatrix(int size) {
        Matrix m = new Matrix(size, size);
        for (int i = 0; i < size; ++i) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    @FunctionalInterface
    public static interface DataProvider {
        public double getValue(int var1, int var2);
    }
}

