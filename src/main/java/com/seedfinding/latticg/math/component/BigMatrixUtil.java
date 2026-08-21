/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.component;

import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.component.BigVectorUtil;
import com.seedfinding.latticg.math.decomposition.LUDecomposition;
import com.seedfinding.latticg.util.StringUtils;
import java.util.ArrayList;

public final class BigMatrixUtil {
    private BigMatrixUtil() {
    }

    public static BigMatrix inverse(BigMatrix matrix) {
        return LUDecomposition.decompose(matrix).inverse();
    }

    public static String toPrettyString(BigMatrix matrix) {
        return BigMatrixUtil.toPrettyString(matrix, false);
    }

    public static String toPrettyString(BigMatrix matrix, boolean approximate) {
        return StringUtils.tableToString(matrix.getRowCount(), matrix.getColumnCount(), (row, column) -> approximate ? String.valueOf(matrix.get(row, column).toDouble()) : matrix.get(row, column).toString());
    }

    public static BigMatrix fromString(String str) {
        if (!(str = str.trim()).startsWith("{") || !str.endsWith("}")) {
            throw new IllegalArgumentException("Illegal BigMatrix format");
        }
        ArrayList<BigVector> rows = new ArrayList<BigVector>();
        int vectorStart = str.indexOf(123, 1);
        while (vectorStart >= 0) {
            int vectorEnd = str.indexOf(125, vectorStart + 1);
            rows.add(BigVectorUtil.fromString(str.substring(vectorStart, vectorEnd + 1)));
            vectorStart = str.indexOf(123, vectorEnd + 1);
        }
        if (rows.isEmpty()) {
            return new BigMatrix(0, 0);
        }
        BigMatrix matrix = new BigMatrix(rows.size(), ((BigVector)rows.get(0)).getDimension());
        for (int i = 0; i < rows.size(); ++i) {
            matrix.setRow(i, (BigVector)rows.get(i));
        }
        return matrix;
    }
}

