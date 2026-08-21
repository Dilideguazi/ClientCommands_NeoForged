/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.util;

public class StringUtils {
    public static String tableToString(int rows, int columns, TableCellFunction<String> cellExtractor) {
        return StringUtils.tableToString(rows, columns, cellExtractor, (row, column) -> {
            if (column == 0) {
                return "[";
            }
            if (column == columns) {
                return "]";
            }
            return " ";
        });
    }

    public static String tableToString(int rows, int columns, TableCellFunction<String> cellExtractor, TableCellFunction<String> separator) {
        StringBuilder[][] parts = new StringBuilder[columns * 2 + 1][rows];
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                parts[column * 2][row] = new StringBuilder(separator.get(row, column));
                parts[column * 2 + 1][row] = new StringBuilder(cellExtractor.get(row, column));
            }
            parts[columns * 2][row] = new StringBuilder(separator.get(row, columns));
        }
        for (StringBuilder[] column : parts) {
            int columnWidth = 0;
            for (StringBuilder cell : column) {
                if (cell.length() <= columnWidth) continue;
                columnWidth = cell.length();
            }
            for (StringBuilder cell : column) {
                int i;
                int whitespace = columnWidth - cell.length();
                int e = whitespace / 2;
                for (i = 0; i < e; ++i) {
                    cell.insert(0, " ");
                }
                e = (whitespace + 1) / 2;
                for (i = 0; i < e; ++i) {
                    cell.insert(0, " ");
                }
            }
        }
        StringBuilder finalStr = new StringBuilder();
        for (int row = 0; row < rows; ++row) {
            if (row != 0) {
                finalStr.append("\n");
            }
            for (StringBuilder[] column : parts) {
                finalStr.append((CharSequence)column[row]);
            }
        }
        return finalStr.toString();
    }

    @FunctionalInterface
    public static interface TableCellFunction<T> {
        public T get(int var1, int var2);
    }
}

