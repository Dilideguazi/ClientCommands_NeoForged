/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.ApiStatus$Experimental
 *  org.jetbrains.annotations.ApiStatus$Internal
 */
package com.seedfinding.latticg.reversal;

import com.seedfinding.latticg.reversal.calltype.CallType;
import com.seedfinding.latticg.reversal.calltype.FilteredSkip;
import com.seedfinding.latticg.util.LCG;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Experimental
public class Program {
    private final LCG lcg;
    private final List<CallType<?>> calls;
    private final List<Long> skips;
    private final List<FilteredSkip> filteredSkips;
    private boolean verbose = false;

    protected Program(LCG lcg, List<CallType<?>> calls, List<Long> skips, List<FilteredSkip> filteredSkips) {
        this.lcg = lcg;
        this.calls = calls;
        this.skips = skips;
        this.filteredSkips = filteredSkips;
    }

    public static ProgramBuilder builder(LCG lcg) {
        return new ProgramBuilder(lcg);
    }

    public ProgramInstance start() {
        return new ProgramInstance(this);
    }

    public LCG getLcg() {
        return this.lcg;
    }

    @ApiStatus.Internal
    public List<CallType<?>> getCalls() {
        return this.calls;
    }

    @ApiStatus.Internal
    public List<Long> getSkips() {
        return this.skips;
    }

    @ApiStatus.Internal
    public List<FilteredSkip> getFilteredSkips() {
        return this.filteredSkips;
    }

    @ApiStatus.Internal
    public boolean isVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

