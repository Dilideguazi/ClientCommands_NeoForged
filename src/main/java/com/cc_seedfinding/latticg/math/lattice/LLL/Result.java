/*
 * Decompiled with CFR 0.152.
 */
package com.cc_seedfinding.latticg.math.lattice.LLL;

import com.cc_seedfinding.latticg.math.component.BigMatrix;
import com.cc_seedfinding.latticg.math.component.BigVector;

public class Result {
    private final int numDependantVectors;
    private final BigMatrix reducedBasis;
    private final BigMatrix transformationsDone;
    private BigMatrix gramSchmidtBasis;
    private BigMatrix gramSchmidtCoefficients;
    private BigVector gramSchmidtSizes;

    public Result(int numDependantVectors, BigMatrix reducedBasis, BigMatrix transformationsDone) {
        this.numDependantVectors = numDependantVectors;
        this.reducedBasis = reducedBasis;
        this.transformationsDone = transformationsDone;
    }

    public Result setGramSchmidtInfo(BigMatrix gramSchmidtBasis, BigMatrix GSCoefficients, BigVector norms) {
        this.gramSchmidtBasis = gramSchmidtBasis;
        this.gramSchmidtCoefficients = GSCoefficients;
        this.gramSchmidtSizes = norms;
        return this;
    }

    public int getNumDependantVectors() {
        return this.numDependantVectors;
    }

    public BigMatrix getReducedBasis() {
        return this.reducedBasis;
    }

    public BigMatrix getTransformations() {
        return this.transformationsDone;
    }

    public BigMatrix getGramSchmidtBasis() {
        return this.gramSchmidtBasis;
    }

    public BigMatrix getGramSchmidtCoefficients() {
        return this.gramSchmidtCoefficients;
    }

    public BigVector getGramSchmidtSizes() {
        return this.gramSchmidtSizes;
    }
}

