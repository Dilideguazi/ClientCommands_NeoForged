/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.cc_seedfinding.latticg.reversal.calltype.java;

import com.cc_seedfinding.latticg.reversal.calltype.CallType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class NextBooleanCall
extends CallType<Boolean> {
    @ApiStatus.Internal
    static final NextBooleanCall EQUAL_TO_TRUE = new NextBooleanCall(false);
    @ApiStatus.Internal
    static final NextBooleanCall EQUAL_TO_FALSE = new NextBooleanCall(true);
    private final boolean inverted;

    private NextBooleanCall(boolean inverted) {
        super(Boolean.class, 1);
        this.inverted = inverted;
    }

    @Override
    public CallType<Boolean> not() {
        return this.inverted ? EQUAL_TO_TRUE : EQUAL_TO_FALSE;
    }

    @Override
    public CallType<Boolean> equalTo(Boolean value) {
        return value != false ? EQUAL_TO_TRUE : EQUAL_TO_FALSE;
    }

    public boolean isInverted() {
        return this.inverted;
    }
}

