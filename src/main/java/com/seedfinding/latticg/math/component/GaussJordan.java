/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.seedfinding.latticg.math.component;

import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

@ApiStatus.Internal
public class GaussJordan {
    private GaussJordan() {
    }

    private static void forAll(BigMatrix matrix, Collection<BigMatrix> others, Consumer<BigMatrix> action) {
        action.accept(matrix);
        others.forEach(action);
    }

    public static int[] reduce(BigMatrix matrix, Collection<BigMatrix> others, ReduceColumnPredicate reduceColumn) {
        int[] pivotRows = new int[matrix.getColumnCount()];
        Arrays.fill(pivotRows, -1);
        int row = 0;
        int pivotColumn = 0;
        while (row < matrix.getRowCount() && pivotColumn < matrix.getColumnCount()) {
            int pivotRow;
            for (pivotRow = row; pivotRow < matrix.getRowCount() && matrix.get(pivotRow, pivotColumn).equals(BigFraction.ZERO); ++pivotRow) {
            }
            if (pivotRow < matrix.getRowCount()) {
                int finalRow = row;
                int finalPivotRow = pivotRow;
                int finalPivotColumn = pivotColumn;
                BigFraction finalPivot = matrix.get(finalPivotRow, finalPivotColumn);
                GaussJordan.forAll(matrix, others, m -> m.getRow(finalPivotRow).divideAndSet(finalPivot));
                for (int i = 0; i < matrix.getRowCount(); ++i) {
                    if (i == finalPivotRow) continue;
                    int finalI = i;
                    BigFraction finalScale = matrix.get(i, finalPivotColumn);
                    GaussJordan.forAll(matrix, others, m -> m.getRow(finalI).subtractAndSet(m.getRow(finalPivotRow).multiply(finalScale)));
                }
                GaussJordan.forAll(matrix, others, m -> m.swapRowsAndSet(finalRow, finalPivotRow));
                pivotRows[finalPivotColumn] = finalRow;
                ++row;
            }
            while (++pivotColumn < matrix.getColumnCount() && !reduceColumn.test(pivotColumn, pivotRows)) {
            }
        }
        return pivotRows;
    }

    public static int[] reduce(BigMatrix matrix, BigMatrix other, ReduceColumnPredicate reduceColumn) {
        return GaussJordan.reduce(matrix, Collections.singleton(other), reduceColumn);
    }

    public static int[] reduce(BigMatrix matrix, ReduceColumnPredicate reduceColumn) {
        return GaussJordan.reduce(matrix, Collections.emptyList(), reduceColumn);
    }

    public static int[] reduce(BigMatrix matrix, Collection<BigMatrix> others) {
        return GaussJordan.reduce(matrix, others, ReduceColumnPredicate.ALWAYS);
    }

    public static int[] reduce(BigMatrix matrix, BigMatrix other) {
        return GaussJordan.reduce(matrix, Collections.singleton(other), ReduceColumnPredicate.ALWAYS);
    }

    public static int[] reduce(BigMatrix matrix) {
        return GaussJordan.reduce(matrix, Collections.emptyList(), ReduceColumnPredicate.ALWAYS);
    }

    @FunctionalInterface
    public static interface ReduceColumnPredicate {
        public static final ReduceColumnPredicate ALWAYS = (pivotColumn, pivotRows) -> true;

        public boolean test(int var1, int[] var2);
    }
}

