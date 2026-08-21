/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.component;

import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.component.Matrix;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

public final class Vector {
    double[] numbers;
    int startPos = 0;
    int step = 1;
    private int dimension;

    public Vector(int dimension) {
        this.dimension = dimension;
        this.numbers = new double[this.dimension];
    }

    public Vector(double ... numbers) {
        this.numbers = numbers;
        this.dimension = this.numbers.length;
    }

    public Vector(int dimension, IntToDoubleFunction generator) {
        this.dimension = dimension;
        this.numbers = new double[this.dimension];
        Arrays.setAll(this.numbers, generator);
    }

    static Vector createView(double[] array, int dimension, int startPos, int step) {
        Vector vec = new Vector(array);
        vec.dimension = dimension;
        vec.startPos = startPos;
        vec.step = step;
        return vec;
    }

    public int getDimension() {
        return this.dimension;
    }

    public double get(int i) {
        if (i < 0 || i >= this.dimension) {
            throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
        }
        return this.numbers[this.step * i + this.startPos];
    }

    public void set(int i, double value) {
        if (i < 0 || i >= this.dimension) {
            throw new IndexOutOfBoundsException("Index " + i + ", dimension " + this.dimension);
        }
        this.numbers[this.step * i + this.startPos] = value;
    }

    public double magnitude() {
        return Math.sqrt(this.magnitudeSq());
    }

    public double magnitudeSq() {
        double magnitude = 0.0;
        for (int i = 0; i < this.getDimension(); ++i) {
            magnitude += this.get(i) * this.get(i);
        }
        return magnitude;
    }

    public boolean isZero() {
        for (int i = 0; i < this.getDimension(); ++i) {
            if (this.get(i) == 0.0) continue;
            return false;
        }
        return true;
    }

    public Vector add(Vector a) {
        return this.copy().addAndSet(a);
    }

    public Vector subtract(Vector a) {
        return this.copy().subtractAndSet(a);
    }

    public Vector multiply(double scalar) {
        return this.copy().multiplyAndSet(scalar);
    }

    public Vector multiply(Matrix m) {
        if (this.getDimension() != m.getRowCount()) {
            throw new IllegalArgumentException("Vector dimension should equal the number of matrix rows");
        }
        Vector v = new Vector(m.getColumnCount());
        for (int i = 0; i < v.getDimension(); ++i) {
            v.set(i, this.dot(m.getColumn(i)));
        }
        return v;
    }

    public Vector divide(double scalar) {
        return this.copy().divideAndSet(scalar);
    }

    public Vector swapNums(int i, int j) {
        return this.copy().swapNumsAndSet(i, j);
    }

    public Vector addAndSet(Vector a) {
        this.assertSameDimension(a);
        for (int i = 0; i < this.getDimension(); ++i) {
            this.set(i, this.get(i) + a.get(i));
        }
        return this;
    }

    public Vector subtractAndSet(Vector a) {
        this.assertSameDimension(a);
        for (int i = 0; i < this.getDimension(); ++i) {
            this.set(i, this.get(i) - a.get(i));
        }
        return this;
    }

    public Vector multiplyAndSet(double scalar) {
        for (int i = 0; i < this.getDimension(); ++i) {
            this.set(i, this.get(i) * scalar);
        }
        return this;
    }

    public Vector divideAndSet(double scalar) {
        for (int i = 0; i < this.getDimension(); ++i) {
            this.set(i, this.get(i) / scalar);
        }
        return this;
    }

    public Vector swapNumsAndSet(int i, int j) {
        double temp = this.get(i);
        this.set(i, this.get(j));
        this.set(j, temp);
        return this;
    }

    public double dot(Vector v) {
        this.assertSameDimension(v);
        double dot = 0.0;
        for (int i = 0; i < this.getDimension(); ++i) {
            dot += this.get(i) * v.get(i);
        }
        return dot;
    }

    public double gramSchmidtCoefficient(Vector v) {
        return this.dot(v) / v.magnitudeSq();
    }

    public Vector projectOnto(Vector v) {
        return v.multiply(this.gramSchmidtCoefficient(v));
    }

    public Vector copy() {
        if (this.step == 1) {
            return new Vector(Arrays.copyOfRange(this.numbers, this.startPos, this.startPos + this.dimension));
        }
        Vector dest = new Vector(this.getDimension());
        for (int i = 0; i < dest.getDimension(); ++i) {
            dest.set(i, this.get(i));
        }
        return dest;
    }

    private void assertSameDimension(Vector other) {
        if (other.dimension != this.dimension) {
            throw new IllegalArgumentException("The other vector is not the same dimension");
        }
    }

    public boolean equals(Vector other, double tolerance) {
        if (other.dimension != this.dimension) {
            return false;
        }
        for (int i = 0; i < this.dimension; ++i) {
            if (!(Math.abs(other.get(i) - this.get(i)) > tolerance)) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        int h = 0;
        for (int i = 0; i < this.dimension; ++i) {
            h = 31 * h + Double.hashCode(this.get(i));
        }
        return h;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != Vector.class) {
            return false;
        }
        Vector that = (Vector)other;
        return this.equals(that, 0.0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < this.getDimension(); ++i) {
            sb.append(this.get(i)).append(i == this.getDimension() - 1 ? "" : ", ");
        }
        return sb.append("}").toString();
    }

    public static Vector fromString(String str) {
        if (!(str = str.trim()).startsWith("{") || !str.endsWith("}")) {
            throw new IllegalArgumentException("Illegal Vector format");
        }
        ArrayList<Double> numbers = new ArrayList<Double>();
        int fracStart = 1;
        int fracEnd = str.indexOf(44, fracStart);
        while (fracEnd >= 0) {
            numbers.add(Double.valueOf(str.substring(fracStart, fracEnd)));
            fracStart = fracEnd + 1;
            fracEnd = str.indexOf(44, fracStart);
        }
        numbers.add(Double.valueOf(str.substring(fracStart, str.length() - 1)));
        return new Vector(numbers.stream().mapToDouble(Double::doubleValue).toArray());
    }

    public static Vector fromBigVector(BigVector v) {
        Vector p = new Vector(v.getDimension());
        for (int i = 0; i < p.getDimension(); ++i) {
            p.set(i, v.get(i).toDouble());
        }
        return p;
    }
}

