/*
 * Decompiled with CFR 0.152.
 */
package com.seedfinding.latticg.math.lattice.enumerate;

import com.seedfinding.latticg.math.component.BigFraction;
import com.seedfinding.latticg.math.component.BigMatrix;
import com.seedfinding.latticg.math.component.BigVector;
import com.seedfinding.latticg.math.optimize.Optimize;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class EnumerateRt {
    private EnumerateRt() {
    }

    public static Stream<BigVector> enumerate(BigMatrix basis, BigVector origin, Optimize constraints, BigMatrix rootInverse, BigVector rootOrigin) {
        int rootSize = basis.getRowCount();
        BigVector rootFixed = new BigVector(rootSize);
        Optimize rootConstraints = constraints.copy();
        ArrayList<BigFraction> widths = new ArrayList<BigFraction>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (int i2 = 0; i2 < rootSize; ++i2) {
            BigFraction min = constraints.copy().minimize(rootInverse.getRow(i2)).getSecond();
            BigFraction max = constraints.copy().maximize(rootInverse.getRow(i2)).getSecond();
            widths.add(max.subtract(min));
            order.add(i2);
        }
        order.sort(Comparator.comparing(i -> (BigFraction)widths.get((int)i)));
        try {
            SearchNode root = new SearchNode(rootSize, 0, rootInverse, rootOrigin, rootFixed, rootConstraints, order);
            return StreamSupport.stream(root.spliterator(), true).map(basis::multiply).map(origin::add);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("constraints are not feasible", e);
        }
    }
}

